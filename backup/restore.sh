#!/bin/bash
set -euo pipefail

# на новом хосте: docker compose up -d db  (дождаться healthcheck)
LATEST=$(rclone lsf "$RCLONE_REMOTE" | sort | tail -n 1)
echo "Restoring from ${LATEST}..."

rclone copy "${RCLONE_REMOTE}/${LATEST}" /backups/
pg_restore -c -h "$PGHOST" -U "$PGUSER" -d "$PGDATABASE" "/backups/${LATEST}"

echo "Restore complete."