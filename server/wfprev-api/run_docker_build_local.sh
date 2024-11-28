#!/bin/bash

# Read .env file and convert to --build-arg parameters
BUILD_ARGS=""
while IFS= read -r line || [[ -n "$line" ]]; do
    # Skip empty lines and comments
    if [[ -z "$line" ]] || [[ "$line" =~ ^# ]]; then
        continue
    fi
    # Remove any quotes from the value
    line=$(echo "$line" | tr -d '"'"'")
    BUILD_ARGS+="--build-arg ${line} "
done < .env

echo "BUILD_ARGS: $BUILD_ARGS"

# Run docker build with all the arguments
docker build $BUILD_ARGS \
    -f Dockerfile.graalvm \
    --build-arg MAVEN_SETTINGS_FILE=vivid.settings.xml \
    -t wfprev-api .