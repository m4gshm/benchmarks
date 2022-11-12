#!/usr/bin/env bash

docker rm -f postgres
docker run -d --restart always --name postgres -p 5432:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres postgres:14.3

WRITE_TRACE=false K6_ITERATIONS=6000 JFR_HTTP_EVENT_ENABLED=false SPRING_DATASOURCE_ENABLED=true ./run-k6.sh
