#!/bin/sh
# SIGSF - Descarga de librerías (Linux / macOS). Uso: sh scripts/descargar_librerias.sh
set -e
cd "$(dirname "$0")/.."
M=https://repo1.maven.org/maven2
curl -fL -o lib/slf4j-api-2.0.16.jar      $M/org/slf4j/slf4j-api/2.0.16/slf4j-api-2.0.16.jar
curl -fL -o lib/logback-core-1.5.12.jar   $M/ch/qos/logback/logback-core/1.5.12/logback-core-1.5.12.jar
curl -fL -o lib/logback-classic-1.5.12.jar $M/ch/qos/logback/logback-classic/1.5.12/logback-classic-1.5.12.jar
[ -f lib/postgresql.jar ] || curl -fL -o lib/postgresql.jar $M/org/postgresql/postgresql/42.7.4/postgresql-42.7.4.jar
ls -1 lib/*.jar
