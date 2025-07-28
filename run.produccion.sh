#!/bin/bash
set -e
clear
./gradlew clean build -Penvironment=produccion
./gradlew --no-daemon run --port 8088 --contextPath /
