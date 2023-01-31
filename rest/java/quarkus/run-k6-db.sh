#!/usr/bin/env bash

docker rm -f postgres-bench
docker run -d --restart always --name postgres-bench -p 5433:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres postgres:15.1

K6_ITERATIONS=6000 QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://localhost:5433/postgres ./run-k6.sh -Dstorage=db
