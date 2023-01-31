#!/usr/bin/env bash

#docker restart postgres
 docker stop postgres
 docker rm -f postgres-bench
 docker run -d --restart always --name postgres-bench -p 5433:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres postgres:15.1 postgres -c max_connections=1000
 sleep 3

WRITE_TRACE=false K6_ITERATIONS=6000 ./run-k6.sh -storage gorm -migrate-db -sql-log-level silent -max-db-conns 100 -max-db-idle-conns 100