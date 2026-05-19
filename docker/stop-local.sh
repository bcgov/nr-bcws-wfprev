#!/bin/bash
set -e

# Stop Docker Compose with local override
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

CLEAR_VOLUMES=false

# Parse arguments
while [[ "$#" -gt 0 ]]; do
    case $1 in
        -v|--clear-volumes) CLEAR_VOLUMES=true ;;
        *) echo "Unknown parameter passed: $1"; exit 1 ;;
    esac
    shift
done

COMMAND="docker-compose -f docker-compose.yml -f docker-compose.local.yml --profile dev down"

if [ "$CLEAR_VOLUMES" = true ]; then
    echo "Stopping and clearing volumes..."
    $COMMAND -v
else
    echo "Stopping..."
    $COMMAND
fi
