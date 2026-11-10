# Herramientas, codificación y resultados 5.6.0

| Herramienta | Código / configuración | Evidencia |
|---|---|---|
| JUnit 4.13.2 | test/ y scripts/validar_proyecto.py | 214 casos aprobados, incluidos 22 nuevos |
| Postman / Newman 6.2.1 | pruebas/postman/ y package-lock.json | 26 peticiones / 63 aserciones, sin fallos |
| Python 3 | pruebas/seguridad/comprobar_entradas.py | 24/24 comprobaciones aprobadas |
| Playwright 1.63.0 | pruebas/e2e/tests/ | 14 casos definidos, sin nueva ejecución de navegador |
| PostgreSQL / JDBC | scripts/probar_postgresql.py y PostgresqlSeguridadIT.java | 7 casos compilados, pendientes de ejecución |
| ZAP | scripts/escanear_zap.py | Bloqueado: falta Docker |
| Dependency-Check / npm audit | scripts/escanear_dependencias.py | Inventario local; análisis en línea no ejecutado |
| Git / GitHub Actions | .git, .githooks y .github/workflows/ | Historial y cambios locales; CI remoto no verificado |

Codificación aplicada: Json.java, Ctx.java, TipoContenidoNoSoportadoException.java, ApiServer.java y web/pages/.
La guía integral contiene la matriz de trazabilidad y rutas de cada herramienta. Registros actuales: evidencias/v5.6.0/.
