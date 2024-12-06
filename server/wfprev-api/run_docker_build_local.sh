#!/bin/bash

mvn -s mvn_settings/vivid.settings.xml -Pnative native:compile -DskipTests
cp target/wfprev-api .
docker build --build-arg MAVEN_SETTINGS_FILE=vivid.settings.xml -f Dockerfile.graalvm .

 docker-compose up