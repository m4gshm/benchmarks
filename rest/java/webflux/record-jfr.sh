#!/usr/bin/env bash

gradle build

APP_PORT=8081
APP_URL=http://localhost:$APP_PORT
TASK_URL=$APP_URL/task
JFR_URL=$APP_URL/actuator/flightrecorder

echo start application
java -XX:+FlightRecorder -Dserver.port=$APP_PORT -jar ./build/libs/webflux.jar &
APP_PID=$!
echo $APP_PID

sleep 4

CYCLES=1
for ((i=1;i<=CYCLES;i++)); do
  echo "warmup$i"
  k6 run --vus 100 --iterations 100000 -e SERVER_PORT=$APP_PORT ../../stress_tests/script.js
#    curl -s -o /dev/null -X POST -H "Content-Type: application/json" -d "{\"id\": \"${i}\",\"text\":\"first\"}" $TASK_URL
#    curl -s -o /dev/null -X GET $TASK_URL/$i
#    curl -s -o /dev/null -X DELETE $TASK_URL/$i
done

echo "start recording"
recordingUrl=$(curl -si -X POST -H "Content-Type: application/json" -d '{"id": 1, "duration": "30","timeUnit":"SECONDS"}' $JFR_URL | grep Location: | awk {'print $2'} | tr --delete '\n\r')
echo created recording $recordingUrl

k6 run --vus 100 --iterations 100000 -e SERVER_PORT=$APP_PORT ../../stress_tests/script.js

echo "stop recording"
curl -si -X PUT --url $recordingUrl

echo "download recording"
curl --output recording.jfr $recordingUrl

echo finish application process $APP_PID
kill $APP_PID