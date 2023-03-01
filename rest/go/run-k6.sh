#!/usr/bin/env bash

make bin

SLEEP=3

APP_PORT=8080
APP_URL=http://localhost:$APP_PORT

K6_SCRIPT=../stress_tests/script.js
: "${K6_USERS:=100}"
: "${K6_ITERATIONS:=100000}"
K6_RUN="k6 run --vus $K6_USERS --iterations $K6_ITERATIONS -e SERVER_PORT=$APP_PORT $K6_SCRIPT"

REC_DURATION=10s
TRACE_REC_OUT=./trace.out
PROFILE_REC_OUT=./pprof.out

echo start application
./bin/server 2>&1 "$@" &
APP_PID=$!
echo app process $APP_PID

sleep $SLEEP

ps -p $APP_PID -s
if ! ps -p $APP_PID > /dev/null
  then echo "app process is not alive"
  exit 1
fi

CYCLES=4
for ((i=1;i<=CYCLES;i++)); do
  echo "warmup $i"
  $K6_RUN
done

: "${WRITE_TRACE:=true}"
if $WRITE_TRACE
then
  echo "start recording"
  curl -o $TRACE_REC_OUT $APP_URL/debug/pprof/trace?seconds=$REC_DURATION &
  TRACE_PID=$!
  echo $TRACE_PID
fi

: "${WRITE_PROFILE:=false}"
if $WRITE_PROFILE
then
  echo "start recording"
  curl -o $PROFILE_REC_OUT $APP_URL/debug/pprof/profile?seconds=$REC_DURATION &
  PROFILE_PID=$!
  echo $PROFILE_PID
fi

$K6_RUN

if $WRITE_TRACE
then
  echo "wait tracing process $TRACE_PID"
  wait $TRACE_PID
fi

if $WRITE_PROFILE
then
  echo "wait profile process $PROFILE_PID"
  wait $PROFILE_PID
fi

echo finish application process $APP_PID
kill $APP_PID

if $WRITE_PROFILE
then
  go tool pprof -web $PROFILE_REC_OUT
fi

if $WRITE_TRACE
then
  go tool trace –http $TRACE_REC_OUT
fi
