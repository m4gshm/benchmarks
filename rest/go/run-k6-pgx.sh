#!/usr/bin/env bash

docker rm -f postgres-bench-pgx
docker run -d --restart always --name postgres-bench-pgx -p 5434:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres postgres:15.1 postgres -c max_connections=200

K6_USERS=200 K6_ITERATIONS=10000 ./run-k6.sh -storage pgx -migrate-db -sql-log-level silent -max-db-conns 80 -dsn "host=localhost port=5434 user=postgres password=postgres dbname=postgres sslmode=disable client_encoding=UTF-8"