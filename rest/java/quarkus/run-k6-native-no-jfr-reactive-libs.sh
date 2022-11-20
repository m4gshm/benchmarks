#!/usr/bin/env bash

BUILD_TASK=quarkusBuildNative APP_RUN="./build/quarkus-runner.exe -Dquarkus.http.port=8082" WRITE_TRACE=false QUARKUS_HIBERNATE_ORM_ACTIVE=false ./run-k6.sh -Preactive
