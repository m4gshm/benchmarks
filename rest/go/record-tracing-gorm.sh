#!/usr/bin/env bash

K6_ITERATIONS=6000 ./record-tracing.sh -storage gorm -migrate-db -sql-log-level silent -max-db-conns 80