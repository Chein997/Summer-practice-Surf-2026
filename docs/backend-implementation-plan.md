# План реализации backend-части MVP картинг-приложения «Апекс»

Документ описывает пошаговый план реализации backend-части на Go для MVP клиентского мобильного приложения картинг-центра «Апекс».

План основан на:

- OpenAPI-контракте `Apex Karting Mobile API`;
- доменных сущностях картинг-домена;
- ограничениях скоупа MVP;
- новой PostgreSQL-модели под картинг-домен;
- текущей структуре backend-проекта: `cmd`, `internal`, `migrations`, `seed`, `k6`, `Makefile`.

## 1. Цель backend-реализации

Backend должен предоставить клиентскому мобильному приложению API для:

- авторизации по номеру телефона и SMS-коду;
- получения списка слотов на ближайшие 7 дней;
- получения деталей слота;
- создания брони на одно место;
- получения списка своих броней;
- получения деталей брони;
- отмены брони клиентом;
- регистрации и удаления push-токена устройства.

Backend является источником истины по:

- расписанию;
- доступности слотов;
- свободным местам;
- статусам слотов;
- статусам броней;
- отсутствию двойных броней;
- правилам возраста и согласий;
- возможности отмены брони;
- отменам заезда центром.

## 2. Что не реализуем в backend MVP

В backend MVP не добавляем:

- онлайн-оплату;
- возвраты платежей;
- заказы и корзину;
- групповое бронирование;
- выбор конкретного карта;
- выбор экипировки;
- фильтры по дате, времени и трассе в UI-смысле;
- рейтинг маршала;
- программу лояльности;
- промокоды;
- админскую панель;
- endpoints для создания и редактирования слотов;
- endpoints для назначения маршалов;
- endpoints для управления трассами;
- endpoints для управления парком картов;
- автоматическое подтверждение брони;
- тайм-аут подтверждения брони.

Админские действия считаются внешней системой.

## 3. Рекомендуемая архитектура backend

Тип архитектуры: **модульный монолит + Clean/Hexagonal Architecture без избыточного усложнения**.

```text
HTTP / OpenAPI layer
        ↓
Handlers
        ↓
Application Services / Use Cases
        ↓
Domain
        ↓
Repository interfaces
        ↓
PostgreSQL repositories
```

Главное правило зависимостей:

```text
transport DTO != domain model
database row != domain model
HTTP error != domain error
```

## 4. Рекомендуемая структура backend

```text
backend/
├── cmd/
│   └── api/
│       └── main.go
│
├── internal/
│   ├── config/
│   │   └── config.go
│   │
│   ├── platform/
│   │   ├── clock/
│   │   ├── logger/
│   │   ├── passwordhash/
│   │   └── transaction/
│   │
│   ├── domain/
│   │   ├── auth.go
│   │   ├── booking.go
│   │   ├── cancellation.go
│   │   ├── customer.go
│   │   ├── errors.go
│   │   ├── push.go
│   │   └── ride_slot.go
│   │
│   ├── app/
│   │   ├── auth/
│   │   │   ├── service.go
│   │   │   ├── ports.go
│   │   │   └── commands.go
│   │   │
│   │   ├── slots/
│   │   │   ├── service.go
│   │   │   ├── ports.go
│   │   │   └── queries.go
│   │   │
│   │   ├── bookings/
│   │   │   ├── service.go
│   │   │   ├── ports.go
│   │   │   ├── commands.go
│   │   │   └── queries.go
│   │   │
│   │   └── push/
│   │       ├── service.go
│   │       ├── ports.go
│   │       └── commands.go
│   │
│   ├── storage/
│   │   └── postgres/
│   │       ├── db.go
│   │       ├── tx.go
│   │       ├── auth_repository.go
│   │       ├── customer_repository.go
│   │       ├── slots_repository.go
│   │       ├── bookings_repository.go
│   │       └── push_repository.go
│   │
│   └── http/
│       ├── openapi/
│       │   └── generated/
│       ├── handlers/
│       │   ├── auth_handler.go
│       │   ├── slots_handler.go
│       │   ├── bookings_handler.go
│       │   └── push_handler.go
│       ├── mapper/
│       │   ├── auth_mapper.go
│       │   ├── slots_mapper.go
│       │   ├── bookings_mapper.go
│       │   └── errors_mapper.go
│       ├── middleware/
│       │   ├── auth.go
│       │   ├── request_id.go
│       │   ├── recover.go
│       │   └── logging.go
│       └── router.go
│
├── migrations/
├── seed/
├── k6/
├── compose.yaml
├── Makefile
└── go.mod
```

