# Apex backend API connected overlay

Этот патч подключает минимально рабочие backend endpoints поверх уже созданной PostgreSQL-схемы.

## Что добавляется

```text
backend/go.mod
backend/cmd/api/main.go
backend/internal/server/
├── auth.go
├── bookings.go
├── push.go
├── server.go
├── slots.go
└── types.go
```

## Что уже должно быть до применения

В `backend` уже должны быть:

```text
migrations/00001_init.sql
migrations/00002_seed_dev.sql
Makefile
compose.yaml
```

И БД должна быть поднята и промигрирована.

## Как применить

Из корня репозитория:

```bash
cp -R apex-backend-api-connected-overlay/* .
```

Потом из `backend`:

```bash
go mod tidy
go test ./...
make run
```

Если `go mod tidy` попросит скачать зависимости, это нормально.
Нужна зависимость:

```bash
go get github.com/jackc/pgx/v5
go mod tidy
```

## Endpoints

Публичные:

```text
GET  /healthz
GET  /readyz
POST /auth/request-sms
POST /auth/verify-sms
GET  /ride-slots
GET  /ride-slots/{slotId}
```

Защищённые Bearer token:

```text
POST   /bookings
GET    /bookings
GET    /bookings/{bookingId}
POST   /bookings/{bookingId}/cancel
POST   /push/device-tokens
DELETE /push/device-tokens
```

## Dev SMS-код

По умолчанию код:

```text
1111
```

Его можно поменять через env:

```bash
OTP_DEV_CODE=1234 make run
```
