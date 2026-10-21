# SIGSF · Consideraciones de seguridad

Referencia: OWASP Top 10 (2021) y buenas prácticas de PostgreSQL. Cada control indica **dónde está implementado** y **cómo se verifica**.

## 1. Controles transversales (todo el sistema)

| # | Riesgo (OWASP) | Control implementado | Dónde | Verificación |
|---|---|---|---|---|
| S-01 | A01 Control de acceso roto: API sin sesión | Toda ruta `/api` exige token; solo `health`, `auth/login`, `portal/login` y `portal/registro` son públicas | `security/FiltroSeguridad`, `PoliticaAcceso` | `ApiIntegracionTest.sinTokenLaApiResponde401` |
| S-02 | A01 Permisos por rol | Matriz módulo × rol (L / LE). ADMIN todo; módulos administrativos solo ADMIN; roles nuevos solo lectura | `PoliticaAcceso` · pantalla Seguridad | `PoliticaAccesoTest`, captura 18 |
| S-03 | A01 IDOR en el portal | Un cliente solo accede a `/api/portal/{suId}/…` | `PoliticaAcceso.permite` | `clienteSoloAccedeASusPropiosTramites` |
| S-04 | A02 Fallas criptográficas | Contraseñas con **PBKDF2-HMAC-SHA256**, sal aleatoria de 16 bytes, 120 000 iteraciones, comparación en tiempo constante. Migración automática de SHA-256 y texto plano | `Pbkdf2PasswordHasher`, `AuthService`, `PortalService`, `db/seguridad.sql` | `Pbkdf2PasswordHasherTest`, `migraHashAntiguo…` |
| S-05 | A07 Fallas de autenticación | Bloqueo tras 5 intentos fallidos (5 min); mensaje genérico (no revela si el correo existe); tiempo de respuesta igualado; solo cuentas ACTIVAS | `LimitadorIntentos`, `AuthService` | `AuthServiceTest`, `LimitadorIntentosTest` |
| S-06 | A07 Gestión de sesiones | Token aleatorio de 256 bits (`SecureRandom`), guardado en el servidor; expira tras 30 min de inactividad; cierre de sesión revoca el token; `sessionStorage` (se borra al cerrar la pestaña) | `GestorSesiones`, `api.js` | `GestorSesionesTest`, `logoutInvalidaElToken` |
| S-07 | A07 Política de contraseñas | Personal: mínimo 8 caracteres con letras y números. Portal: mínimo 6. Usuario nuevo queda PENDIENTE | `UsuarioService`, `PortalService` | `aplicaPoliticaDeContrasenas` |
| S-08 | A03 Inyección SQL | 100 % de consultas con `PreparedStatement` y parámetros `?`; el respaldo JDBC escapa comillas | `util/Db`, `dao/jdbc/*`, `RespaldoJdbcSql.literal` | `literalSqlEscapaComillas` |
| S-09 | A03 XSS | El frontend escapa todo dato antes de pintarlo (`SIGSF.escapar`); CSP restringe orígenes de scripts | `sigsf-crud.js`, `ApiServer` | Cabecera `Content-Security-Policy` |
| S-10 | A04 Diseño inseguro: datos manipulados | El servidor recalcula totales, cuota, saldo y vencimiento; ignora `totalRecibir`, `tipoCambio` y la tasa del portal enviados por el navegador (protección *mass assignment*) | `OperacionService`, `MapeadorEntrada`, `PortalController` | `ignoraTotalesYCotizacionManipulados…` |
| S-11 | A05 Configuración insegura | Cabeceras `X-Content-Type-Options`, `X-Frame-Options`, `Referrer-Policy`, `Permissions-Policy`, CSP; CORS cerrado (antes `*`); `Cache-Control: no-store` en la API; límite de 1 MB por petición; carpeta `_respaldo_diseno_anterior` no se sirve | `ApiServer`, `Ctx` | `respuestasIncluyenCabecerasDeSeguridad`, captura 23 |
| S-12 | A05 Fuga de información | Errores 500 muestran solo un código de referencia; el SQL y la traza van a la consola. Duplicados → 409 con mensaje claro | `ApiServer.responderError` | — |
| S-13 | A01 Path traversal | Rutas de archivos normalizadas; nombres de respaldo validados con expresión regular | `ApiServer.servirArchivo`, `RespaldoService.resolverSeguro` | `impidePathTraversalAlDescargar` |
| S-14 | A09 Registro y monitoreo | Bitácora de LOGIN_OK/FALLIDO, CREAR, ACTUALIZAR, ELIMINAR, EXPORTAR, RESPALDO, ACCESO_DENEGADO con usuario, rol, IP y código HTTP. Inmutable (disparador). Nunca guarda contraseñas | `AuditoriaService`, tabla `auditoria` | captura 05, `adminGeneraRespaldoYQuedaEnLaAuditoria` |
| S-15 | A08 Integridad de datos | FK, UNIQUE (correo, documento), CHECK de montos/plazos/stock/avance/estados | `db/schema.sql`, `db/seguridad.sql` | — |
| S-16 | Disponibilidad | Respaldo manual y automático (24 h), retención de 10 archivos, scripts de restauración | `backup/*`, `scripts/*.bat` | `RespaldoServiceTest`, captura 04 |
| S-17 | Mínimo privilegio en BD | Rol `sigsf_app` sin superusuario, sin crear objetos, sin modificar la auditoría; contraseña configurable por variable de entorno `SIGSF_DB_PASSWORD` | `db/seguridad.sql`, `Configuracion` | — |

