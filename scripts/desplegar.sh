#!/bin/sh
# ============================================================
#  SIGSF 5.11.0 · Despliegue por versiones con reversión automática
#
#  Uso:
#    sh scripts/desplegar.sh release/sigsf-5.11.0.tar.gz [opciones]
#  Opciones:
#    --destino DIR     raíz de instalación (por defecto /opt/sigsf)
#    --env ARCHIVO     variables SIGSF_* (por defecto DIR/shared/sigsf.env)
#    --systemd         reinicia el servicio systemd "sigsf" en vez de un proceso
#    --login U:C       prueba de humo con inicio de sesión (solo demostración)
#    --conservar N     versiones antiguas a mantener (por defecto 5)
#
#  Pasos: 1 verificar SHA-256 · 2 extraer en releases/<versión>-<fecha>
#         3 enlazar shared/ · 4 detener versión actual · 5 cambiar enlace current
#         6 arrancar · 7 prueba de humo · 8 si falla, volver a la versión anterior
# ============================================================
set -eu
PAQUETE="${1:?Indique el paquete sigsf-<versión>.tar.gz}"; shift
DESTINO=/opt/sigsf; ENVF=""; SYSTEMD=0; LOGIN=""; CONSERVAR=5
while [ $# -gt 0 ]; do
  case "$1" in
    --destino) DESTINO="$2"; shift 2;;
    --env) ENVF="$2"; shift 2;;
    --systemd) SYSTEMD=1; shift;;
    --login) LOGIN="$2"; shift 2;;
    --conservar) CONSERVAR="$2"; shift 2;;
    *) echo "Opción desconocida: $1" >&2; exit 2;;
  esac
done
RAIZ_REPO="$(cd "$(dirname "$0")/.." && pwd)"
log() { printf '%s [desplegar] %s\n' "$(date '+%Y-%m-%d %H:%M:%S')" "$*"; }

# 1. Integridad del paquete
SUMA="$PAQUETE.sha256"
if [ -f "$SUMA" ]; then
  ( cd "$(dirname "$PAQUETE")" && sha256sum -c "$(basename "$SUMA")" ) >/dev/null || { log "SHA-256 NO coincide: paquete alterado"; exit 3; }
  log "SHA-256 verificado: $(cut -c1-16 "$SUMA")..."
else
  log "ADVERTENCIA: no existe $SUMA; no se verificó la integridad"
fi

# 2. Extraer en una carpeta nueva (nunca se sobrescribe una versión instalada)
VERSION="$(tar -xzOf "$PAQUETE" ./VERSION 2>/dev/null || tar -xzOf "$PAQUETE" VERSION)"
VERSION="$(printf '%s' "$VERSION" | tr -d '\r\n ')"
ID="$VERSION-$(date +%Y%m%d%H%M%S)"
NUEVA="$DESTINO/releases/$ID"
mkdir -p "$DESTINO/releases" "$DESTINO/shared/logs" "$DESTINO/shared/respaldos" "$NUEVA"
tar -xzf "$PAQUETE" -C "$NUEVA"
log "Versión $VERSION extraída en $NUEVA"

# 3. Recursos compartidos entre versiones
ln -sfn "$DESTINO/shared/logs" "$NUEVA/logs"
ln -sfn "$DESTINO/shared/respaldos" "$NUEVA/respaldos"
[ -n "$ENVF" ] || ENVF="$DESTINO/shared/sigsf.env"
[ -f "$ENVF" ] || { cp "$NUEVA/deploy/sigsf.env.ejemplo" "$ENVF"; chmod 600 "$ENVF"; log "Creado $ENVF desde la plantilla: revise las contraseñas"; }

ANTERIOR=""
[ -L "$DESTINO/current" ] && ANTERIOR="$(readlink "$DESTINO/current")"

detener() {
  if [ "$SYSTEMD" = 1 ]; then systemctl stop sigsf || true; return; fi
  PIDF="$DESTINO/shared/sigsf.pid"
  if [ -f "$PIDF" ] && kill -0 "$(cat "$PIDF")" 2>/dev/null; then
    kill "$(cat "$PIDF")"; i=0
    while kill -0 "$(cat "$PIDF")" 2>/dev/null && [ $i -lt 30 ]; do sleep 1; i=$((i+1)); done
    log "Proceso anterior detenido (PID $(cat "$PIDF"))"
  fi
  rm -f "$PIDF"
}
arrancar() {
  if [ "$SYSTEMD" = 1 ]; then systemctl daemon-reload; systemctl start sigsf; return; fi
  ( cd "$DESTINO/current"
    set -a; . "$ENVF"; set +a
    export SIGSF_WEB_DIR="$DESTINO/current/web" SIGSF_BACKUP_DIR="$DESTINO/shared/respaldos"
    nohup java ${JAVA_OPTS:-} -Dfile.encoding=UTF-8 -Dsigsf.log.dir="$DESTINO/shared/logs" \
      -jar dist/SIGSF.jar >> "$DESTINO/shared/logs/consola.log" 2>&1 &
    echo $! > "$DESTINO/shared/sigsf.pid" )
  log "Iniciado PID $(cat "$DESTINO/shared/sigsf.pid")"
}
cambiar_a() { ln -sfn "$1" "$DESTINO/current.tmp" && mv -Tf "$DESTINO/current.tmp" "$DESTINO/current"; }

# 4-6. Cambio atómico del enlace "current"
detener
cambiar_a "$NUEVA"
log "current -> $NUEVA"
arrancar

# 7. Prueba de humo
PUERTO="$(sed -n 's/^SIGSF_SERVER_PORT=//p' "$ENVF" | tail -1)"; PUERTO="${PUERTO:-8091}"
EVID="$DESTINO/shared/despliegues/$ID"; mkdir -p "$EVID"
set -- --url "http://127.0.0.1:$PUERTO" --version "$VERSION" --salida "$EVID/smoke.json" --esperar 90
[ -n "$LOGIN" ] && set -- "$@" --login "$LOGIN"
if python3 "$NUEVA/scripts/smoke_test.py" "$@"; then
  log "DESPLIEGUE CORRECTO: $VERSION en http://127.0.0.1:$PUERTO/"
  printf '%s %s OK %s\n' "$(date -Iseconds)" "$ID" "${ANTERIOR:-ninguna}" >> "$DESTINO/shared/historial_despliegues.log"
  # Limpieza: conservar solo las N versiones más recientes (nunca la activa)
  ls -1dt "$DESTINO"/releases/* | tail -n +"$((CONSERVAR+1))" | while read -r v; do
    [ "$v" = "$NUEVA" ] || { rm -rf "$v"; log "Eliminada versión antigua $(basename "$v")"; }
  done
  exit 0
fi

# 8. Reversión automática
log "PRUEBA DE HUMO FALLIDA: revirtiendo"
detener
if [ -n "$ANTERIOR" ]; then
  cambiar_a "$ANTERIOR"; arrancar
  python3 "$ANTERIOR/scripts/smoke_test.py" --url "http://127.0.0.1:$PUERTO" --esperar 90 --salida "$EVID/smoke_reversion.json" \
    && log "Reversión completada: current -> $ANTERIOR" || log "La versión anterior tampoco respondió; revise los logs"
else
  log "No hay versión anterior a la cual volver"
fi
printf '%s %s REVERTIDO %s\n' "$(date -Iseconds)" "$ID" "${ANTERIOR:-ninguna}" >> "$DESTINO/shared/historial_despliegues.log"
exit 1
