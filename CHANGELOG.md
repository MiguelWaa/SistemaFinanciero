# Registro de cambios · SIGSF

Todos los cambios importantes del proyecto se anotan aquí.
Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/)
y numeración [SemVer](https://semver.org/lang/es/): `MAYOR.MENOR.PARCHE`.

- **MAYOR**: cambios que rompen compatibilidad (API, esquema de BD).
- **MENOR**: funcionalidad nueva compatible.
- **PARCHE**: correcciones sin funcionalidad nueva.

## [Sin publicar]

### Cambiado
- Fondo nuevo en todo el sistema: degradado de color, auroras más vivas en movimiento, rejilla de puntos y una trama
  ondulada tipo billete (`web/img/guilloche.svg`) que se desliza lentamente. Tarjetas, indicadores y barras con efecto
  vidrio para que el fondo se vea detrás. Al imprimir, el fondo se quita.

## [5.3.0] - 2026-10-26 · Interfaz animada

### Agregado
- Capa visual animada `web/sigsf-anim.css` + `web/sigsf-anim.js`, cargada en todas las páginas sin cambiar su lógica:
  fondo con auroras suaves, encabezados y barra lateral con degradado en movimiento, indicadores en tarjetas con ícono automático
  y números que cuentan hasta su valor, entrada escalonada de tarjetas, filas y listas, botones con degradado, brillo y onda,
  pestañas con indicador deslizante, gráficos que se dibujan, ticket de pago con línea de escaneo y confeti al confirmar o registrar un pago.
- Banner de bienvenida animado en el panel y en el Portal del Cliente; íconos flotantes en las pantallas de ingreso y bienvenida.
- Campana que suena al llegar un pago nuevo y transición suave al abrir un módulo.
- Todo respeta la opción «reducir movimiento» del sistema operativo; los refrescos automáticos no repiten la animación si los datos no cambian.

## [5.2.0] - 2026-10-26 · Pagos con QR, cotizaciones e interfaz Bootstrap 5

### Agregado
- **Pagos con Yape, Plin y Tunki desde el Portal del Cliente**: elegir trámite, monto y billetera; QR de cobro con monto, número, titular y referencia; registro del número de operación y de la captura de la constancia (`PagoService`, `PortalController`).
- **Módulo interno Pagos y cobranzas** (`web/pagos.html`): bandeja *Por verificar* con confirmar/rechazar, historial con filtros, cobro en caja, cotizaciones y configuración de billeteras con subida del QR.
- Al confirmar un pago se descuenta el saldo del préstamo o empeño (pasa a `CANCELADO` / `RECUPERADO` en cero) y se emite el comprobante `RC-000001`.
- **Cotizaciones** de préstamo (con cronograma), empeño y cambio de moneda, con número, vigencia de 7 días y conversión en solicitud (`CotizacionService`).
- Documentos imprimibles / PDF: comprobante de pago, cotización, estado de cuenta y reporte de pagos (`documento.html`, `portal-documento.html`, `sigsf-documentos.js`).
- Exportación de pagos y cotizaciones a Excel, CSV y JSON; nuevos indicadores en `GET /api/dashboard` (`pagosPorVerificar`, `montoPorVerificar`, `cobradoMes`, `cotizacionesVigentes`).
- Esquema de BD **5.2.0**: `db/pagos_qr.sql` (tablas `billetera`, `pago`, `cotizacion`); el servidor las crea solo si faltan.
- Configuración `pagos.confirmacion.automatica` y `pagos.monto.maximo` en `db.properties`.
- Pruebas: `PagoServiceTest`, `CotizacionServiceTest`, `PagosApiTest` (126 → 151).
- `docs/09_PAGOS_QR_Y_COTIZACIONES.md` y capturas `evidencias/v52_*.png`.

### Cambiado
- **Toda la interfaz migrada a Bootstrap 5.3.3** servido desde `web/vendor/` (también Font Awesome y el generador de QR): funciona sin internet.
- Nuevo tema `sigsf-ui.css` + utilidades `sigsf-ui.js`; el motor `sigsf-crud.js` ahora ordena por columna, pagina, valida y confirma con ventanas modales, manteniendo la misma configuración de cada módulo.
- Panel principal con menú lateral agrupado (adaptable a celular), campana de pagos por verificar, gráfico de cobros de 14 días y apertura directa de módulos (`index.html#pagos.html`).
- Portal del Cliente, ingreso del personal, bienvenida, solicitudes y reportes rediseñados.
- Servidor de archivos: caché de 7 días para `web/vendor`, `ETag`, compresión gzip y más hilos; CSP sin CDN.
- Centro de ayuda: respuestas corregidas según lo que el sistema hace realmente y nuevas preguntas sobre pagos.

### Corregido
- `scripts/compilar_y_probar.*` no copiaba `semilla_100.tsv` al classpath: 9 pruebas fallaban en GitHub Actions.
- Configuración mostraba «PostgreSQL» aun en modo memoria.

### Eliminado
- Hojas de estilo antiguas que ninguna página usaba (`styles*.css`, `almacen.css`, `logistica.css`…); siguen en el historial de Git (`v5.1.0`).

## [5.1.0] - 2026-10-25 · Avance 3: datos de demostración

### Agregado
- `db/datos_100_registros.sql`: completa **100 registros por módulo** (300 operaciones = 100 cambios + 100 préstamos + 100 empeños) en las 32 tablas. Es re-ejecutable (no duplica), no borra datos y resuelve las claves foráneas por clave natural.
- `DatosMasivos` + `semilla_100.tsv`: los mismos datos en el modo demostración (`persistencia=memoria`); se desactiva con `memoria.datos.masivos=false`.
- `scripts/datos/generar_datos_100.py`: generador determinista (semilla fija) que produce el SQL y el TSV.
- `DatosMasivosTest` (9 pruebas): 100 por módulo, 300 operaciones, idempotencia, claves foráneas, avance 100 % y acceso al portal.
- `docs/08_DATOS_DE_DEMOSTRACION.md` y capturas de los módulos con 100 registros en `evidencias/`.

### Cambiado
- Guías de instalación (`db/LEEME.txt`, `INSTALAR_LEEME.txt`) con el paso opcional de los 100 registros. Pruebas: 117 → 126.

## [5.0.1] - 2026-10-21 · Parche

### Corregido
- Nombre de la base inconsistente: README y `db/LEEME.txt` decían `sistema_financiero`; el sistema usa `SistemaFinanciero`.
- Puerto por defecto 8080 en código y README; el sistema usa 8091 (`server.port`).

## [5.0.0] - 2026-10-21 · Taller de control de versiones

Versión MAYOR: cambia el contrato `DaoFactory` (nuevo `versionesEsquema()`), el
esquema de la base (tabla `version_esquema`) y la forma de configurar la conexión
(`db.properties` ya no viene en el repositorio).

### Agregado
- Plantilla `db.properties.ejemplo`; `db.properties` sale del repositorio (credenciales).
- Hooks Git `pre-commit` (bloquea credenciales y archivos generados) y `commit-msg`.
- Servicio de versión (`VersionService`) y API `GET /api/version`, `GET /api/versiones` (solo ADMIN).
- Versionamiento del esquema de la base: tabla `version_esquema` y `db/version_esquema.sql`.
- `version.properties` generado por Ant al compilar (commit, rama, etiqueta).
- Pantalla `versiones.html` (resumen, historial, red de ramas, etiquetas, esquema BD) y versión en el pie del panel.
- Integración continua (GitHub Actions) y `scripts/compilar_y_probar.(sh|bat)`.
- `docs/07_CONTROL_DE_VERSIONES.md`, inventario de documentación v5.0 y `CONTRIBUTING.md`.

### Corregido
- Ramas/etiquetas vacías en la vista de versiones (`for-each-ref` usa `%1f`) y referencias mal detectadas en el grafo.

### Revertido
- Truncamiento de la cuota mensual de préstamos (`RoundingMode.DOWN`): rompía `PrestamoServiceTest` y descuadraba el saldo. Se revirtió el merge con `git revert -m 1`.

## [4.0.0] - Taller de recursos de código fuente

### Agregado
- Registro con SLF4J + Logback (log general, acceso, seguridad, errores y uno por área).
- Exportación Excel/CSV/JSON en 15 recursos e importación CSV/Excel con vista previa.
- Seguridad Java en ficheros: lector XLSX sin XXE ni bomba ZIP, neutralización de fórmulas.
- 99 pruebas JUnit 4 y `docs/06_RECURSOS_DE_CODIGO_FUENTE.md`.

## [3.0.0] - Documentación del proyecto
- Requisitos y estado por área (PDF del proyecto); corrección del nombre oficial SIGSF.

## [2.0.0] - Taller de arquitectura
- Arquitectura MVC + DAO + DTO + SOLID, seguridad (PBKDF2, tokens, roles), respaldos y TDD.

## [1.0.0] - Avance 2
- Requisitos, casos de uso, prototipos UX/UI y modelo de datos (31 tablas).