### 1.1 Controles nuevos (versión 4.0: ficheros y registro)

| # | Riesgo (OWASP / CWE) | Control implementado | Dónde | Verificación |
|---|---|---|---|---|
| S-18 | A03 Inyección de fórmulas en CSV (CWE-1236) | Textos que empiezan con `= + - @` tab o CR se exportan con apóstrofo; los números con signo no se alteran. En XLSX todo texto es *inline string* (nunca fórmula) | `ProteccionFormulas`, `EscritorCsv`, `ExcelExporter` | `EscrituraArchivosTest.neutralizaFormulas…` |
| S-19 | A05 XXE en archivos subidos (CWE-611) | El XML interno del .xlsx se lee con StAX sin DTD ni entidades externas; un `<!DOCTYPE>` se rechaza | `LectorExcel.fabricaSegura` | `excelRechazaArchivoQueNoEsZipYXmlConDoctype` |
| S-20 | A05 Bomba ZIP / DoS (CWE-409) | Máx. 500 entradas, 10 MB por parte y 30 MB descomprimidos; solo se leen los XML necesarios; máx. 2000 filas y 500 caracteres por celda; petición ≤ 1 MB | `LectorExcel`, `LectorCsv`, `ArchivoService`, `Ctx.body` | `rechazaBinariosComillasSinCerrarYExcesoDeFilas` |
| S-21 | A04 Carga de archivos sin restricción (CWE-434) | Lista blanca de extensiones (.csv, .txt, .xlsx) + firma del contenido (`PK\3\4` para xlsx, sin bytes nulos para csv). El archivo nunca se guarda en disco ni se ejecuta | `FormatoArchivo`, `LectorCsv.decodificar` | `rechazaArchivoSinColumnasObligatoriasOExtensionNoPermitida` |
| S-22 | A04 *Mass assignment* por importación | Solo columnas marcadas como importables; id, fechas de registro, totales y estados de aprobación se ignoran y se informan; cada fila pasa por `MapeadorEntrada` + servicio | `CatalogoArchivos`, `ArchivoService` | `ignoraColumnasCalculadasPorElServidor` |
| S-23 | A04 Fraude por carga masiva de dinero | Operaciones, ventas, préstamos, empeños y tipos de cambio **no tienen ruta de importación** (404) | `CatalogoArchivos` (`soloExportar`) | `lasOperacionesFinancierasNoSeImportan`, `operacionesNoTienenRutaDeImportacion` |
| S-24 | A01 Permisos sobre ficheros | Exportar exige lectura del módulo; importar exige escritura; la auditoría solo la exporta ADMIN | `FiltroSeguridad`, `PoliticaAcceso` | `cajeroNoPuedeImportarProveedoresNiExportarAuditoria` |
| S-25 | A09 Trazabilidad de ficheros | Acciones IMPORTAR y EXPORTAR en la tabla `auditoria` y en `logs/sigsf-seguridad.log` | `AuditoriaService` | `adminImportaProspectosYQuedaEnLaAuditoria` |
| S-26 | A09 Inyección en logs (CWE-117) y fuga en logs (CWE-532) | `%replace` quita saltos de línea; nunca se registran contraseñas, tokens ni cuerpos; errores 500 con código de referencia | `src/logback.xml`, `ApiServer` | `losPatronesEliminanSaltosDeLineaContraLogInjection` |