## 5. Технический стек

| Зона | Инструмент |
|---|---|
| Язык | Go |
| HTTP | `net/http` + текущий роутер проекта / `chi`, если уже используется |
| API contract | OpenAPI |
| Генерация API | `oapi-codegen` или текущий генератор проекта |
| БД | PostgreSQL |
| Driver | `pgx` |
| Миграции | `goose` |
| Логи | `log/slog` |
| Конфиг | env variables + `.env.example` |
| Тесты | `testing`, `httptest`, интеграционные тесты с PostgreSQL |
| Нагрузочные проверки | k6 |
| Локальное окружение | Docker Compose |

Если в проекте уже есть конкретные инструменты, не заменять их без необходимости. Цель — привести домен и реализацию к ТЗ, а не переписать инфраструктуру.

## 6. Этапы реализации

## Этап 0. Подготовка и фиксация границ

### BE-000. Зафиксировать backend scope

**Цель:** чтобы команда не добавляла функции вне MVP.

**Действия:**

- добавить в `backend/README.md` ссылку на документы:
  - `domain-entities.md`;
  - `scope-constraints.md`;
  - OpenAPI;
  - этот план реализации;
- явно указать, что backend реализует только клиентский API;
- явно указать, что админские действия вне скоупа.

**Критерии готовности:**

- README не говорит про SUP/Volna как про текущий домен;
- README называет проект картинг-приложением «Апекс»;
- есть список endpoints MVP;
- есть список того, что не реализуется.

**Приоритет:** высокий.

---

### BE-001. Убрать SUP/Volna-наследие из backend-документации

**Цель:** убрать путаницу старого домена.

**Действия:**

- заменить упоминания `Volna SUP club` на `Apex Karting`;
- заменить пути вида `../01-analysis/api`, если фактический путь отличается;
- заменить переменные и примеры БД `volna`, если принято переименовать БД в `apex`;
- обновить k6/seed README, если там есть SUP-термины.

**Критерии готовности:**

- документация backend не содержит терминов `SUP`, `Volna`, `routes`, `boards`, если они не нужны технически;
- разработчик по README понимает, что проект про картинг.

**Приоритет:** высокий.

---

## Этап 1. Миграции и модель PostgreSQL

### BE-010. Заменить миграцию инициализации БД

**Цель:** заменить старую SUP-схему на картинг-домен.

**Входные файлы:**

- `00001_init.sql`;
- `00002_seed_dev.sql`.

**Действия:**

- заменить `backend/migrations/00001_init.sql`;
- заменить `backend/migrations/00002_seed_dev.sql`;
- проверить корректный goose-формат:
  - `-- +goose Up` отдельной строкой;
  - `-- +goose Down` отдельной строкой;
- удалить или не использовать старые таблицы:
  - `routes`;
  - `instructors`, если заменены на `marshals`;
  - `rental_boards_total`;
  - `free_rental_boards`;
  - `rental_price`.

**Ожидаемые таблицы:**

```text
clients
otp_codes
auth_sessions
track_configurations
ride_levels
marshals
ride_slots
bookings
push_device_tokens
push_notification_events
idempotency_keys
```

**Критерии готовности:**

- `make migrate` успешно применяет миграции на пустую БД;
- `goose down` откатывает миграции;
- seed создаёт тестовые картинг-данные;
- в БД нет SUP-домена.

**Приоритет:** критический.

---

### BE-011. Проверить ограничения БД

**Цель:** убедиться, что PostgreSQL защищает базовые инварианты.

**Проверить constraints:**

- формат телефона E.164;
- возраст бронирования `>= 16`;
- обязательное согласие с правилами безопасности;
- родительское согласие для возраста 16–17;
- статусы слотов:
  - `AVAILABLE`;
  - `NO_FREE_PLACES`;
  - `CANCELLED`;
- статусы броней:
  - `PENDING_CONFIRMATION`;
  - `ACTIVE`;
  - `CANCELLED_BY_CLIENT`;
  - `CANCELLED_BY_CENTER`;
  - `REJECTED_BY_CENTER`;
  - `COMPLETED`;
  - `NO_SHOW`;
- уникальность незавершённой брони клиента на слот;
- `free_places >= 0`;
- `free_places <= capacity`;
- причина отмены обязательна для отменённого центром слота или брони.

