# Plan integrado de pruebas de software 5.6.0

Entrada: copia identificada, JDK 17+, datos de prueba, dependencias y entorno aislado.
Ejecución: `python scripts/validar_proyecto.py`. Resultados por etapa, sin sumar aserciones y casos.

| Nivel | Implementación | Estado de esta entrega |
|---|---|---|
| Unitarias | test/: servicios, seguridad, JSON, ficheros y DAO memoria | Incluidas en 214 JUnit aprobadas |
| Integración API | ApiIntegracionTest, SeguridadEntradasApiTest y colección Postman | 26 peticiones, 63 aserciones aprobadas |
| Sistema / aceptación | SistemaFlujoCompletoTest y AceptacionHistoriasUsuarioTest | Incluidas en JUnit aprobadas |
| Navegador / UX | pruebas/e2e/tests, 14 casos | Pendiente por limitación del entorno |
| Rendimiento local | RendimientoApiTest, 500 peticiones | Registro actual en evidencias/v5.6.0/integracion/junit.txt |
| JDBC / PostgreSQL | PostgresqlSeguridadIT, 7 casos | Implementadas y compiladas; falta PostgreSQL para ejecutarlas |
| Seguridad | 12_PLAN_SEGURIDAD.md | Regresiones aprobadas; DAST/SCA ampliados pendientes |

Salida propuesta: pruebas críticas aprobadas, análisis de riesgos cerrado, evidencia verificable y aceptación del equipo.
El estado actual no equivale a aprobación para producción. El workflow de CI está configurado pero no se ejecutó remotamente.
