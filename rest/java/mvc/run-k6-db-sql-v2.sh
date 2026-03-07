#!/usr/bin/env bash

docker rm -f postgres-bench
docker run -d --restart always --name postgres-bench -p 5433:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres postgres:17.5 -c max_connections=200

K6_USERS=100 K6_ITERATIONS=10000 SPRING_DATASOURCE_ENABLED=true NATIVE_JDBC_V2_ENABLED=true ./run-k6.sh
