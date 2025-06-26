#!/bin/bash
set -e
clear
sudo docker stop educaflow
sudo docker run --name educaflow -e POSTGRES_USER=educaflow -e POSTGRES_PASSWORD=educaflow -e POSTGRES_DB=educaflow -p 5432:5432 -d --rm postgres:12.22
./gradlew clean build
export JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
./gradlew --no-daemon run
