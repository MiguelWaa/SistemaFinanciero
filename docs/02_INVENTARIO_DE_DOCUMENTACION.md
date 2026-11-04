# SIGSF · Inventario de documentación (v6.0)

Relación de los documentos y artefactos del proyecto, su ubicación y estado.
Desde la versión 5.0 **todo el inventario vive en el repositorio Git** (actualizado para la versión 6.0, Avance 4 · Plan de Pruebas): la
columna *Ubicación* es la ruta dentro del repositorio y el historial de cada
documento se consulta con `git log --follow <ruta>`.
**Estados:** ✅ Completo · 🔄 En actualización · 📌 Pendiente.

## A. Documentación transversal del proyecto

| N° | Documento | Ubicación | Contenido | Estado |
|---|---|---|---|---|
| T-01 | Project Charter | Avance de Proyecto Final 2 (Anexos) | Objetivos, alcance, equipo, cronograma, hitos, presupuesto | ✅ |
| T-02 | Modelo Lean Canvas | Avance 2 (Anexos) | Propuesta de valor, segmentos, canales | ✅ |
| T-03 | Definición del problema y diagramas de procesos (AS-IS / TO-BE) | Avance 2 §1.1 | Procesos de los 9 módulos | ✅ |
| T-04 | Requisitos funcionales (RF) y no funcionales (RNF) | Avance 2 §3.1 | 12 módulos con ID, prioridad, dependencias y criterio de aceptación | ✅ |
| T-05 | Casos de uso | Avance 2 §3.2.1 | Actores y casos por módulo | ✅ |
| T-06 | Prototipos UX/UI | Avance 2 §3.2.2 · `web/` | Pantallas y su implementación | ✅ |
| T-07 | Prototipos de informes y reportes | Avance 2 §3.2.3 · `/api/*/excel` | 4 exportaciones Excel + indicadores | ✅ |
| T-08 | Diagrama de clases | Avance 2 §3.2.4 · `src/.../model` | 32 entidades | 🔄 agregar `Auditoria` y capas DAO/DTO |
| T-09 | Modelo lógico/físico y diccionario de datos | Avance 2 §3.2.5 · `db/schema.sql` | 32 tablas, PK/FK, cardinalidad | 🔄 agregar tabla `auditoria` |
| T-10 | Arquitectura y patrones | `docs/01_ARQUITECTURA_Y_PATRONES.md` | Capas, MVC, DAO, DTO, SOLID, flujo de una petición | ✅ |
| T-11 | Consideraciones de seguridad | `docs/03_CONSIDERACIONES_DE_SEGURIDAD.md` | Controles por área y transversales | ✅ |
| T-12 | Respaldo y configuración de BD | `docs/04_RESPALDO_Y_CONFIGURACION_BD.md` · `scripts/` · `db/seguridad.sql` | Backup, restauración, rol de mínimo privilegio | ✅ |
| T-13 | Pruebas unitarias y TDD | `docs/05_PRUEBAS_TDD.md` · `test/` | 216 pruebas JUnit 4 + Mockito (35 clases) | ✅ |
| T-14 | Manual de instalación | `INSTALAR_LEEME.txt` · `README.md` | Pasos en pgAdmin, NetBeans y configuración | ✅ |
| T-15 | Manual de usuario / ayuda en línea | `web/ayuda.html` | Preguntas frecuentes y asistente por voz | ✅ |
| T-16 | Evidencias (capturas) | `evidencias/` | Capturas de pantallas, pruebas, API y control de versiones | ✅ |
| T-17 | Diccionario de la API REST | `README.md` (tabla de endpoints) | Método, ruta, rol requerido | ✅ |
| T-18 | Plan de continuidad (recuperación ante desastres) | — | RPO/RTO, simulacro de restauración | 📌 |
| T-19 | Recursos de código fuente | `docs/06_RECURSOS_DE_CODIGO_FUENTE.md` | Carpetas, 13 paquetes, librerías, dependencias, configuración | ✅ |
| T-20 | Configuración de registro (Logback) | `src/logback.xml` · `test/logback-test.xml` · `docs/06` §5 | 5 tipos de log, rotación, MDC por área, registro seguro | ✅ |
| T-21 | Diccionario de importación/exportación | `controller/CatalogoArchivos` · `docs/06` §6 | 15 recursos, columnas, obligatorias, formatos | ✅ |
| T-22 | Plantillas de importación | `GET /api/{recurso}/plantilla` | Una plantilla CSV por recurso importable (7) | ✅ |
| T-23 | Gestión de dependencias | `lib/LEEME.txt` · `scripts/descargar_librerias.*` · `nbproject/project.properties` | Versiones, licencias, coordenadas Maven | ✅ |
| T-24 | Registros de operación (logs) | `logs/` (se genera al ejecutar, no se versiona) | Acceso, seguridad, errores, por área | ✅ |
| T-25 | **Sistema de control de versiones** | `docs/07_CONTROL_DE_VERSIONES.md` | Git Flow, convención de commits, SemVer, merge, conflictos, rollback, remoto, CI | ✅ |
| T-26 | Registro de cambios | `CHANGELOG.md` | Cambios por versión (1.0.0 → 6.0.0) | ✅ |
| T-27 | Versión de la aplicación | `VERSION` · `build.xml` (`-post-compile`) · `version.properties` | SemVer y datos del commit dentro del .jar | ✅ |
| T-28 | Reglas del repositorio | `.gitignore` · `.gitattributes` · `db.properties.ejemplo` | Qué no se versiona, fin de línea, plantilla de configuración | ✅ |
| T-29 | Hooks de Git | `.githooks/pre-commit` · `.githooks/commit-msg` · `scripts/git/instalar_hooks.*` | Bloqueo de credenciales y archivos generados; formato de commits | ✅ |
| T-30 | Integración continua | `.github/workflows/ci.yml` · `scripts/compilar_y_probar.*` | 2 trabajos: JUnit + PostgreSQL y API/sistema/aceptación/carga, en cada push / Pull Request | ✅ |
| T-31 | Guía de contribución | `CONTRIBUTING.md` · `.github/pull_request_template.md` | Pasos para trabajar en equipo y lista de verificación de PR | ✅ |
| T-32 | Versionamiento del esquema BD | `db/version_esquema.sql` · tabla `version_esquema` · `version/HistorialEsquema` | Versión de la base de cada equipo (1.0.0 → 5.0.0) | ✅ |
| T-33 | Pantalla de control de versiones | `web/versiones.html` · `GET /api/version` · `GET /api/versiones` | Versión, commits, red de ramas, etiquetas y esquema BD | ✅ |
| T-34 | Repositorio empaquetado | `SIGSF_v6.0.0.bundle` (entregable) | Copia completa del repositorio con todas las ramas y etiquetas | ✅ |

