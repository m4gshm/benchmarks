#!/usr/bin/env bash

docker rm -f postgres-bench
docker run -d --restart always --name postgres-bench -p 5433:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres postgres:14.3

K6_ITERATIONS=6000 ./run-k6.sh -storage pgx -migrate-db -sql-log-level silent -max-db-conns 100 -max-db-idle-conns 20 -dsn "host=localhost port=5433 user=postgres password=postgres dbname=postgres sslmode=disable client_encoding=UTF-8"