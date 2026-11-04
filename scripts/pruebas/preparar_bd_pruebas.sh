#!/bin/sh
# ============================================================
#  SIGSF · Plan de Pruebas · Preparación del entorno (PE-01)
#  Crea desde cero la base de PRUEBAS  SistemaFinanciero_test
#  (nunca toca la base de desarrollo SistemaFinanciero) y ejecuta
#  los scripts en el orden oficial. Re-ejecutable.
#
#  Uso:  PGPASSWORD=123456 sh scripts/pruebas/preparar_bd_pruebas.sh
#  Variables: PGHOST (localhost) PGPORT (5432) PGUSER (postgres)
#             SIGSF_DB_NAME (SistemaFinanciero_test)
# ============================================================
set -e
cd "$(dirname "$0")/../.."
BD="${SIGSF_DB_NAME:-SistemaFinanciero_test}"
export PGHOST="${PGHOST:-localhost}" PGPORT="${PGPORT:-5432}" PGUSER="${PGUSER:-postgres}"
case "$BD" in *_test) ;; *) echo "Por seguridad la base de pruebas debe terminar en _test"; exit 1;; esac

echo "== Recreando la base $BD en $PGHOST:$PGPORT"
psql -q -v ON_ERROR_STOP=1 -d postgres -c "DROP DATABASE IF EXISTS \"$BD\" WITH (FORCE);"
psql -q -v ON_ERROR_STOP=1 -d postgres -c "CREATE DATABASE \"$BD\" ENCODING 'UTF8' TEMPLATE template0;"

for f in schema.sql datos_ejemplo.sql portal_cliente.sql seguridad.sql version_esquema.sql datos_100_registros.sql pagos_qr.sql; do
  echo "   - db/$f"
  psql -q -v ON_ERROR_STOP=1 -d "$BD" -f "db/$f" > /dev/null
done
echo "== Base de pruebas lista:"
psql -d "$BD" -At -c "SELECT 'tablas=' || count(*) FROM information_schema.tables WHERE table_schema='public';"
psql -d "$BD" -At -c "SELECT 'clientes=' || count(*) FROM cliente;"
psql -d "$BD" -At -c "SELECT 'operaciones=' || count(*) FROM operacion;"
