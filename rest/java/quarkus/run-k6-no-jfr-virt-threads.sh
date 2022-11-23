#!/usr/bin/env bash

WRITE_TRACE=false QUARKUS_HIBERNATE_ORM_ACTIVE=false JAVA_OPTS=--enable-preview ./run-k6.sh -Preactive
