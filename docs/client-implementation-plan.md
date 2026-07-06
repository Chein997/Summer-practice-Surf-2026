# План реализации клиентской части MVP приложения «Апекс»

Документ описывает план реализации клиентской части приложения на Compose Multiplatform.

Основано на:

- ТЗ экранов SCR-001…SCR-013;
- логиках LOGIC-001…LOGIC-007;
- OpenAPI `Apex Karting Mobile API`;
- доменных сущностях картинг-домена;
- ограничениях скоупа MVP;
- текущей структуре client-проекта: `shared`, `androidApp`, `iosApp`, `webApp`.

## 1. Цель клиентской реализации

Клиентское приложение должно дать пользователю возможность:

- авторизоваться по номеру телефона и SMS-коду;
- посмотреть слоты заездов на ближайшие 7 дней;
- увидеть пустое расписание, если слотов нет;
- открыть детали слота;
- заполнить форму бронирования;
- создать бронь в статусе `PENDING_CONFIRMATION`;
- увидеть экран успешного создания брони;
- посмотреть список своих броней;
- открыть детали брони;
- отменить бронь, если действие доступно;
- увидеть состояние отказа в действии или ошибку API;
- разрешить push-уведомления и зарегистрировать push-токен устройства.

## 2. Что не реализуем в клиентском MVP

В клиентском приложении не реализуем:

- онлайн-оплату;
- групповое бронирование;
- фильтры по дате;
- фильтры по времени;
- фильтры по трассе;
- выбор конкретного карта;
- выбор экипировки;
- рейтинг маршала;
- программу лояльности;
- промокоды;
- админские действия;
- создание и редактирование слотов;
- назначение маршалов;
- управление трассами;
- управление парком картов;
- подтверждение брони администратором;
- отклонение брони администратором;
- автоматическое подтверждение брони;
- таймер подтверждения брони;
- время, к которому нужно приехать заранее.

Клиент не является источником истины по свободным местам, статусам слотов, статусам броней и возможности отмены. Эти правила проверяет backend.

## 3. Текущая структура client-проекта

Ориентируемся на существующую структуру:

```text
client/
├── shared
├── androidApp
├── iosApp
├── webApp
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
└── gradlew.bat
```

`shared` должен содержать общую UI-логику, domain-модели, MVI primitives, feature contracts и platform adapters.

`androidApp`, `iosApp`, `webApp` должны оставаться тонкими host-приложениями.

## 4. Рекомендуемая архитектура клиента

Тип архитектуры: **Clean Architecture + MVI/ViewModel**.

```text
Composable Screen
    ↓ user event
ViewModel / Store
    ↓ use case
Repository interface
    ↓ repository implementation
Remote API / Local storage / Platform adapter
    ↓
Backend API
```

Правило разделения моделей:

```text
Remote DTO != Domain Model != UI Model
```

Правильная цепочка:

```text
API DTO → Data Mapper → Domain Model → UI Mapper → UI State
```

## 5. Рекомендуемый стек клиента

| Зона | Инструмент |
|---|---|
| UI | Compose Multiplatform |
| Архитектура | Clean Architecture + MVI |
| State | ViewModel + StateFlow |
| Side effects | SharedFlow / Channel |
| Async | Kotlin Coroutines |
| Network | Ktor Client |
| JSON | kotlinx.serialization |
| DI | Koin |
| Navigation | Compose Multiplatform Navigation или простой typed router для MVP |
| Secure storage | `expect/actual` token storage |
| Push | `expect/actual` push permission + token provider |
| Tests | `kotlin.test`, coroutine test, Turbine опционально |

Если в проекте уже выбран другой конкретный инструмент внутри этих зон, не заменять его без необходимости. Цель — реализовать MVP, а не переписать инфраструктуру.

## 6. Целевая структура `shared`

```text
shared/
└── src/
    ├── commonMain/
    │   └── kotlin/
    │       └── ru/apex/app/
    │           ├── core/
    │           │   ├── config/
    │           │   ├── error/
    │           │   ├── mvi/
    │           │   ├── network/
    │           │   ├── navigation/
    │           │   ├── storage/
    │           │   ├── theme/
    │           │   ├── time/
    │           │   └── ui/
    │           │
    │           ├── domain/
    │           │   ├── model/
    │           │   ├── repository/
    │           │   └── usecase/
    │           │
    │           ├── data/
    │           │   ├── remote/
    │           │   │   ├── api/
    │           │   │   ├── dto/
    │           │   │   └── mapper/
    │           │   ├── repository/
    │           │   └── storage/
    │           │
    │           ├── feature/
    │           │   ├── auth/
    │           │   ├── slots/
    │           │   ├── slotdetails/
    │           │   ├── bookingform/
    │           │   ├── bookingcreated/
    │           │   ├── mybookings/
    │           │   ├── bookingdetails/
    │           │   ├── cancelbooking/
    │           │   ├── errorstate/
    │           │   └── push/
    │           │
    │           ├── app/
    │           │   ├── App.kt
    │           │   ├── AppRoot.kt
    │           │   └── AppNavigation.kt
    │           │
    │           └── di/
    │               ├── CommonModule.kt
    │               ├── NetworkModule.kt
    │               ├── RepositoryModule.kt
    │               └── FeatureModule.kt
    │
    ├── androidMain/
    ├── iosMain/
    └── wasmJsMain/
```

