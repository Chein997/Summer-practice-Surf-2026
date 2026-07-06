# Apex Web Demo

В проекте сейчас нет полноценного Gradle/Compose Web проекта:

```text
gradlew отсутствует
settings.gradle.kts отсутствует
build.gradle.kts отсутствует
webApp отсутствует
```

Поэтому для быстрой проверки в браузере добавлен простой статический web-demo.

## Что можно проверить

- health / ready backend;
- авторизацию по телефону и dev SMS-коду `1111`;
- получение списка слотов;
- детали слота;
- создание брони;
- список моих броней;
- отмену брони;
- регистрацию и удаление push-token.

## Как применить

Из корня репозитория:

```bash
cp -R apex-web-demo-overlay/* .
```

После применения появится:

```text
web-demo/index.html
README_WEB_DEMO.md
```

## Как запустить backend

В отдельном терминале:

```bash
cd /Users/chein/Documents/Univer/практика/backend
HTTP_ADDR=":8082" DATABASE_URL="postgres://apex:apex@127.0.0.1:5434/apex?sslmode=disable" make run
```

Проверить:

```bash
curl http://127.0.0.1:8082/healthz
curl http://127.0.0.1:8082/readyz
```

## Как запустить web-demo

В другом терминале из корня проекта:

```bash
cd /Users/chein/Documents/Univer/практика
python3 -m http.server 5173 --directory web-demo
```

Открыть в браузере:

```text
http://127.0.0.1:5173
```

## Важные замечания

1. В поле API base должен быть адрес backend:

```text
http://127.0.0.1:8082
```

2. Если backend запущен на другом порту, измени API base прямо на странице.

3. Если при создании брони приходит `DUPLICATE_BOOKING`, значит бронь на этот слот уже есть. Можно:
   - выбрать другой доступный слот;
   - отменить текущую бронь;
   - сделать reset БД.

4. Reset БД:

```bash
cd backend
make db-reset
make migrate
```
