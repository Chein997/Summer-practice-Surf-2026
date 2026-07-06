package server

import (
	"context"
	"net/http"
	"strings"
	"time"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgtype"
)

func (s *Server) handleCreateBooking(w http.ResponseWriter, r *http.Request) {
	clientID, ok := s.requireClientID(w, r)
	if !ok {
		return
	}

	var req CreateBookingRequest
	if err := decodeJSON(r, &req); err != nil {
		writeError(w, http.StatusBadRequest, "BAD_REQUEST", "Некорректное тело запроса.")
		return
	}
	if errCode, msg := validateCreateBooking(req); errCode != "" {
		writeError(w, http.StatusUnprocessableEntity, errCode, msg)
		return
	}

	tx, err := s.db.Begin(r.Context())
	if err != nil {
		s.logger.Error("begin create booking tx", "error", internalErr(err))
		writeError(w, http.StatusInternalServerError, "INTERNAL_ERROR", "Не удалось создать бронь.")
		return
	}
	defer tx.Rollback(r.Context()) //nolint:errcheck

	var slotStatus string
	var freePlaces int
	err = tx.QueryRow(r.Context(), `
		SELECT status, free_places
		FROM ride_slots
		WHERE id = $1
		FOR UPDATE
	`, req.SlotID).Scan(&slotStatus, &freePlaces)
	if err != nil {
		if err == pgx.ErrNoRows {
			writeError(w, http.StatusNotFound, "SLOT_NOT_FOUND", "Слот не найден.")
			return
		}
		s.logger.Error("lock slot failed", "error", internalErr(err))
		writeError(w, http.StatusInternalServerError, "INTERNAL_ERROR", "Не удалось проверить слот.")
		return
	}
	if slotStatus == "CANCELLED" {
		writeError(w, http.StatusConflict, "SLOT_CANCELLED", "Заезд отменён центром.")
		return
	}
	if slotStatus != "AVAILABLE" || freePlaces <= 0 {
		writeError(w, http.StatusConflict, "NO_FREE_PLACES", "На этот заезд больше нет свободных мест.")
		return
	}

	var duplicate bool
	err = tx.QueryRow(r.Context(), `
		SELECT EXISTS (
			SELECT 1
			FROM bookings
			WHERE client_id = $1
			  AND slot_id = $2
			  AND status IN ('PENDING_CONFIRMATION', 'ACTIVE')
		)
	`, clientID, req.SlotID).Scan(&duplicate)
	if err != nil {
		s.logger.Error("duplicate check failed", "error", internalErr(err))
		writeError(w, http.StatusInternalServerError, "INTERNAL_ERROR", "Не удалось проверить бронь.")
		return
	}
	if duplicate {
		writeError(w, http.StatusConflict, "DUPLICATE_BOOKING", "У вас уже есть бронь на этот заезд.")
		return
	}

	var bookingID string
	err = tx.QueryRow(r.Context(), `
		INSERT INTO bookings (
			slot_id,
			client_id,
			profile_full_name,
			profile_phone,
			profile_email,
			profile_age,
			safety_rules_accepted,
			parental_consent_accepted,
			status
		)
		VALUES ($1, $2, $3, $4, $5, $6, $7, $8, 'PENDING_CONFIRMATION')
		RETURNING id::text
	`,
		req.SlotID,
		clientID,
		strings.TrimSpace(req.Profile.FullName),
		strings.TrimSpace(req.Profile.Phone),
		strings.TrimSpace(req.Profile.Email),
		req.Profile.Age,
		req.SafetyRulesAccepted,
		req.ParentalConsentAccepted,
	).Scan(&bookingID)
	if err != nil {
		s.logger.Error("insert booking failed", "error", internalErr(err))
		writeError(w, http.StatusInternalServerError, "INTERNAL_ERROR", "Не удалось создать бронь.")
		return
	}

	_, err = tx.Exec(r.Context(), `
		UPDATE ride_slots
		SET free_places = free_places - 1,
		    status = CASE WHEN free_places - 1 = 0 THEN 'NO_FREE_PLACES' ELSE 'AVAILABLE' END,
		    updated_at = now()
		WHERE id = $1
	`, req.SlotID)
	if err != nil {
		s.logger.Error("decrement free places failed", "error", internalErr(err))
		writeError(w, http.StatusInternalServerError, "INTERNAL_ERROR", "Не удалось обновить свободные места.")
		return
	}

	if err := tx.Commit(r.Context()); err != nil {
		s.logger.Error("commit create booking tx", "error", internalErr(err))
		writeError(w, http.StatusInternalServerError, "INTERNAL_ERROR", "Не удалось создать бронь.")
		return
	}

	booking, err := s.getBookingResponse(r.Context(), bookingID)
	if err != nil {
		s.logger.Error("fetch created booking failed", "error", internalErr(err))
		writeError(w, http.StatusInternalServerError, "INTERNAL_ERROR", "Бронь создана, но не удалось загрузить детали.")
		return
	}
	writeJSON(w, http.StatusCreated, booking)
}

