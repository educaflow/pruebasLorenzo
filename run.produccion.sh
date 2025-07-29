#!/bin/bash
set -e
clear
export AXELOR_CONFIG="../secretaria-virtual-private/axelor-config.production.properties"
./gradlew clean build
./gradlew --no-daemon run --port 8080 --contextPath /
