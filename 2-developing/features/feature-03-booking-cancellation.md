# Фича 03. Отмена брони клиентом

## 1. Цель

Реализовать отмену брони клиентом в мобильном приложении «Апекс».

Фича нужна, чтобы пользователь мог отказаться от созданной или активной брони, если до начала заезда осталось больше 1 часа.

После успешной отмены бронь должна перейти в статус `CANCELLED_BY_CLIENT`, а свободное место в слоте должно вернуться обратно.

## 2. Требования

### Функциональные требования

| ID | Требование |
|---|---|
| CANCEL-FR-001 | Пользователь может открыть детали своей брони. |
| CANCEL-FR-002 | Пользователь может отменить только свою бронь. |
| CANCEL-FR-003 | Отменить можно бронь в статусе `PENDING_CONFIRMATION` или `ACTIVE`. |
| CANCEL-FR-004 | Отменить можно только если до начала заезда больше 1 часа. |
| CANCEL-FR-005 | После отмены статус брони становится `CANCELLED_BY_CLIENT`. |
| CANCEL-FR-006 | После отмены `free_places` у слота увеличивается на 1. |
| CANCEL-FR-007 | Повторная отмена той же брони невозможна. |
| CANCEL-FR-008 | Если отмена невозможна, backend возвращает business error `ACTION_UNAVAILABLE`. |

### Нефункциональные требования

| ID | Требование |
|---|---|
| CANCEL-NFR-001 | Отмена брони должна выполняться транзакционно. |
| CANCEL-NFR-002 | Backend является источником истины по доступности действия отмены. |
| CANCEL-NFR-003 | Клиент должен показывать подтверждение перед отменой. |
| CANCEL-NFR-004 | Ошибка отмены не должна отображаться как raw JSON. |
| CANCEL-NFR-005 | Отмена не должна запрашивать причину от клиента в рамках MVP. |

## 3. Реализация

### Backend

Задействованные файлы:

```text
backend/internal/server/bookings.go
backend/internal/server/types.go
backend/internal/domain/apex.go
backend/internal/service/bookings/rules.go
backend/migrations/00001_init.sql
```

Endpoint:

```text
POST /bookings/{bookingId}/cancel
```

Задействованные таблицы:

```text
bookings
ride_slots
auth_sessions
clients
```

Логика:

1. Проверить Bearer token.
2. Получить `client_id` из auth-session.
3. Найти бронь по `bookingId`.
4. Проверить, что бронь принадлежит текущему клиенту.
5. Проверить статус:
   - разрешены `PENDING_CONFIRMATION`;
   - разрешены `ACTIVE`;
   - остальные статусы запрещены.
6. Получить связанный слот.
7. Проверить правило времени: старт заезда должен быть позже `now + 1 hour`.
8. Обновить бронь:
   - `status = CANCELLED_BY_CLIENT`;
   - `cancel_source = CLIENT`;
   - `cancelled_at = now`.
9. Увеличить `ride_slots.free_places` на 1.
10. Если после увеличения мест слот снова доступен — вернуть статус слота к `AVAILABLE`.
11. Вернуть обновлённую бронь.

### Client

Задействованные файлы:

```text
client/shared/src/commonMain/kotlin/com/volna/app/apex/feature/bookingdetails/BookingDetailsContracts.kt
client/shared/src/commonMain/kotlin/com/volna/app/apex/feature/bookingdetails/BookingDetailsStore.kt
client/shared/src/commonMain/kotlin/com/volna/app/apex/feature/bookingdetails/BookingDetailsScreen.kt

client/shared/src/commonMain/kotlin/com/volna/app/apex/feature/cancelbooking/CancelBookingContracts.kt
client/shared/src/commonMain/kotlin/com/volna/app/apex/feature/cancelbooking/CancelBookingStore.kt
client/shared/src/commonMain/kotlin/com/volna/app/apex/feature/cancelbooking/CancelBookingScreens.kt

client/shared/src/commonMain/kotlin/com/volna/app/apex/domain/usecase/BookingUseCases.kt
```

Экраны:

```text
SCR-009 — детали брони
SCR-010 — подтверждение отмены
SCR-011 — бронь отменена
SCR-012 — ошибка / отказ в действии
```

Client flow:

```text
MyBookingsScreen
→ BookingDetailsScreen
→ CancelBookingScreen
→ BookingCancelledScreen
```

