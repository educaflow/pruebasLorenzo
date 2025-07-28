#!/bin/bash
#if [ -n "$HOME" ]; then
#  rm -rf ${HOME}/.axelor/attachments/
#fi
set -e
clear
#docker stop educaflow-db
#docker run --name educaflow-db --hostname educaflow-db --network educaflow -e POSTGRES_USER=educaflow -e POSTGRES_PASSWORD=educaflow -e POSTGRES_DB=educaflow -p 5432:5432 -d --rm postgres:12.22
./gradlew clean build
#./gradlew --no-daemon run --debug-jvm --port 8080 --contextPath /
./gradlew --no-daemon run --port 8080 --contextPath /

