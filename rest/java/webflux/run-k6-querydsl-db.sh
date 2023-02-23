#!/usr/bin/env bash

docker rm -f postgres-bench
docker run -d --restart always --name postgres-bench -p 5433:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres postgres:15.1

K6_USERS=200 K6_ITERATIONS=10000 SPRING_DATASOURCE_ENABLED=true QUERYDSL_R2DBC_ENABLED=true REC_FILE_NAME=recording-querydsl ./run-k6.sh
