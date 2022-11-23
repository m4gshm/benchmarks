#!/usr/bin/env bash

WRITE_TRACE=false JFR_HTTP_EVENT_ENABLED=false VIRTUAL_THREADS_ENABLED=true ./run-k6.sh
