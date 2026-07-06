# Apex frontend design overlay

Overlay применяет визуальный стиль загруженного дизайн-прототипа к ранее подготовленной CMP-верстке.

## Что добавлено

```text
client/shared/src/commonMain/kotlin/com/volna/app/apex/design/
├── ApexDesignTokens.kt
└── ApexComponents.kt
```

Обновлены Compose-экраны:

```text
feature/auth/AuthScreens.kt
feature/slots/SlotsScreens.kt
feature/bookingform/BookingFormScreen.kt
feature/bookingcreated/BookingCreatedScreen.kt
feature/mybookings/MyBookingsScreen.kt
feature/bookingdetails/BookingDetailsScreen.kt
feature/cancelbooking/CancelBookingScreens.kt
feature/errorstate/ErrorStateScreen.kt
feature/push/PushPermissionScreen.kt
```

Добавлен preview/app shell:

```text
app/ApexDesignedAppRoot.kt
```

## Как применить

Из корня репозитория:

```bash
cp -R apex-frontend-design-overlay/* .
```

После копирования проверьте сборку:

```bash
cd client
./gradlew :shared:allTests
./gradlew :androidApp:assembleDebug
```

## Важно

Overlay использует Material3 компоненты:
- `MaterialTheme`;
- `TextField`;
- `Checkbox`;
- `CircularProgressIndicator`.

Если в проекте не подключён `compose.material3`, нужно добавить зависимость или заменить компоненты на используемую Material-библиотеку.

## Дизайн-токены

Основные цвета:

```text
Background  #F1ECE3
Text        #171717
Muted       #716D65
Accent      #A7FF4F
AccentSoft  #DDFDC6
Warning     #FFE46B
Error       #FF433A
ErrorSoft   #FFD2D0
TrackBlue   #BFEFF7
Surface     #FFFFFF
SurfaceMuted #E9E1D4
```

Основные паттерны:
- чёрные pill-кнопки;
- белые карточки с округлением и нижней тенью;
- зелёный accent для активных состояний;
- bottom navigation на экранах списка;
- chip-теги для уровня, трассы и статусов;
- отдельные success/error круги;
- графический placeholder трассы вместо внешних изображений.