## 7. Базовые MVI-контракты

Для каждой feature использовать одинаковый контракт:

```kotlin
data class FeatureState(
    val isLoading: Boolean = false,
    val error: UiError? = null,
)

sealed interface FeatureEvent

sealed interface FeatureEffect
```

Рекомендованная структура feature:

```text
feature/<name>/
├── <Name>Screen.kt
├── <Name>ViewModel.kt
├── <Name>Contract.kt
├── <Name>Mapper.kt
└── components/
```

Разделение:

- `State` — что показываем на экране;
- `Event` — что сделал пользователь или lifecycle;
- `Effect` — одноразовые действия: навигация, snackbar, запрос permission.

## 8. Этапы реализации

## Этап 0. Подготовка клиента

### CL-000. Зафиксировать client scope

**Цель:** исключить реализацию функций вне MVP.

**Действия:**

- обновить `client/README.md`;
- заменить упоминания старого домена `Volna` на `Apex Karting`;
- добавить список экранов SCR-001…SCR-013;
- добавить список функций вне скоупа;
- добавить команды запуска и тестов.

**Критерии готовности:**

- README говорит про картинг-приложение «Апекс»;
- README не вводит в заблуждение терминами SUP/Volna;
- разработчик понимает, какие экраны входят в MVP.

**Приоритет:** высокий.

---

### CL-001. Проверить Gradle setup

**Цель:** убедиться, что проект запускается локально.

**Действия:**

- проверить наличие `gradlew`;
- проверить команды:
  - `./gradlew :shared:allTests`;
  - `./gradlew :androidApp:assembleDebug`;
  - `./gradlew :webApp:wasmJsBrowserDevelopmentRun`;
- поправить README, если он говорит, что wrapper отсутствует, но файлы `gradlew` есть;
- зафиксировать версию Kotlin/Compose Multiplatform.

**Критерии готовности:**

- Gradle wrapper запускается;
- `shared` собирается;
- Android debug build собирается;
- Web/Wasm build запускается или известна причина, почему нет.

**Приоритет:** высокий.

---

### CL-002. Настроить базовую конфигурацию окружения

**Цель:** приложение должно знать base URL API и режим запуска.

**Действия:**

- добавить `AppConfig`;
- добавить `ApiConfig`;
- настроить dev/prod base URL;
- не хардкодить URL в feature-коде;
- предусмотреть mock mode, если backend ещё не готов.

**Пример:**

```kotlin
data class AppConfig(
    val apiBaseUrl: String,
    val isDebug: Boolean,
)
```

**Критерии готовности:**

- API base URL задаётся централизованно;
- feature-код не знает про конкретный URL;
- dev-сборка может ходить в локальный backend.

**Приоритет:** высокий.

---

## Этап 1. Core layer

### CL-010. Реализовать core MVI primitives

**Цель:** унифицировать состояние экранов.

**Добавить:**

```text
core/mvi
├── UiState
├── UiEvent
├── UiEffect
├── BaseViewModel
└── LoadableState
```

**Критерии готовности:**

- features используют единый подход к `State/Event/Effect`;
- loading/error/success состояния не дублируются хаотично;
- эффекты навигации не хранятся как постоянное состояние.

**Приоритет:** высокий.

---

### CL-011. Реализовать core error model

**Цель:** единая обработка ошибок API и сети.

**Модели:**

```kotlin
sealed interface AppError {
    data object NetworkUnavailable : AppError
    data object Unauthorized : AppError
    data object Forbidden : AppError
    data object ServiceUnavailable : AppError
    data object Unknown : AppError

    data class Validation(
        val message: String,
        val fields: Map<String, String> = emptyMap(),
    ) : AppError

    data class Business(
        val code: String,
        val message: String,
    ) : AppError
}
```

```kotlin
data class UiError(
    val title: String,
    val message: String,
    val retryAvailable: Boolean,
)
```

**Критерии готовности:**