**Критерии готовности:**

- есть SQL/integration-тесты на ключевые ограничения;
- невозможно создать две pending/active брони одного клиента на один слот;
- невозможно создать бронь с возрастом меньше 16;
- невозможно создать бронь без `safety_rules_accepted = true`.

**Приоритет:** критический.

---

## Этап 2. Domain layer

### BE-020. Описать доменные модели

**Цель:** вынести бизнес-сущности из HTTP/DB-слоёв.

**Создать модели:**

```text
Customer
OtpCode
AuthSession
RideSlot
TrackConfiguration
RideLevel
Marshal
Booking
BookingProfile
BookingConsents
Cancellation
PushDeviceToken
PushNotificationEvent
Money
DomainError
```

**Файлы:**

```text
internal/domain/customer.go
internal/domain/auth.go
internal/domain/ride_slot.go
internal/domain/booking.go
internal/domain/cancellation.go
internal/domain/push.go
internal/domain/errors.go
```

**Критерии готовности:**

- в domain нет импортов из `internal/http`;
- в domain нет SQL-зависимостей;
- статусы и error codes описаны как типы/константы;
- бизнес-правила можно читать без знания HTTP и БД.

**Приоритет:** высокий.

---

### BE-021. Описать доменные ошибки

**Цель:** единообразно маппить бизнес-ошибки в HTTP.

**Минимальные ошибки:**

```text
VALIDATION_ERROR
UNAUTHORIZED
FORBIDDEN
NOT_FOUND
SLOT_NOT_FOUND
BOOKING_NOT_FOUND
SLOT_CANCELLED
NO_FREE_PLACES
DUPLICATE_BOOKING
BOOKING_RULES_VIOLATION
ACTION_UNAVAILABLE
RATE_LIMITED
INTERNAL_ERROR
SERVICE_UNAVAILABLE
```

**Критерии готовности:**

- service-слой возвращает доменные ошибки;
- handlers не анализируют текст ошибки;
- HTTP-маппинг централизован.

**Приоритет:** высокий.

---

## Этап 3. Storage layer

### BE-030. Реализовать подключение к PostgreSQL

**Цель:** настроить стабильный доступ к БД.

**Действия:**

- проверить `DATABASE_URL`;
- проверить `TEST_DATABASE_URL`;
- настроить pool;
- реализовать readiness check через `Ping`;
- добавить graceful close.

**Критерии готовности:**

- `/healthz` работает без БД;
- `/readyz` проверяет доступность БД;
- приложение корректно стартует через `make run`;
- приложение корректно стартует через Docker Compose.

**Приоритет:** высокий.

---

### BE-031. Реализовать transaction manager

**Цель:** централизовать транзакционные сценарии бронирования и отмены.

**Интерфейс:**

```go
type TxManager interface {
    WithinTx(ctx context.Context, fn func(ctx context.Context) error) error
}
```

**Критерии готовности:**

- создание брони выполняется в транзакции;
- отмена брони выполняется в транзакции;
- в транзакции можно использовать `SELECT ... FOR UPDATE`;
- rollback происходит при любой ошибке.

**Приоритет:** критический.

---

### BE-032. Реализовать репозитории

**Репозитории:**

```text
CustomerRepository
AuthRepository
RideSlotRepository
BookingRepository
PushRepository
IdempotencyRepository
```

**Основные методы:**

```text
CustomerRepository:
- FindByPhone
- FindByID
- Create
- FindOrCreateByPhone

AuthRepository:
- SaveOtpCode
- FindActualOtpCode
- ConsumeOtpCode
- CreateSession
- FindSessionByTokenHash
- RevokeSession

RideSlotRepository:
- ListUpcomingSlots
- GetSlotByID
- GetSlotByIDForUpdate
- DecreaseFreePlaces
- IncreaseFreePlaces

BookingRepository:
- Create
- GetByID
- GetByIDForUpdate
- ListByClientID
- ExistsNotFinalByClientAndSlot
- MarkCancelledByClient
- MarkCancelledByCenter

PushRepository:
- UpsertDeviceToken
- DeleteDeviceToken
- CreateNotificationEvent
```

**Критерии готовности:**

- repository не знает про HTTP;
- repository возвращает domain-модели или storage-модели с явным mapper;
- конкурентные сценарии используют row lock;
- интеграционные тесты проходят на PostgreSQL.

**Приоритет:** критический.

---

## Этап 4. Auth use cases

