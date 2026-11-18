# Inventario y trazabilidad 5.11.0

Entrada general: ../DOCUMENTACION_INTEGRAL.html. El inventario completo está en Inventario_documentacion.csv.

| Requisito | Evidencia / recurso |
|---|---|
| 1. Procesos | modelos/Proceso_acceso.mmd, Proceso_pago.mmd, Proceso_archivos.mmd |
| 2. DTO, DAO, MVC, SOLID | 01_ARQUITECTURA_Y_PATRONES.md, modelos/Arq_capas.mmd y código src/ |
| 3. Clases, atributos, métodos y relaciones | modelos/Catalogo_clases.html, Atributos_y_metodos.csv, Clases_pagos.mmd |
| 4. Modelo lógico y físico | modelos/Modelo_logico_completo.mmd, Modelo_fisico_completo.mmd, DDL_revisado.sql |
| 5. Diccionario | modelos/Diccionario_DDL.csv y Relaciones_cardinalidad.csv |
| 6. UX/UI | web/, 18_ENTRADA_AUTOMATICA.md, evidencias/v5.10.0/ y capturas históricas identificadas |
| 7. Reportes | documentación integral, model/pagos/ y exportadores |
| 8. Recursos, dependencias, logback y ficheros | 06_RECURSOS_DE_CODIGO_FUENTE.md, inventario local de dependencias |
| 9. Seguridad Java y SCV | 12_PLAN_SEGURIDAD.md, 13_IMPLEMENTACION_Y_RETEST.md, .git y workflows |
| 10. Pruebas | 10_PLAN_DE_PRUEBAS.md, 11_HERRAMIENTAS_Y_RESULTADOS_DE_PRUEBAS.md y evidencias/v5.6.0/ |
| Plan de seguridad | Matriz PS-01 a PS-18, pruebas/seguridad/ y test/ |
| 11. Despliegue (fundamentos, empaquetado, CI/CD, configuración) | 19_PLAN_DE_DESPLIEGUE.md, modelos/Despliegue.mmd, Dockerfile, docker-compose.yml, deploy/, .github/workflows/cd.yml, scripts/desplegar.sh, rollback.sh, smoke_test.py, empaquetar_release.py, pipeline_local.py |
| Resultados del despliegue | evidencias/v5.11.0/ (pipeline, despliegue, docker_postgresql, configuracion, capturas, regresion, pruebas_postgresql) |
| Herramientas / código / resultados | scripts/, test/, reportes actuales y presentación |

Los documentos conservados de versiones anteriores sirven como antecedentes. La validación selectiva actual está en evidencias/v5.10.0/; la regresión completa anterior está en evidencias/v5.9.0/; la auditoría npm y análisis anteriores siguen en evidencias/v5.6.0/.

Actualización autorizada: 14_AUDITORIA_NPM.md y evidencias/v5.6.0/sca/npm_autorizado/ contienen el resultado actual del análisis npm.