- все API-ошибки приводятся к `AppError`;
- экраны получают `UiError`, а не HTTP exception;
- SCR-012 можно переиспользовать для отказов в действии.

**Приоритет:** высокий.

---

### CL-012. Реализовать theme и UI primitives

**Цель:** подготовить общий визуальный фундамент.

**Добавить:**

```text
core/theme
core/ui/components
core/ui/state
```

**Базовые компоненты:**

- primary button;
- secondary button;
- text field;
- phone input;
- code input;
- loading state;
- empty state;
- error state;
- status badge;
- price text;
- slot card;
- booking card;
- confirmation dialog.

**Критерии готовности:**

- features используют общие компоненты;
- нет копипасты кнопок/полей;
- состояния loading/empty/error выглядят единообразно.

**Приоритет:** средний.

---

### CL-013. Реализовать navigation foundation

**Цель:** централизованная навигация между SCR-экранами.

**Маршруты:**

```text
PhoneInput
SmsCode(phone)
Slots
EmptySchedule
SlotDetails(slotId)
BookingForm(slotId)
BookingCreated(bookingId)
MyBookings
BookingDetails(bookingId)
CancelBookingConfirmation(bookingId)
BookingCancelled(bookingId)
ErrorState
PushPermission
```

**Критерии готовности:**

- маршруты типизированы или централизованы;
- feature не знает про внутреннюю реализацию navigator;
- deep link можно добавить позже без переписывания features.

**Приоритет:** высокий.

---

## Этап 2. Domain layer

### CL-020. Описать domain-модели

**Модели:**

```text
Customer
AuthToken
RideSlot
TrackConfiguration
RideLevel
Marshal
Booking
BookingProfile
BookingConsents
CancellationInfo
PushDeviceToken
Money
```

**Статусы:**

```text
RideSlotStatus:
- AVAILABLE
- NO_FREE_PLACES
- CANCELLED

BookingStatus:
- PENDING_CONFIRMATION
- ACTIVE
- CANCELLED_BY_CLIENT
- CANCELLED_BY_CENTER
- REJECTED_BY_CENTER
- COMPLETED
- NO_SHOW
```

**Критерии готовности:**

- domain не зависит от Ktor DTO;
- domain не зависит от Compose;
- domain-модели отражают картинг-домен, а не SUP.

**Приоритет:** высокий.

---

### CL-021. Описать repository interfaces

**Интерфейсы:**

```text
AuthRepository
RideSlotsRepository
BookingsRepository
PushRepository
TokenRepository
```

**Методы:**

```text
AuthRepository:
- requestSms(phone)
- verifySms(phone, code)
- logout()

RideSlotsRepository:
- getRideSlots(days, includeUnavailable)
- getRideSlot(slotId)

BookingsRepository:
- createBooking(command)
- getMyBookings()
- getBooking(bookingId)
- cancelBooking(bookingId)

PushRepository:
- registerDeviceToken(command)
- deleteDeviceToken(token)

TokenRepository:
- getAccessToken()
- saveAccessToken(token)
- clearAccessToken()
```

**Критерии готовности:**

- use cases зависят от repository interfaces;
- реализации лежат в data layer;
- можно заменить real API на fake repository в тестах.

**Приоритет:** высокий.

---

### CL-022. Реализовать use cases

**Use cases:**

```text
RequestSmsCodeUseCase
VerifySmsCodeUseCase
ObserveAuthStateUseCase
LoadRideSlotsUseCase
LoadRideSlotDetailsUseCase
CreateBookingUseCase
LoadMyBookingsUseCase
LoadBookingDetailsUseCase
CancelBookingUseCase
RegisterPushTokenUseCase
DeletePushTokenUseCase
```

**Критерии готовности:**

- ViewModel не вызывает repository напрямую, если сценарий содержит бизнес-логику;
- use case возвращает domain result;
- ошибки приводятся к `AppError`.

**Приоритет:** высокий.

---

## Этап 3. Data/network layer

### CL-030. Реализовать Ktor API client

**Цель:** создать единый слой вызова backend API.

**Файлы:**

```text
data/remote/api/ApexApi.kt
data/remote/api/ApexApiImpl.kt
data/remote/dto/*
data/remote/mapper/*
```

**Endpoints:**

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

- все endpoints MVP описаны;
- bearer token добавляется автоматически для защищённых запросов;
- 401 маппится в `Unauthorized`;
- ошибки OpenAPI маппятся в `AppError`;
- DTO не используются напрямую в UI.

**Приоритет:** критический.

---

### CL-031. Реализовать DTO-модели и mappers

**Цель:** изолировать OpenAPI-формат от domain.

