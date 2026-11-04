#!/bin/sh
# SIGSF - Descarga de librerías (Linux / macOS). Uso: sh scripts/descargar_librerias.sh
set -e
cd "$(dirname "$0")/.."
M=https://repo1.maven.org/maven2
curl -fL -o lib/slf4j-api-2.0.16.jar      $M/org/slf4j/slf4j-api/2.0.16/slf4j-api-2.0.16.jar
curl -fL -o lib/logback-core-1.5.12.jar   $M/ch/qos/logback/logback-core/1.5.12/logback-core-1.5.12.jar
curl -fL -o lib/logback-classic-1.5.12.jar $M/ch/qos/logback/logback-classic/1.5.12/logback-classic-1.5.12.jar
[ -f lib/postgresql.jar ] || curl -fL -o lib/postgresql.jar $M/org/postgresql/postgresql/42.7.4/postgresql-42.7.4.jar
# Librerías de prueba (Plan de Pruebas, Avance 4)
mkdir -p lib/test
curl -fL -o lib/test/junit-4.13.2.jar       $M/junit/junit/4.13.2/junit-4.13.2.jar
curl -fL -o lib/test/hamcrest-core-1.3.jar  $M/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar
curl -fL -o lib/test/mockito-core.jar       $M/org/mockito/mockito-core/5.14.2/mockito-core-5.14.2.jar
curl -fL -o lib/test/byte-buddy.jar         $M/net/bytebuddy/byte-buddy/1.15.10/byte-buddy-1.15.10.jar
curl -fL -o lib/test/byte-buddy-agent.jar   $M/net/bytebuddy/byte-buddy-agent/1.15.10/byte-buddy-agent-1.15.10.jar
curl -fL -o lib/test/objenesis.jar          $M/org/objenesis/objenesis/3.3/objenesis-3.3.jar
curl -fL -o lib/test/jacocoagent.jar        $M/org/jacoco/org.jacoco.agent/0.8.12/org.jacoco.agent-0.8.12-runtime.jar
curl -fL -o lib/test/jacococli.jar          $M/org/jacoco/org.jacoco.cli/0.8.12/org.jacoco.cli-0.8.12-nodeps.jar
ls -1 lib/*.jar lib/test/*.jar
