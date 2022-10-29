#!/usr/bin/env bash

docker rm -f postgres
docker run -d --restart always --name postgres -p 5432:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres postgres:14.3

WRITE_TRACE=false SPRING_DATASOURCE_ENABLED=false ./run-k6.sh