**Добавить DTO:**

```text
RequestSmsCodeRequestDto
VerifySmsCodeRequestDto
AuthTokenResponseDto
RideSlotDto
RideSlotDetailsDto
BookingDto
CreateBookingRequestDto
ErrorResponseDto
RegisterDeviceTokenRequestDto
```

**Mapper-правила:**

```text
DTO status string -> enum
DTO price -> Money
DTO error -> AppError
Domain command -> request DTO
Domain model -> UI model
```

**Критерии готовности:**

- неизвестный enum не роняет приложение;
- nullable-поля обрабатываются безопасно;
- mapping покрыт unit-тестами для ключевых DTO.

**Приоритет:** высокий.

---

### CL-032. Реализовать token storage

**Цель:** сохранять access token между запусками.

**Подход:**

```text
commonMain:
- expect class SecureTokenStorage

androidMain:
- actual через EncryptedSharedPreferences или DataStore

iosMain:
- actual через Keychain

wasmJsMain:
- actual через localStorage/sessionStorage
```

**Критерии готовности:**

- после login токен сохраняется;
- после logout токен удаляется;
- API client использует сохранённый токен;
- при 401 можно сбросить auth state.

**Приоритет:** высокий.

---

### CL-033. Реализовать fake repositories для разработки UI

**Цель:** не блокировать UI-разработку готовностью backend.

**Действия:**

- добавить fake `RideSlotsRepository`;
- добавить fake `BookingsRepository`;
- добавить fake `AuthRepository`;
- включать fake mode через config/debug flag.

**Критерии готовности:**

- UI экранов можно разрабатывать без backend;
- fake данные соответствуют картинг-домену;
- fake mode не попадает случайно в prod build.

**Приоритет:** средний.

---

## Этап 4. Auth feature

### CL-040. Реализовать SCR-001: ввод номера телефона

**Экран:** `SCR-001. Ввод номера телефона`

**Сценарии:**

- пользователь вводит номер телефона;
- клиент валидирует формат;
- по кнопке отправляет `POST /auth/request-sms`;
- при успехе переходит на экран SMS-кода;
- при ошибке показывает сообщение.

**State:**

```text
phone
phoneError
isLoading
error
isContinueEnabled
```

**Events:**

```text
PhoneChanged
ContinueClicked
RetryClicked
```

**Effects:**

```text
NavigateToSmsCode(phone)
ShowError
```

**Критерии готовности:**

- кнопка неактивна при пустом/невалидном номере;
- loading блокирует повторную отправку;
- ошибка API отображается;
- успешный запрос ведёт на SCR-002.

**Приоритет:** высокий.

---

### CL-041. Реализовать SCR-002: ввод SMS-кода

**Экран:** `SCR-002. Ввод SMS-кода`

**Сценарии:**

- пользователь вводит SMS-код;
- клиент отправляет `POST /auth/verify-sms`;
- при успехе сохраняет token;
- переходит на список слотов;
- при неверном коде показывает ошибку.

**State:**

```text
phone
code
codeError
isLoading
error
canSubmit
```

**Events:**

```text
CodeChanged
SubmitClicked
BackClicked
ResendClicked
```

**Effects:**

```text
NavigateToSlots
NavigateBack
```

**Критерии готовности:**

- код валидируется по длине;
- неверный код отображается понятным сообщением;
- успешная авторизация сохраняет токен;
- защищённые запросы получают bearer token.

**Приоритет:** высокий.

---

### CL-042. Реализовать auth state и стартовый route

**Цель:** приложение должно понимать, куда вести пользователя при старте.

**Правило:**

```text
Если token есть → Slots
Если token нет → PhoneInput
```

**Критерии готовности:**

- после перезапуска авторизованный пользователь попадает на список слотов;
- после logout пользователь возвращается на auth flow;
- при 401 token очищается.

**Приоритет:** высокий.

---

## Этап 5. Slots feature

### CL-050. Реализовать SCR-003: список доступных слотов

**Экран:** `SCR-003. Список доступных слотов`

**API:** `GET /ride-slots?days=7&includeUnavailable=true`

**State:**

```text
isLoading
items
isEmpty
error
```

**UI item:**

```text
slotId
date
time
duration
trackConfigurationName
rideLevelName
freePlacesText
priceText
statusBadge
canBook
```

**Сценарии:**

- экран загружает слоты при открытии;
- показывает loading;
- показывает список;
- показывает пустое расписание, если список пустой;
- показывает ошибку и retry;
- по клику на слот открывает детали.

**Критерии готовности:**

