#!/bin/bash
set -e

# Start Docker Compose with local override
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

CLEAN=false
RESTORE_DATA=false

# Parse arguments
while [[ "$#" -gt 0 ]]; do
    case $1 in
        -c|--clean) CLEAN=true ;;
        -r|--restore-data) RESTORE_DATA=true ;;
        *) echo "Unknown parameter passed: $1"; exit 1 ;;
    esac
    shift
done

if [ "$CLEAN" = true ]; then
    echo "Cleaning volumes requested. Running stop-local.sh --clear-volumes..."
    "$SCRIPT_DIR/stop-local.sh" --clear-volumes
fi

export SKIP_RESTORE="true"

# Build the API locally
cd server/wfprev-api
echo "Building API locally..."
if ./mvnw clean package -DskipTests; then
    echo "Maven build completed successfully!"
else
    echo "Warning: Maven build failed (this is common if Java is not installed in the current shell)."
    if [ -f "target/wfprev-api-1.0.0-SNAPSHOT.jar" ]; then
        echo "Found existing JAR: target/wfprev-api-1.0.0-SNAPSHOT.jar. Reusing the existing build..."
    else
        echo "Error: No existing JAR found in server/wfprev-api/target/. A successful Maven build is required." >&2
        exit 1
    fi
fi
cd "$SCRIPT_DIR/.."

# Remove orphans to clean up potential old service states
docker-compose -f docker-compose.yml -f docker-compose.local.yml --profile dev up -d --build --remove-orphans

if [ "$RESTORE_DATA" = true ]; then
    echo "Data restore requested. Waiting for Liquibase to complete migrations..."
    
    # Wait for liquibase container to exit and get its exit code
    # This command blocks until the container stops
    EXIT_CODE=$(docker wait nr-bcws-wfprev-liquibase-1)

    if [ "$EXIT_CODE" -ne 0 ]; then
        echo "Liquibase migrations failed with exit code $EXIT_CODE." >&2
        exit 1
    fi

    echo "Liquibase finished (0). Restoring data from dump..."
    # Run pg_restore in data-only mode (temporarily disable set -e as pg_restore returns 1 on warnings)
    set +e
    docker exec -i wfprev-postgres pg_restore -U wfprev -d wfprev --data-only --disable-triggers /dump/wfprev.dump/wfprev.dump
    RESTORE_STATUS=$?
    set -e

    if [ "$RESTORE_STATUS" -eq 0 ]; then
        echo "Data restoration complete!"
    else
        echo "Warning: Data restoration completed with warnings (this is common for pg_restore with existing schemas)."
    fi
fi
