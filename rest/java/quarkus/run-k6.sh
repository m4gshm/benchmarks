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

APP_PORT=7072
APP_PORT_PARAM=-Dquarkus.http.port=$APP_PORT
if [[ -z ${APP_RUN} ]]; then
  APP_RUN="java $JAVA_OPTS $APP_PORT_PARAM -jar ./build/quarkus-app/quarkus-run.jar"
fi
APP_URL=http://localhost:$APP_PORT

K6_SCRIPT=../../stress_tests/script.js
: "${K6_USERS:=100}"
: "${K6_ITERATIONS:=100000}"
K6_RUN="k6 run --vus $K6_USERS --iterations $K6_ITERATIONS -e SERVER_PORT=$APP_PORT $K6_SCRIPT"

: "${K6_WARMUP_USERS:=$K6_USERS}"
: "${K6_WARMUP_ITERATIONS:=$K6_ITERATIONS}"
K6_WARMUP_RUN="k6 run --vus $K6_WARMUP_USERS --iterations $K6_WARMUP_ITERATIONS -e SERVER_PORT=$APP_PORT $K6_SCRIPT"

REC_DURATION=30s
: "${REC_FILE_NAME:=recording}"
REC_OUT=./${REC_FILE_NAME}.jfr
REC_PROFILE=profile.jfc

BUILD_APP_TASK="${BUILD_APP_TASK:-quarkusBuild}"
: "${GRADLE_MODULE:=:rest:java:quarkus}"
GRADLE_TASKS=$GRADLE_MODULE:$BUILD_APP_TASK
if [[ $CLEAN_BEFORE_BUILD ]]; then
  GRADLE_TASKS="$GRADLE_MODULE:clean $GRADLE_TASKS" "$@"
fi
echo build application
../../../gradlew $GRADLE_TASKS
retVal=$?
if [ $retVal -ne 0 ]; then
  exit $retVal
fi

echo start application
echo $APP_RUN "$@"
$APP_RUN "&@" &
APP_PID=$!
JCMD_APP_PID=$APP_PID
if $IS_WIN; then
  JCMD_APP_PID=$(cat /proc/$APP_PID/winpid)
fi

echo "APP PID  $APP_PID"
echo "JCMD PID $JCMD_APP_PID"

sleep $SLEEP

: "${WRITE_TRACE:=false}"

: "${WARM_CYCLES:=6}"
for ((i=1;i<=WARM_CYCLES;i++)); do
  echo "warmup $i"
  if $WRITE_TRACE; then
    REC_ID=$(jcmd $JCMD_APP_PID JFR.start duration=$REC_DURATION filename=/tmp/ settings=$REC_PROFILE | grep "Started recording " | awk {'print $3'} | tr -d '.')
    echo "rec id $REC_ID"
  fi

  $K6_WARMUP_RUN

  if $WRITE_TRACE
  then
    jcmd $JCMD_APP_PID JFR.stop name=$REC_ID
  fi
done

: "${REC_CYCLES:=3}"
for ((i=1;i<=REC_CYCLES;i++)); do
  echo "start bench $i"
  if $WRITE_TRACE; then
    REC_ID=$(jcmd $JCMD_APP_PID JFR.start duration=$REC_DURATION filename=$REC_OUT settings=$REC_PROFILE | grep "Started recording " | awk {'print $3'} | tr -d '.')
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
