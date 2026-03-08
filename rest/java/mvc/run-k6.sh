#!/usr/bin/env bash

IS_WIN=false
case "$(uname)" in
CYGWIN*)
  IS_WIN=true
  ;;
MSYS* | MINGW*)
  IS_WIN=true
  ;;
esac

SLEEP=15

: "${APP_PORT:=8071}"
: "${APP_RUN:=java -XX:+FlightRecorder --enable-preview -Dserver.port=$APP_PORT -jar ./build/libs/mvc.jar}"
APP_URL=http://localhost:$APP_PORT

K6_SCRIPT=../../stress_tests/script.js
: "${K6_USERS:=100}"
: "${K6_ITERATIONS:=100000}"
K6_RUN="k6 run --vus $K6_USERS --iterations $K6_ITERATIONS -e SERVER_PORT=$APP_PORT $K6_SCRIPT"

: "${K6_WARMUP_USERS:=$K6_USERS}"
: "${K6_WARMUP_ITERATIONS:=$K6_ITERATIONS}"

K6_WARMUP_RUN="k6 run --vus $K6_WARMUP_USERS --iterations $K6_WARMUP_ITERATIONS -e SERVER_PORT=$APP_PORT $K6_SCRIPT"

: "${REC_DURATION:=30}"
: "${REC_FILE_NAME:=recording}"
REC_OUT=./${REC_FILE_NAME}.jfr
REC_PROFILE=profile.jfc

echo build application
../../../gradlew :rest:java:mvc:clean :rest:java:mvc:build
retVal=$?
if [ $retVal -ne 0 ]; then
  exit $retVal
fi

echo start application
echo $APP_RUN "$@"
$APP_RUN "&@" &
APP_PID=$!
REAL_APP_PID=$APP_PID
echo "$IS_WIN"
if  $IS_WIN; then
  REAL_APP_PID=$(cat /proc/$APP_PID/winpid)
  echo "APP PID $APP_PID, WIN_PID $REAL_APP_PID"
else
  echo "APP_PID $APP_PID"
fi

sleep $SLEEP

: "${WRITE_TRACE:=false}"
: "${WRITE_PROFILE:=false}"
: "${FLAMEGRAPH_FILE_OUT:=flamegraph}"

if $WRITE_TRACE
then
  REC_ID=$(jcmd $REAL_APP_PID JFR.start duration=${REC_DURATION}s filename=/tmp/ settings=$REC_PROFILE | grep "Started recording " | awk {'print $3'} | tr -d '.')
  echo "rec id $REC_ID"
fi

: "${WARM_CYCLES:=6}"
for ((i=1;i<=WARM_CYCLES;i++)); do
  echo "warmup $i"
  $K6_WARMUP_RUN
done

if $WRITE_TRACE
then
  jcmd $REAL_APP_PID JFR.stop name=$REC_ID
fi

if $WRITE_TRACE
then
  REC_ID=$(jcmd $REAL_APP_PID JFR.start duration=$REC_DURATION filename=/tmp/ settings=$REC_PROFILE | grep "Started recording " | awk {'print $3'} | tr -d '.')
  echo "rec id $REC_ID"
fi

if $WRITE_PROFILE
then
  echo "start recording"
  asprof -d $REC_DURATION -f $FLAMEGRAPH_FILE_OUT.html -s -o flamegraph $REAL_APP_PID &
  retVal=$?
  if [ $retVal -ne 0 ]; then
    exit $retVal
  fi
fi

: "${REC_CYCLES:=3}"
for ((i=1;i<=REC_CYCLES;i++)); do
  echo "start bench $i"
  $K6_RUN
  echo "stop bench $i"
done

if $WRITE_TRACE
then
  echo "stop recording"
  jcmd $REAL_APP_PID JFR.stop name=$REC_ID
fi

echo finish application process $APP_PID
kill $APP_PID
