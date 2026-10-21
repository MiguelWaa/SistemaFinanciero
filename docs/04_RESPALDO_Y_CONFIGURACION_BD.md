# SIGSF · Respaldo (backup) y configuración de la base de datos

## 1. Configuración (`db.properties`)

| Clave | Valor por defecto | Descripción |
|---|---|---|
| `persistencia` | `postgresql` | `postgresql` = base real · `memoria` = demostración sin BD |
| `db.host` / `db.port` / `db.name` | localhost / 5432 / SistemaFinanciero | Conexión |
| `db.user` / `db.password` | postgres / postgres | Recomendado: `sigsf_app` (ver §3). También por variable `SIGSF_DB_PASSWORD` |
| `server.port` | 8091 | Puerto web |
| `seguridad.sesion.minutos` | 30 | Expiración por inactividad |
| `seguridad.intentos.max` / `seguridad.bloqueo.minutos` | 5 / 5 | Fuerza bruta |
| `seguridad.pbkdf2.iteraciones` | 120000 | Costo del cifrado |
| `backup.dir` | respaldos | Carpeta de respaldos |
| `backup.pgdump` | auto | Ruta de `pg_dump.exe` (auto = PATH y `C:\Program Files\PostgreSQL\*\bin`) |
| `backup.retener` | 10 | Cantidad de respaldos que se conservan |
| `backup.automatico.horas` | 24 | Frecuencia del respaldo automático (0 = desactivado) |

## 2. Orden de scripts en pgAdmin (Query Tool sobre `SistemaFinanciero`)

1. `db/schema.sql` — 32 tablas (incluye `auditoria`).
2. `db/datos_ejemplo.sql` — datos de prueba.
3. `db/portal_cliente.sql` — accesos del Portal.
4. `db/seguridad.sql` — auditoría inmutable, CHECK, índices, migración de claves en texto plano y rol `sigsf_app`.
5. `db/version_esquema.sql` — **v5.0**: historial de versiones del esquema (si no se ejecuta, el sistema crea la tabla al arrancar).

> Configuración: desde la v5.0 `db.properties` **no se versiona**. Copie `db.properties.ejemplo` a `db.properties` y edítelo; si no existe, el sistema usa los valores del ejemplo.

## 3. Rol de mínimo privilegio

`db/seguridad.sql` crea `sigsf_app` (sin superusuario) con `SELECT/INSERT/UPDATE/DELETE` sobre las tablas y sin permiso para modificar la auditoría. Para usarlo:

```properties
db.user=sigsf_app
db.password=<nueva contraseña>
```
```sql
ALTER ROLE sigsf_app PASSWORD '<nueva contraseña>';
```

## 4. Respaldo

| Forma | Cómo | Resultado |
|---|---|---|
| Desde el sistema | Menú **Respaldos y auditoría → Generar respaldo ahora** (solo ADMIN) | `respaldos/sigsf_AAAAMMDD_HHMMSSmmm_pgdump.sql` (o `_jdbc.sql`) |
| Automático | Lo hace el servidor cada `backup.automatico.horas` | Igual; conserva los últimos 10 |
| Manual (Windows) | `scripts\backup_bd.bat` | `respaldos\sigsf_manual_….sql` |
| Programado (Windows) | `scripts\programar_backup_windows.bat` (como administrador) | Tarea diaria 23:00 |
| Linux / macOS | `scripts/backup_bd.sh` + cron | — |

**Estrategias (Strategy):** 1) `pg_dump` → estructura + datos; 2) si no existe pg_dump, exportación JDBC → solo datos (INSERT) + secuencias; 3) en modo memoria → JSON.

## 5. Restauración

```bat
scripts\restaurar_bd.bat respaldos\sigsf_20261010_230000000_pgdump.sql
```
- Respaldo **pgdump**: restaura estructura y datos.
- Respaldo **jdbc**: ejecutar antes `db/schema.sql`.
- También se puede abrir el `.sql` en el Query Tool de pgAdmin y ejecutarlo (F5).

## 6. Política recomendada

| Indicador | Meta |
|---|---|
| RPO (pérdida máxima de datos) | 24 h (respaldo diario) |
| RTO (tiempo de recuperación) | < 1 h |
| Copias fuera del servidor | Semanal |
| Prueba de restauración | Trimestral, en una base de prueba |
