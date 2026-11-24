#!/usr/bin/env bash
set -euo pipefail
: "${DB_HOST:=localhost}"
: "${DB_USER:=evidencevault}"
: "${DB_PASSWORD:?Set DB_PASSWORD}"
: "${BACKUP_DIR:=./backups}"
mkdir -p "$BACKUP_DIR"
file="$BACKUP_DIR/evidencevault-$(date +%Y%m%d-%H%M%S).sql.gz"
MYSQL_PWD="$DB_PASSWORD" mysqldump -h "$DB_HOST" -u "$DB_USER" --single-transaction --routines --triggers evidencevault | gzip > "$file"
echo "Created $file"