## A2. Documentación de pruebas (Avance 4 · versión 6.0)

| N° | Documento / artefacto | Ubicación | Contenido | Estado |
|---|---|---|---|---|
| P-01 | **Plan de Pruebas** | `docs/09_PLAN_DE_PRUEBAS.md` | Alcance, estrategia, técnicas, entorno, niveles, criterios, defectos, riesgos, trazabilidad | ✅ |
| P-02 | **Herramientas, codificación y resultados** | `docs/10_HERRAMIENTAS_Y_RESULTADOS.md` | Herramientas elegidas, extractos de código, resultados por nivel, cobertura, carga, registro de defectos, rutas compartidas | ✅ |
| P-03 | Preparación del entorno | `scripts/pruebas/preparar_bd_pruebas.*` | Base `SistemaFinanciero_test` desde cero (PE-01) | ✅ |
| P-04 | Ejecución del plan completo | `scripts/pruebas/ejecutar_plan_de_pruebas.*` | PE → PU → PI → PS → PA → PR con reportes | ✅ |
| P-05 | Casos unitarios e integración (JUnit) | `test/` · `test/.../integracion/` | 216 casos (171 unitarios, 45 de integración) | ✅ |
| P-06 | Colección de API | `pruebas/postman/SIGSF_API.postman_collection.json` (+ entorno, generador) | 31 casos PI-NEW, 51 aserciones | ✅ |
| P-07 | Pruebas de sistema | `pruebas/e2e/test_sistema_e2e.py` · `pruebas/conftest.py` | 13 casos PS con capturas | ✅ |
| P-08 | Criterios de aceptación | `pruebas/aceptacion/features/*.feature` · `test_aceptacion.py` | 4 historias, 10 escenarios en Gherkin español | ✅ |
| P-09 | Plan de carga | `pruebas/rendimiento/SIGSF_carga.jmx` · `cajeros.csv` · `analizar_jtl.py` | PR-01: 50 usuarios, 60 s, criterios | ✅ |
| P-10 | Registros y reportes de ejecución | `evidencias/pruebas/reportes/` | JUnit, Newman (HTML), pytest (HTML), JMeter, JaCoCo (HTML y CSV), consola completa | ✅ |
| P-11 | Capturas de pantalla | `evidencias/pruebas/e2e/` · `evidencias/pruebas/aceptacion/` | Una por caso de sistema / criterio | ✅ |
| P-12 | Evidencia TDD (rojo) | `evidencias/pruebas/01_…txt` · `02_…txt` | 22 y 5 fallas antes de corregir | ✅ |
| P-13 | Registro de defectos | `docs/10` §4 | 13 defectos: 11 corregidos, 2 diferidos (baja) | ✅ |
| P-14 | Acta de aceptación (UAT) | `docs/09` §5.4 | Tabla de firmas por historia | 📌 firma del usuario |

