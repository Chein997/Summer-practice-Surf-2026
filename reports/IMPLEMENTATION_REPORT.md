
# Отчёт по реализации

## Реализовано

### Backend

- Заменена миграция инициализации БД под картинг-домен:
  - `clients`;
  - `otp_codes`;
  - `auth_sessions`;
  - `track_configurations`;
  - `ride_levels`;
  - `marshals`;
  - `ride_slots`;
  - `bookings`;
  - `push_device_tokens`;
  - `push_notification_events`;
  - `idempotency_keys`.

- Добавлен dev seed под картинг:
  - короткая и длинная трасса;
  - уровни `NOVICE` и `EXPERIENCED`;
  - маршалы;
  - клиенты;
  - слоты `AVAILABLE`, `NO_FREE_PLACES`, `CANCELLED`;
  - тестовые брони;
  - push-токены.

- Добавлен backend domain foundation:
  - статусы слотов;
  - статусы броней;
  - причины отмены центром;
  - модели `Customer`, `RideSlot`, `Booking`, `BookingProfile`, `BookingConsents`, `PushDeviceToken`;
  - бизнес-валидации возраста, телефона, email, согласий;
  - проверка `CanBook`;
  - проверка `CanCancel`.

- Добавлен skeleton service-слоя бронирований:
  - транзакционное создание брони;
  - проверка слота;
  - проверка дубля;
  - создание `PENDING_CONFIRMATION`;
  - уменьшение мест;
  - транзакционная отмена клиентом;
  - увеличение свободных мест.

- Добавлен mapper доменных ошибок в HTTP-like error response:
  - `VALIDATION_ERROR`;
  - `SLOT_CANCELLED`;
  - `NO_FREE_PLACES`;
  - `DUPLICATE_BOOKING`;
  - `ACTION_UNAVAILABLE`;
  - `INTERNAL_ERROR`.

### Client / CMP

- Добавлены domain-модели картинг-домена:
  - `Customer`;
  - `AuthToken`;
  - `RideSlot`;
  - `TrackConfiguration`;
  - `RideLevel`;
  - `Marshal`;
  - `Booking`;
  - `BookingProfile`;
  - `BookingConsents`;
  - `Money`;
  - `PushPlatform`.

- Добавлены статусы:
  - `RideSlotStatus`;
  - `BookingStatus`;
  - `TrackConfigurationType`;
  - `RideLevelCode`.

- Добавлен общий error model:
  - `AppError`;
  - `UiError`;
  - mapper `AppError.toUiError()`.

- Добавлены MVI primitives:
  - `UiState`;
  - `UiEvent`;
  - `UiEffect`;
  - `LoadableState`.

- Добавлены repository interfaces:
  - `AuthRepository`;
  - `RideSlotsRepository`;
  - `BookingsRepository`;
  - `PushRepository`;
  - `TokenRepository`.

- Добавлены use cases:
  - `RequestSmsCodeUseCase`;
  - `VerifySmsCodeUseCase`;
  - `LoadRideSlotsUseCase`;
  - `LoadRideSlotDetailsUseCase`;
  - `CreateBookingUseCase`;
  - `LoadMyBookingsUseCase`;
  - `LoadBookingDetailsUseCase`;
  - `CancelBookingUseCase`.

- Добавлен контракт формы бронирования:
  - `BookingFormState`;
  - `BookingFormEvent`;
  - `BookingFormEffect`.

### Документация

- Добавлены документы:
  - `domain-entities.md`;
  - `scope-constraints.md`;
  - `backend-implementation-plan.md`;
  - `client-implementation-plan.md`.

## Что не удалось выполнить

- Не удалось напрямую клонировать репозиторий в sandbox: `git clone` завершился ошибкой DNS `Could not resolve host: github.com`.
- Из-за этого изменения не были применены коммитом в реальный репозиторий.
- Не удалось запустить `go test`, `go test ./...`, `./gradlew :shared:allTests`, `make migrate`, потому что локальной копии репозитория с зависимостями не было.
- Go imports в skeleton-файлах могут потребовать ручной корректировки под фактический `module` из `backend/go.mod`.
- Client code добавлен как новый namespace `com.volna.app.apex...`, чтобы не ломать существующий `com.volna.app` до полноценного рефакторинга.
- Реальные HTTP handlers, Ktor API client, DTO и ViewModel-реализации не были полностью подключены к существующему проекту, потому что без клонирования нельзя безопасно встроиться в текущие interfaces/build files.

## Следующий шаг

1. Распаковать overlay в корень репозитория.
2. Проверить `backend/go.mod` и поправить Go import path.
3. Запустить:
   ```bash
   cd backend
   make migrate
   make test
   ```
4. Запустить:
   ```bash
   cd client
   ./gradlew :shared:allTests
   ```
5. После компиляции подключить service skeleton к существующим handlers/repositories и CMP use cases к реальным экранам.
