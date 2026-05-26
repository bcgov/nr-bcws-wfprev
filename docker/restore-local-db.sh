#!/bin/bash
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

echo "Restoring database 'wfprev' from dump..."
# Run pg_restore in data-only mode (temporarily disable set -e as pg_restore returns 1 on warnings)
set +e
docker exec -i wfprev-postgres pg_restore -U wfprev -d wfprev --data-only --disable-triggers /dump/wfprev.dump/wfprev.dump
RESTORE_STATUS=$?
set -e

if [ "$RESTORE_STATUS" -eq 0 ]; then
    echo "Database restoration completed successfully."
else
    echo "Database restoration completed with warnings (this is common for pg_restore with existing schemas)."
fi