## B. Documentación por área del negocio

| Área | Requisitos | Pantalla (Vista) | Código (Controlador → Servicio → DAO) | Tablas | Reporte / ficheros | Pruebas |
|---|---|---|---|---|---|---|
| **Marketing** | RF-MKT-01…07, RNF-MKT-01…03 | Marketing.html | MarketingController → Campania/ProspectoService → CampaniaDao, ProspectoDao | campania, prospecto, campania_prospecto, actividad_marketing | Exporta prospectos y campañas (xlsx/csv/json); importa ambos con plantilla · `logs/areas/MARKETING.log` | ArchivosApiTest (importa prospectos) |
| **Ventas** | RF-VNT-01…06, RNF-VNT-01…04 | Ventas.html | VentaController → VentaService → VentaDao | venta, detalle_venta | `ventas.xlsx` (automático) + exportar xlsx/csv/json · no se importa · `VENTAS.log` | VentaYEmpenoServiceTest |
| **Logística** | RF-LOG-01…04, RNF-LOG-01…03 | Logistica.html | LogisticaController → ProveedorService → ProveedorDao | proveedor, movimiento_logistica | Exporta e importa proveedores · `LOGISTICA.log` | ArchivoServiceTest (validación previa), ArchivosApiTest (403 cajero) |
| **Almacén** | RF-ALM-01…06, RNF-ALM-01…04 | almacen.html | AlmacenController → ProductoService → ProductoDao | producto, inventario, movimiento_inventario | `productos.xlsx`, alerta stock bajo; exporta e importa productos · `ALMACEN.log` | RepositorioMemoriaTest |
| **Producción** | RF-PRD-01…05, RNF-PRD-01…03 | produccion.html | ProduccionController → ProduccionService → LineaProduccionDao, OrdenProduccionDao | linea_produccion, orden_produccion | Avance de órdenes; exporta líneas y órdenes, importa líneas · `PRODUCCION.log` | ProduccionServiceTest |
| **Operaciones** | RF-OPE-01…07, RNF-OPE-01…05 | operaciones financieras.html | OperacionController → Operacion/TipoCambio/Prestamo/EmpenoService | tipo_cambio, operacion, prestamo, empeno, articulo, comprobante | `operaciones.xlsx`; exporta operaciones, tipos de cambio, préstamos y empeños · nunca se importan · `OPERACIONES.log` | OperacionServiceTest, PrestamoServiceTest |
| **Dirección y Gerencia General** | RF-DIR-01…05, RNF-DIR-01…03 | direccion gerencia.html, gerencia general… | DireccionController, SistemaController → ExpedienteService, DashboardService | expediente_aprobacion, decision_direccion, reporte_direccion, decision_gerencia, resolucion_gerencial | Panel de KPIs, `reportes.html`; exporta e importa expedientes (entran PENDIENTE) · `DIRECCION_GERENCIA.log` | ArchivoServiceTest (ignora estado importado) |
| **Clientes y servicios** | RF-CLI-01…07, RNF-CLI-01…04 | clientes y servicios financieros.html | ClienteController → ClienteService, ServicioFinancieroService | cliente, servicio_financiero, cliente_servicio, cliente_acceso | `clientes.xlsx`; exporta clientes y servicios, importa clientes · `CLIENTES.log` | ClienteServiceTest |
| **Seguridad y usuarios** | RF-SEG-01…03, RNF-SEG-01/02 | login.html, usuarios.html, seguridad.html, respaldos.html | Auth/SeguridadController → Auth/Usuario/RolService | rol, permiso, rol_permiso, usuario, auditoria | Bitácora (exportable solo por ADMIN), matriz de permisos · `sigsf-seguridad.log` | AuthServiceTest, UsuarioServiceTest, pruebas de `security/` |
| **Portal y solicitudes** | RF-POR-01…05, RF-SOL-01/02 | portal.html, portal-inicio.html, solicitudes.html | Portal/SolicitudController → Portal/SolicitudService | operacion, prestamo, empeno, cliente_acceso | Resumen del cliente | SolicitudServiceTest, ApiIntegracionTest |

