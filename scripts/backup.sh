#!/bin/bash
# Automated backup script for Zona Muerta Minecraft server
# Usage: ./backup.sh or cron: 0 3 * * * /backup.sh

BACKUP_DIR="/data/backups"
SERVER_DIR="/data"
RETENTION_DAYS=7
DATE=$(date +%Y%m%d_%H%M%S)

mkdir -p "$BACKUP_DIR"

echo "[$(date)] Starting backup..."

# Stop server gracefully
echo "[$(date)] Stopping server gracefully..."
if [ -f "$SERVER_DIR/server.pid" ]; then
    kill $(cat "$SERVER_DIR/server.pid")
    sleep 10
fi

# Create backup
BACKUP_NAME="zonamuerta_backup_${DATE}.tar.gz"
tar -czf "$BACKUP_DIR/$BACKUP_NAME" \
    --exclude='./logs/*.log' \
    --exclude='./crash-reports/*' \
    --exclude='./cache/*' \
    -C "$SERVER_DIR" . 2>/dev/null

echo "[$(date)] Backup created: $BACKUP_NAME"

# Clean old backups
find "$BACKUP_DIR" -name "zonamuerta_backup_*.tar.gz" -mtime +${RETENTION_DAYS} -delete
echo "[$(date)] Old backups cleaned (retention: ${RETENTION_DAYS} days)"

# Restart server
echo "[$(date)] Restarting server..."
cd "$SERVER_DIR"
if command -v rcon &> /dev/null; then
    rcon-cli "restart" 2>/dev/null || true
fi

echo "[$(date)] Backup complete!"
