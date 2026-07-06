# Апекс — MVP приложения для картинг-центра

## Краткое описание

«Апекс» — учебный MVP клиентского приложения для картинг-центра.

Проект позволяет клиенту авторизоваться по телефону, посмотреть доступные заезды, создать бронь на слот, посмотреть свои брони и отменить бронь при соблюдении правил отмены.

На текущем этапе реализованы backend API, PostgreSQL-схема, миграции, seed-данные, статический web-demo для ручной проверки сценариев в браузере и Gradle bootstrap для запуска web-demo через привычную команду.

## Что реализовано

### Backend endpoints

```text
GET    /healthz
GET    /readyz

POST   /auth/request-sms
POST   /auth/verify-sms

GET    /ride-slots
GET    /ride-slots/{slotId}

POST   /bookings
GET    /bookings
GET    /bookings/{bookingId}
POST   /bookings/{bookingId}/cancel

POST   /push/device-tokens
DELETE /push/device-tokens
```

### Основные сценарии

| Сценарий | Статус |
|---|---|
| Авторизация по телефону и SMS-коду | Реализовано |
| Dev SMS-код `1111` | Реализовано |
| Просмотр списка слотов | Реализовано |
| Просмотр деталей слота | Реализовано |
| Бронирование одного места | Реализовано |
| Статус новой брони `PENDING_CONFIRMATION` | Реализовано |
| Просмотр моих броней | Реализовано |
| Просмотр деталей брони | Реализовано |
| Отмена брони клиентом | Реализовано |
| Регистрация push-token | Реализовано |
| Удаление push-token | Реализовано |
| Реальная отправка push-уведомлений | Не реализовано |
| Админское подтверждение/отклонение брони | Не реализовано |
| Отмена заезда центром через API | Не реализовано |
| Полноценное мобильное CMP-приложение | Не реализовано |

## Стек

### Backend

- Go
- `net/http`
- PostgreSQL
- `pgx`
- Goose migrations
- Docker Compose
- Makefile
- REST API
- JSON

### Web demo

- HTML
- CSS
- JavaScript
- Fetch API
- Static web-server через `python3 -m http.server`

### Dev tools

- Docker
- Docker Compose
- Make
- Gradle bootstrap
- curl
- Python 3

## Структура проекта

```text
.
├── backend/
│   ├── cmd/
│   │   └── api/
│   │       └── main.go
│   ├── internal/
│   │   ├── domain/
│   │   ├── http/
│   │   ├── server/
│   │   └── service/
│   ├── migrations/
│   │   ├── 00001_init.sql
│   │   └── 00002_seed_dev.sql
│   ├── compose.yaml
│   ├── Dockerfile
│   ├── Makefile
│   ├── go.mod
│   └── .env.example
│
├── web-demo/
│   └── index.html
│
├── webApp/
│   └── build.gradle.kts
│
├── settings.gradle.kts
├── build.gradle.kts
├── gradlew
├── gradlew.bat
└── README.md
```

## Требования для запуска

Нужно установить:

- Go
- Docker
- Docker Compose
- Make
- Python 3
- Gradle, если используется Gradle bootstrap

Проверка инструментов:

```bash
go version
docker --version
docker compose version
make --version
python3 --version
gradle -v
```

Если Gradle не установлен на macOS:

```bash
brew install gradle
```

## Как запустить backend

### 1. Перейти в backend

```bash
cd backend
```

### 2. Подготовить `.env`

```bash
make env
```

### 3. Запустить PostgreSQL

В проекте PostgreSQL используется на внешнем порту `5434`, чтобы не конфликтовать с локальным PostgreSQL на `5432`.

```bash
make db-up
```

Проверить контейнер:

```bash
docker compose ps
```

### 4. Применить миграции

```bash
make migrate
```

Проверить статус миграций:

```bash
make migrate-status
```

### 5. Запустить backend API

```bash
HTTP_ADDR=":8082" DATABASE_URL="postgres://apex:apex@127.0.0.1:5434/apex?sslmode=disable" make run
```

Backend будет доступен по адресу:

```text
http://127.0.0.1:8082
```

### 6. Проверить health/ready

В другом терминале:

```bash
curl http://127.0.0.1:8082/healthz
curl http://127.0.0.1:8082/readyz
```

Ожидаемый результат:

```json
{"status":"ok"}
```

```json
{"status":"ready"}
```

## Как запустить web-demo

Web-demo — это простая страница для ручной проверки реализованных backend-сценариев.

### Вариант 1. Через Python

Из корня проекта:

```bash
python3 -m http.server 5173 --directory web-demo
```

Открыть в браузере:

```text
http://127.0.0.1:5173
```

На странице в поле API base должен быть указан backend:

```text
http://127.0.0.1:8082
```

### Вариант 2. Через Gradle bootstrap

Из корня проекта:

```bash
./gradlew :webApp:wasmJsBrowserDevelopmentRun
```

Открыть:

```text
http://127.0.0.1:5173
```

## Как проверить основные сценарии через web-demo

1. Запустить backend на `8082`.
2. Запустить web-demo на `5173`.
3. Открыть `http://127.0.0.1:5173`.
4. Проверить, что API base равен `http://127.0.0.1:8082`.
5. Нажать проверку backend.
6. Авторизоваться по телефону.
7. Использовать dev SMS-код `1111`.
8. Загрузить слоты.
9. Выбрать доступный слот.
10. Создать бронь.
11. Открыть «Мои брони».
12. Отменить бронь.
13. Проверить регистрацию и удаление push-token.