## C. Inventario de artefactos del código fuente (v5.0)

| Tipo de artefacto | Ubicación | Cantidad |
|---|---|---|
| Clases Java del backend | `src/com/utp/sistemafinanciero/` (13 paquetes) | 197 |
| · controladores (MVC) | `controller/` | 21 |
| · servicios (reglas de negocio) | `service/` | 23 |
| · DAO: interfaces + JDBC + memoria | `dao/` | 69 |
| · modelo (entidades) | `model/` | 33 |
| · DTO | `dto/` | 9 |
| · seguridad / respaldo / ficheros / config / util / web | `security/` `backup/` `archivos/` `config/` `util/` `web/` | 37 |
| · control de versiones | `version/` | 5 |
| Clases de prueba JUnit | `test/` | 35 (216 pruebas) |
| Pruebas de API, sistema, aceptación y carga | `pruebas/` | 31 + 13 + 10 casos · 1 plan JMeter |
| Vistas (pantallas HTML) | `web/*.html` | 23 |
| Hojas de estilo y scripts del frontend | `web/*.css`, `web/*.js` | 15 |
| Scripts SQL | `db/*.sql` | 6 |
| Scripts de operación y de pruebas | `scripts/` · `scripts/pruebas/` | 15 |
| Documentos técnicos | `docs/` | 10 |
| Librerías | `lib/*.jar` | 4 (+ JUnit para pruebas) |

## D. Control de versiones del documento

| Versión | Fecha | Cambio |
|---|---|---|
| 1.0 | Avance 2 | Requisitos, casos de uso, prototipos, modelo de datos |
| 2.0 | Taller de arquitectura | Arquitectura MVC/DAO/DTO/SOLID, seguridad, respaldo, TDD, inventario |
| 3.0 | Documentación del proyecto | Requisitos y estado por área (PDF del proyecto) |
| 4.0 | Taller: recursos de código | Recursos de código fuente (T-19), Logback (T-20), importación/exportación (T-21, T-22), dependencias (T-23), logs (T-24), seguridad Java; 99 pruebas |
| 5.0 | Taller: control de versiones | Repositorio Git con Git Flow (T-25), CHANGELOG (T-26), VERSION (T-27), reglas del repositorio (T-28), hooks (T-29), CI (T-30), contribución (T-31), versionamiento del esquema (T-32), pantalla de versiones (T-33), bundle (T-34), inventario de artefactos (§C); 117 pruebas |
