# Implementación y retest 5.6.0

| Hallazgo anterior | Cambio aplicado | Resultado actual |
|---|---|---|
| JSON con sufijo daba 200 usando credenciales válidas | Json.Parser exige fin; rechaza claves duplicadas, números inválidos, controles y más de 64 niveles | HTTP 400; regresiones aprobadas |
| JSON con text/plain daba 200 | Ctx valida media type, charset y bytes UTF-8; excepción web 415 | HTTP 415, sin token |
| script-src permitía unsafe-inline | 26 bloques externos, eventos addEventListener y script-src-attr none | Cabecera y referencias verificadas; E2E pendiente |

Los cambios no introducen DAO alternos, nuevos frameworks ni un mecanismo de deserialización de objetos Java.
Se conserva MVC/DTO/DAO y la inyección de servicios existente. No se modificó el DDL de producción.

Código compilado y probado: commit 614f35b, posterior a a453fe1. Los commits siguientes agrupan documentación y evidencia.
La aplicación conserva el proceso manual de verificación de pagos QR. No integra una API bancaria real.

## Reproducción

`python scripts/validar_proyecto.py` ejecuta compilación, JUnit, validación JS, API, entradas adversas y navegador.
`--omitir-e2e` deja el navegador NO EJECUTADO. `--source-target-17` es una excepción explícita para un runtime incompleto.
La entrega actual registra exactamente esa excepción; no certifica APIs Java 17 mediante --release.

## Pruebas ampliadas

- PostgreSQL: configure SIGSF_DB_PASSWORD y ejecute `python scripts/probar_postgresql.py`. Solo crea una base local nueva.
- ZAP: con Docker y la demo iniciada, `python scripts/escanear_zap.py` (pasivo). `--activo` aplica ataques en la copia local desechable.
- SCA local: `python scripts/escanear_dependencias.py` inventaría versiones y SHA256 sin enviar datos.
- SCA online: `--online` requiere autorización del propietario, Dependency-Check instalado y acceso al registro npm; envía el árbol de paquetes para la auditoría.

Actualización: npm audit se ejecutó con autorización y señaló 21 paquetes (1 crítico, 13 altos y 7 moderados). Correcciones pendientes; consultar 14_AUDITORIA_NPM.md. Los demás análisis ampliados continúan pendientes. No se emitió una aprobación para producción.