- слоты `AVAILABLE`, `NO_FREE_PLACES`, `CANCELLED` отображаются разными статусами;
- недоступные слоты не ведут к форме бронирования напрямую;
- retry повторяет запрос;
- pull-to-refresh можно добавить, если быстро реализуется, но не обязателен.

**Приоритет:** высокий.

---

### CL-051. Реализовать SCR-004: пустое расписание

**Экран:** `SCR-004. Пустое расписание`

**Сценарии:**

- отображается, если backend вернул пустой массив;
- пользователь может обновить расписание;
- можно перейти в мои брони, если навигация это предусматривает.

**Критерии готовности:**

- пустое состояние не считается ошибкой;
- текст объясняет, что пока нет доступных заездов;
- кнопка retry работает.

**Приоритет:** средний.

---

### CL-052. Реализовать SCR-005: детали слота

**Экран:** `SCR-005. Детали слота`

**API:** `GET /ride-slots/{slotId}`

**Показывать:**

- дату и время;
- длительность;
- трассу;
- уровень;
- маршала;
- свободные места;
- цену;
- адрес;
- место встречи;
- правила безопасности;
- условия отмены;
- статус;
- причину отмены, если слот отменён.

**Действия:**

```text
Если canBook = true → кнопка «Забронировать»
Если canBook = false → кнопка бронирования недоступна или отсутствует
```

**Критерии готовности:**

- доступный слот ведёт на форму бронирования;
- заполненный слот показывает «Мест нет»;
- отменённый слот показывает «Отменён» и причину;
- backend error отображается через общий error state.

**Приоритет:** высокий.

---

## Этап 6. Booking creation feature

### CL-060. Реализовать SCR-006: форма бронирования

**Экран:** `SCR-006. Форма бронирования`

**API:** `POST /bookings`

**Поля формы:**

- имя;
- телефон;
- email;
- возраст;
- согласие с правилами безопасности;
- согласие родителя/законного представителя, если возраст 16–17.

**Клиентская валидация:**

- имя не пустое;
- телефон не пустой и похож на E.164;
- email похож на email;
- возраст >= 16;
- согласие с правилами принято;
- если возраст 16–17, родительское согласие принято.

**Важно:** backend всё равно повторно проверяет все правила.

**State:**

```text
slotId
fullName
phone
email
age
safetyRulesAccepted
parentalConsentAccepted
fieldErrors
isLoading
error
canSubmit
```

**Events:**

```text
FullNameChanged
PhoneChanged
EmailChanged
AgeChanged
SafetyRulesToggled
ParentalConsentToggled
SubmitClicked
```

**Effects:**

```text
NavigateToBookingCreated(bookingId)
ShowError
```

**Критерии готовности:**

- форма не отправляется с невалидными данными;
- при `409 NO_FREE_PLACES` показывается отказ в действии;
- при `409 SLOT_CANCELLED` показывается отказ в действии;
- при `409 DUPLICATE_BOOKING` показывается понятное сообщение;
- при успехе открывается SCR-007.

**Приоритет:** критический.

---

### CL-061. Реализовать SCR-007: бронь создана, ожидает подтверждения

**Экран:** `SCR-007. Бронь создана: ожидает подтверждения`

**Сценарии:**

- отображается после успешного `POST /bookings`;
- показывает статус `PENDING_CONFIRMATION`;
- объясняет, что бронь ожидает ручного подтверждения;
- позволяет перейти в «Мои брони»;
- позволяет вернуться к списку слотов.

**Критерии готовности:**

- экран не обещает автоматическое подтверждение;
- нет таймера подтверждения;
- статус и текст соответствуют MVP.

**Приоритет:** высокий.

---

## Этап 7. My bookings feature

### CL-070. Реализовать SCR-008: мои брони

**Экран:** `SCR-008. Мои брони`

**API:** `GET /bookings`

**Показывать:**

- дату и время заезда;
- статус брони;
- трассу;
- уровень;
- цену;
- адрес;
- краткое действие, если доступно.

**Сценарии:**

- загрузка списка;
- пустой список;
- ошибка загрузки;
- переход в детали брони.

**Критерии готовности:**

- клиент видит список своих броней;
- пустой список не считается ошибкой;
- отменённые/отклонённые/завершённые брони отображаются, если пришли от backend;
- статусы визуально различаются.

**Приоритет:** высокий.

---

### CL-071. Реализовать SCR-009: детали брони

**Экран:** `SCR-009. Детали брони`

**API:** `GET /bookings/{bookingId}`

**Показывать:**

- статус брони;
- дату и время заезда;
- длительность;
- трассу;
- уровень;
- маршала;
- адрес;
- место встречи;
- правила безопасности;
- условия отмены;
- данные клиента из брони;
- доступность действия отмены;
- причину отмены центром, если есть.

