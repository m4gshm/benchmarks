#!/usr/bin/env bash

: "${APP_PORT:=8071}"

DOCKER_NAME="benchmark-jvm-mvc"
DOCKER_IMG="$DOCKER_NAME:latest"

docker rm -f $DOCKER_NAME

CMD="docker run -d --rm --name $DOCKER_NAME -p $APP_PORT:8080"

CMD="$CMD $*"
#for var in "$@"
#do
#  if [[ $var == *"="* ]]; then
#    CMD="$CMD -e $var"
#  fi
#done

CMD="$CMD "
CMD="$CMD $DOCKER_IMG"

echo $CMD
$CMD

until [ "$(docker inspect -f {{.State.Running}} $DOCKER_NAME)" == "true" ]; do
    sleep 0.1;
done;

docker logs --tail 500 $DOCKER_NAME

SLEEP=10
sleep $SLEEP


APP_URL=http://localhost:$APP_PORT

K6_SCRIPT=../../stress_tests/script.js
: "${K6_USERS:=100}"
: "${K6_ITERATIONS:=100000}"
K6_RUN="k6 run --vus $K6_USERS --iterations $K6_ITERATIONS -e SERVER_PORT=$APP_PORT $K6_SCRIPT"

: "${K6_WARMUP_USERS:=$K6_USERS}"
: "${K6_WARMUP_ITERATIONS:=$K6_ITERATIONS}"
K6_WARMUP_RUN="k6 run --vus $K6_WARMUP_USERS --iterations $K6_WARMUP_ITERATIONS -e SERVER_PORT=$APP_PORT $K6_SCRIPT"

: "${WARM_CYCLES:=6}"
for ((i=1;i<=WARM_CYCLES;i++)); do
  echo "warmup $i"
  $K6_WARMUP_RUN
done

: "${WRITE_PROFILE:=false}"
if $WRITE_PROFILE
then
  : "${EVENT:=cpu}"
  : "${FORMAT:=flamegraph}"
  : "${OPTIONS:=threads}"
  echo "start recording"
  curl -X POST "$APP_URL"/asyncprof?event="${EVENT}"\&format="${FORMAT}"\&options="${OPTIONS}"
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
  curl -X PUT --output "${FORMAT}"-java.html $APP_URL/asyncprof
fi

echo "stop docker"
docker stop $DOCKER_NAME

