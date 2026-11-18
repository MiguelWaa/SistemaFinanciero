#!/bin/sh
# ============================================================
#  SIGSF 5.11.0 · Reversión manual a la versión anterior
#    sh scripts/rollback.sh [--destino /opt/sigsf] [--systemd] [--a releases/<id>] [--login U:C]
#  Sin --a vuelve a la versión instalada inmediatamente antes que la activa.
# ============================================================
set -eu
DESTINO=/opt/sigsf; SYSTEMD=0; OBJETIVO=""; LOGIN=""
while [ $# -gt 0 ]; do
  case "$1" in
    --destino) DESTINO="$2"; shift 2;;
    --systemd) SYSTEMD=1; shift;;
    --a) OBJETIVO="$2"; shift 2;;
    --login) LOGIN="$2"; shift 2;;
    *) echo "Opción desconocida: $1" >&2; exit 2;;
  esac
done
log() { printf '%s [rollback] %s\n' "$(date '+%Y-%m-%d %H:%M:%S')" "$*"; }
ACTUAL="$(readlink "$DESTINO/current")"
if [ -z "$OBJETIVO" ]; then
  OBJETIVO="$(ls -1dt "$DESTINO"/releases/* | grep -vxF "$ACTUAL" | head -1)"
fi
case "$OBJETIVO" in /*) ;; *) OBJETIVO="$DESTINO/$OBJETIVO";; esac
[ -d "$OBJETIVO" ] || { log "No existe la versión $OBJETIVO"; exit 3; }
log "Activa: $(basename "$ACTUAL") -> objetivo: $(basename "$OBJETIVO")"
ENVF="$DESTINO/shared/sigsf.env"; PIDF="$DESTINO/shared/sigsf.pid"
if [ "$SYSTEMD" = 1 ]; then systemctl stop sigsf; else
  [ -f "$PIDF" ] && kill "$(cat "$PIDF")" 2>/dev/null && sleep 3 || true; rm -f "$PIDF"; fi
ln -sfn "$OBJETIVO" "$DESTINO/current.tmp" && mv -Tf "$DESTINO/current.tmp" "$DESTINO/current"
if [ "$SYSTEMD" = 1 ]; then systemctl start sigsf; else
  ( cd "$DESTINO/current"; set -a; . "$ENVF"; set +a
    export SIGSF_WEB_DIR="$DESTINO/current/web" SIGSF_BACKUP_DIR="$DESTINO/shared/respaldos"
    nohup java -Dfile.encoding=UTF-8 -Dsigsf.log.dir="$DESTINO/shared/logs" -jar dist/SIGSF.jar >> "$DESTINO/shared/logs/consola.log" 2>&1 &
    echo $! > "$PIDF" ); fi
PUERTO="$(sed -n 's/^SIGSF_SERVER_PORT=//p' "$ENVF" | tail -1)"; PUERTO="${PUERTO:-8091}"
VERSION="$(tr -d '\r\n ' < "$OBJETIVO/VERSION")"
mkdir -p "$DESTINO/shared/despliegues"
set -- --url "http://127.0.0.1:$PUERTO" --version "$VERSION" --esperar 90 \
  --salida "$DESTINO/shared/despliegues/rollback_$(date +%Y%m%d%H%M%S).json"
[ -n "$LOGIN" ] && set -- "$@" --login "$LOGIN"
python3 "$OBJETIVO/scripts/smoke_test.py" "$@"
printf '%s ROLLBACK %s -> %s\n' "$(date -Iseconds)" "$(basename "$ACTUAL")" "$(basename "$OBJETIVO")" >> "$DESTINO/shared/historial_despliegues.log"
log "Reversión completada: current -> $(basename "$OBJETIVO") ($VERSION)"