**Действия:**

```text
Если canCancel = true → показать кнопку отмены
Если canCancel = false → не показывать кнопку или показать причину недоступности
Если status = CANCELLED_BY_CENTER → показать кнопку «Выбрать другой заезд»
```

**Критерии готовности:**

- `canCancel` берётся из backend или считается только как UI-подсказка;
- финальное решение об отмене всё равно принимает backend;
- отмена центром отображается с причиной;
- статус `PENDING_CONFIRMATION` не называется подтверждённой бронью.

**Приоритет:** высокий.

---

## Этап 8. Cancel booking feature

### CL-080. Реализовать SCR-010: подтверждение отмены

**Экран:** `SCR-010. Подтверждение отмены брони клиентом`

**Сценарии:**

- пользователь нажимает «Отменить бронь» в деталях;
- открывается подтверждение;
- пользователь подтверждает;
- клиент отправляет `POST /bookings/{bookingId}/cancel`;
- при успехе переходит на экран отменённой брони или обновляет детали;
- при отказе показывает SCR-012.

**Критерии готовности:**

- отмена не выполняется без подтверждения;
- loading блокирует повторный клик;
- при `ACTION_UNAVAILABLE` показывается понятное сообщение;
- после успеха статус обновляется.

**Приоритет:** высокий.

---

### CL-081. Реализовать SCR-011: бронь отменена клиентом

**Экран:** `SCR-011. Бронь отменена клиентом`

**Сценарии:**

- отображается после успешной отмены;
- показывает, что бронь отменена;
- даёт перейти к списку слотов;
- даёт перейти в мои брони.

**Критерии готовности:**

- экран не запрашивает причину отмены;
- экран не обещает возврат оплаты, потому что оплаты нет в MVP;
- статус соответствует `CANCELLED_BY_CLIENT`.

**Приоритет:** средний.

---

## Этап 9. Error handling feature

### CL-090. Реализовать SCR-012: ошибка / отказ в действии

**Экран:** `SCR-012. Ошибка / отказ в действии`

**Типовые случаи:**

```text
NETWORK_UNAVAILABLE
SERVICE_UNAVAILABLE
UNAUTHORIZED
VALIDATION_ERROR
SLOT_CANCELLED
NO_FREE_PLACES
DUPLICATE_BOOKING
ACTION_UNAVAILABLE
BOOKING_NOT_FOUND
UNKNOWN
```

**Поведение:**

- показать title;
- показать message;
- если действие можно повторить — показать retry;
- если нужно выбрать другой слот — показать кнопку «Выбрать другой заезд»;
- если token истёк — отправить на login.

**Критерии готовности:**

- ошибки не отображаются как raw JSON;
- пользователь видит понятное сообщение;
- есть действие восстановления, где оно уместно;
- features используют общий mapper ошибок.

**Приоритет:** высокий.

---

## Этап 10. Push feature

### CL-100. Реализовать SCR-013: push-уведомления

**Экран:** `SCR-013. Push-уведомления`

**Сценарии:**

- объяснить, какие уведомления нужны;
- запросить permission;
- получить platform push token;
- отправить `POST /push/device-tokens`;
- обработать отказ пользователя;
- обработать ошибку регистрации токена.

**Критерии готовности:**

- permission запрашивается на платформе корректно;
- отказ не блокирует основной сценарий бронирования;
- token регистрируется только для авторизованного пользователя;
- повторная регистрация не ломает приложение.

**Приоритет:** средний.

---

### CL-101. Реализовать platform adapters для push

**commonMain:**

```kotlin
expect class PushPermissionManager {
    suspend fun requestPermission(): PushPermissionResult
}

expect class PushTokenProvider {
    suspend fun getToken(): String?
}
```

**androidMain:**

- adapter под Android notification permission;
- adapter под FCM, если подключается;
- если FCM не подключён в MVP, использовать stub/dev token.

**iosMain:**

- adapter под APNs permission;
- adapter под APNs token, если подключается;
- если iOS host placeholder, зафиксировать ограничение.

**wasmJsMain:**

- adapter под browser notification permission;
- если web push не подключается, явно вернуть unsupported.

**Критерии готовности:**

- common code не содержит platform API;
- unsupported platform не падает;
- push можно отключить без поломки core сценариев.

**Приоритет:** средний / низкий.

---

## Этап 11. App shell и навигационный сценарий

### CL-110. Собрать основной user flow

**Главный flow:**

```text
PhoneInput
→ SmsCode
→ Slots
→ SlotDetails
→ BookingForm
→ BookingCreated
→ MyBookings
→ BookingDetails
→ CancelBookingConfirmation
→ BookingCancelled
```