### BE-040. Реализовать `POST /auth/request-sms`

**Цель:** создать OTP-код для номера телефона.

**Логика:**

1. Принять номер телефона.
2. Проверить формат.
3. Сгенерировать SMS-код.
4. Сохранить hash кода.
5. Вернуть `202 Accepted`.
6. Не раскрывать технические ошибки отправки SMS клиенту.

**Для dev-режима:**

- можно не отправлять реальное SMS;
- можно логировать dev-код;
- нельзя хранить код в открытом виде.

**Критерии готовности:**

- endpoint возвращает `202`;
- невалидный телефон возвращает `400`;
- частые запросы ограничиваются `429`, если rate limit реализован;
- тесты покрывают happy path и validation error.

**Приоритет:** высокий.

---

### BE-041. Реализовать `POST /auth/verify-sms`

**Цель:** подтвердить SMS-код и выдать access token.

**Логика:**

1. Принять телефон и код.
2. Найти актуальный OTP.
3. Проверить срок действия.
4. Проверить код.
5. Создать или найти клиента.
6. Создать auth session.
7. Вернуть access token и данные клиента.
8. Пометить OTP как использованный.

**Критерии готовности:**

- валидный код возвращает `200`;
- неверный или истёкший код возвращает `401`;
- невалидный payload возвращает `400`;
- после успешного входа можно вызвать защищённые endpoints.

**Приоритет:** высокий.

---

### BE-042. Реализовать auth middleware

**Цель:** защищать endpoints, кроме запроса и проверки SMS.

**Действия:**

- читать `Authorization: Bearer <token>`;
- хешировать токен и искать session;
- проверять `expires_at`;
- проверять `revoked_at`;
- класть `client_id` в request context.

**Критерии готовности:**

- защищённые endpoints без токена возвращают `401`;
- endpoints с неверным токеном возвращают `401`;
- handlers получают текущего клиента из context;
- тесты покрывают middleware.

**Приоритет:** высокий.

---

## Этап 5. RideSlots use cases

### BE-050. Реализовать `GET /ride-slots`

**Цель:** вернуть список слотов на ближайшие 7 дней.

**Логика:**

1. Получить `days`, default `7`, максимум `7`.
2. Получить `includeUnavailable`, default `true`.
3. Выбрать слоты от текущего момента до `now + days`.
4. Вернуть доступные, заполненные и отменённые слоты.
5. Рассчитать `canBook`.

**Правила `canBook`:**

```text
canBook = status == AVAILABLE && free_places > 0
```

**Критерии готовности:**

- endpoint возвращает список слотов;
- пустое расписание возвращает пустой массив, не ошибку;
- `days > 7` не допускается;
- слоты `NO_FREE_PLACES` и `CANCELLED` возвращаются при `includeUnavailable = true`;
- для недоступных слотов `canBook = false`.

**Приоритет:** высокий.

---

### BE-051. Реализовать `GET /ride-slots/{slotId}`

**Цель:** вернуть детали слота.

**Логика:**

- найти слот по ID;
- вернуть трассу, уровень, маршала, адрес, место встречи, правила безопасности, условия отмены;
- вернуть статус и `canBook`;
- для отменённого слота вернуть причину отмены.

**Критерии готовности:**

- существующий слот возвращает `200`;
- неизвестный слот возвращает `404`;
- отменённый слот возвращается с `canBook = false`;
- тесты покрывают available/no_free_places/cancelled.

**Приоритет:** высокий.

---

## Этап 6. Bookings use cases

### BE-060. Реализовать `POST /bookings`

**Цель:** создать бронь на одно место.

**Транзакционный сценарий:**

```text
1. Получить текущего клиента из context.
2. Проверить request payload.
3. Открыть транзакцию.
4. Получить slot FOR UPDATE.
5. Проверить, что слот существует.
6. Проверить, что slot.status = AVAILABLE.
7. Проверить, что slot.free_places > 0.
8. Проверить отсутствие pending/active брони клиента на этот slot.
9. Проверить возраст и согласия.
10. Создать booking со статусом PENDING_CONFIRMATION.
11. Уменьшить free_places на 1.
12. Если free_places стал 0, обновить slot.status = NO_FREE_PLACES.
13. Зафиксировать транзакцию.
14. Вернуть booking.
```

**Ошибки:**

