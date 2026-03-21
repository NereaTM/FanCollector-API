# Levanta la BD en desarrollo
dev:
	docker compose --env-file .env.dev -f docker-compose.dev.yaml up -d

# Para la BD de desarrollo
stop-dev:
	docker compose -f docker-compose.dev.yaml down

# Levanta BD + API en producción
prod:
	docker compose --env-file .env.prod up --build -d

# Para producción
stop-prod:
	docker compose down

.PHONY: dev stop-dev prod stop-prod