up:
	docker compose up --build 
user:
	docker compose -f docker-compose.user.yaml up --watch
auth:
	docker compose -f docker-compose.auth.yaml up --watch

dev:
	docker compose up --watch

down:
	docker compose down

re:
	docker compose -f docker-compose.yaml restart payment-service
db:
	docker exec -it airbnb-tgbot-db-1  psql -U postgres -d telegrambot
rm:
	docker compose rm -f gmail-service
st:
	docker compose up --build gmail-service -d