| Ситуация | HTTP | Error code |
|---|---:|---|
| Невалидный payload | 400 | `VALIDATION_ERROR` |
| Клиент не авторизован | 401 | `UNAUTHORIZED` |
| Слот не найден | 404 | `SLOT_NOT_FOUND` |
| Слот отменён | 409 | `SLOT_CANCELLED` |
| Нет мест | 409 | `NO_FREE_PLACES` |
| Дубль брони | 409 | `DUPLICATE_BOOKING` |
| Возраст/согласия нарушены | 422 | `BOOKING_RULES_VIOLATION` |

**Критерии готовности:**

- бронь создаётся только в статусе `PENDING_CONFIRMATION`;
- `free_places` уменьшается на 1;
- нельзя создать бронь на отменённый слот;
- нельзя создать бронь на заполненный слот;
- нельзя создать дубль брони;
- нельзя создать бронь клиенту младше 16 лет;
- нельзя создать бронь без согласия с правилами;
- нельзя создать бронь 16–17 лет без родительского согласия;
- конкурентное бронирование не уводит `free_places` ниже 0.

**Приоритет:** критический.

---

### BE-061. Реализовать `GET /bookings`

**Цель:** вернуть брони текущего клиента.

**Логика:**

- получить текущего клиента;
- вернуть брони клиента;
- не скрывать отменённые, отклонённые, завершённые брони и неявки, если они есть;
- поддержать limit/cursor, если это уже есть в OpenAPI;
- status-фильтр можно поддержать на уровне API, даже если UI MVP его не использует.

**Критерии готовности:**

- клиент видит только свои брони;
- чужие брони не попадают в выдачу;
- список сортируется по дате создания или дате заезда по согласованному правилу;
- пустой список возвращает пустой массив.

**Приоритет:** высокий.

---

### BE-062. Реализовать `GET /bookings/{bookingId}`

**Цель:** вернуть детали брони.

**Логика:**

- найти бронь;
- проверить владельца;
- вернуть статус, данные слота, условия отмены, доступные действия;
- если бронь отменена центром — вернуть причину отмены.

**Правила `canCancel`:**

```text
canCancel =
    booking.status IN (PENDING_CONFIRMATION, ACTIVE)
    AND slot.start_at > now + 1 hour
```

**Критерии готовности:**

- владелец получает `200`;
- чужая бронь возвращает `403` или `404` по выбранной политике;
- несуществующая бронь возвращает `404`;
- `canCancel` корректен для разных статусов и времени до старта.

**Приоритет:** высокий.

---

### BE-063. Реализовать `POST /bookings/{bookingId}/cancel`

**Цель:** отменить бронь клиентом.

**Транзакционный сценарий:**

```text
1. Получить текущего клиента из context.
2. Открыть транзакцию.
3. Получить booking FOR UPDATE.
4. Проверить владельца.
5. Получить slot FOR UPDATE.
6. Проверить статус booking: PENDING_CONFIRMATION или ACTIVE.
7. Проверить, что до slot.start_at больше 1 часа.
8. Обновить booking:
   - status = CANCELLED_BY_CLIENT
   - canceled_at = now
   - cancel_source = CLIENT
9. Увеличить slot.free_places на 1.
10. Если slot.status = NO_FREE_PLACES и free_places > 0:
    - обновить status = AVAILABLE.
11. Зафиксировать транзакцию.
12. Вернуть обновлённую бронь.
```

**Ошибки:**

| Ситуация | HTTP | Error code |
|---|---:|---|
| Нет авторизации | 401 | `UNAUTHORIZED` |
| Чужая бронь | 403 | `FORBIDDEN` |
| Бронь не найдена | 404 | `BOOKING_NOT_FOUND` |
| Статус не позволяет отмену | 409 | `ACTION_UNAVAILABLE` |
| До старта 1 час или меньше | 409 | `ACTION_UNAVAILABLE` |

**Критерии готовности:**

- бронь отменяется только владельцем;
- отмена за 1 час или меньше запрещена;
- место освобождается;
- отмена повторно не проходит;
- причина отмены от клиента не запрашивается;
- конкурентная отмена не ломает счётчик мест.

**Приоритет:** критический.

---

## Этап 7. Push use cases

### BE-070. Реализовать `POST /push/device-tokens`

**Цель:** зарегистрировать устройство клиента для push-уведомлений.

**Логика:**

- получить текущего клиента;
- принять platform, token, appVersion, locale;
- upsert по `client_id + token`;
- обновить `last_seen_at`.

**Критерии готовности:**

