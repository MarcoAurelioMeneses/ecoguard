.PHONY: install lint format test db-up db-down

install:
	pnpm install

lint:
	./backend/gradlew -p backend check
	pnpm lint
	pnpm typecheck
	./scripts/check-design-sync.sh

format:
	./backend/gradlew -p backend spotlessApply

test:
	./backend/gradlew -p backend test

db-up:
	docker compose -f infra/docker-compose.yml up -d --build --wait

db-down:
	docker compose -f infra/docker-compose.yml down
