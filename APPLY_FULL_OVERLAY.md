# Полный overlay реализации Apex Karting MVP

Этот архив объединяет все подготовленные файлы:

- backend-реализацию и миграции;
- frontend/CMP-реализацию;
- дизайн-систему и экраны под макеты;
- Dockerfile / Makefile / compose.yaml;
- документацию по домену, ограничениям и планам реализации.

## Как применить

Распаковать архив в любую временную папку, затем из корня репозитория выполнить:

```bash
cp -R apex-full-implementation-overlay/* .
```

Если папка архива находится не рядом с репозиторием, укажите полный путь:

```bash
cp -R /path/to/apex-full-implementation-overlay/* .
```

## Что сделать перед применением

Рекомендуется удалить временные overlay-папки и `.DS_Store`, если они уже попали в репозиторий:

```bash
rm -rf apex-implementation-overlay
rm -rf apex-frontend-overlay
rm -rf apex-frontend-design-overlay
rm -rf apex-backend-devops
rm -rf apex-full-implementation-overlay

find . -name ".DS_Store" -delete
grep -qxF ".DS_Store" .gitignore || echo ".DS_Store" >> .gitignore
```

## Важно про переносы строк

Файлы в этом архиве сохранены как реальные файлы с нормальными переносами строк.

После копирования проверьте:

```bash
head -20 backend/migrations/00001_init.sql
head -20 backend/internal/domain/apex.go
head -20 client/shared/src/commonMain/kotlin/com/volna/app/apex/design/ApexDesignTokens.kt
```

Должно быть:

```sql
-- +goose Up

CREATE EXTENSION IF NOT EXISTS pgcrypto;
```

А не:

```sql
-- +goose Up CREATE EXTENSION IF NOT EXISTS pgcrypto;
```

## Backend-проверка

```bash
cd backend
make env
make db-up
make migrate
make test
make run
```

В отдельном терминале:

```bash
cd backend
make health
```

## Client-проверка

```bash
cd client
./gradlew :shared:allTests
./gradlew :androidApp:assembleDebug
```

## Возможные ручные правки

### Go module path

В некоторых новых Go-файлах imports могут требовать замены под фактический module path из `backend/go.mod`.

Проверьте:

```bash
cat backend/go.mod
grep -R "summer-practice-surf-2026" -n backend/internal
```

Если module path другой, замените imports.

### Material3

Дизайн-overlay использует Material3:

- `MaterialTheme`;
- `TextField`;
- `Checkbox`;
- `CircularProgressIndicator`.

Если в CMP-проекте Material3 не подключён, добавьте зависимость или замените компоненты на текущую Material-библиотеку.

### Подключение нового client-кода

Новый код лежит в namespace:

```text
client/shared/src/commonMain/kotlin/com/volna/app/apex/
```

Его нужно подключить к существующему `VolnaApp.kt` / `AppNavigation.kt` или постепенно заменить старую навигацию на `apex/app`.

## Что входит

Основные директории:

```text
backend/
client/shared/src/commonMain/kotlin/com/volna/app/apex/
docs/
reports/
```
