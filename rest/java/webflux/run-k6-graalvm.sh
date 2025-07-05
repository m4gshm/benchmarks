#!/usr/bin/env bash

IS_WIN=false
case "`uname`" in
  CYGWIN* )
    IS_WIN=true
    ;;
  MSYS* | MINGW* )
    IS_WIN=true
    ;;
esac


APP_RUN="./build/native/nativeCompile/webflux"
if [[ $IS_WIN == true ]]; then
    APP_RUN="$APP_RUN.exe"
fi

export APP_PORT=8089
export APP_RUN="$APP_RUN -Dserver.port=$APP_PORT"
export BUILD_APP_TASK=nativeCompile
source ./run-k6.sh