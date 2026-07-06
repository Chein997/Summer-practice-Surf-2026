# Фича 01. Авторизация по телефону и SMS-коду

## 1. Цель

Реализовать простой вход пользователя в мобильное приложение «Апекс» по номеру телефона и SMS-коду.

Фича нужна, чтобы клиент мог:

- войти без пароля;
- получить токен авторизации;
- выполнять защищённые действия: создавать бронь, смотреть свои брони, отменять бронь, регистрировать push-token.

В MVP SMS не отправляется через внешний провайдер. Для dev-проверки используется фиксированный код `1111`.

## 2. Требования

### Функциональные требования

| ID | Требование |
|---|---|
| AUTH-FR-001 | Пользователь вводит номер телефона. |
| AUTH-FR-002 | Backend принимает запрос на отправку SMS-кода. |
| AUTH-FR-003 | Backend создаёт или обновляет клиента по номеру телефона. |
| AUTH-FR-004 | Backend принимает номер телефона и код подтверждения. |
| AUTH-FR-005 | Если код верный, backend создаёт auth-session. |
| AUTH-FR-006 | Backend возвращает `accessToken`. |
| AUTH-FR-007 | Защищённые endpoints принимают токен через `Authorization: Bearer <token>`. |
| AUTH-FR-008 | Если токен отсутствует или неверный, backend возвращает `401 Unauthorized`. |

### Нефункциональные требования

| ID | Требование |
|---|---|
| AUTH-NFR-001 | Номер телефона должен проверяться на базовый E.164-формат. |
| AUTH-NFR-002 | Dev-код не должен быть захардкожен в client UI как production-логика. |
| AUTH-NFR-003 | Токен должен храниться на клиенте отдельно от UI-состояния. |
| AUTH-NFR-004 | Ошибки авторизации должны возвращаться в понятном JSON-формате. |

## 3. Реализация

### Backend

Задействованные файлы:

```text
backend/cmd/api/main.go
backend/internal/server/server.go
backend/internal/server/types.go
backend/internal/server/auth.go
backend/migrations/00001_init.sql
```

Задействованные таблицы:

```text
clients
otp_codes
auth_sessions
```

Endpoints:

```text
POST /auth/request-sms
POST /auth/verify-sms
```

Логика `POST /auth/request-sms`:

1. Получить `phone` из JSON.
2. Проверить, что телефон не пустой.
3. Найти клиента по телефону.
4. Если клиента нет — создать запись в `clients`.
5. Создать запись в `otp_codes`.
6. Вернуть `{ "status": "sent" }`.

Логика `POST /auth/verify-sms`:

1. Получить `phone` и `code`.
2. Проверить код.
3. В dev-режиме код по умолчанию: `1111`.
4. Найти или создать клиента.
5. Создать запись в `auth_sessions`.
6. Вернуть `accessToken`, `tokenType`, `expiresInSeconds`.

### Client

Задействованные файлы:

```text
client/shared/src/commonMain/kotlin/com/volna/app/apex/feature/auth/AuthContracts.kt
client/shared/src/commonMain/kotlin/com/volna/app/apex/feature/auth/AuthStores.kt
client/shared/src/commonMain/kotlin/com/volna/app/apex/feature/auth/AuthScreens.kt
client/shared/src/commonMain/kotlin/com/volna/app/apex/domain/usecase/AuthAndSlotsUseCases.kt
client/shared/src/commonMain/kotlin/com/volna/app/apex/domain/repository/Repositories.kt
```

Экраны:

```text
SCR-001 — ввод номера телефона
SCR-002 — ввод SMS-кода
```

Состояния:

```text
PhoneInputState
SmsCodeState
```

События:

```text
PhoneChanged
ContinueClicked
CodeChanged
SubmitClicked
ResendClicked
BackClicked
```

Эффекты:

```text
NavigateToSmsCode
NavigateToSlots
NavigateBack
```

## 4. Промты

### Промт для реализации backend

```text
Реализуй backend-фичу авторизации по телефону и SMS-коду для MVP приложения картинг-центра «Апекс».
Нужны endpoints POST /auth/request-sms и POST /auth/verify-sms.
Используй PostgreSQL-таблицы clients, otp_codes, auth_sessions.
В dev-режиме используй фиксированный SMS-код 1111.
После успешной проверки кода возвращай accessToken.
Защищённые endpoints должны использовать Authorization: Bearer <token>.
Ошибки возвращай в JSON с code и message.
```

### Промт для реализации client

```text
Реализуй CMP-экраны авторизации:
SCR-001 ввод телефона и SCR-002 ввод SMS-кода.
Используй MVI: State, Event, Effect.
На первом экране пользователь вводит телефон и отправляет requestSms.
На втором экране вводит код и вызывает verifySms.
После успешной проверки сохрани accessToken и перейди на список заездов.
Примени дизайн-систему Apex: бежевый фон, белые карточки, чёрные pill-кнопки, крупные input-поля.
```

### Промт для проверки

```text
Составь curl-проверку для авторизации:
1. POST /auth/request-sms с телефоном +79990000001.
2. POST /auth/verify-sms с кодом 1111.
3. Сохрани accessToken в переменную TOKEN.
4. Проверь, что защищённый endpoint без токена возвращает 401.
5. Проверь, что с Bearer token endpoint доступен.
```

## 5. Проверка

### 5.1. Запуск backend

```bash
cd backend
DATABASE_URL="postgres://apex:apex@127.0.0.1:5434/apex?sslmode=disable" make run
```

### 5.2. Проверка health

```bash
curl http://127.0.0.1:8080/healthz
curl http://127.0.0.1:8080/readyz
```

Ожидаемо:

```json
{"status":"ok"}
{"status":"ready"}
```

### 5.3. Запрос SMS

```bash
curl -X POST http://127.0.0.1:8080/auth/request-sms \
  -H "Content-Type: application/json" \
  -d '{"phone":"+79990000001"}'
```

Ожидаемо:

```json
{"status":"sent"}
```

### 5.4. Проверка SMS

```bash
curl -X POST http://127.0.0.1:8080/auth/verify-sms \
  -H "Content-Type: application/json" \
  -d '{"phone":"+79990000001","code":"1111"}'
```

Ожидаемо:

```json
{
  "accessToken": "...",
  "tokenType": "Bearer",
  "expiresInSeconds": 86400
}
```

### 5.5. Сохранение токена

```bash
TOKEN=$(curl -s -X POST http://127.0.0.1:8080/auth/verify-sms \
  -H "Content-Type: application/json" \
  -d '{"phone":"+79990000001","code":"1111"}' \
  | python3 -c 'import sys,json; print(json.load(sys.stdin)["accessToken"])')

echo "$TOKEN"
```

### 5.6. Негативная проверка

```bash
curl -X POST http://127.0.0.1:8080/auth/verify-sms \
  -H "Content-Type: application/json" \
  -d '{"phone":"+79990000001","code":"0000"}'
```

Ожидаемо: ошибка авторизации или business error с понятным сообщением.
