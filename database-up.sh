sudo docker run \
  --name educaflow-db \
  --hostname educaflow-db \
  --network educaflow \
  -e POSTGRES_USER=educaflow \
  -e POSTGRES_PASSWORD=educaflow \
  -e POSTGRES_DB=educaflow \
  -p 5432:5432 \
  -d \
  --rm \
	-e PGDATA=/var/lib/postgresql/data/pgdata \
	-v ../postgresql-database:/var/lib/postgresql/data \
  postgres:12.22