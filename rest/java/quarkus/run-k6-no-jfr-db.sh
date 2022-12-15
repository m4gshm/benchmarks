#!/usr/bin/env bash

docker rm -f postgres-bench
docker run -d --restart always --name postgres-bench -p 5433:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres postgres:14.3

WRITE_TRACE=false K6_ITERATIONS=6000 ./run-k6.sh -Dstorage=db
