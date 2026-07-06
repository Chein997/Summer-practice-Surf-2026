package main

import (
	"context"
	"log/slog"
	"net/http"
	"os"
	"time"

	"github.com/jackc/pgx/v5/pgxpool"
	"summer-practice-surf-2026/backend/internal/server"
)

func main() {
	logger := slog.New(slog.NewTextHandler(os.Stdout, &slog.HandlerOptions{}))

	cfg := server.Config{
		HTTPAddr:       getenv("HTTP_ADDR", ":8080"),
		DatabaseURL:    getenv("DATABASE_URL", "postgres://apex:apex@127.0.0.1:5434/apex?sslmode=disable"),
		AccessTokenTTL: 24 * time.Hour,
		OTPDevCode:     getenv("OTP_DEV_CODE", "1111"),
	}

	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	db, err := pgxpool.New(ctx, cfg.DatabaseURL)
	if err != nil {
		logger.Error("failed to create db pool", "error", err)
		os.Exit(1)
	}
	defer db.Close()

	api := server.New(db, cfg, logger)

	srv := &http.Server{
		Addr:              cfg.HTTPAddr,
		Handler:           api.Routes(),
		ReadHeaderTimeout: 5 * time.Second,
	}

	logger.Info("starting api", "addr", cfg.HTTPAddr)
	if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
		logger.Error("server stopped with error", "error", err)
		os.Exit(1)
	}
}

func getenv(key string, fallback string) string {
	value := os.Getenv(key)
	if value == "" {
		return fallback
	}
	return value
}
