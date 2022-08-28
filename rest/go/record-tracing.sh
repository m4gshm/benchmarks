#!/usr/bin/env bash

make bin

APP_PORT=8080
APP_URL=http://localhost:$APP_PORT

K6_SCRIPT=../stress_tests/script.js
K6_USERS=100
K6_ITERATIONS=100000
K6_RUN="k6 run --vus $K6_USERS --iterations $K6_ITERATIONS -e SERVER_PORT=$APP_PORT $K6_SCRIPT"

REC_DURATION=10s
REC_OUT=./trace.out

echo start application
./bin/server "$@" &
APP_PID=$!
echo $APP_PID

CYCLES=4
for ((i=1;i<=CYCLES;i++)); do
  echo "warmup $i"
  $K6_RUN
done

echo "start recording"
curl -o $REC_OUT $APP_URL/debug/pprof/trace?seconds=$REC_DURATION &
TRACE_PID=$!
echo $TRACE_PID

$K6_RUN

echo "wait tracing process $TRACE_PID"
wait $TRACE_PID

echo finish application process $APP_PID
kill $APP_PID

go tool trace –http $REC_OUT