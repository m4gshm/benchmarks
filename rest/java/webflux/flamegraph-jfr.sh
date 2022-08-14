#!/usr/bin/env bash

APP_URL=http://localhost:8080
TASK_URL=$APP_URL/task
JFR_URL=$APP_URL/actuator/flightrecorder

echo "start recording"
RECORDING_URL=$(curl -si -X POST -H "Content-Type: application/json" -d '{"id": 1, "duration": "30","timeUnit":"SECONDS"}' $JFR_URL | grep Location: | awk {'print $2'} | tr --delete '\n\r')
echo created recording $RECORDING_URL

k6 run --vus 100 --iterations 100000 -e SERVER_PORT=8080 ../../stress_tests/script.js

echo "stop recording"
curl -si -X PUT --url $RECORDING_URL

RECORD_ID=${RECORDING_URL##*/}
FLAMEGRAPH_URL=$JFR_URL/ui/$RECORD_ID/flamegraph.html
echo ""
echo "flamegraph url: $FLAMEGRAPH_URL"
