# SIGSF · Datos de demostración (100 registros por módulo)

**Versión:** 5.1.0 · **Avance:** 3 · **Archivos:** `db/datos_100_registros.sql`, `scripts/datos/generar_datos_100.py`,
`dao/memoria/DatosMasivos.java`, `dao/memoria/semilla_100.tsv`, `test/.../DatosMasivosTest.java`.

## 1. Para qué sirve

Para la sustentación, cada módulo debe mostrar información suficiente (tablas, buscador, KPIs, alertas de stock, bandeja
de solicitudes con pendientes). Con los 3-5 registros de `datos_ejemplo.sql` los indicadores quedaban casi vacíos.

## 2. Qué carga (objetivo por tabla)

| Módulo | Tabla(s) | Registros |
|---|---|---|
| Seguridad | `rol`, `permiso`, `rol_permiso` | 100 + 100 + 100 |
| Usuarios | `usuario` | 100 |
| Clientes y Servicios | `cliente`, `servicio_financiero`, `cliente_servicio` (+ acceso al portal de cada cliente) | 100 + 100 + 100 |
| Operaciones Financieras | `tipo_cambio` · `operacion` · `prestamo` · `empeno` · `articulo` · `comprobante` | 100 · **300** (100 cambios + 100 préstamos + 100 empeños) · 100 · 100 · 100 · 100 |
| Solicitudes de clientes | `operacion` (estado PENDIENTE = bandeja) | ≈ 70 pendientes entre las 300 |
| Ventas | `venta`, `detalle_venta` | 100 + 100 |
| Almacén | `producto`, `inventario`, `movimiento_inventario` | 100 + 100 + 100 (15 en alerta de stock bajo) |
| Logística | `proveedor`, `movimiento_logistica` | 100 + 100 |
| Marketing | `prospecto`, `campania`, `campania_prospecto`, `actividad_marketing` | 100 × 4 |
| Producción | `linea_produccion`, `orden_produccion` | 100 + 100 |
| Dirección / Gerencia | `expediente_aprobacion`, `decision_direccion`, `decision_gerencia`, `resolucion_gerencial`, `reporte_direccion` | 100 × 5 |

> Cada préstamo y cada empeño cuelga de su propia operación (`id_operacion`), por eso la tabla `operacion` llega a 300.

## 3. Reglas de diseño del script

1. **Completa, no duplica.** Cada `INSERT` usa `LIMIT objetivo - filas_actuales`; si la tabla ya tiene 100, no inserta nada. Se puede ejecutar varias veces.
2. **No toca lo existente.** Solo `INSERT`; no cambia el esquema (por eso no sube la versión de `version_esquema`).
3. **Claves foráneas por clave natural**, no por id: `cliente.documento`, `usuario.email`, `producto.codigo`, `proveedor.ruc`, `articulo.numero_serie`…
   Así funciona aunque la base ya tenga datos de prueba propios.
4. **Fechas relativas** a `CURRENT_DATE`/`CURRENT_TIMESTAMP`: los indicadores «de hoy» y «del mes» siempre tienen datos.
5. **Respeta `db/seguridad.sql`**: montos > 0, avance 0-100, estados válidos de usuario, stock ≥ 0.
6. **Solo ASCII**, igual que `datos_ejemplo.sql` (sin problemas de codificación en pgAdmin).
7. **Coherencia de negocio:** interés simple de `PrestamoService` (`total = monto × (1 + tasa/100 × meses)`), cambio con la tasa de venta vigente,
   órdenes TERMINADA con avance 100 %, historial de tipos de cambio con una sola cotización ACTIVA por par.

## 4. Verificación

- Se simuló la carga en una base de pruebas con el esquema y los datos de ejemplo: las 32 tablas llegan a su objetivo, sin violar claves foráneas ni las restricciones de `seguridad.sql`, y una segunda ejecución no cambia ningún conteo.
- `DatosMasivosTest` (9 pruebas) comprueba lo mismo en el modo memoria.
- Comprobación rápida en pgAdmin: `SELECT COUNT(*) FROM cliente;` → 100; `SELECT COUNT(*) FROM operacion;` → 300.

## 5. Modo demostración (sin PostgreSQL)

Con `persistencia=memoria` en `db.properties`, el servidor carga los mismos datos desde `semilla_100.tsv`
(`memoria.datos.masivos=false` los desactiva). Las pruebas automatizadas siguen usando `DatosSemilla` (pocas filas).

## 6. Regenerar

```
python3 scripts/datos/generar_datos_100.py .
```
Usa una semilla fija (20261025): siempre produce los mismos datos. Genera `db/datos_100_registros.sql` y `semilla_100.tsv`.

## 7. Accesos útiles para la demostración

| Quién | Usuario | Clave |
|---|---|---|
| Administrador | `admin@sigsf.com` | `admin123` |
| Cajero / Analista | `lucia.ramos@sigsf.com` / `jorge.torres@sigsf.com` | `lucia123` / `jorge123` |
| Cualquier usuario generado | su correo (ver pantalla Usuarios) | `sigsf123` |
| Cliente del portal | documento `71000001` … `71000100` | el mismo documento |
