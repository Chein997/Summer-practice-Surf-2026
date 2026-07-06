# Отчёт по реализации front-end части

## Реализовано

### Domain layer

Добавлены модели картинг-домена:

- `Customer`;
- `AuthToken`;
- `Money`;
- `TrackConfiguration`;
- `RideLevel`;
- `Marshal`;
- `RideSlot`;
- `CenterCancellation`;
- `Booking`;
- `BookingProfile`;
- `BookingConsents`;
- `PushPlatform`.

Добавлены enum-статусы:

- `RideSlotStatus`;
- `BookingStatus`;
- `TrackConfigurationType`;
- `RideLevelCode`.

### Core layer

Добавлены:

- `AppError`;
- `UiError`;
- mapper `AppError.toUiError()`;
- MVI primitives:
  - `UiState`;
  - `UiEvent`;
  - `UiEffect`;
  - `Store`;
- общий UI kit:
  - `ApexScreen`;
  - `ApexTextField`;
  - `ApexPrimaryButton`;
  - `ApexSecondaryButton`;
  - `ApexErrorBlock`;
  - `EmptyState`;
  - `StatusBadge`;
  - `KeyValue`;
  - отображение статусов слотов и броней.

### Repository contracts

Добавлены interfaces:

- `AuthRepository`;
- `RideSlotsRepository`;
- `BookingsRepository`;
- `PushRepository`;
- `TokenRepository`.

### Use cases

Добавлены:

- `RequestSmsCodeUseCase`;
- `VerifySmsCodeUseCase`;
- `ObserveAuthStateUseCase`;
- `LoadRideSlotsUseCase`;
- `LoadRideSlotDetailsUseCase`;
- `CreateBookingUseCase`;
- `LoadMyBookingsUseCase`;
- `LoadBookingDetailsUseCase`;
- `CancelBookingUseCase`.

### Fake data layer

Добавлены fake-реализации для разработки UI без backend:

- `FakeAuthRepository`;
- `FakeRideSlotsRepository`;
- `FakeBookingsRepository`;
- `FakePushRepository`;
- `InMemoryTokenRepository`.

Fake-данные соответствуют картинг-домену:

- доступный слот;
- заполненный слот;
- отменённый слот с причиной;
- тестовая бронь;
- fake SMS-код `1111`.

### Screens / Features

Реализованы contracts, stores и Compose UI для:

- SCR-001 `PhoneInputScreen`;
- SCR-002 `SmsCodeScreen`;
- SCR-003 `SlotsScreen`;
- SCR-004 empty schedule через `EmptyState`;
- SCR-005 `SlotDetailsScreen`;
- SCR-006 `BookingFormScreen`;
- SCR-007 `BookingCreatedScreen`;
- SCR-008 `MyBookingsScreen`;
- SCR-009 `BookingDetailsScreen`;
- SCR-010 `CancelBookingScreen`;
- SCR-011 `BookingCancelledScreen`;
- SCR-012 `ErrorStateScreen`;
- SCR-013 `PushPermissionScreen`.

### App shell

Добавлены:

- `AppRoute`;
- `SimpleBackStack`;
- `ApexAppRoot`.

`ApexAppRoot` является lightweight shell и показывает, как подключать MVP-маршруты. Полную интеграцию в текущую навигацию проекта нужно выполнить после применения overlay.

## Что не удалось

- Не удалось напрямую клонировать репозиторий из sandbox: `git clone` завершился DNS-ошибкой `Could not resolve host: github.com`.
- Из-за этого изменения не применены прямо в существующий проект и не закоммичены.
- Не удалось запустить:
  - `./gradlew :shared:allTests`;
  - `./gradlew :androidApp:assembleDebug`;
  - `./gradlew :webApp:wasmJsBrowserDevelopmentRun`.
- Не удалось проверить точные package names текущего клиента, поэтому новый код помещён в `com.volna.app.apex`.
- Реальный Ktor API client и DTO по OpenAPI не подключены, чтобы не ломать build без знания текущих зависимостей и генерации.
- Platform-specific push adapters не реализованы полностью:
  - Android FCM;
  - iOS APNs;
  - Web Push.
  Пока добавлен UI и repository/usecase boundary для будущего подключения.
- Полная навигация не встроена в существующий router проекта. Добавлены route-модели и app shell для ручного подключения.
