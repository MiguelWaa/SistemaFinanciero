# Orden de carga de los scripts SQL · SIGSF

Base de datos: `SistemaFinanciero` (PostgreSQL). Herramienta: pgAdmin > Query Tool.

## Orden obligatorio

| # | Script | Para qué sirve |
|---|---|---|
| 1 | `db/schema.sql` | Crea las tablas |
| 2 | `db/datos_ejemplo.sql` | Datos base de ejemplo |
| 3 | `db/portal_cliente.sql` | Acceso de clientes al portal |
| 4 | `db/seguridad.sql` | Roles, permisos y usuarios |
| 5 | `db/version_esquema.sql` | Tabla de versión del esquema |
| 6 | `db/datos_100_registros.sql` | Completa 100 registros por módulo |

El script 6 puede ejecutarse varias veces: si la tabla ya tiene 100 filas, no inserta nada.

## Comprobación rápida

```sql
SELECT COUNT(*) FROM cliente;    -- 100
SELECT COUNT(*) FROM operacion;  -- 300
```

## Accesos de prueba

* Usuarios generados: su correo + clave `sigsf123`.
* Portal de clientes: documento `71000001` a `71000100` (la clave inicial es el documento).

## Sin PostgreSQL

En `db.properties` poner `persistencia=memoria`. Los mismos datos se cargan desde `semilla_100.tsv`.
