#!/usr/bin/env bash

REC_OUT=recording_reactive.jfr WRITE_TRACE=true QUARKUS_HIBERNATE_ORM_ACTIVE=false ./run-k6.sh -Preactive
