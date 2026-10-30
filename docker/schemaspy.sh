#!/bin/bash
set -e

# Generate SchemaSpy docs for the local 'wfprev' schema into ./schemaspy/schemaspy-output
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

OUTPUT_DIR="schemaspy/schemaspy-output"

if [ "$(docker inspect -f '{{.State.Running}}' wfprev-postgres 2>/dev/null)" != "true" ]; then
    echo "wfprev-postgres is not running. Start the stack first: ./docker/start-local.sh"
    exit 1
fi

# Clear the previous run so pages for dropped tables don't linger. Creating the folder from the
# host also keeps it writable by the container's non-root user on Linux.
rm -rf "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"

echo "Generating schema docs..."
docker-compose -f docker-compose.yml -f docker-compose.local.yml --profile schemaspy run --rm --no-deps schemaspy

echo "Done. Open $OUTPUT_DIR/index.html"
