.PHONY: install lint format test db-up db-down

install:
	uv sync --all-packages --group dev
	pnpm install

lint:
	uv run ruff check .
	uv run ruff format --check .
	uv run mypy
	pnpm lint
	pnpm typecheck
	./scripts/check-design-sync.sh

format:
	uv run ruff check . --fix
	uv run ruff format .

test:
	uv run pytest

db-up:
	docker compose -f infra/docker-compose.yml up -d --build --wait

db-down:
	docker compose -f infra/docker-compose.yml down
