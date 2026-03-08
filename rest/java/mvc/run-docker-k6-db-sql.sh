#!/usr/bin/env bash

docker network create benchmarks
docker rm -f postgres-bench
docker run -d --restart always --name postgres-bench --hostname postgres-bench --network=benchmarks -p 5433:5432 \
 --memory=512m \
 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres postgres:17.5 -c max_connections=200

K6_USERS=100 K6_ITERATIONS=10000 \
 ./run-docker-k6.sh --network=benchmarks \
 --memory=640m \
 -e SPRING_DATASOURCE_ENABLED=true \
 -e NATIVE_JDBC_ENABLED=true \
 -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-bench:5432/postgres
