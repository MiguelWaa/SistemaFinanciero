# SIGSF 5.6.0 - Proyecto integrado

Sistema Integral de Gestión de Servicios Financieros. Curso Integrador I Sistemas - Software, UTP.

Integrantes: Miguel Ángel Machaca Machaca; Ricardo Gabriel Cervantes Tejada; Cesar Huaripaucar Huaman.
Docente: Rene Alonso Nieto Valencia.

## Abrir primero

- `DOCUMENTACION_INTEGRAL.html`: índice y desarrollo de todos los requisitos.
- `docs/presentacion/SIGSF_Integrado_5min.pptx` y `.pdf`: 7 diapositivas para 5 minutos y 12 anexos.
- `INICIAR_DEMO.bat` (Windows) o `sh INICIAR_DEMO.sh` (Linux/macOS): aplicación con datos en memoria.

## Ejecutar la aplicación

Instale Java 17 o superior. Descomprima todo el proyecto y ejecute el iniciador desde esa carpeta.
Abra http://localhost:8091/. Cuenta de demostración: admin@sigsf.com / admin123.
Los datos de la demostración se pierden al cerrar. Para desarrollar, abra el proyecto en NetBeans.
La configuración PostgreSQL usa `db.properties.ejemplo` y `db.properties`; el iniciador demo fuerza memoria sin editarlos.

## Pruebas reproducibles

Requisitos adicionales: JDK completo 17+, Python 3.10+ y Node.js 20+.
Los JAR están incluidos en el ZIP. En un clon sin JAR: `python scripts/preparar_dependencias.py`.

```sh
cd pruebas/e2e
npm ci --no-audit --no-fund
npx playwright install chromium
cd ../..
python scripts/validar_proyecto.py
```

Solo Java: `python scripts/validar_proyecto.py --solo-junit`.
Sin navegador: `python scripts/validar_proyecto.py --omitir-e2e` (queda NO EJECUTADO).
Cada ejecución genera una carpeta fechada en `pruebas/reportes/`, con registros y resultado.json.
Empaquetar después de validar: `python scripts/empaquetar.py`.

## Implementación de seguridad

- JSON completo, claves únicas, números válidos, controles escapados y profundidad máxima de 64 niveles.
- Content-Type application/json, UTF-8 estricto y respuesta 415 para tipos no admitidos.
- CSP con script-src 'self', script-src-attr 'none' y object-src 'none'.
- 26 scripts trasladados a `web/pages/`; ayuda utiliza eventos explícitos.
- 22 pruebas JUnit nuevas y una nueva prueba E2E, junto a las suites recibidas.
- Ejecutores para PostgreSQL, ZAP e inventario/SCA en `scripts/`.

## Resultados de esta entrega

214/214 JUnit; 26 peticiones Newman y 63 aserciones sin fallos; 24/24 comprobaciones adicionales.
Los informes actuales están en `evidencias/v5.6.0/`. Compilación local con Java 21.0.13,
-source 17 -target 17 porque el runtime no dispone del soporte --release 17.
El modo normal y CI requieren JDK completo y usan --release 17.

Pendientes: 14 E2E en Chromium, 7 pruebas JDBC, ZAP y análisis de vulnerabilidades de dependencias.
No se atribuyen los 13 E2E históricos a esta versión. El inventario local de dependencias no calcula CVE.
El análisis npm en línea requiere autorización explícita para enviar el árbol de paquetes al registro.

## Base de datos

Use una BD nueva para instalar. `db/schema.sql` contiene DROP TABLE y reinicializa tablas.
Orden: schema.sql, datos_ejemplo.sql, portal_cliente.sql, seguridad.sql, version_esquema.sql, pagos_qr.sql.
Para pruebas aisladas, configure SIGSF_DB_PASSWORD y ejecute `python scripts/probar_postgresql.py`:
crea una BD nueva sigsf_test_ y conserva el resultado para inspección.
No ejecuta cambios sobre una base existente.

## Versionamiento

La copia conserva el historial, ramas y remoto declarado. Integración local en
feature/integracion-seguridad-5.6.0, merge en release/5.6.0 y etiqueta v5.6.0.
No se publica en GitHub. Para activar los hooks tras clonar: scripts/git/instalar_hooks.sh o .bat.
Los documentos originales se conservan como histórico donde corresponde.
