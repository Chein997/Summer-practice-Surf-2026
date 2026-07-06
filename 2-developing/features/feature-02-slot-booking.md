# Фича 02. Бронирование слота

## 1. Цель

Реализовать создание брони на доступный слот заезда в картинг-центре «Апекс».

Фича нужна, чтобы авторизованный клиент мог:

- выбрать доступный заезд;
- посмотреть детали слота;
- заполнить данные участника;
- принять правила безопасности;
- отправить заявку на бронь;
- получить бронь в статусе `PENDING_CONFIRMATION`.

Важно: клиент создаёт не подтверждённую бронь, а заявку на бронирование, которая ожидает ручного подтверждения центром.

## 2. Требования

### Функциональные требования

| ID | Требование |
|---|---|
| BOOK-FR-001 | Пользователь может получить список слотов на ближайшие 7 дней. |
| BOOK-FR-002 | Пользователь может открыть детали слота. |
| BOOK-FR-003 | Забронировать можно только слот со статусом `AVAILABLE`. |
| BOOK-FR-004 | Нельзя забронировать слот без свободных мест. |
| BOOK-FR-005 | Нельзя создать повторную активную или ожидающую бронь на тот же слот. |
| BOOK-FR-006 | Одна бронь создаётся только на одно место. |
| BOOK-FR-007 | Для брони обязательны: имя, телефон, email, возраст, согласие с правилами безопасности. |
| BOOK-FR-008 | Минимальный возраст участника — 16 лет. |
| BOOK-FR-009 | Для участника 16–17 лет обязательно родительское согласие. |
| BOOK-FR-010 | После успешного создания бронь получает статус `PENDING_CONFIRMATION`. |
| BOOK-FR-011 | При создании брони backend уменьшает количество свободных мест в слоте. |

### Нефункциональные требования

| ID | Требование |
|---|---|
| BOOK-NFR-001 | Backend является источником истины по свободным местам. |
| BOOK-NFR-002 | Клиентская валидация нужна только для UX, backend повторно проверяет все правила. |
| BOOK-NFR-003 | Создание брони должно быть транзакционным. |
| BOOK-NFR-004 | Ошибки бизнес-логики должны возвращаться как понятные коды: `NO_FREE_PLACES`, `SLOT_CANCELLED`, `DUPLICATE_BOOKING`, `VALIDATION_ERROR`. |
| BOOK-NFR-005 | UI не должен обещать автоматическое подтверждение брони. |

## 3. Реализация

### Backend

Задействованные файлы:

```text
backend/internal/server/slots.go
backend/internal/server/bookings.go
backend/internal/server/types.go
backend/internal/domain/apex.go
backend/internal/service/bookings/rules.go
backend/migrations/00001_init.sql
backend/migrations/00002_seed_dev.sql
```

Endpoints:

```text
GET  /ride-slots
GET  /ride-slots/{slotId}
POST /bookings
```

Задействованные таблицы:

```text
ride_slots
bookings
clients
auth_sessions
track_configurations
ride_levels
marshals
```

Логика `GET /ride-slots`:

1. Получить параметры `days` и `includeUnavailable`.
2. Вернуть слоты из `ride_slots`.
3. Добавить данные трассы, уровня, маршала.
4. Вернуть статусы `AVAILABLE`, `NO_FREE_PLACES`, `CANCELLED`.

Логика `GET /ride-slots/{slotId}`:

1. Найти слот по ID.
2. Вернуть детальную информацию:
   - дата и время;
   - длительность;
   - трасса;
   - уровень;
   - маршал;
   - свободные места;
   - цена;
   - адрес;
   - место встречи;
   - правила безопасности;
   - условия отмены;
   - причина отмены центром, если есть.

Логика `POST /bookings`:

1. Проверить Bearer token.
2. Получить `client_id` из auth-session.
3. Получить `slotId`, `profile`, `safetyRulesAccepted`, `parentalConsentAccepted`.
4. Проверить валидность профиля.
5. Проверить возраст.
6. Проверить согласия.
7. Проверить слот:
   - существует;
   - не отменён;
   - есть свободные места;
   - статус `AVAILABLE`.
8. Проверить отсутствие дубля активной/ожидающей брони.
9. Создать бронь в статусе `PENDING_CONFIRMATION`.
10. Уменьшить `free_places`.
11. Вернуть созданную бронь.

### Client

Задействованные файлы:

```text
client/shared/src/commonMain/kotlin/com/volna/app/apex/feature/slots/SlotsContracts.kt
client/shared/src/commonMain/kotlin/com/volna/app/apex/feature/slots/SlotsStores.kt
client/shared/src/commonMain/kotlin/com/volna/app/apex/feature/slots/SlotsScreens.kt

client/shared/src/commonMain/kotlin/com/volna/app/apex/feature/bookingform/BookingFormContract.kt
client/shared/src/commonMain/kotlin/com/volna/app/apex/feature/bookingform/BookingFormStore.kt
client/shared/src/commonMain/kotlin/com/volna/app/apex/feature/bookingform/BookingFormScreen.kt

client/shared/src/commonMain/kotlin/com/volna/app/apex/feature/bookingcreated/BookingCreatedScreen.kt

client/shared/src/commonMain/kotlin/com/volna/app/apex/domain/usecase/BookingUseCases.kt
client/shared/src/commonMain/kotlin/com/volna/app/apex/domain/repository/Repositories.kt
```