func (s *Server) handleListBookings(w http.ResponseWriter, r *http.Request) {
	clientID, ok := s.requireClientID(w, r)
	if !ok {
		return
	}

	rows, err := s.db.Query(r.Context(), bookingSelectSQL+`
		WHERE b.client_id = $1
		ORDER BY b.created_at DESC
	`, clientID)
	if err != nil {
		s.logger.Error("list bookings failed", "error", internalErr(err))
		writeError(w, http.StatusInternalServerError, "INTERNAL_ERROR", "Не удалось загрузить брони.")
		return
	}
	defer rows.Close()

	items := make([]BookingResponse, 0)
	for rows.Next() {
		item, err := scanBooking(rows)
		if err != nil {
			s.logger.Error("scan booking failed", "error", internalErr(err))
			writeError(w, http.StatusInternalServerError, "INTERNAL_ERROR", "Не удалось прочитать брони.")
			return
		}
		items = append(items, item)
	}
	if err := rows.Err(); err != nil {
		s.logger.Error("booking rows failed", "error", internalErr(err))
		writeError(w, http.StatusInternalServerError, "INTERNAL_ERROR", "Не удалось прочитать брони.")
		return
	}
	writeJSON(w, http.StatusOK, BookingsListResponse{Items: items})
}

func (s *Server) handleGetBooking(w http.ResponseWriter, r *http.Request) {
	clientID, ok := s.requireClientID(w, r)
	if !ok {
		return
	}
	bookingID := r.PathValue("bookingId")
	booking, err := s.getBookingResponse(r.Context(), bookingID)
	if err != nil {
		if err == pgx.ErrNoRows {
			writeError(w, http.StatusNotFound, "BOOKING_NOT_FOUND", "Бронь не найдена.")
			return
		}
		s.logger.Error("get booking failed", "error", internalErr(err))
		writeError(w, http.StatusInternalServerError, "INTERNAL_ERROR", "Не удалось загрузить бронь.")
		return
	}
	if booking.ClientID != clientID {
		writeError(w, http.StatusForbidden, "FORBIDDEN", "Нет доступа к этой брони.")
		return
	}
	writeJSON(w, http.StatusOK, booking)
}

