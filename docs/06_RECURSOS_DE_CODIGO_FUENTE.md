# SIGSF · Documentación de recursos de código fuente

**Proyecto:** Sistema Integral de Gestión de Servicios Financieros (SIGSF)
**Curso:** Integrador I Sistemas – Software (UTP) · **Taller:** MVC, TDD, DAO, SOLID · **Versión:** 4.0

Este documento describe cómo está organizado el código: carpetas, paquetes, librerías y dependencias, configuración, registro con Logback, e importación y exportación de ficheros, por cada área del negocio.

---

## 1. Estructura de carpetas del proyecto

```
SistemaFinanciero/
├── src/                     Código fuente Java (188 clases, ~13 100 líneas)
│   ├── logback.xml          Configuración de registro (se copia al classpath)
│   └── com/utp/sistemafinanciero/…   (paquetes, ver §2)
├── test/                    Pruebas JUnit 4 (21 clases, 99 pruebas) + logback-test.xml
├── web/                     Frontend: 22 páginas HTML, CSS y JS (api.js, sigsf-crud.js)
├── db/                      Scripts SQL: schema, datos de ejemplo, portal, seguridad
├── lib/                     Librerías .jar (driver PostgreSQL, SLF4J, Logback)
├── scripts/                 Respaldo/restauración y descarga de librerías (.bat / .sh)
├── docs/                    Documentación técnica (01 a 06)
├── evidencias/              Capturas de pantallas, pruebas y API
├── nbproject/               Configuración del proyecto NetBeans (Ant)
├── db.properties            Configuración en tiempo de ejecución (sin recompilar)
├── build.xml · manifest.mf  Construcción con Ant
│
│   Carpetas que crea el sistema al ejecutarse (no se versionan, ver .gitignore):
├── logs/                    Registros de Logback (§5)
├── respaldos/               Copias de la base de datos
└── reportes/                Excel de ventas que se actualiza en cada venta
```

## 2. Organización de paquetes (`src/com/utp/sistemafinanciero`)

| Paquete | Clases | Capa / patrón | Responsabilidad |
|---|---|---|---|
| `web` | 8 | Front Controller | `ApiServer` (HTTP, cabeceras seguras, MDC y log de acceso), `Rutas`, `Ctx`, `Filtro`, `ObservadorPeticion`, `Descarga`, `Handler`, **`AreaNegocio`** |
| `controller` | 20 | MVC – Controlador | Un controlador por módulo, `ControladorCrud<E,S>`, `MapeadorEntrada`, **`ArchivoController`**, **`CatalogoArchivos`** |
| `service` | 23 | MVC – Modelo (reglas) | Reglas de negocio con `BigDecimal`, validaciones, **`ArchivoService`** |
| `dao` | 24 | DAO + Abstract Factory | 19 interfaces DAO, interfaces segregadas (`LecturaDao`, `EscrituraDao`, `ActualizableDao`, `CrudDao`), `DaoFactory` |
| `dao/jdbc` | 20 | DAO (producción) | Implementación PostgreSQL con `PreparedStatement` |
| `dao/memoria` | 22 | DAO (pruebas/demo) | Implementación en memoria + `DatosSemilla` |
| `model/*` | 32 | Entidades | POJO por área: seguridad, clientes, operaciones, ventas, almacén, marketing, logística, producción, dirección, gerencia |
| `dto` | 9 | DTO | `UsuarioDTO` (sin contraseña), `SesionDTO`, `LoginDTO`, `ClientePublicoDTO`, `SolicitudDTO`, `VentaReporteDTO`, `IndicadoresDTO`, `RespaldoDTO`, **`ResultadoImportacionDTO`** |
| **`archivos`** | 9 | Recursos de ficheros | `FormatoArchivo`, `TipoDato`, `Columna`, `DefinicionArchivo`, `EscritorCsv`, `LectorCsv`, `LectorExcel`, `ProteccionFormulas`, `ArchivoGenerado` |
| `security` | 9 | Chain of Responsibility, Strategy | `FiltroSeguridad`, `PoliticaAcceso` (RBAC), `GestorSesiones`, `LimitadorIntentos`, `Pbkdf2PasswordHasher` |
| `backup` | 5 | Strategy | `RespaldoService` + `RespaldoPgDump`, `RespaldoJdbcSql`, `RespaldoMemoriaJson` |
| `config` | 2 | Composition Root | `Configuracion` (lee `db.properties`), `Contenedor` (inyección de dependencias) |
| `util` | 3 | Utilidades | `Db` (JDBC), `Json`, `ExcelExporter` (xlsx sin librerías) |

