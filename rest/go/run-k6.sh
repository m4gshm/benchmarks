#!/usr/bin/env bash

IS_WIN=false
case "`uname`" in
  CYGWIN* )
    IS_WIN=true
    ;;
  MSYS* | MINGW* )
    IS_WIN=true
    ;;
esac

SLEEP=3

APP_PORT=8080
APP_URL=http://localhost:$APP_PORT

K6_SCRIPT=../stress_tests/script.js
: "${K6_USERS:=100}"
: "${K6_ITERATIONS:=100000}"
K6_RUN="k6 run --vus $K6_USERS --iterations $K6_ITERATIONS -e SERVER_PORT=$APP_PORT $K6_SCRIPT"

: "${K6_WARMUP_USERS:=$K6_USERS}"
: "${K6_WARMUP_ITERATIONS:=$K6_ITERATIONS}"
K6_WARMUP_RUN="k6 run --vus $K6_WARMUP_USERS --iterations $K6_WARMUP_ITERATIONS -e SERVER_PORT=$APP_PORT $K6_SCRIPT"

: "${REC_DURATION:=30s}"

: "${PROFILE_REC_OUT:=pprof.out}"

echo build application
make bin
retVal=$?
if [ $retVal -ne 0 ]; then
  exit $retVal
fi

echo start application
./bin/server 2>&1 "$@" &
APP_PID=$!
echo "APP PID $APP_PID"
if  $IS_WIN; then
  REAL_APP_PID=$(cat /proc/$APP_PID/winpid)
  echo "APP PID $APP_PID, WIN PID $REAL_APP_PID"
fi

sleep $SLEEP

ps -p $APP_PID -s
if ! ps -p $APP_PID > /dev/null
  then echo "app process is not alive"
  exit 1
fi

: "${WRITE_PROFILE:=false}"

: "${WARM_CYCLES:=2}"
for ((i=1;i<=WARM_CYCLES;i++)); do
  echo "warmup $i"
  $K6_WARMUP_RUN
done

if $WRITE_PROFILE
then
  echo "start profile recording"
  curl -X POST ${APP_URL}/profile/start
fi

: "${REC_CYCLES:=3}"
for ((i=1;i<=REC_CYCLES;i++)); do
  echo "start bench $i"
  $K6_RUN
  echo "stop bench $i"
done

if $WRITE_PROFILE
then
  echo "stop profile recording"
  curl -X PUT --output ${PROFILE_REC_OUT} ${APP_URL}/profile/stop
fi

echo finish application process $APP_PID
kill $APP_PID

if $WRITE_PROFILE
then
  go tool pprof --http :9999 ${PROFILE_REC_OUT}
fi