- endpoint защищён авторизацией;
- `ANDROID` и `IOS` принимаются;
- пустой token не принимается;
- повторная регистрация не создаёт дубль.

**Приоритет:** средний.

---

### BE-071. Реализовать `DELETE /push/device-tokens`

**Цель:** удалить push-токен устройства.

**Логика:**

- получить текущего клиента;
- удалить token только для этого клиента.

**Критерии готовности:**

- клиент может удалить свой токен;
- нельзя удалить чужой токен;
- повторное удаление не ломает API.

**Приоритет:** средний.

---

### BE-072. Подготовить push-события без реальной интеграции с FCM/APNs

**Цель:** оставить backend готовым к отправке push, но не усложнять MVP.

**Действия:**

- создать service-интерфейс `PushSender`;
- сделать `NoopPushSender` или `LogPushSender` для dev;
- писать события в `push_notification_events`, если нужен аудит.

**События MVP:**

```text
BOOKING_CONFIRMED
BOOKING_REJECTED
RIDE_REMINDER_24H
RIDE_REMINDER_2H
RIDE_CANCELLED_BY_CENTER
```

**Не отправлять в MVP:**

```text
BOOKING_CREATED_PENDING
BOOKING_CANCELLED_BY_CLIENT
CANCELLATION_NOT_ALLOWED
```

**Критерии готовности:**

- push API готов для клиента;
- реальная отправка push может быть подключена позже;
- нет блокировки MVP на FCM/APNs.

**Приоритет:** низкий / средний.

---

## Этап 8. HTTP layer и OpenAPI

### BE-080. Синхронизировать OpenAPI и generated-код

**Цель:** handlers должны соответствовать API-контракту.

**Действия:**

- проверить актуальность `openapi-apex-mobile.yaml`;
- запустить генерацию:
  - `make generate`;
- не редактировать generated-файлы вручную;
- привести handlers к generated interfaces.

**Критерии готовности:**

- `make generate` проходит;
- `make check-generated` проходит, если есть такая команда;
- OpenAPI и handlers совпадают по request/response;
- нет ручных изменений в generated-коде.

**Приоритет:** высокий.

---

### BE-081. Реализовать HTTP mappers

**Цель:** изолировать transport DTO от domain.

**Mapper-слои:**

```text
HTTP request DTO -> app command/query
domain model -> HTTP response DTO
domain error -> HTTP error response
```

**Критерии готовности:**

- handlers короткие;
- handlers не содержат бизнес-правил;
- ошибки возвращаются в формате OpenAPI;
- поля enum совпадают с OpenAPI.

**Приоритет:** высокий.

---

### BE-082. Реализовать middleware

**Минимальный набор:**

- request id;
- structured logging;
- recover panic;
- auth;
- CORS, если клиенту нужен web/Wasm;
- timeout, если принят в проекте.

**Критерии готовности:**

- panic не падает всем сервером;
- каждый запрос логируется с request id;
- защищённые endpoints требуют токен;
- ошибки middleware возвращаются в едином формате.

**Приоритет:** средний.

---

## Этап 9. Тестирование

### BE-090. Unit-тесты domain/app layer

**Покрыть:**

- возраст меньше 16;
- возраст 16–17 без родительского согласия;
- отсутствие согласия с правилами;
- `canCancel` при разных статусах;
- `canCancel` при старте больше/меньше 1 часа;
- маппинг доменных ошибок.

**Критерии готовности:**

- ключевые бизнес-правила покрыты unit-тестами;
- тесты не требуют PostgreSQL;
- запуск через `make test`.

**Приоритет:** высокий.

---

### BE-091. Integration-тесты PostgreSQL repository

**Покрыть:**

- применение миграций;
- создание клиента;
- создание OTP/session;
- список слотов;
- детали слота;
- создание брони;
- дубль брони;
- no free places;
- cancelled slot;
- cancel booking;
- ограничения БД.

**Критерии готовности:**

- тесты используют `TEST_DATABASE_URL`;
- тесты изолированы по schema или чистят данные;
- `go test ./internal/storage/postgres -count=1` проходит.

**Приоритет:** высокий.

---

### BE-092. HTTP handler tests

**Покрыть endpoints:**

```text
POST /auth/request-sms
POST /auth/verify-sms
GET /ride-slots
GET /ride-slots/{slotId}
POST /bookings
GET /bookings
GET /bookings/{bookingId}
POST /bookings/{bookingId}/cancel
POST /push/device-tokens
DELETE /push/device-tokens
```

