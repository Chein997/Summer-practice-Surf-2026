package server

import (
	"net/http"
	"strings"
	"time"
)

func (s *Server) handleRequestSMS(w http.ResponseWriter, r *http.Request) {
	var req RequestSMSRequest
	if err := decodeJSON(r, &req); err != nil {
		writeError(w, http.StatusBadRequest, "BAD_REQUEST", "Некорректное тело запроса.")
		return
	}
	if !isValidPhone(req.Phone) {
	writeError(w, http.StatusUnprocessableEntity, "VALIDATION_ERROR", "Введите телефон в международном формате.")
	return
}

	expiresAt := time.Now().Add(5 * time.Minute)
	_, err := s.db.Exec(r.Context(), `
		INSERT INTO otp_codes (phone, purpose, code_hash, expires_at)
		VALUES ($1, 'LOGIN', $2, $3)
	`, req.Phone, hashString(s.cfg.OTPDevCode), expiresAt)
	if err != nil {
		s.logger.Error("request sms failed", "error", internalErr(err))
		writeError(w, http.StatusInternalServerError, "INTERNAL_ERROR", "Не удалось отправить SMS-код.")
		return
	}

	writeJSON(w, http.StatusOK, statusResponse{Status: "sent"})
}

func (s *Server) handleVerifySMS(w http.ResponseWriter, r *http.Request) {
	var req VerifySMSRequest
	if err := decodeJSON(r, &req); err != nil {
		writeError(w, http.StatusBadRequest, "BAD_REQUEST", "Некорректное тело запроса.")
		return
	}
	req.Phone = strings.TrimSpace(req.Phone)
	req.Code = strings.TrimSpace(req.Code)
	if !isValidPhone(req.Phone) {
	writeError(w, http.StatusUnprocessableEntity, "VALIDATION_ERROR", "Введите телефон в международном формате.")
	return
	}

	if req.Code == "" {
		writeError(w, http.StatusUnprocessableEntity, "VALIDATION_ERROR", "Введите SMS-код.")
		return
	}

	tx, err := s.db.Begin(r.Context())
	if err != nil {
		s.logger.Error("begin verify sms tx", "error", internalErr(err))
		writeError(w, http.StatusInternalServerError, "INTERNAL_ERROR", "Не удалось проверить SMS-код.")
		return
	}
	defer tx.Rollback(r.Context()) //nolint:errcheck

	var otpID string
	err = tx.QueryRow(r.Context(), `
		SELECT id::text
		FROM otp_codes
		WHERE phone = $1
		  AND purpose = 'LOGIN'
		  AND consumed_at IS NULL
		  AND expires_at > now()
		ORDER BY created_at DESC
		LIMIT 1
		FOR UPDATE
	`, req.Phone).Scan(&otpID)
	if err != nil {
		writeError(w, http.StatusUnauthorized, "INVALID_SMS_CODE", "SMS-код не найден или истёк.")
		return
	}

	if hashString(req.Code) != hashString(s.cfg.OTPDevCode) {
		_, _ = tx.Exec(r.Context(), `UPDATE otp_codes SET attempt_count = attempt_count + 1 WHERE id = $1`, otpID)
		writeError(w, http.StatusUnauthorized, "INVALID_SMS_CODE", "Неверный SMS-код.")
		return
	}

	var clientID string
	err = tx.QueryRow(r.Context(), `
		INSERT INTO clients (phone)
		VALUES ($1)
		ON CONFLICT (phone) DO UPDATE SET phone = EXCLUDED.phone
		RETURNING id::text
	`, req.Phone).Scan(&clientID)
	if err != nil {
		s.logger.Error("upsert client failed", "error", internalErr(err))
		writeError(w, http.StatusInternalServerError, "INTERNAL_ERROR", "Не удалось создать клиента.")
		return
	}

	token, err := randomToken()
	if err != nil {
		s.logger.Error("random token failed", "error", internalErr(err))
		writeError(w, http.StatusInternalServerError, "INTERNAL_ERROR", "Не удалось создать сессию.")
		return
	}
	expiresAt := time.Now().Add(s.cfg.AccessTokenTTL)
	_, err = tx.Exec(r.Context(), `
		INSERT INTO auth_sessions (client_id, token_hash, expires_at)
		VALUES ($1, $2, $3)
	`, clientID, hashString(token), expiresAt)
	if err != nil {
		s.logger.Error("insert auth session failed", "error", internalErr(err))
		writeError(w, http.StatusInternalServerError, "INTERNAL_ERROR", "Не удалось создать сессию.")
		return
	}

	_, err = tx.Exec(r.Context(), `UPDATE otp_codes SET consumed_at = now() WHERE id = $1`, otpID)
	if err != nil {
		s.logger.Error("consume otp failed", "error", internalErr(err))
		writeError(w, http.StatusInternalServerError, "INTERNAL_ERROR", "Не удалось завершить авторизацию.")
		return
	}

	if err := tx.Commit(r.Context()); err != nil {
		s.logger.Error("commit verify sms tx", "error", internalErr(err))
		writeError(w, http.StatusInternalServerError, "INTERNAL_ERROR", "Не удалось завершить авторизацию.")
		return
	}

	writeJSON(w, http.StatusOK, AuthTokenResponse{
		AccessToken:      token,
		TokenType:        "Bearer",
		ExpiresInSeconds: int64(s.cfg.AccessTokenTTL.Seconds()),
		Customer:         AuthCustomerResponse{ID: clientID, Phone: req.Phone},
	})
}
