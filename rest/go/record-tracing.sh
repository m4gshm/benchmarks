#!/usr/bin/env bash

make bin

APP_URL=http://localhost:8080
TASK_URL=$APP_URL/task

echo start application
./bin/server &
APP_PID=$!
echo $APP_PID

CYCLES=1
for ((i=1;i<=CYCLES;i++)); do
  echo "warmup$i"
  k6 run --vus 100 --iterations 100000 -e SERVER_PORT=8080 ../stress_tests/script.js
done

echo "start recording"
curl -o trace.out $APP_URL/debug/pprof/trace?seconds=10 &
TRACE_PID=$!
echo $TRACE_PID

k6 run --vus 100 --iterations 100000 -e SERVER_PORT=8080 ../stress_tests/script.js

echo "wait tracing process $TRACE_PID"
wait $TRACE_PID

echo finish application process $APP_PID
kill $APP_PID

go tool trace –http trace.out