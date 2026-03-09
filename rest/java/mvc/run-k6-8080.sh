#!/usr/bin/env bash

: "${APP_PORT:=8080}"
APP_URL=http://localhost:$APP_PORT


K6_SCRIPT=../../stress_tests/script.js
: "${K6_USERS:=100}"
: "${K6_ITERATIONS:=100000}"
K6_RUN="k6 run --vus $K6_USERS --iterations $K6_ITERATIONS -e SERVER_PORT=$APP_PORT $K6_SCRIPT"

$K6_RUN
