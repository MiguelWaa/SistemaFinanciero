# SIGSF · Inventario de documentación

Relación de los documentos del proyecto, su ubicación, responsable y estado.
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
| T-13 | Plan y evidencia de pruebas (TDD) | `docs/05_PRUEBAS_TDD.md` · `test/` | 99 pruebas JUnit 4 (21 clases) | ✅ |
| T-14 | Manual de instalación | `INSTALAR_LEEME.txt` · `README.md` | Pasos en pgAdmin, NetBeans y configuración | ✅ |
| T-15 | Manual de usuario / ayuda en línea | `web/ayuda.html` | Preguntas frecuentes y asistente por voz | ✅ |
| T-16 | Evidencias (capturas) | `evidencias/` | 23 capturas de pantallas, pruebas y API | ✅ |
| T-17 | Diccionario de la API REST | `README.md` (tabla de endpoints) | Método, ruta, rol requerido | ✅ |
| T-18 | Plan de continuidad (recuperación ante desastres) | — | RPO/RTO, simulacro de restauración | 📌 |
| T-19 | Recursos de código fuente | `docs/06_RECURSOS_DE_CODIGO_FUENTE.md` | Carpetas, 13 paquetes, librerías, dependencias, configuración | ✅ |
| T-20 | Configuración de registro (Logback) | `src/logback.xml` · `test/logback-test.xml` · `docs/06` §5 | 5 tipos de log, rotación, MDC por área, registro seguro | ✅ |
| T-21 | Diccionario de importación/exportación | `controller/CatalogoArchivos` · `docs/06` §6 | 15 recursos, columnas, obligatorias, formatos | ✅ |
| T-22 | Plantillas de importación | `GET /api/{recurso}/plantilla` | Una plantilla CSV por recurso importable (7) | ✅ |
| T-23 | Gestión de dependencias | `lib/LEEME.txt` · `scripts/descargar_librerias.*` · `nbproject/project.properties` | Versiones, licencias, coordenadas Maven | ✅ |
| T-24 | Registros de operación (logs) | `logs/` (se genera al ejecutar) | Acceso, seguridad, errores, por área | ✅ |

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

## C. Control de versiones del documento

| Versión | Fecha | Cambio |
|---|---|---|
| 1.0 | Avance 2 | Requisitos, casos de uso, prototipos, modelo de datos |
| 2.0 | Taller de arquitectura | Arquitectura MVC/DAO/DTO/SOLID, seguridad, respaldo, TDD, inventario |
| 3.0 | Documentación del proyecto | Requisitos y estado por área (PDF del proyecto) |
| 4.0 | Taller: recursos de código | Recursos de código fuente (T-19), Logback (T-20), importación/exportación (T-21, T-22), dependencias (T-23), logs (T-24), seguridad Java; 99 pruebas |
