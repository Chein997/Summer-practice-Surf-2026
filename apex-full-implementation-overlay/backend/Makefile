SHELL := /bin/sh

APP_NAME := apex-api
HTTP_ADDR ?= :8080

DATABASE_URL ?= postgres://apex:apex@127.0.0.1:5432/apex?sslmode=disable
TEST_DATABASE_URL ?= postgres://apex:apex@127.0.0.1:5433/apex_test?sslmode=disable

MIGRATIONS_DIR ?= ./migrations
GOOSE := go run github.com/pressly/goose/v3/cmd/goose@v3.24.1

DOCKER_COMPOSE ?= docker compose

.DEFAULT_GOAL := help

.PHONY: help
help: ## Show available commands
	@awk 'BEGIN {FS = ":.*##"; printf "\nUsage:\n  make <target>\n\nTargets:\n"} /^[a-zA-Z0-9_.-]+:.*##/ { printf "  %-24s %s\n", $$1, $$2 }' $(MAKEFILE_LIST)

.PHONY: env
env: ## Create .env from .env.example if missing
	@test -f .env || cp .env.example .env
	@echo ".env is ready"

.PHONY: deps
deps: ## Download Go dependencies
	go mod download

.PHONY: tidy
tidy: ## Run go mod tidy
	go mod tidy

.PHONY: run
run: ## Run API locally against DATABASE_URL
	HTTP_ADDR=$(HTTP_ADDR) DATABASE_URL="$(DATABASE_URL)" go run ./cmd/api

.PHONY: build
build: ## Build API binary
	mkdir -p bin
	CGO_ENABLED=0 go build -trimpath -ldflags="-s -w" -o bin/$(APP_NAME) ./cmd/api

.PHONY: test
test: ## Run Go tests
	TEST_DATABASE_URL="$(TEST_DATABASE_URL)" go test ./...

.PHONY: test-race
test-race: ## Run Go tests with race detector
	TEST_DATABASE_URL="$(TEST_DATABASE_URL)" go test -race ./...

.PHONY: fmt
fmt: ## Format Go code
	gofmt -w $$(find . -name '*.go' -not -path './vendor/*')

.PHONY: vet
vet: ## Run go vet
	go vet ./...

.PHONY: lint
lint: fmt vet test ## Run basic local checks

.PHONY: db-up
db-up: ## Start local PostgreSQL
	$(DOCKER_COMPOSE) up -d db

.PHONY: test-db-up
test-db-up: ## Start local test PostgreSQL
	$(DOCKER_COMPOSE) --profile test up -d test-db

.PHONY: db-down
db-down: ## Stop local PostgreSQL
	$(DOCKER_COMPOSE) down

.PHONY: db-reset
db-reset: ## Delete local PostgreSQL volume and start fresh DB
	$(DOCKER_COMPOSE) down -v
	$(DOCKER_COMPOSE) up -d db

.PHONY: db-logs
db-logs: ## Show PostgreSQL logs
	$(DOCKER_COMPOSE) logs -f db

.PHONY: migrate
migrate: ## Apply migrations to DATABASE_URL
	$(GOOSE) -dir $(MIGRATIONS_DIR) postgres "$(DATABASE_URL)" up

.PHONY: migrate-down
migrate-down: ## Roll back one migration
	$(GOOSE) -dir $(MIGRATIONS_DIR) postgres "$(DATABASE_URL)" down

.PHONY: migrate-status
migrate-status: ## Show migration status
	$(GOOSE) -dir $(MIGRATIONS_DIR) postgres "$(DATABASE_URL)" status

.PHONY: migrate-reset
migrate-reset: ## Roll back all migrations and apply again
	$(GOOSE) -dir $(MIGRATIONS_DIR) postgres "$(DATABASE_URL)" reset
	$(GOOSE) -dir $(MIGRATIONS_DIR) postgres "$(DATABASE_URL)" up

.PHONY: docker-build
docker-build: ## Build API Docker image
	docker build -t $(APP_NAME):local .

.PHONY: docker-run
docker-run: ## Run API container connected to compose DB
	$(DOCKER_COMPOSE) --profile app up --build api

.PHONY: compose-up
compose-up: ## Start DB, apply migrations, then start API
	$(DOCKER_COMPOSE) up -d db
	$(DOCKER_COMPOSE) --profile tools run --rm migrate
	$(DOCKER_COMPOSE) --profile app up --build api

.PHONY: compose-down
compose-down: ## Stop all compose services
	$(DOCKER_COMPOSE) --profile app --profile tools --profile test down

.PHONY: logs
logs: ## Show API logs
	$(DOCKER_COMPOSE) logs -f api

.PHONY: health
health: ## Check health endpoints
	@curl -fsS http://127.0.0.1:8080/healthz && echo
	@curl -fsS http://127.0.0.1:8080/readyz && echo

.PHONY: clean
clean: ## Remove local build artifacts
	rm -rf bin coverage *.out
