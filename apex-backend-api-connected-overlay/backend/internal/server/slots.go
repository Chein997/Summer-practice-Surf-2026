package server

import (
	"net/http"
	"strconv"
	"time"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgtype"
)

func (s *Server) handleListRideSlots(w http.ResponseWriter, r *http.Request) {
	days := 7
	if raw := r.URL.Query().Get("days"); raw != "" {
		parsed, err := strconv.Atoi(raw)
		if err != nil || parsed < 1 || parsed > 31 {
			writeError(w, http.StatusUnprocessableEntity, "VALIDATION_ERROR", "Параметр days должен быть от 1 до 31.")
			return
		}
		days = parsed
	}
	includeUnavailable := true
	if raw := r.URL.Query().Get("includeUnavailable"); raw != "" {
		parsed, err := strconv.ParseBool(raw)
		if err == nil {
			includeUnavailable = parsed
		}
	}

	rows, err := s.db.Query(r.Context(), rideSlotSelectSQL+`
		WHERE rs.start_at >= now()
		  AND rs.start_at < now() + ($1::int * interval '1 day')
		  AND ($2::bool OR rs.status = 'AVAILABLE')
		ORDER BY rs.start_at ASC
	`, days, includeUnavailable)
	if err != nil {
		s.logger.Error("list ride slots failed", "error", internalErr(err))
		writeError(w, http.StatusInternalServerError, "INTERNAL_ERROR", "Не удалось загрузить расписание.")
		return
	}
	defer rows.Close()

	items := make([]RideSlotResponse, 0)
	for rows.Next() {
		item, err := scanRideSlot(rows)
		if err != nil {
			s.logger.Error("scan ride slot failed", "error", internalErr(err))
			writeError(w, http.StatusInternalServerError, "INTERNAL_ERROR", "Не удалось прочитать расписание.")
			return
		}
		items = append(items, item)
	}
	if err := rows.Err(); err != nil {
		s.logger.Error("ride slots rows failed", "error", internalErr(err))
		writeError(w, http.StatusInternalServerError, "INTERNAL_ERROR", "Не удалось прочитать расписание.")
		return
	}

	writeJSON(w, http.StatusOK, RideSlotsListResponse{Items: items})
}

func (s *Server) handleGetRideSlot(w http.ResponseWriter, r *http.Request) {
	slotID := r.PathValue("slotId")
	row := s.db.QueryRow(r.Context(), rideSlotSelectSQL+`WHERE rs.id = $1`, slotID)
	item, err := scanRideSlot(row)
	if err != nil {
		if err == pgx.ErrNoRows {
			writeError(w, http.StatusNotFound, "SLOT_NOT_FOUND", "Слот не найден.")
			return
		}
		s.logger.Error("get ride slot failed", "error", internalErr(err))
		writeError(w, http.StatusInternalServerError, "INTERNAL_ERROR", "Не удалось загрузить слот.")
		return
	}
	writeJSON(w, http.StatusOK, item)
}

const rideSlotSelectSQL = `
	SELECT
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
	FROM ride_slots rs
	JOIN track_configurations tc ON tc.id = rs.track_configuration_id
	JOIN ride_levels rl ON rl.id = rs.ride_level_id
	LEFT JOIN marshals m ON m.id = rs.marshal_id
`

type rideSlotScanner interface {
	Scan(dest ...any) error
}

func scanRideSlot(scanner rideSlotScanner) (RideSlotResponse, error) {
	var item RideSlotResponse
	var marshalID pgtype.Text
	var marshalName pgtype.Text
	var startAt time.Time
	var reasonType pgtype.Text
	var reasonText pgtype.Text
	var cancelledAt pgtype.Timestamptz

	err := scanner.Scan(
		&item.ID,
		&item.TrackConfiguration.ID,
		&item.TrackConfiguration.Name,
		&item.TrackConfiguration.Type,
		&item.RideLevel.ID,
		&item.RideLevel.Code,
		&item.RideLevel.Name,
		&marshalID,
		&marshalName,
		&startAt,
		&item.DurationMinutes,
		&item.Capacity,
		&item.FreePlaces,
		&item.Price.Amount,
		&item.Price.Currency,
		&item.Status,
		&item.Address,
		&item.MeetingPoint,
		&item.SafetyRules,
		&item.CancellationTerms,
		&reasonType,
		&reasonText,
		&cancelledAt,
	)
	if err != nil {
		return RideSlotResponse{}, err
	}

	item.StartAt = startAt.Format(time.RFC3339)
	item.CanBook = item.Status == "AVAILABLE" && item.FreePlaces > 0
	if marshalID.Valid {
		item.Marshal = &MarshalResponse{ID: marshalID.String, Name: marshalName.String}
	}
	if reasonType.Valid && cancelledAt.Valid {
		item.CenterCancellation = &CenterCancellationResponse{
			ReasonType:  reasonType.String,
			ReasonText:  nullableText(reasonText),
			CancelledAt: cancelledAt.Time.Format(time.RFC3339),
		}
	}
	return item, nil
}

func nullableText(value pgtype.Text) string {
	if !value.Valid {
		return ""
	}
	return value.String
}
