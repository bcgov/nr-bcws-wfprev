#!/bin/bash
set -e

# Define the dump file path inside the container
DUMP_FILE="/dump/wfprev.dump"

echo "Checking for dump file at $DUMP_FILE..."

if [ -f "$DUMP_FILE" ]; then
    echo "Dump file found! Restoring to database 'wfprev'..."
    
    # Run pg_restore
    # -U: username
    # -d: database name
    # --clean: drop database objects before creating them
    # --if-exists: used with --clean to prevent errors if objects don't exist
    # --no-owner: do not try to set ownership of objects to match the original database
    # --no-privileges: do not restore access privileges (grant/revoke)
    pg_restore -U "$POSTGRES_USER" -d "$POSTGRES_DB" --clean --if-exists --no-owner --no-privileges "$DUMP_FILE" || {
        echo "pg_restore reported errors (exit code $?). This might be normal for some warnings."
    }
    
    echo "Database restoration complete."
else
    echo "Dump file not found at $DUMP_FILE. Skipping restore."
fi
