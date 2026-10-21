#!/bin/sh
# SIGSF - Respaldo con pg_dump (Linux / macOS). Programable con cron:
#   0 23 * * *  /ruta/SistemaFinanciero/scripts/backup_bd.sh
cd "$(dirname "$0")/.." || exit 1
mkdir -p respaldos
ARCHIVO="respaldos/sigsf_manual_$(date +%Y%m%d_%H%M).sql"
pg_dump -h "${PGHOST:-localhost}" -p "${PGPORT:-5432}" -U "${PGUSER:-postgres}" \
  --format=plain --no-owner --no-privileges --clean --if-exists --encoding=UTF8 \
  -f "$ARCHIVO" "${PGDATABASE:-SistemaFinanciero}" && echo "Respaldo generado: $ARCHIVO"
