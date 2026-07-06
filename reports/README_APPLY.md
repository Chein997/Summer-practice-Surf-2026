
# Apex implementation overlay

Пакет содержит готовые файлы для переноса проекта с SUP/Volna-наследия на MVP картинг-домена «Апекс».

## Как применить

Из корня репозитория:

```bash
cp -R apex-implementation-overlay/* .
```

Или вручную скопировать файлы по путям внутри архива.

## Состав

```text
backend/migrations/00001_init.sql
backend/migrations/00002_seed_dev.sql
backend/internal/domain/apex.go
backend/internal/service/bookings/rules.go
backend/internal/http/mapper/apex_errors.go

client/shared/src/commonMain/kotlin/com/volna/app/apex/domain/model/ApexModels.kt
client/shared/src/commonMain/kotlin/com/volna/app/apex/domain/repository/Repositories.kt
client/shared/src/commonMain/kotlin/com/volna/app/apex/domain/usecase/AuthAndSlotsUseCases.kt
client/shared/src/commonMain/kotlin/com/volna/app/apex/domain/usecase/BookingUseCases.kt
client/shared/src/commonMain/kotlin/com/volna/app/apex/core/error/AppError.kt
client/shared/src/commonMain/kotlin/com/volna/app/apex/core/mvi/Mvi.kt
client/shared/src/commonMain/kotlin/com/volna/app/apex/feature/bookingform/BookingFormContract.kt

docs/domain-entities.md
docs/scope-constraints.md
docs/backend-implementation-plan.md
docs/client-implementation-plan.md
```

## Важно

Пути Go imports в overlay используют предположительный module path:

```go
summer-practice-surf-2026/backend/internal/...
```

После копирования проверь `backend/go.mod` и замени imports на фактический module path проекта.