**Regla de dependencias:** `web → controller → service → dao (interfaz)`. `archivos` es un paquete de apoyo que usan `service` y `controller`; no conoce la base de datos. Las implementaciones `dao/jdbc` y `dao/memoria` dependen de las interfaces, nunca al revés. Solo `config/Contenedor` crea objetos con `new`.

## 3. Librerías y dependencias

### 3.1 Librerías externas (carpeta `lib/`)

| Librería | Versión | Coordenadas Maven | Uso en SIGSF | Licencia | Ámbito |
|---|---|---|---|---|---|
| Driver JDBC PostgreSQL | 42.7.x | `org.postgresql:postgresql` | Conexión a la base `SistemaFinanciero` (`util/Db`) | BSD-2 | compilación y ejecución |
| SLF4J API | 2.0.16 | `org.slf4j:slf4j-api` | Interfaz de registro: `Logger`, `LoggerFactory`, `MDC` | MIT | compilación y ejecución |
| Logback Core | 1.5.12 | `ch.qos.logback:logback-core` | Appenders, rotación, codificación | EPL 1.0 / LGPL 2.1 | ejecución |
| Logback Classic | 1.5.12 | `ch.qos.logback:logback-classic` | Implementación de SLF4J, `SiftingAppender`, `AsyncAppender` | EPL 1.0 / LGPL 2.1 | ejecución |
| JUnit | 4.13.2 | `junit:junit` | Pruebas unitarias e integración | EPL 1.0 | solo pruebas (biblioteca de NetBeans) |
| Hamcrest | 1.3 | `org.hamcrest:hamcrest-core` | Dependencia de JUnit 4 | BSD | solo pruebas |

Descarga: `scripts\descargar_librerias.bat` (Windows) o `scripts/descargar_librerias.sh`. Los nombres quedan registrados en `nbproject/project.properties` (`javac.classpath`).

**Decisión de diseño:** Excel, CSV, JSON, servidor HTTP y cifrado se resolvieron con el JDK, sin Apache POI, Jackson ni frameworks. Así hay menos dependencias que mantener y menos superficie de ataque por vulnerabilidades de terceros (OWASP A06).

### 3.2 Módulos del JDK utilizados

| Módulo / paquete | Para qué |
|---|---|
| `jdk.httpserver` (`com.sun.net.httpserver`) | Servidor HTTP del `ApiServer` |
| `java.sql` | JDBC: `Connection`, `PreparedStatement` |
| `javax.crypto`, `java.security` | PBKDF2-HMAC-SHA256, `SecureRandom`, `MessageDigest` |
| `java.xml` (`javax.xml.stream`) | Lectura segura del XML interno de los .xlsx (StAX) |
| `java.util.zip` | Crear y leer .xlsx (son archivos ZIP) |
| `java.nio.file`, `java.nio.charset` | Archivos de respaldo y reportes; decodificación UTF-8 estricta |
| `java.math.BigDecimal` | Montos, tipos de cambio, cuotas e intereses |
| `java.util.concurrent` | Hilos del servidor y respaldo automático programado |
| `java.net.http` | Cliente HTTP de las pruebas de integración |

### 3.3 Frontend y herramientas

| Recurso | Uso |
|---|---|
| HTML5, CSS3, JavaScript (sin frameworks) | 22 páginas; motor común `sigsf-crud.js` y cliente `api.js` |
| Font Awesome 6.5.1 (CDN cdnjs) | Íconos; permitido por la política CSP |
| NetBeans + Apache Ant | Compilar (F11), ejecutar (F6) y probar (Alt+F6) |
| JDK 17 | `javac.source=17` / `javac.target=17` |
| PostgreSQL + pgAdmin 4, `pg_dump` | Base de datos y respaldo |

## 4. Configuración (`db.properties`)

Se lee al arrancar; no requiere recompilar. Cualquier clave puede darse por variable de entorno (`SIGSF_` + clave en mayúsculas, p. ej. `SIGSF_DB_PASSWORD`).

| Grupo | Claves |
|---|---|
| Persistencia | `persistencia=postgresql` o `memoria` |
| Base de datos | `db.host`, `db.port`, `db.name`, `db.user`, `db.password` |
| Servidor | `server.port`, `web.dir`, `server.max.cuerpo.kb`, `seguridad.cors.origen` |
| Seguridad | `seguridad.sesion.minutos`, `seguridad.intentos.max`, `seguridad.bloqueo.minutos`, `seguridad.pbkdf2.iteraciones` |
| Respaldos | `backup.dir`, `backup.pgdump`, `backup.retener`, `backup.automatico.horas` |
| **Ficheros (nuevo)** | `importacion.max.filas` (por defecto 2000) |
| **Logs (nuevo)** | propiedad de la JVM `-Dsigsf.log.dir=…` (por defecto `logs`) |

