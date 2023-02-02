#!/usr/bin/env bash

docker rm -f postgres-bench
docker run -d --restart always --name postgres-bench -p 5433:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres postgres:15.1

K6_USERS=200 K6_ITERATIONS=10000 ./run-k6.sh -storage gorm-gen -migrate-db -sql-log-level silent -max-db-conns 99 -max-db-idle-conns 20 -gorm-create-batch-size 20 -dsn "host=localhost port=5433 user=postgres password=postgres dbname=postgres sslmode=disable client_encoding=UTF-8"