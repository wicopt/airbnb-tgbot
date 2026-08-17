#!/bin/bash
set -euo pipefail

TIMESTAMP=$(date +%Y%m%d_%H%M%S)
FILENAME="backup_${TIMESTAMP}.dump"
LOCAL_PATH="/backups/${FILENAME}"

echo "[$(date)] Starting pg_dump..."
pg_dump -Fc -h "$PGHOST" -U "$PGUSER" -d "$PGDATABASE" -f "$LOCAL_PATH"

echo "[$(date)] Uploading to Google Drive..."
rclone copy "$LOCAL_PATH" "$RCLONE_REMOTE"

echo "[$(date)] Cleaning up old backups (local + remote, older than ${RETENTION_DAYS} days)..."
find /backups -name "backup_*.dump" -mtime +"${RETENTION_DAYS}" -delete
rclone delete "$RCLONE_REMOTE" --min-age "${RETENTION_DAYS}d"

echo "[$(date)] Done."