**Критерии готовности:**

- happy path покрыт;
- основные ошибки покрыты;
- unauthorized покрыт;
- forbidden для чужой брони покрыт;
- response body соответствует OpenAPI.

**Приоритет:** высокий.

---

### BE-093. Конкурентные тесты бронирования

**Цель:** доказать, что нельзя забронировать больше мест, чем есть.

**Сценарий:**

```text
1. Создать слот с capacity = 1 и free_places = 1.
2. Запустить N параллельных POST /bookings.
3. Убедиться:
   - успешна только 1 бронь;
   - остальные получили 409;
   - free_places = 0;
   - slot.status = NO_FREE_PLACES.
```

**Критерии готовности:**

- тест стабильно проходит;
- нет отрицательных `free_places`;
- нет дублей active/pending брони.

**Приоритет:** критический.

---

## Этап 10. k6 и operational checks

### BE-100. Обновить k6 seed под картинг

**Цель:** заменить SUP-данные в нагрузочных сценариях.

**Действия:**

- обновить `cmd/k6seed`, если есть;
- заменить маршруты/инструкторов/доски на трассы/маршалов/слоты;
- подготовить deterministic пользователей и токены;
- подготовить слоты для booking/cancel сценариев.

**Критерии готовности:**

- `make k6-seed` создаёт картинг-данные;
- seed не конфликтует с миграционным seed;
- можно повторно запускать seed.

**Приоритет:** средний.

---

### BE-101. Обновить k6 сценарии

**Сценарии:**

- smoke;
- booking under load;
- cancel under load.

**Проверить:**

- `POST /bookings` под нагрузкой;
- `POST /bookings/{bookingId}/cancel` под нагрузкой;
- отсутствие overselling;
- корректные `409` при исчерпании мест.

**Критерии готовности:**

- `make k6-smoke` проходит;
- `make k6-booking-300` проходит без нарушения инвариантов;
- `make k6-cancel-300` проходит без нарушения инвариантов.

**Приоритет:** средний.

---

### BE-102. Проверить health/readiness

**Endpoints:**

```text
GET /healthz
GET /readyz
```

**Правила:**

- `/healthz` показывает, что процесс жив;
- `/readyz` показывает, что backend готов работать с БД.

**Критерии готовности:**

- оба endpoints работают локально;
- readiness падает при недоступной БД;
- Docker Compose healthcheck, если есть, использует правильный endpoint.

**Приоритет:** средний.

---

## Этап 11. Документация и developer experience

### BE-110. Обновить backend README

**Добавить:**

- описание картинг-домена;
- переменные окружения;
- запуск PostgreSQL;
- запуск миграций;
- запуск API;
- запуск тестов;
- запуск k6;
- OpenAPI workflow;
- список out of scope.

**Критерии готовности:**

- новый разработчик может поднять backend по README;
- README не содержит устаревший SUP-домен;
- команды актуальны.

**Приоритет:** высокий.

---

### BE-111. Добавить backend architecture.md

**Содержание:**

```text
1. Цель backend
2. Скоуп MVP
3. Слои архитектуры
4. Dependency rule
5. Основные use cases
6. Транзакционные сценарии
7. Error mapping
8. PostgreSQL модель
9. Testing strategy
10. Out of scope
```

**Критерии готовности:**

- архитектурные решения описаны;
- есть правила, куда класть новый код;
- есть схема flow для создания и отмены брони.

**Приоритет:** средний.

---

## 7. Порядок выполнения работ

Рекомендуемый порядок:

```text
1. BE-000 / BE-001: зафиксировать scope и убрать SUP/Volna-путаницу.
2. BE-010 / BE-011: заменить миграции и проверить ограничения БД.
3. BE-020 / BE-021: описать domain-модели и ошибки.
4. BE-030 / BE-031 / BE-032: реализовать PostgreSQL access, tx manager, repositories.
5. BE-080 / BE-081: синхронизировать OpenAPI и mappers.
6. BE-040 / BE-041 / BE-042: реализовать Auth.
7. BE-050 / BE-051: реализовать RideSlots.
8. BE-060 / BE-061 / BE-062 / BE-063: реализовать Bookings.
9. BE-070 / BE-071 / BE-072: реализовать Push.
10. BE-090 / BE-091 / BE-092 / BE-093: закрыть тесты.
11. BE-100 / BE-101 / BE-102: k6 и operational checks.
12. BE-110 / BE-111: документация.
```