func (s *Server) handleCancelBooking(w http.ResponseWriter, r *http.Request) {
	clientID, ok := s.requireClientID(w, r)
	if !ok {
		return
	}
	bookingID := r.PathValue("bookingId")

	tx, err := s.db.Begin(r.Context())
	if err != nil {
		s.logger.Error("begin cancel booking tx", "error", internalErr(err))
		writeError(w, http.StatusInternalServerError, "INTERNAL_ERROR", "Не удалось отменить бронь.")
		return
	}
	defer tx.Rollback(r.Context()) //nolint:errcheck

	var ownerID string
	var slotID string
	var status string
	var startAt time.Time
	err = tx.QueryRow(r.Context(), `
		SELECT b.client_id::text, b.slot_id::text, b.status, rs.start_at
		FROM bookings b
		JOIN ride_slots rs ON rs.id = b.slot_id
		WHERE b.id = $1
		FOR UPDATE OF b, rs
	`, bookingID).Scan(&ownerID, &slotID, &status, &startAt)
	if err != nil {
		if err == pgx.ErrNoRows {
			writeError(w, http.StatusNotFound, "BOOKING_NOT_FOUND", "Бронь не найдена.")
			return
		}
		s.logger.Error("lock booking failed", "error", internalErr(err))
		writeError(w, http.StatusInternalServerError, "INTERNAL_ERROR", "Не удалось проверить бронь.")
		return
	}
	if ownerID != clientID {
		writeError(w, http.StatusForbidden, "FORBIDDEN", "Нет доступа к этой брони.")
		return
	}
	if status != "PENDING_CONFIRMATION" && status != "ACTIVE" {
		writeError(w, http.StatusConflict, "ACTION_UNAVAILABLE", "Эту бронь уже нельзя отменить.")
		return
	}
	if !startAt.After(time.Now().Add(time.Hour)) {
		writeError(w, http.StatusConflict, "ACTION_UNAVAILABLE", "Отмена недоступна менее чем за 1 час до старта.")
		return
	}

	_, err = tx.Exec(r.Context(), `
		UPDATE bookings
		SET status = 'CANCELLED_BY_CLIENT',
		    canceled_at = now(),
		    cancel_source = 'CLIENT',
		    updated_at = now()
		WHERE id = $1
	`, bookingID)
	if err != nil {
		s.logger.Error("cancel booking update failed", "error", internalErr(err))
		writeError(w, http.StatusInternalServerError, "INTERNAL_ERROR", "Не удалось отменить бронь.")
		return
	}

	_, err = tx.Exec(r.Context(), `
		UPDATE ride_slots
		SET free_places = LEAST(capacity, free_places + 1),
		    status = CASE WHEN status = 'CANCELLED' THEN status ELSE 'AVAILABLE' END,
		    updated_at = now()
		WHERE id = $1
	`, slotID)
	if err != nil {
		s.logger.Error("increase free places failed", "error", internalErr(err))
		writeError(w, http.StatusInternalServerError, "INTERNAL_ERROR", "Не удалось освободить место.")
		return
	}

	if err := tx.Commit(r.Context()); err != nil {
		s.logger.Error("commit cancel booking tx", "error", internalErr(err))
		writeError(w, http.StatusInternalServerError, "INTERNAL_ERROR", "Не удалось отменить бронь.")
		return
	}

	booking, err := s.getBookingResponse(r.Context(), bookingID)
	if err != nil {
		s.logger.Error("fetch cancelled booking failed", "error", internalErr(err))
		writeError(w, http.StatusInternalServerError, "INTERNAL_ERROR", "Бронь отменена, но не удалось загрузить детали.")
		return
	}
	writeJSON(w, http.StatusOK, booking)
}

func validateCreateBooking(req CreateBookingRequest) (string, string) {
	if strings.TrimSpace(req.SlotID) == "" {
		return "VALIDATION_ERROR", "Не указан слот."
	}
	if strings.TrimSpace(req.Profile.FullName) == "" {
		return "VALIDATION_ERROR", "Введите имя."
	}
	if !strings.HasPrefix(strings.TrimSpace(req.Profile.Phone), "+") {
		return "VALIDATION_ERROR", "Введите телефон в международном формате."
	}
	if !strings.Contains(strings.TrimSpace(req.Profile.Email), "@") {
		return "VALIDATION_ERROR", "Введите корректный email."
	}
	if req.Profile.Age < 16 || req.Profile.Age > 120 {
		return "VALIDATION_ERROR", "Возраст участника должен быть от 16 лет."
	}
	if !req.SafetyRulesAccepted {
		return "VALIDATION_ERROR", "Нужно принять правила безопасности."
	}
	if req.Profile.Age < 18 && !req.ParentalConsentAccepted {
		return "VALIDATION_ERROR", "Для участников младше 18 лет нужно согласие родителя или законного представителя."
	}
	return "", ""
}

func (s *Server) getBookingResponse(ctx context.Context, bookingID string) (BookingResponse, error) {
	row := s.db.QueryRow(ctx, bookingSelectSQL+`WHERE b.id = $1`, bookingID)
	return scanBooking(row)
}

