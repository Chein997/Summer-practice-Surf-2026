package server

import (
	"net/http"
	"strings"
)

func (s *Server) handleRegisterDeviceToken(w http.ResponseWriter, r *http.Request) {
	clientID, ok := s.requireClientID(w, r)
	if !ok {
		return
	}

	var req RegisterDeviceTokenRequest
	if err := decodeJSON(r, &req); err != nil {
		writeError(w, http.StatusBadRequest, "BAD_REQUEST", "Некорректное тело запроса.")
		return
	}
	req.Platform = strings.ToUpper(strings.TrimSpace(req.Platform))
	req.Token = strings.TrimSpace(req.Token)
	if req.Platform != "IOS" && req.Platform != "ANDROID" {
		writeError(w, http.StatusUnprocessableEntity, "VALIDATION_ERROR", "Платформа должна быть IOS или ANDROID.")
		return
	}
	if req.Token == "" {
		writeError(w, http.StatusUnprocessableEntity, "VALIDATION_ERROR", "Не указан push-токен.")
		return
	}

	_, err := s.db.Exec(r.Context(), `
		INSERT INTO push_device_tokens (client_id, platform, token, app_version, locale, last_seen_at)
		VALUES ($1, $2, $3, NULLIF($4, ''), NULLIF($5, ''), now())
		ON CONFLICT (client_id, token) DO UPDATE
		SET platform = EXCLUDED.platform,
		    app_version = EXCLUDED.app_version,
		    locale = EXCLUDED.locale,
		    last_seen_at = now()
	`, clientID, req.Platform, req.Token, req.AppVersion, req.Locale)
	if err != nil {
		s.logger.Error("register device token failed", "error", internalErr(err))
		writeError(w, http.StatusInternalServerError, "INTERNAL_ERROR", "Не удалось зарегистрировать push-токен.")
		return
	}
	writeJSON(w, http.StatusOK, statusResponse{Status: "registered"})
}

func (s *Server) handleDeleteDeviceToken(w http.ResponseWriter, r *http.Request) {
	clientID, ok := s.requireClientID(w, r)
	if !ok {
		return
	}

	token := strings.TrimSpace(r.URL.Query().Get("token"))
	if token == "" && r.Body != nil {
		var req DeleteDeviceTokenRequest
		if err := decodeJSON(r, &req); err == nil {
			token = strings.TrimSpace(req.Token)
		}
	}
	if token == "" {
		writeError(w, http.StatusUnprocessableEntity, "VALIDATION_ERROR", "Не указан push-токен.")
		return
	}

	_, err := s.db.Exec(r.Context(), `
		DELETE FROM push_device_tokens
		WHERE client_id = $1 AND token = $2
	`, clientID, token)
	if err != nil {
		s.logger.Error("delete device token failed", "error", internalErr(err))
		writeError(w, http.StatusInternalServerError, "INTERNAL_ERROR", "Не удалось удалить push-токен.")
		return
	}
	writeJSON(w, http.StatusOK, statusResponse{Status: "deleted"})
}
