# Apex frontend overlay

Пакет содержит front-end/CMP-реализацию MVP-каркаса для приложения картинг-центра «Апекс».

## Как применить

Из корня репозитория:

```bash
cp -R apex-frontend-overlay/* .
```

Или вручную скопировать содержимое `client/shared/src/commonMain/kotlin/com/volna/app/apex`.

## Что внутри

```text
client/shared/src/commonMain/kotlin/com/volna/app/apex/
├── app
├── core
├── data/fake
├── domain
└── feature
```

## Важно

Файлы добавлены в namespace `com.volna.app.apex`, чтобы не ломать текущий код проекта до ручного встраивания.

Для полноценного подключения нужно:
1. Подключить `ApexAppRoot()` в host-приложении или перенести экраны в существующую навигацию.
2. Если в build нет Material3, добавить compose material3 или заменить UI components на используемый material.
3. Если в build нет coroutines, добавить `kotlinx-coroutines-core`.
4. Подключить реальные repositories вместо fake.
5. Добавить Ktor API client и DTO по OpenAPI.