const bookingSelectSQL = `
	SELECT
		b.id::text,
		b.slot_id::text,
		b.client_id::text,
		b.profile_full_name,
		b.profile_phone,
		b.profile_email,
		b.profile_age,
		b.safety_rules_accepted,
		b.parental_consent_accepted,
		b.status,
		b.created_at,
		b.updated_at,
		b.center_cancel_reason_type,
		b.center_cancel_reason_text,
		rs.id::text,
		tc.id::text,
		tc.name,
		tc.type,
		rl.id::text,
		rl.code,
		rl.name,
		m.id::text,
		m.name,
		rs.start_at,
		rs.duration_minutes,
		rs.capacity,
		rs.free_places,
		rs.price_amount::text,
		rs.price_currency,
		rs.status,
		rs.address,
		rs.meeting_point,
		rs.safety_rules,
		rs.cancellation_terms,
		rs.center_cancel_reason_type,
		rs.center_cancel_reason_text,
		rs.center_cancelled_at
	FROM bookings b
	JOIN ride_slots rs ON rs.id = b.slot_id
	JOIN track_configurations tc ON tc.id = rs.track_configuration_id
	JOIN ride_levels rl ON rl.id = rs.ride_level_id
	LEFT JOIN marshals m ON m.id = rs.marshal_id
`

type bookingScanner interface {
	Scan(dest ...any) error
}

func scanBooking(scanner bookingScanner) (BookingResponse, error) {
	var booking BookingResponse
	var createdAt time.Time
	var updatedAt time.Time
	var bookingReasonType pgtype.Text
	var bookingReasonText pgtype.Text

	var slot RideSlotResponse
	var marshalID pgtype.Text
	var marshalName pgtype.Text
	var slotStartAt time.Time
	var slotReasonType pgtype.Text
	var slotReasonText pgtype.Text
	var slotCancelledAt pgtype.Timestamptz

	err := scanner.Scan(
		&booking.ID,
		&booking.SlotID,
		&booking.ClientID,
		&booking.Profile.FullName,
		&booking.Profile.Phone,
		&booking.Profile.Email,
		&booking.Profile.Age,
		&booking.SafetyRulesAccepted,
		&booking.ParentalConsentAccepted,
		&booking.Status,
		&createdAt,
		&updatedAt,
		&bookingReasonType,
		&bookingReasonText,
		&slot.ID,
		&slot.TrackConfiguration.ID,
		&slot.TrackConfiguration.Name,
		&slot.TrackConfiguration.Type,
		&slot.RideLevel.ID,
		&slot.RideLevel.Code,
		&slot.RideLevel.Name,
		&marshalID,
		&marshalName,
		&slotStartAt,
		&slot.DurationMinutes,
		&slot.Capacity,
		&slot.FreePlaces,
		&slot.Price.Amount,
		&slot.Price.Currency,
		&slot.Status,
		&slot.Address,
		&slot.MeetingPoint,
		&slot.SafetyRules,
		&slot.CancellationTerms,
		&slotReasonType,
		&slotReasonText,
		&slotCancelledAt,
	)
	if err != nil {
		return BookingResponse{}, err
	}

	booking.CreatedAt = createdAt.Format(time.RFC3339)
	booking.UpdatedAt = updatedAt.Format(time.RFC3339)
	booking.CanCancel = (booking.Status == "PENDING_CONFIRMATION" || booking.Status == "ACTIVE") && slotStartAt.After(time.Now().Add(time.Hour))
	if bookingReasonType.Valid {
		booking.CenterCancellation = &CenterCancellationResponse{
			ReasonType: bookingReasonType.String,
			ReasonText: nullableText(bookingReasonText),
		}
	}

	slot.StartAt = slotStartAt.Format(time.RFC3339)
	slot.CanBook = slot.Status == "AVAILABLE" && slot.FreePlaces > 0
	if marshalID.Valid {
		slot.Marshal = &MarshalResponse{ID: marshalID.String, Name: marshalName.String}
	}
	if slotReasonType.Valid && slotCancelledAt.Valid {
		slot.CenterCancellation = &CenterCancellationResponse{
			ReasonType:  slotReasonType.String,
			ReasonText:  nullableText(slotReasonText),
			CancelledAt: slotCancelledAt.Time.Format(time.RFC3339),
		}
	}
	booking.Slot = &slot
	return booking, nil
}