Важное UX-правило:

```text
Отмена не выполняется сразу из деталей брони.
Сначала пользователь видит экран подтверждения.
```

## 4. Промты

### Промт для реализации backend

```text
Реализуй backend-фичу отмены брони клиентом.
Нужен endpoint POST /bookings/{bookingId}/cancel.
Endpoint должен быть защищён Bearer token.
Пользователь может отменить только свою бронь.
Разрешённые статусы: PENDING_CONFIRMATION и ACTIVE.
Если до начала заезда осталось 1 час или меньше, вернуть ACTION_UNAVAILABLE.
После отмены установи status=CANCELLED_BY_CLIENT, cancel_source=CLIENT, cancelled_at=now.
В той же транзакции увеличь ride_slots.free_places на 1.
Повторная отмена должна возвращать ACTION_UNAVAILABLE.
```

### Промт для реализации client

```text
Реализуй CMP-flow отмены брони:
SCR-009 детали брони, SCR-010 подтверждение отмены, SCR-011 бронь отменена.
На экране деталей показывай кнопку «Отменить бронь», только если canCancel=true.
По нажатию открывай экран подтверждения, не выполняй отмену сразу.
На подтверждении вызывай CancelBookingUseCase.
При успехе показывай экран «Бронь отменена».
При ACTION_UNAVAILABLE показывай SCR-012 с понятным сообщением.
Примени дизайн Apex: крупная карточка подтверждения, чёрная основная кнопка, вторичная кнопка «Оставить бронь».
```

### Промт для проверки

```text
Составь curl-проверку отмены брони:
1. Получить token.
2. Создать новую бронь на доступный слот.
3. Сохранить bookingId.
4. Получить детали брони.
5. Вызвать POST /bookings/{bookingId}/cancel.
6. Проверить, что статус стал CANCELLED_BY_CLIENT.
7. Повторить отмену и проверить ошибку ACTION_UNAVAILABLE.
8. Проверить, что free_places у слота увеличился.
```

## 5. Проверка

### 5.1. Получить token

```bash
TOKEN=$(curl -s -X POST http://127.0.0.1:8080/auth/verify-sms \
  -H "Content-Type: application/json" \
  -d '{"phone":"+79990000001","code":"1111"}' \
  | python3 -c 'import sys,json; print(json.load(sys.stdin)["accessToken"])')

echo "$TOKEN"
```

### 5.2. Создать бронь для отмены

```bash
CREATE_RESPONSE=$(curl -s -X POST http://127.0.0.1:8080/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "slotId":"50000000-0000-0000-0000-000000000002",
    "profile":{
      "fullName":"Тестовый Клиент",
      "phone":"+79990000001",
      "email":"test@example.com",
      "age":24
    },
    "safetyRulesAccepted":true,
    "parentalConsentAccepted":false
  }')

echo "$CREATE_RESPONSE"
```

Если бронь на этот слот уже есть, сначала сделай reset БД:

```bash
cd backend
make db-reset
make migrate
```

Потом повтори создание брони.

### 5.3. Сохранить bookingId

```bash
BOOKING_ID=$(echo "$CREATE_RESPONSE" | python3 -c 'import sys,json; print(json.load(sys.stdin)["id"])')
echo "$BOOKING_ID"
```

### 5.4. Получить детали брони

```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://127.0.0.1:8080/bookings/$BOOKING_ID
```

Ожидаемо:

```json
{
  "id": "...",
  "status": "PENDING_CONFIRMATION",
  "canCancel": true
}
```

### 5.5. Отменить бронь

```bash
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  http://127.0.0.1:8080/bookings/$BOOKING_ID/cancel
```

Ожидаемо:

```json
{
  "id": "...",
  "status": "CANCELLED_BY_CLIENT",
  "canCancel": false
}
```

### 5.6. Повторная отмена

```bash
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  http://127.0.0.1:8080/bookings/$BOOKING_ID/cancel
```

Ожидаемо: ошибка с кодом:

```text
ACTION_UNAVAILABLE
```

### 5.7. Проверить свободные места слота

```bash
curl http://127.0.0.1:8080/ride-slots/50000000-0000-0000-0000-000000000002
```

Ожидаемо:

```text
freePlaces увеличился на 1 после отмены
```