**Альтернативные flow:**

```text
Slots empty → EmptySchedule
API error → ErrorState
Slot cancelled → SlotDetails with cancelled state
Booking cancelled by center → BookingDetails with reason + ChooseAnotherRide
Unauthorized → PhoneInput
Push permission → PushPermission
```

**Критерии готовности:**

- переходы работают;
- back navigation предсказуемая;
- после создания брони нельзя случайно повторно отправить форму через back;
- logout/401 очищает стек до auth flow.

**Приоритет:** высокий.

---

## Этап 12. Тестирование клиента

### CL-120. Unit-тесты use cases

**Покрыть:**

- валидацию телефона;
- валидацию SMS-кода;
- валидацию формы бронирования;
- возраст меньше 16;
- возраст 16–17 без родительского согласия;
- отсутствие согласия с правилами;
- mapping API errors;
- mapping statuses;
- auth state при наличии/отсутствии токена.

**Критерии готовности:**

- тесты не требуют реального backend;
- fake repositories используются в тестах;
- `:shared:allTests` проходит.

**Приоритет:** высокий.

---

### CL-121. ViewModel-тесты

**Покрыть features:**

```text
Auth phone input
SMS code
Slots list
Slot details
Booking form
My bookings
Booking details
Cancel booking
Error state
Push permission
```

**Проверять:**

- initial state;
- loading state;
- success state;
- error state;
- navigation effects;
- retry behavior.

**Критерии готовности:**

- ViewModel logic тестируется без Compose UI;
- effects не теряются;
- ошибки маппятся корректно.

**Приоритет:** высокий.

---

### CL-122. UI-тесты smoke уровня

**Минимум:**

- auth flow;
- slots list renders;
- booking form renders validation errors;
- my bookings renders;
- error screen renders.

**Критерии готовности:**

- smoke UI-тесты проходят на Android;
- критические экраны не падают при пустых данных;
- длинные тексты не ломают layout.

**Приоритет:** средний.

---

### CL-123. Contract/manual тестирование с backend

**Сценарии:**

```text
1. request sms → verify sms → token saved
2. get ride slots
3. get slot details
4. create booking
5. get my bookings
6. get booking details
7. cancel booking
8. try cancel again → error
9. create booking on no free places → error
10. create booking on cancelled slot → error
```

**Критерии готовности:**

- клиент корректно работает с реальным backend;
- ошибки backend отображаются человекочитаемо;
- статусы совпадают с OpenAPI.

**Приоритет:** высокий.

---

## 9. Рекомендуемый порядок работ

```text
1. CL-000 / CL-001 / CL-002: подготовить README, Gradle и config.
2. CL-010 / CL-011 / CL-012 / CL-013: core MVI, ошибки, UI primitives, навигация.
3. CL-020 / CL-021 / CL-022: domain-модели, repository interfaces, use cases.
4. CL-030 / CL-031 / CL-032 / CL-033: API client, DTO/mappers, token storage, fake repositories.
5. CL-040 / CL-041 / CL-042: auth flow.
6. CL-050 / CL-051 / CL-052: slots flow.
7. CL-060 / CL-061: создание брони.
8. CL-070 / CL-071: мои брони и детали брони.
9. CL-080 / CL-081: отмена брони.
10. CL-090: общий error flow.
11. CL-100 / CL-101: push permission + token registration.
12. CL-110: собрать полный app flow.
13. CL-120 / CL-121 / CL-122 / CL-123: тесты и интеграционная проверка с backend.
```

Критический путь:

```text
Core → Domain → Network → Auth → Slots → Booking creation → My bookings → Cancel booking
```

Push можно делать после основных сценариев бронирования.

## 10. Карта экранов и features

| Экран | Feature | Приоритет |
|---|---|---|
| SCR-001 Ввод номера телефона | `auth` | Высокий |
| SCR-002 Ввод SMS-кода | `auth` | Высокий |
| SCR-003 Список доступных слотов | `slots` | Высокий |
| SCR-004 Пустое расписание | `slots` | Средний |
| SCR-005 Детали слота | `slotdetails` | Высокий |
| SCR-006 Форма бронирования | `bookingform` | Критический |
| SCR-007 Бронь создана | `bookingcreated` | Высокий |
| SCR-008 Мои брони | `mybookings` | Высокий |
| SCR-009 Детали брони | `bookingdetails` | Высокий |
| SCR-010 Подтверждение отмены | `cancelbooking` | Высокий |
| SCR-011 Бронь отменена клиентом | `cancelbooking` | Средний |
| SCR-012 Ошибка / отказ | `errorstate` | Высокий |
| SCR-013 Push-уведомления | `push` | Средний |

