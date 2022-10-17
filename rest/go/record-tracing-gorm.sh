#!/usr/bin/env bash

docker rm -f postgres
docker run -d --restart always --name postgres -p 5432:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres postgres:14.3

K6_ITERATIONS=6000 ./record-tracing.sh -storage gorm -migrate-db -sql-log-level silent -max-db-conns 80