### 1.2 Recursos de seguridad de Java usados

| API de Java | Uso en SIGSF | Clase |
|---|---|---|
| `javax.crypto.SecretKeyFactory` (`PBKDF2WithHmacSHA256`) + `PBEKeySpec` | Hash de contraseñas con sal y 120 000 iteraciones; se limpia la clave (`clearPassword`) | `Pbkdf2PasswordHasher` |
| `java.security.SecureRandom` | Sal de 16 bytes y token de sesión de 256 bits | `Pbkdf2PasswordHasher`, `GestorSesiones` |
| `java.security.MessageDigest.isEqual` | Comparación en tiempo constante (evita ataques de tiempo) | `Pbkdf2PasswordHasher` |
| `java.sql.PreparedStatement` | 100 % de consultas parametrizadas (anti inyección SQL) | `util/Db`, `dao/jdbc/*` |
| `java.math.BigDecimal` + `RoundingMode` | Dinero sin errores de redondeo; el servidor recalcula totales | `OperacionService`, `PrestamoService`, `EmpenoService` |
| `java.nio.file.Path.normalize` + `startsWith` | Contra *path traversal* en archivos estáticos y respaldos | `ApiServer`, `RespaldoService` |
| `javax.xml.stream.XMLInputFactory` (sin DTD) | Lectura de .xlsx sin XXE | `LectorExcel` |
| `java.util.zip.ZipInputStream` con límites | Contra bombas ZIP | `LectorExcel` |
| `java.nio.charset.CharsetDecoder` (`REPORT`) | Decodificación UTF-8 estricta de archivos subidos | `LectorCsv` |
| `ConcurrentHashMap` + `Instant` | Sesiones y limitador de intentos seguros entre hilos | `GestorSesiones`, `LimitadorIntentos` |
| Excepciones de dominio (`ReglaNegocioException`, `NoAutenticadoException`, `AccesoDenegadoException`) | Respuestas 400/401/403 sin exponer detalles internos | `ApiServer.responderError` |
| SLF4J + Logback (MDC, `AsyncAppender`, rotación) | Registro y monitoreo seguros | `src/logback.xml` |

## 2. Consideraciones por área del negocio

