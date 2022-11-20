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

SLEEP=3

APP_PORT=8082
if [[ -z ${APP_RUN} ]]; then
  APP_RUN="java $JAVA_OPTS -Dquarkus.http.port=$APP_PORT -jar ./build/quarkus-app/quarkus-run.jar"
fi
APP_URL=http://localhost:$APP_PORT

K6_SCRIPT=../../stress_tests/script.js
K6_USERS=100
: ${K6_ITERATIONS:=100000}
K6_RUN="k6 run --vus $K6_USERS --iterations $K6_ITERATIONS -e SERVER_PORT=$APP_PORT $K6_SCRIPT"

REC_DURATION=30s
: ${REC_OUT:=./recording.jfr}
REC_PROFILE=profile.jfc

BUILD_TASK="${BUILD_TASK:-quarkusBuild}"
echo build application: $BUILD_TASK
../../../gradlew :rest:java:quarkus-reactive:clean :rest:java:quarkus-reactive:$BUILD_TASK "$@"
retVal=$?
if [ $retVal -ne 0 ]; then
  exit $retVal
fi

echo start application: $APP_RUN
$APP_RUN &
APP_PID=$!
JCMD_APP_PID=$APP_PID
if $IS_WIN; then
  JCMD_APP_PID=$(cat /proc/$APP_PID/winpid)
fi

echo "APP PID  $APP_PID"
echo "JCMD PID $JCMD_APP_PID"

sleep $SLEEP

: ${WRITE_TRACE:=true}

WARM_CYCLES=4
for ((i = 1; i <= WARM_CYCLES; i++)); do
  echo "warmup $i"
  if $WRITE_TRACE; then
    REC_ID=$(jcmd $JCMD_APP_PID JFR.start duration=$REC_DURATION filename=/tmp/ settings=$REC_PROFILE | grep "Started recording " | awk {'print $3'} | tr --delete '.')
    echo "rec id $REC_ID"
  fi

  $K6_RUN

  if $WRITE_TRACE; then
    jcmd $JCMD_APP_PID JFR.stop name=$REC_ID
  fi
done

REC_CYCLES=2
for ((i = 1; i <= REC_CYCLES; i++)); do
  echo "start bench $i"
  if $WRITE_TRACE; then
    REC_ID=$(jcmd $JCMD_APP_PID JFR.start duration=$REC_DURATION filename=$REC_OUT settings=$REC_PROFILE | grep "Started recording " | awk {'print $3'} | tr --delete '.')
    echo "rec id $REC_ID"
  fi

  $K6_RUN

  echo "stop bench $i"
  if $WRITE_TRACE; then
    jcmd $JCMD_APP_PID JFR.stop name=$REC_ID
  fi
done

echo finish application process $APP_PID
kill $APP_PID
