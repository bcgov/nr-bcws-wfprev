#!/bin/bash
set -e

# Update the client (UI) service without tearing down the DB or API
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

echo "Rebuilding and updating the client service..."
docker-compose -f docker-compose.yml -f docker-compose.local.yml --profile dev up -d --build client
