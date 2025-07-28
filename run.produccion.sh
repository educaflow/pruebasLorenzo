#!/bin/bash
set -e
clear
export AXELOR_CONFIG="./src/main/resources/axelor-config.production.properties"
./gradlew clean build
./gradlew --no-daemon run --port 8080 --contextPath /
