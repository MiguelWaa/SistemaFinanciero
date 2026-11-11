# Auditoría npm autorizada — SIGSF 5.6.0

Fecha local: 10/11/2026, 20:41 (America/Lima). Registro UTC: 2026-11-11T01:41:13.641095+00:00.

## Resultado comprobado

21 paquetes señalados: 1 crítico, 13 altos y 7 moderados. La auditoría terminó correctamente como análisis, pero **no cumple el umbral de seguridad high**: devolvió código 1 por los hallazgos. Se evaluó el package-lock.json de pruebas/e2e con autorización expresa del propietario.

El total cuenta entradas de paquetes afectadas, incluidas alertas propagadas por dependencias. No representa 21 CVE distintas ni 21 ataques demostrados. Dos paquetes son dependencias directas: newman 6.2.1 y newman-reporter-htmlextra 1.23.1; los otros 19 son transitivos.

## Alcance e interpretación

Son dependencias de desarrollo para ejecutar pruebas y generar reportes. Este análisis no evalúa el backend Java, los JAR, PostgreSQL ni los recursos copiados en web/vendor. No tengo evidencia suficiente para afirmar que estos avisos sean explotables en los flujos de SIGSF. La clasificación indica versiones asociadas a avisos, no una prueba de explotación.

El paquete con clasificación crítica es handlebars, presente en versiones 4.7.7 y 4.7.8. Los avisos GHSA-8r5x-fm3f-whwj y GHSA-p8wg-vrv2-v86f señalan 4.7.10 como versión corregida para esos fallos concretos. El primero depende de compilar objetos AST no confiables; el segundo requiere una plantilla controlada por un atacante y allowProtoMethodsByDefault activado. Esas condiciones no se comprobaron en este proyecto. La eliminación de esos dos avisos no garantiza resolver todas las alertas del árbol.

## Plan de corrección propuesto

1. Priorizar las dos rutas que incorporan handlebars y comprobar versiones compatibles de Newman y su reportero. Si el reporte HTML no es necesario, evaluar retirar el reportero conservando las salidas CLI/JUnit usadas por el ejecutor unificado.
2. Revisar las cadenas de Newman, postman-runtime y postman-sandbox; resolver las dependencias transitivas mediante versiones compatibles o una migración de la herramienta.
3. Revisar qs y las alertas moderadas después de los hallazgos críticos/altos.
4. Actualizar package.json y el lock en una rama separada; ejecutar las 63 aserciones de la colección API y probar la generación de reportes.
5. Repetir npm audit. Cierre: cero alertas críticas/altas o aceptación de riesgo documentada basada en alcance y uso real.

No se modificaron package.json, package-lock.json ni las versiones instaladas durante esta auditoría. No se aplicó npm audit fix --force: el informe propone Newman 4.6.1 y el reportero 1.22.5, versiones anteriores a las instaladas; una propuesta automática no demuestra compatibilidad ni que toda la cadena quede resuelta. El uso de --force permite cambios fuera de los rangos declarados, según la documentación oficial.

## Evidencias

- ../evidencias/v5.6.0/sca/npm_autorizado/npm-audit.json: respuesta original completa del registro.
- ../evidencias/v5.6.0/sca/npm_autorizado/resultado.json: comando, autorización, fecha, salida y SHA256 del lock.
- ../evidencias/v5.6.0/sca/npm_autorizado/paquetes_senalados.csv: las 21 entradas y su estado.
- ../evidencias/v5.6.0/sca/npm_autorizado/avisos_originales.csv: avisos de origen sin contar propagaciones como CVE adicionales.

## Reproducir

Desde pruebas/e2e, con autorización para enviar dependencias al registro:

```sh
npm audit --package-lock-only --json --audit-level=high --registry=https://registry.npmjs.org
```

El resultado depende de los avisos disponibles al momento de ejecutar. Los informes previos se conservan como antecedentes. Permanecen pendientes Dependency-Check para JAR, recursos frontend, ZAP, PostgreSQL y navegador. Los hallazgos npm siguen abiertos.

## Fuentes oficiales verificadas

- https://docs.npmjs.com/cli/v11/commands/npm-audit/
- https://github.com/advisories/GHSA-8r5x-fm3f-whwj
- https://github.com/advisories/GHSA-p8wg-vrv2-v86f

## Inventario de paquetes señalados

| Paquete | Versiones en lock | Severidad | Dependencia |
|---|---|---|---|
| handlebars | 4.7.7, 4.7.8 | Crítica | Transitiva |
| @faker-js/faker | 5.5.3 | Alta | Transitiva |
| flatted | 3.2.6 | Alta | Transitiva |
| httpntlm | 1.8.13 | Alta | Transitiva |
| lodash | 4.17.21 | Alta | Transitiva |
| newman | 6.2.1 | Alta | Directa |
| newman-reporter-htmlextra | 1.23.1 | Alta | Directa |
| node-forge | 1.3.1 | Alta | Transitiva |
| postman-collection | 4.4.0 | Alta | Transitiva |
| postman-collection-transformer | 4.1.8 | Alta | Transitiva |
| postman-runtime | 7.39.1 | Alta | Transitiva |
| postman-sandbox | 4.7.1 | Alta | Transitiva |
| underscore | 1.12.1 | Alta | Transitiva |
| uvm | 2.1.1 | Alta | Transitiva |
| @budibase/handlebars-helpers | 0.13.1 | Moderada | Transitiva |
| csv-parse | 4.16.3 | Moderada | Transitiva |
| jose | 4.14.4 | Moderada | Transitiva |
| postman-request | 2.88.1-postman.34 | Moderada | Transitiva |
| qs | 6.5.5 | Moderada | Transitiva |
| serialised-error | 1.1.3 | Moderada | Transitiva |
| uuid | 3.4.0, 8.3.2, 9.0.1 | Moderada | Transitiva |