Экраны:

```text
SCR-003 — список доступных слотов
SCR-004 — пустое расписание
SCR-005 — детали слота
SCR-006 — форма бронирования
SCR-007 — бронь создана, ожидает подтверждения
SCR-012 — ошибка / отказ в действии
```

Основной client flow:

```text
SlotsScreen
→ SlotDetailsScreen
→ BookingFormScreen
→ BookingCreatedScreen
```

## 4. Промты

### Промт для реализации backend

```text
Реализуй backend-фичу бронирования слота для MVP приложения «Апекс».
Нужны endpoints GET /ride-slots, GET /ride-slots/{slotId}, POST /bookings.
Создание брони должно быть доступно только авторизованному пользователю.
Проверь, что слот AVAILABLE, free_places > 0, слот не CANCELLED.
Проверь отсутствие дубля брони для того же client_id и slot_id в статусах PENDING_CONFIRMATION или ACTIVE.
Проверь профиль: имя, телефон, email, возраст >= 16, safetyRulesAccepted=true, parentalConsentAccepted=true для возраста 16–17.
Создавай бронь в статусе PENDING_CONFIRMATION.
В одной транзакции создай бронь и уменьши free_places.
Возвращай понятные ошибки: NO_FREE_PLACES, SLOT_CANCELLED, DUPLICATE_BOOKING, VALIDATION_ERROR.
```

### Промт для реализации client

```text
Реализуй CMP-flow бронирования слота:
SCR-003 список слотов, SCR-005 детали слота, SCR-006 форма бронирования, SCR-007 бронь создана.
Используй MVI: State, Event, Effect.
Список слотов получает данные через LoadRideSlotsUseCase.
Детали слота получают данные через LoadRideSlotDetailsUseCase.
Форма вызывает CreateBookingUseCase.
Покажи статусы слотов, свободные места, цену, трассу, уровень, адрес.
Не показывай бронирование как подтверждённое: статус после создания — «Ожидает подтверждения».
Примени дизайн Apex: карточки, chips, чёрные кнопки, бежевый фон.
```

### Промт для проверки

```text
Составь curl-проверку для создания брони:
1. Получить token через auth.
2. Получить список слотов.
3. Создать бронь на доступный слот.
4. Проверить, что статус брони PENDING_CONFIRMATION.
5. Повторно создать бронь на тот же слот и убедиться, что вернулась ошибка DUPLICATE_BOOKING.
6. Попробовать создать бронь на заполненный слот и получить NO_FREE_PLACES.
7. Попробовать создать бронь на отменённый слот и получить SLOT_CANCELLED.
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

### 5.2. Получить список слотов

```bash
curl "http://127.0.0.1:8080/ride-slots?days=7&includeUnavailable=true"
```

### 5.3. Получить детали доступного слота

```bash
curl http://127.0.0.1:8080/ride-slots/50000000-0000-0000-0000-000000000002
```

### 5.4. Создать бронь

```bash
curl -X POST http://127.0.0.1:8080/bookings \
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
  }'
```

Ожидаемо:

```json
{
  "id": "...",
  "status": "PENDING_CONFIRMATION",
  "slotId": "50000000-0000-0000-0000-000000000002"
}
```

### 5.5. Проверить список моих броней

```bash
curl -H "Authorization: Bearer $TOKEN" http://127.0.0.1:8080/bookings
```

### 5.6. Проверка дубля

Повторить запрос создания брони на тот же `slotId`.

Ожидаемо: ошибка с кодом:

```text
DUPLICATE_BOOKING
```

### 5.7. Проверка заполненного слота

```bash
curl -X POST http://127.0.0.1:8080/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "slotId":"50000000-0000-0000-0000-000000000003",
    "profile":{
      "fullName":"Тестовый Клиент",
      "phone":"+79990000001",
      "email":"test@example.com",
      "age":24
    },
    "safetyRulesAccepted":true,
    "parentalConsentAccepted":false
  }'
```

Ожидаемо: ошибка с кодом:

```text
NO_FREE_PLACES
```

### 5.8. Проверка отменённого слота

```bash
curl -X POST http://127.0.0.1:8080/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "slotId":"50000000-0000-0000-0000-000000000004",
    "profile":{
      "fullName":"Тестовый Клиент",
      "phone":"+79990000001",
      "email":"test@example.com",
      "age":24
    },
    "safetyRulesAccepted":true,
    "parentalConsentAccepted":false
  }'
```

Ожидаемо: ошибка с кодом:

```text
SLOT_CANCELLED
```