## 11. Карта API и client use cases

| API | Client use case |
|---|---|
| `POST /auth/request-sms` | `RequestSmsCodeUseCase` |
| `POST /auth/verify-sms` | `VerifySmsCodeUseCase` |
| `GET /ride-slots` | `LoadRideSlotsUseCase` |
| `GET /ride-slots/{slotId}` | `LoadRideSlotDetailsUseCase` |
| `POST /bookings` | `CreateBookingUseCase` |
| `GET /bookings` | `LoadMyBookingsUseCase` |
| `GET /bookings/{bookingId}` | `LoadBookingDetailsUseCase` |
| `POST /bookings/{bookingId}/cancel` | `CancelBookingUseCase` |
| `POST /push/device-tokens` | `RegisterPushTokenUseCase` |
| `DELETE /push/device-tokens` | `DeletePushTokenUseCase` |

## 12. Definition of Done для client-задачи

Client-задача считается готовой, если:

- экран соответствует SCR/LOGIC;
- не добавлен функционал вне MVP;
- feature использует `State/Event/Effect`;
- loading/empty/error/success состояния обработаны;
- ошибки API не показываются как raw JSON;
- domain-модели не зависят от DTO;
- UI не использует DTO напрямую;
- API-вызовы идут через repository/use case;
- токен авторизации не хардкодится;
- защищённые запросы используют bearer token;
- есть unit/ViewModel tests для логики;
- `./gradlew :shared:allTests` проходит;
- Android debug build собирается;
- при изменении API обновлены DTO/mappers;
- экран проверен на fake данных и, если backend готов, на реальном API.

## 13. Основные риски

| Риск | Как снизить |
|---|---|
| В UI попадёт старый SUP/Volna-домен | Начать с README, моделей и fake данных под картинг. |
| DTO начнут использоваться прямо в UI | Ввести обязательные mappers DTO → Domain → UI. |
| Клиент станет источником истины по местам | Всегда повторно полагаться на backend при создании/отмене. |
| Ошибки API будут непонятны пользователю | Сделать единый AppError → UiError mapper. |
| Push заблокирует основной MVP | Реализовать platform adapters и graceful unsupported/stub режим. |
| Backend ещё не готов | Использовать fake repositories и переключатель режима. |
| Навигация станет хаотичной | Сначала централизовать routes и app flow. |
| Form validation будет дублировать backend как финальное правило | Валидировать на клиенте только для UX, backend остаётся источником истины. |
| 401 не обработается | В network layer централизованно очищать token и вести в auth flow. |

## 14. Минимальный релизный срез клиента

Для первого рабочего client-среза достаточно:

```text
1. Core MVI + navigation.
2. Auth:
   - ввод телефона;
   - ввод SMS-кода;
   - сохранение token.
3. Slots:
   - список слотов;
   - пустое расписание;
   - детали слота.
4. Booking:
   - форма бронирования;
   - создание брони;
   - экран pending confirmation.
5. My bookings:
   - список моих броней;
   - детали брони;
   - отмена брони.
6. Error handling:
   - network error;
   - validation error;
   - business/action error.
7. Fake repositories для UI-разработки.
8. Интеграционная проверка с backend.
```

Push можно включить вторым срезом, если сроки ограничены.

## 15. PR-разбиение

Рекомендуемые Pull Requests:

```text
PR-1: Client README cleanup + scope + Gradle verification
PR-2: Core MVI + error model + navigation foundation
PR-3: Domain models + repository interfaces + use cases
PR-4: Ktor API client + DTO + mappers + token storage
PR-5: Auth screens SCR-001/SCR-002
PR-6: Slots screens SCR-003/SCR-004/SCR-005
PR-7: Booking form SCR-006 + booking created SCR-007
PR-8: My bookings SCR-008 + booking details SCR-009
PR-9: Cancel booking SCR-010/SCR-011
PR-10: Error state SCR-012
PR-11: Push SCR-013
PR-12: Tests + fake repositories + integration checklist
```

## 16. Итоговая схема клиента

```text
AppRoot
  ↓
Navigation
  ↓
Feature Screen
  ↓
ViewModel
  ↓
UseCase
  ↓
Repository
  ↓
ApexApi / TokenStorage / PlatformAdapter
  ↓
Backend
```

Главный фокус клиентского MVP:

```text
Авторизация → Список слотов → Детали слота → Создание брони → Мои брони → Отмена брони
```

Все остальные элементы должны поддерживать этот путь, а не расширять продукт за пределы MVP.