Критический путь:

```text
Миграции → Domain → Repositories → Auth → Slots → Bookings → Tests
```

Push можно делать после базовых сценариев бронирования.

## 8. Карта endpoints и use cases

| Endpoint | Use case | Приоритет |
|---|---|---|
| `POST /auth/request-sms` | Запрос SMS-кода | Высокий |
| `POST /auth/verify-sms` | Подтверждение SMS и получение токена | Высокий |
| `GET /ride-slots` | Список слотов на 7 дней | Высокий |
| `GET /ride-slots/{slotId}` | Детали слота | Высокий |
| `POST /bookings` | Создание брони | Критический |
| `GET /bookings` | Мои брони | Высокий |
| `GET /bookings/{bookingId}` | Детали брони | Высокий |
| `POST /bookings/{bookingId}/cancel` | Отмена брони клиентом | Критический |
| `POST /push/device-tokens` | Регистрация push-токена | Средний |
| `DELETE /push/device-tokens` | Удаление push-токена | Средний |

## 9. Definition of Done для backend

Backend-задача считается готовой, если:

- реализация соответствует OpenAPI;
- реализация не добавляет функциональность вне MVP;
- business rules находятся в service/domain layer;
- handlers не содержат бизнес-логики;
- DTO не протекают в domain;
- БД-операции критичных сценариев выполняются в транзакциях;
- есть unit-тесты или integration-тесты;
- ошибки возвращаются в едином формате;
- `make test` проходит;
- `make lint` проходит;
- `make lint-api` проходит;
- `make generate` / `make check-generated` проходит, если затронут OpenAPI;
- миграции применяются на пустую БД;
- документация обновлена, если изменилась команда запуска, API или скоуп.

## 10. Риски реализации

| Риск | Что сделать |
|---|---|
| Старая SUP-модель останется в коде | Начать с миграций, README и переименования домена. |
| Overselling мест при параллельных бронированиях | Использовать транзакцию и `SELECT ... FOR UPDATE`. |
| Клиент создаёт дубль брони | Проверять в service и защищать partial unique index в БД. |
| Статусы в БД не совпадают с OpenAPI | Использовать единые enum-константы и tests. |
| Клиентская валидация заменит серверную | Все правила возраста, согласий и доступности проверять на backend. |
| Отмена за 1 час реализована только в UI | Проверять в backend по `slot.start_at` и текущему времени. |
| Generated-код правят вручную | Запретить ручные правки generated-файлов в README/PR checklist. |
| Push-интеграция заблокирует MVP | Ввести интерфейс и noop/log sender, реальную отправку подключить позже. |

## 11. Минимальный релизный срез

Для первого backend-среза достаточно реализовать:

```text
1. Миграции под картинг-домен.
2. Seed с тестовыми слотами.
3. Auth:
   - request sms;
   - verify sms;
   - middleware.
4. RideSlots:
   - list;
   - details.
5. Bookings:
   - create;
   - list my bookings;
   - details;
   - cancel.
6. Базовый error mapping.
7. Unit/integration tests для бронирования и отмены.
8. README с запуском.
```

Push можно включить во второй срез, если сроки ограничены, но таблицу `push_device_tokens` лучше оставить в схеме сразу, потому что endpoint есть в OpenAPI MVP.

## 12. PR-разбиение

Рекомендуемые Pull Requests:

```text
PR-1: Backend docs cleanup + scope
PR-2: PostgreSQL migrations + seed
PR-3: Domain models + domain errors
PR-4: Storage repositories + tx manager
PR-5: Auth endpoints + middleware
PR-6: RideSlots endpoints
PR-7: CreateBooking transaction
PR-8: List/Get/Cancel booking
PR-9: Push device tokens
PR-10: Integration tests + concurrent booking test
PR-11: k6 update + README/architecture
```

Такой порядок уменьшает риск большого непроверяемого PR и позволяет быстро получить рабочий вертикальный срез.

## 13. Итоговая схема реализации

```text
OpenAPI
  ↓ generate
HTTP handlers
  ↓ map request
Application service
  ↓ business rules
Domain models/errors
  ↓ repository ports
PostgreSQL repositories
  ↓ transaction
Database constraints
  ↓ map response
HTTP response
```

Главный backend-фокус MVP:

```text
Авторизация → Слоты → Создание брони → Мои брони → Отмена брони
```

Всё остальное добавлять только если оно прямо поддерживает эти сценарии.
