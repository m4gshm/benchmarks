#!/usr/bin/env bash

REC_OUT=recording_vthreads.jfr WRITE_TRACE=true QUARKUS_HIBERNATE_ORM_ACTIVE=false JAVA_OPTS=--enable-preview ./run-k6.sh -Preactive