## 5. Registro con Logback (`src/logback.xml`)

El código escribe con SLF4J (`LoggerFactory.getLogger(...)`). Logback decide adónde va cada mensaje. Se reemplazaron todos los `System.out` / `printStackTrace` del servidor.

### 5.1 Archivos de log

| Archivo | Contenido | Rotación | Conservación |
|---|---|---|---|
| `logs/sigsf.log` | Todo lo de la aplicación (INFO o superior) | diaria y cada 10 MB, `.gz` | 30 días, tope 1 GB |
| `logs/sigsf-acceso.log` | Una línea por petición a la API: método, ruta, código HTTP, milisegundos | diaria y cada 20 MB | 30 días |
| `logs/sigsf-seguridad.log` | LOGIN_OK/FALLIDO, ACCESO_DENEGADO, RESPALDO, IMPORTAR, EXPORTAR | diaria | **90 días** |
| `logs/sigsf-errores.log` | Solo WARN y ERROR, con la traza completa | diaria y cada 10 MB | 30 días |
| `logs/areas/<ÁREA>.log` | Un archivo por área del negocio (`SiftingAppender` por MDC `area`) | diaria | 30 días |

Áreas: `MARKETING`, `VENTAS`, `LOGISTICA`, `ALMACEN`, `PRODUCCION`, `OPERACIONES`, `DIRECCION_GERENCIA`, `CLIENTES`, `SEGURIDAD`, `SISTEMA`. La clase `web/AreaNegocio` asigna el área según la ruta (`/api/prospectos/…` → `MARKETING`).

### 5.2 Formato de cada línea

```
fecha       hora         nivel [hilo]    [área]      [usuario@ip]               [petición] clase - mensaje
2026-10-14 09:15:02.418 INFO  [pool-2-t-3] [MARKETING] [admin@sigsf.com@127.0.0.1] [a1b2c3d4] SIGSF.ACCESO - POST /api/prospectos/importar -> 200 (38 ms)
```

El área, el usuario, la IP y el id de petición se guardan en el **MDC** (`ApiServer`), así que cualquier mensaje de un servicio sale etiquetado sin código adicional.

### 5.3 Loggers y niveles

| Logger | Nivel | Destino |
|---|---|---|
| `root` | INFO | consola, `sigsf.log`, `sigsf-errores.log`, `areas/` |
| `SIGSF.ACCESO` | INFO | `sigsf-acceso.log` y `areas/` (no sale por consola) |
| `SIGSF.SEGURIDAD` | INFO | `sigsf-seguridad.log` + root |
| `com.utp.sistemafinanciero` | INFO | — |
| `com.utp.sistemafinanciero.dao` | WARN (cambiar a DEBUG para depurar) | — |
| `org.postgresql` | WARN | — |

`scan="true"` hace que Logback relea el archivo cada 60 s: se puede cambiar un nivel sin reiniciar. En las pruebas se usa `test/logback-test.xml` (solo WARN por consola, sin archivos).

### 5.4 Reglas de registro seguro

1. `%replace(%msg){'[\r\n]+', ' | '}` elimina saltos de línea: un usuario no puede falsificar líneas (log injection, CWE-117).
2. Nunca se registran contraseñas, tokens ni el cuerpo de las peticiones.
3. Los errores 500 dan al navegador solo un código de referencia; la traza va a `sigsf-errores.log` con ese mismo código.
4. `AsyncAppender` evita que el disco frene las peticiones (`discardingThreshold=0`: no se descartan eventos).

## 6. Importación y exportación de ficheros

### 6.1 Endpoints

| Método y ruta | Permiso requerido | Qué hace |
|---|---|---|
| `GET /api/{recurso}/exportar?formato=xlsx\|csv\|json` | lectura (L) del módulo | Descarga los datos del recurso |
| `GET /api/{recurso}/plantilla` | lectura (L) | Plantilla CSV con encabezados (`*` = obligatorio) y una fila de ejemplo |
| `POST /api/{recurso}/importar` | escritura (LE) | Cuerpo `{"nombre","contenido" (Base64),"soloValidar"}`; devuelve `ResultadoImportacionDTO` |
| `GET /api/{ventas\|productos\|clientes\|operaciones}/excel` | lectura | Exportaciones anteriores (se mantienen) |

