#!/bin/sh
set -eu
cd "$(dirname "$0")"
export SIGSF_PERSISTENCIA=memoria SIGSF_SERVER_PORT=8091 SIGSF_BACKUP_AUTOMATICO_HORAS=0
printf '%s\n' 'SIGSF 5.8.0 - PORTADA BANCARIA ANIMADA' 'Cierre la demostracion anterior antes de continuar.' 'Abrir http://localhost:8091/ - Ctrl+C para detener.'
exec java -jar dist/SIGSF.jar
