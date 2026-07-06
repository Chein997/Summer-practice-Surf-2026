# syntax=docker/dockerfile:1.7

ARG GO_VERSION=1.23

FROM golang:${GO_VERSION}-alpine AS builder

WORKDIR /src

RUN apk add --no-cache ca-certificates git tzdata

COPY go.mod go.sum ./
RUN --mount=type=cache,target=/go/pkg/mod \
    go mod download

COPY . .

RUN --mount=type=cache,target=/go/pkg/mod \
    --mount=type=cache,target=/root/.cache/go-build \
    CGO_ENABLED=0 GOOS=linux GOARCH=amd64 \
    go build -trimpath -ldflags="-s -w" -o /out/apex-api ./cmd/api

FROM alpine:3.20 AS runtime

WORKDIR /app

RUN apk add --no-cache ca-certificates tzdata curl \
    && addgroup -S app \
    && adduser -S app -G app

COPY --from=builder /out/apex-api /app/apex-api

USER app

EXPOSE 8080

ENV HTTP_ADDR=:8080

HEALTHCHECK --interval=10s --timeout=3s --start-period=10s --retries=5 \
  CMD curl -fsS http://127.0.0.1:8080/healthz || exit 1

ENTRYPOINT ["/app/apex-api"]