### 6.2 Formatos

| Formato | Exporta | Importa | Detalles |
|---|---|---|---|
| XLSX | sí | sí | Encabezado con formato, fila inmovilizada, autofiltro, ancho de columnas; DNI/RUC con ceros se guardan como texto |
| CSV | sí | sí | UTF-8 con BOM, RFC 4180; al importar detecta `,` `;` o tabulador y acepta Windows-1252 (CSV guardado por Excel) |
| JSON | sí | no | `{recurso, area, generado, total, datos[]}` |

### 6.3 Flujo de una importación

```
Pantalla (sigsf-crud.js)                 Backend
1. Elegir .csv / .xlsx (≤700 KB) ──► 2. ArchivoController: decodifica Base64
                                      3. ArchivoService: extensión en lista blanca + firma del archivo
                                      4. LectorCsv / LectorExcel → filas (máx. 2000)
                                      5. Encabezados ↔ columnas importables (sin tildes ni mayúsculas)
                                         columnas desconocidas o calculadas → ignoradas e informadas
                                      6. Por fila: MapeadorEntrada → Servicio del área (mismas reglas)
7. Vista previa (soloValidar=true) ◄─  8. ResultadoImportacionDTO: leídas, válidas, errores por fila
9. «Importar N filas» (soloValidar=false) → se registran; auditoría IMPORTAR + log
```

## 7. Recursos de código por área del negocio

| Área | Vista (`web/`) | Controlador | Servicio(s) | DAO | Exporta | Importa | Log |
|---|---|---|---|---|---|---|---|
| **Marketing** | `Marketing.html` | `MarketingController` | `CampaniaService`, `ProspectoService` | `CampaniaDao`, `ProspectoDao` | prospectos, campañas | prospectos, campañas | `areas/MARKETING.log` |
| **Ventas** | `Ventas.html` | `VentaController` | `VentaService` | `VentaDao` (`VentaReporteDTO`) | ventas (+ `reportes/ventas.xlsx`) | — (dinero) | `areas/VENTAS.log` |
| **Logística** | `Logistica.html` | `LogisticaController` | `ProveedorService` | `ProveedorDao` | proveedores | proveedores | `areas/LOGISTICA.log` |
| **Almacén** | `almacen.html` | `AlmacenController` | `ProductoService` | `ProductoDao` | productos | productos | `areas/ALMACEN.log` |
| **Producción** | `produccion.html` | `ProduccionController` | `ProduccionService` | `LineaProduccionDao`, `OrdenProduccionDao` | líneas, órdenes | líneas | `areas/PRODUCCION.log` |
| **Operaciones** | `operaciones financieras.html` | `OperacionController` | `Operacion/TipoCambio/Prestamo/EmpenoService` | `OperacionDao`, `TipoCambioDao`, `PrestamoDao`, `EmpenoDao` | operaciones, tipos de cambio, préstamos, empeños | — (dinero) | `areas/OPERACIONES.log` |
| **Dirección y Gerencia** | `direccion gerencia.html`, `gerencia general…html` | `DireccionController`, `SistemaController` | `ExpedienteService`, `DashboardService` | `ExpedienteAprobacionDao` | expedientes | expedientes (entran PENDIENTE) | `areas/DIRECCION_GERENCIA.log` |
| **Clientes y servicios** | `clientes y servicios financieros.html` | `ClienteController` | `ClienteService`, `ServicioFinancieroService` | `ClienteDao`, `ServicioFinancieroDao` | clientes, servicios | clientes | `areas/CLIENTES.log` |
| **Seguridad y usuarios** | `usuarios.html`, `seguridad.html`, `respaldos.html` | `Auth/Seguridad/RespaldoController` | `Auth/Usuario/Rol/AuditoriaService`, `RespaldoService` | `UsuarioDao`, `RolDao`, `AuditoriaDao` | auditoría (solo ADMIN) | — | `areas/SEGURIDAD.log`, `sigsf-seguridad.log` |

## 8. Compilar, ejecutar y probar

1. `scripts\descargar_librerias.bat` (una sola vez).
2. NetBeans: **Clean and Build** (Shift+F11) → **Run** (F6). Abrir `http://localhost:8091/`.
3. **Test Project** (Alt+F6): 99 pruebas; no necesitan PostgreSQL.
4. Revisar la carpeta `logs/` después de usar el sistema.
