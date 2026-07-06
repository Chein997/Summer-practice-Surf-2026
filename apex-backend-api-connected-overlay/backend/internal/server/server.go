package server

import (
	"context"
	"crypto/rand"
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"errors"
	"fmt"
	"log/slog"
	"net/http"
	"strings"
	"time"

	"github.com/jackc/pgx/v5/pgxpool"
)

type Server struct {
	db     *pgxpool.Pool
	cfg    Config
	logger *slog.Logger
}

func New(db *pgxpool.Pool, cfg Config, logger *slog.Logger) *Server {
	if cfg.AccessTokenTTL == 0 {
		cfg.AccessTokenTTL = 24 * time.Hour
	}
	if cfg.OTPDevCode == "" {
		cfg.OTPDevCode = "1111"
	}
	return &Server{db: db, cfg: cfg, logger: logger}
}

func (s *Server) Routes() http.Handler {
	mux := http.NewServeMux()
	mux.HandleFunc("GET /healthz", s.handleHealth)
	mux.HandleFunc("GET /readyz", s.handleReady)
	mux.HandleFunc("POST /auth/request-sms", s.handleRequestSMS)
	mux.HandleFunc("POST /auth/verify-sms", s.handleVerifySMS)
	mux.HandleFunc("GET /ride-slots", s.handleListRideSlots)
	mux.HandleFunc("GET /ride-slots/{slotId}", s.handleGetRideSlot)
	mux.HandleFunc("POST /bookings", s.handleCreateBooking)
	mux.HandleFunc("GET /bookings", s.handleListBookings)
	mux.HandleFunc("GET /bookings/{bookingId}", s.handleGetBooking)
	mux.HandleFunc("POST /bookings/{bookingId}/cancel", s.handleCancelBooking)
	mux.HandleFunc("POST /push/device-tokens", s.handleRegisterDeviceToken)
	mux.HandleFunc("DELETE /push/device-tokens", s.handleDeleteDeviceToken)
	return s.withRecover(s.withCORS(mux))
}

func (s *Server) handleHealth(w http.ResponseWriter, _ *http.Request) {
	writeJSON(w, http.StatusOK, statusResponse{Status: "ok"})
}

func (s *Server) handleReady(w http.ResponseWriter, r *http.Request) {
	ctx, cancel := context.WithTimeout(r.Context(), 2*time.Second)
	defer cancel()
	if err := s.db.Ping(ctx); err != nil {
		writeJSON(w, http.StatusServiceUnavailable, statusResponse{Status: "db_unavailable"})
		return
	}
	writeJSON(w, http.StatusOK, statusResponse{Status: "ready"})
}

func (s *Server) withRecover(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		defer func() {
			if recovered := recover(); recovered != nil {
				s.logger.Error("panic recovered", "value", recovered)
				writeError(w, http.StatusInternalServerError, "INTERNAL_ERROR", "Внутренняя ошибка сервера.")
			}
		}()
		next.ServeHTTP(w, r)
	})
}

func (s *Server) withCORS(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Access-Control-Allow-Origin", "*")
		w.Header().Set("Access-Control-Allow-Headers", "Authorization, Content-Type")
		w.Header().Set("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS")
		if r.Method == http.MethodOptions {
			w.WriteHeader(http.StatusNoContent)
			return
		}
		next.ServeHTTP(w, r)
	})
}

func decodeJSON(r *http.Request, dst any) error {
	dec := json.NewDecoder(r.Body)
	dec.DisallowUnknownFields()
	return dec.Decode(dst)
}

func writeJSON(w http.ResponseWriter, status int, payload any) {
	w.Header().Set("Content-Type", "application/json; charset=utf-8")
	w.WriteHeader(status)
	_ = json.NewEncoder(w).Encode(payload)
}

func writeError(w http.ResponseWriter, status int, code string, message string) {
	writeJSON(w, status, ErrorResponse{Code: code, Message: message})
}

func randomToken() (string, error) {
	buf := make([]byte, 32)
	if _, err := rand.Read(buf); err != nil {
		return "", err
	}
	return hex.EncodeToString(buf), nil
}

func hashString(value string) string {
	sum := sha256.Sum256([]byte(value))
	return hex.EncodeToString(sum[:])
}

func normalizeBearer(header string) string {
	header = strings.TrimSpace(header)
	if header == "" {
		return ""
	}
	parts := strings.SplitN(header, " ", 2)
	if len(parts) == 2 && strings.EqualFold(parts[0], "Bearer") {
		return strings.TrimSpace(parts[1])
	}
	return header
}

func (s *Server) authClientID(ctx context.Context, r *http.Request) (string, error) {
	token := normalizeBearer(r.Header.Get("Authorization"))
	if token == "" {
		return "", errors.New("missing token")
	}
	var clientID string
	err := s.db.QueryRow(ctx, `
		SELECT client_id::text
		FROM auth_sessions
		WHERE token_hash = $1
		  AND revoked_at IS NULL
		  AND expires_at > now()
	`, hashString(token)).Scan(&clientID)
	if err != nil {
		return "", err
	}
	return clientID, nil
}

func (s *Server) requireClientID(w http.ResponseWriter, r *http.Request) (string, bool) {
	clientID, err := s.authClientID(r.Context(), r)
	if err != nil {
		writeError(w, http.StatusUnauthorized, "UNAUTHORIZED", "Нужно войти по номеру телефона.")
		return "", false
	}
	return clientID, true
}

func internalErr(err error) string {
	if err == nil {
		return ""
	}
	return fmt.Sprintf("%T: %v", err, err)
}