| Área | Activos sensibles | Amenazas principales | Controles en SIGSF |
|---|---|---|---|
| **Marketing** | Datos personales de prospectos (DNI, teléfono, correo) | Fuga de bases de prospectos, uso sin consentimiento | Solo ANALISTA/SUPERVISOR/ADMIN escriben; exportaciones auditadas; Ley N.° 29733 (Protección de Datos Personales): registrar el origen y el consentimiento del prospecto |
| **Ventas** | Importes, historial por cliente | Alteración o borrado de ventas | Ventas no editables; fecha y hora las asigna el servidor; importe ≥ 0 (servicio + CHECK); cada eliminación queda en la auditoría |
| **Logística** | RUC y contactos de proveedores | Proveedores ficticios | Solo ANALISTA/ADMIN registran; razón social obligatoria; auditoría de altas y bajas |
| **Almacén** | Stock de insumos y bienes en garantía | Ajustes de stock no autorizados | Stock ≥ 0 (CHECK); edición restringida por rol; alerta de stock bajo; exportación auditada |
| **Producción** | Órdenes y avance | Cierre fraudulento de órdenes | Avance entre 0 y 100; TERMINADA y fecha real solo las fija el servidor; producto y línea deben existir |
| **Operaciones** | Dinero: cambios, préstamos, empeños | Manipular cotización, totales o tasa; fraude interno | Cálculo 100 % en servidor con `BigDecimal`; tipo de cambio leído de la BD; estados válidos; segregación: CAJERO registra, SUPERVISOR/ADMIN aprueba; auditoría |
| **Dirección y Gerencia** | Expedientes y decisiones | Aprobaciones no autorizadas | Solo SUPERVISOR/ANALISTA/ADMIN resuelven; fecha de resolución automática; estados controlados; trazabilidad |
| **Clientes y servicios** | Datos personales y credenciales del portal | Robo de cuentas, enumeración de documentos, IDOR | Credenciales en tabla separada (`cliente_acceso`) con PBKDF2; mensaje genérico de error; bloqueo por intentos; un cliente solo ve lo suyo; ADMIN puede bloquear cuentas |
| **Seguridad y usuarios** | Cuentas del personal | Escalamiento de privilegios | Solo ADMIN gestiona usuarios y respaldos; usuario nuevo PENDIENTE; la API nunca devuelve hashes (`UsuarioDTO`) |

### 2.1 Ficheros por área (importación y exportación)

| Área | Exportación | Importación | Control específico |
|---|---|---|---|
| Marketing | prospectos, campañas | sí | Datos personales (Ley 29733): exportar solo ANALISTA/SUPERVISOR/ADMIN, queda auditado |
| Ventas | ventas | **no** | Importes solo los registra el sistema |
| Logística | proveedores | sí (ANALISTA, ADMIN) | Razón social obligatoria; CAJERO recibe 403 |
| Almacén | productos | sí | Stock entero ≥ 0 validado por servicio y CHECK |
| Producción | líneas, órdenes | solo líneas | Órdenes y su avance se gestionan en el sistema |
| Operaciones | operaciones, tipos de cambio, préstamos, empeños | **no** | Totales, cuotas y cotización siempre del servidor |
| Dirección y Gerencia | expedientes | sí | El estado importado se ignora: entran PENDIENTE |
| Clientes | clientes, servicios | solo clientes | Documento único; duplicados rechazados por fila |
| Seguridad | auditoría | **no** | Solo ADMIN; usuarios y contraseñas nunca se exportan |

## 3. Riesgos residuales y recomendaciones para producción

1. **HTTPS obligatorio**: publicar detrás de un proxy (Nginx/IIS) con certificado TLS. Hoy el servidor escucha en HTTP local.
2. Cambiar la contraseña por defecto del rol `sigsf_app` y no usar `postgres` en `db.properties`.
3. Forzar el cambio de la clave temporal `cambiar123` en el primer ingreso.
4. Copiar los respaldos fuera del servidor (nube o disco externo) y cifrarlos.
5. Hacer un simulacro de restauración trimestral (ver `04_RESPALDO_Y_CONFIGURACION_BD.md`).
6. Considerar doble factor (2FA) para ADMIN.
7. Enviar `logs/sigsf-seguridad.log` a un servidor de monitoreo (SIEM) y proteger la carpeta `logs/` con permisos del sistema operativo.
8. Analizar con antivirus los archivos subidos si en el futuro se guardan en disco (hoy solo se procesan en memoria).