## Как проверить API через curl

### 1. Базовый URL

```bash
BASE_URL="http://127.0.0.1:8082"
```

### 2. Запросить SMS

```bash
curl -X POST "$BASE_URL/auth/request-sms" \
  -H "Content-Type: application/json" \
  -d '{"phone":"+79990000001"}'
```

Ожидаемый результат:

```json
{"status":"sent"}
```

### 3. Получить access token

```bash
TOKEN=$(curl -s -X POST "$BASE_URL/auth/verify-sms" \
  -H "Content-Type: application/json" \
  -d '{"phone":"+79990000001","code":"1111"}' \
  | python3 -c 'import sys,json; print(json.load(sys.stdin)["accessToken"])')

echo "$TOKEN"
```

### 4. Получить список слотов

```bash
curl "$BASE_URL/ride-slots?days=7&includeUnavailable=true"
```

### 5. Получить детали слота

```bash
curl "$BASE_URL/ride-slots/50000000-0000-0000-0000-000000000001"
```

### 6. Создать бронь

```bash
curl -X POST "$BASE_URL/bookings" \
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

Ожидаемый статус новой брони:

```text
PENDING_CONFIRMATION
```

### 7. Получить мои брони

```bash
curl -H "Authorization: Bearer $TOKEN" "$BASE_URL/bookings"
```

### 8. Отменить бронь

Подставить ID брони из ответа:

```bash
BOOKING_ID="<booking_id>"
```

```bash
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  "$BASE_URL/bookings/$BOOKING_ID/cancel"
```

Ожидаемый статус:

```text
CANCELLED_BY_CLIENT
```

### 9. Зарегистрировать push-token

Для текущего backend использовать platform `ANDROID` или `IOS`.

```bash
curl -X POST "$BASE_URL/push/device-tokens" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "platform":"ANDROID",
    "token":"dev-token-from-curl",
    "appVersion":"1.0.0",
    "locale":"ru-RU"
  }'
```

### 10. Удалить push-token

```bash
curl -X DELETE "$BASE_URL/push/device-tokens?token=dev-token-from-curl" \
  -H "Authorization: Bearer $TOKEN"
```

## Как прогнать backend-проверки

Из папки `backend`:

```bash
go test ./...
```

Проверить миграции:

```bash
make migrate-status
```

Проверить БД:

```bash
docker compose ps
docker exec apex-postgres pg_isready -U apex -d apex
```

## Как сбросить БД

Если нужно вернуть seed-данные в исходное состояние:

```bash
cd backend
make db-reset
make migrate
```

После этого можно снова прогонять проверки.

## Частые проблемы

### Порт `5432` занят

В проекте используется внешний порт PostgreSQL `5434`.

Проверить `compose.yaml`:

```bash
grep -A2 "ports:" backend/compose.yaml
```

Должно быть:

```yaml
ports:
  - "5434:5432"
```

### Порт `8082` занят

Проверить процесс:

```bash
lsof -i :8082
```

Остановить процесс:

```bash
kill -9 <PID>
```

Или запустить backend на другом порту:

```bash
HTTP_ADDR=":8083" DATABASE_URL="postgres://apex:apex@127.0.0.1:5434/apex?sslmode=disable" make run
```

### `DUPLICATE_BOOKING`

Если при создании брони возвращается `DUPLICATE_BOOKING`, значит текущий пользователь уже создал бронь на этот слот.

Варианты:

- выбрать другой слот;
- использовать другой телефон;
- отменить текущую бронь;
- сбросить БД.

```bash
cd backend
make db-reset
make migrate
```

### `db_unavailable` на `/readyz`

Проверить, что PostgreSQL запущен:

```bash
cd backend
docker compose ps
```

Проверить порт и `DATABASE_URL`:

```bash
grep "DATABASE_URL ?=" Makefile
```

Ожидаемо:

```text
postgres://apex:apex@127.0.0.1:5434/apex?sslmode=disable
```

## Известные ограничения

На текущем этапе не реализованы:

- полноценное мобильное Compose Multiplatform приложение;
- production SMS provider;
- реальная отправка push-уведомлений;
- scheduler для напоминаний за 24 часа и за 2 часа;
- административное подтверждение/отклонение брони;
- административная отмена заезда центром;
- онлайн-оплата;
- выбор экипировки;
- групповые бронирования;
- фильтры по дате, времени и трассе;
- оценка маршала;
- программа лояльности.

## Полезные команды

### Backend

```bash
cd backend
make env
make db-up
make migrate
make migrate-status
go test ./...
HTTP_ADDR=":8082" DATABASE_URL="postgres://apex:apex@127.0.0.1:5434/apex?sslmode=disable" make run
```

### Web-demo

```bash
python3 -m http.server 5173 --directory web-demo
```

или:

```bash
./gradlew :webApp:wasmJsBrowserDevelopmentRun
```

### Очистка временных overlay-папок

Если в корне остались overlay-папки, их можно удалить:

```bash
rm -rf apex-backend-api-connected-overlay
rm -rf apex-full-implementation-overlay
rm -rf apex-gradle-web-demo-fix-overlay
rm -rf apex-web-demo-overlay
```

И добавить в `.gitignore`:

```gitignore
apex-*-overlay/
```
