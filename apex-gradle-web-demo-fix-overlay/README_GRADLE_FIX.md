# Gradle bootstrap fix for Apex

## Что это

В проекте отсутствовали:

```text
gradlew
settings.gradle.kts
build.gradle.kts
webApp/build.gradle.kts
```

Этот фикс добавляет минимальную Gradle-структуру и модуль `webApp`.

Команда:

```bash
./gradlew :webApp:wasmJsBrowserDevelopmentRun
```

запускает текущий `web-demo/index.html` на:

```text
http://127.0.0.1:5173
```

Это bootstrap-режим. Он нужен, чтобы можно было проверять web-часть через Gradle-команду уже сейчас.

## Что важно

Это пока не полноценная Kotlin/Wasm Compose-сборка.

Полноценный Compose Multiplatform Web проект требует отдельной настройки Kotlin Multiplatform / Compose plugin, source sets, dependencies и entrypoint. Сейчас в репозитории нет исходной Gradle-структуры, поэтому сначала добавлен безопасный runnable bootstrap.

## Как применить

Из корня проекта:

```bash
unzip /path/to/apex-gradle-web-demo-fix-overlay.zip
cp -R apex-gradle-web-demo-fix-overlay/* .
chmod +x ./gradlew
```

## Если Gradle не установлен

```bash
brew install gradle
```

Проверка:

```bash
gradle -v
```

## Проверить структуру

```bash
./gradlew doctor
```

## Запустить backend

В отдельном терминале:

```bash
cd backend
HTTP_ADDR=":8082" DATABASE_URL="postgres://apex:apex@127.0.0.1:5434/apex?sslmode=disable" make run
```

## Запустить web mode через Gradle

В другом терминале из корня проекта:

```bash
./gradlew :webApp:wasmJsBrowserDevelopmentRun
```

Открыть:

```text
http://127.0.0.1:5173
```

## Сгенерировать официальный Gradle Wrapper

После установки Gradle можно заменить bootstrap-скрипт настоящим wrapper:

```bash
gradle wrapper --gradle-version 8.10.2
chmod +x ./gradlew
```

После этого `./gradlew` будет работать как официальный Gradle Wrapper.
