# Backend devops files

Добавлены файлы для упрощения локальной работы с backend-сервером:

```text
backend/Dockerfile
backend/Makefile
backend/compose.yaml
backend/.dockerignore
backend/.env.example
```

## Быстрый старт

Из папки `backend`:

```bash
make env
make db-up
make migrate
make run
```

Проверка:

```bash
make health
```

## Запуск полностью через Docker Compose

```bash
make compose-up
```

Это поднимет PostgreSQL, применит миграции и запустит API-контейнер.

## Полезные команды

```bash
make help
make test
make lint
make docker-build
make docker-run
make db-reset
make migrate-status
make migrate-down
```

## Примечание

Если в репозитории уже есть `Dockerfile`, `Makefile` или `compose.yaml`, сравните их с этими файлами перед заменой. Эти версии ориентированы на Go backend с точкой входа `./cmd/api`, PostgreSQL и goose-миграциями.
