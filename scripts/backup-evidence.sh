#!/usr/bin/env bash
set -euo pipefail
: "${STORAGE_PATH:=./storage/evidence}"
: "${BACKUP_DIR:=./backups}"
mkdir -p "$BACKUP_DIR"
archive="$BACKUP_DIR/evidence-$(date +%Y%m%d-%H%M%S).tar.gz"
tar -czf "$archive" -C "$(dirname "$STORAGE_PATH")" "$(basename "$STORAGE_PATH")"
echo "Created $archive"
