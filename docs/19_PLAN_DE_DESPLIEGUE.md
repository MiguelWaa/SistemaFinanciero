# 19 · Plan de Despliegue de Aplicación · SIGSF 5.11.0

Curso Integrador I Sistemas - Software (UTP) · Taller de Despliegue
Integrantes: Miguel Ángel Machaca Machaca; Ricardo Gabriel Cervantes Tejada; Cesar Huaripaucar Huaman.
Docente: Rene Alonso Nieto Valencia.

Diagrama: [modelos/Despliegue.mmd](modelos/Despliegue.mmd) · Evidencias: [../evidencias/v5.11.0/](../evidencias/v5.11.0/)

---

## 1. Fundamentos

| Aspecto | Decisión en SIGSF |
|---|---|
| Objetivo | Llevar cada versión aprobada a un servidor de forma repetible, verificable y reversible, sin pasos manuales propensos a error. |
| Artefacto | Un único JAR ejecutable (`dist/SIGSF.jar`, Java 17) con servidor HTTP propio (`jdk.httpserver`), frontend estático en `web/` y PostgreSQL 16. |
| Principios | Construir una vez y promover el mismo artefacto; configuración por entorno (variables `SIGSF_*`, nunca contraseñas en Git ni en la imagen); versiones inmutables; verificación automática después de desplegar; reversión en segundos. |
| Versionado | SemVer `MAYOR.MENOR.PARCHE`; archivo `VERSION`; etiqueta Git `vX.Y.Z`; `version.properties` dentro del JAR con commit, rama y etiqueta. |
| Estrategia | **Despliegue por versiones con enlace simbólico** (`/opt/sigsf/releases/<versión-fecha>` + `current`): el cambio de versión es atómico (`mv -T`) y la anterior queda lista para volver. En contenedores, **imagen inmutable** por versión en GHCR. |

### Entornos

| Entorno | Persistencia | Cómo se despliega | Propósito |
|---|---|---|---|
| Desarrollo | Memoria o PostgreSQL local | NetBeans / `INICIAR_DEMO.*` | Programar y depurar |
| Integración (CI) | Memoria + PostgreSQL de servicio | GitHub Actions `ci.yml`, `plan-pruebas.yml` | 229 JUnit, API, E2E, 7 JDBC |
| Staging | Memoria (demostración) | `scripts/desplegar.sh` o `pipeline_local.py` | Ensayo del despliegue y de la reversión |
| Producción | PostgreSQL 16 | `cd.yml` → SSH + `desplegar.sh --systemd`, o `docker compose` | Uso real detrás de Nginx HTTPS |

## 2. Arquitectura de despliegue

```
Usuario ──HTTPS──> Nginx :443 (TLS, HSTS) ──> SIGSF.jar :8091 (systemd o contenedor, usuario no root)
                                                 ├── PostgreSQL 16 :5432 (rol sigsf_app, privilegios mínimos)
                                                 └── /opt/sigsf/shared/{logs, respaldos, sigsf.env}
```

## 3. Empaquetado

| Paso | Comando | Resultado |
|---|---|---|
| Compilar + pruebas | `python scripts/validar_proyecto.py --solo-junit` | Clases en `build/integrado`, 229 JUnit |
| JAR | `python scripts/empaquetar.py` | `dist/SIGSF.jar` con `Main-Class`, `Class-Path ../lib/*.jar` y `version.properties` |
| Paquete de despliegue | `python scripts/empaquetar_release.py` | `release/sigsf-5.11.0.tar.gz` y `.zip` (113 archivos, 5,4 MB), `.sha256`, `SHA256SUMS`, `MANIFIESTO.json` (SHA-256 de cada archivo, commit, etiqueta) |
| Imagen | `docker build -t sigsf:5.11.0 .` | Imagen de 133 MB, usuario 10001, `HEALTHCHECK` con la sonda Java `SondaSalud` |

El paquete **solo** contiene lo necesario para ejecutar: `dist/`, `lib/*.jar` (sin `lib/test`), `web/`, `db/`, `deploy/`, scripts de despliegue, `VERSION`, `CHANGELOG.md`, plantilla de configuración y esta guía. Nunca incluye fuentes de prueba, `db.properties`, logs ni respaldos.

## 4. Configuración

`Configuracion.get(clave)` lee en orden: variable de entorno `SIGSF_<CLAVE>` → `db.properties` → `db.properties.ejemplo`. Así un mismo artefacto sirve en todos los entornos.

| Variable | Producción | Notas |
|---|---|---|
| `SIGSF_PERSISTENCIA` | `postgresql` | `memoria` solo para demostración |
| `SIGSF_DB_HOST/PORT/NAME/USER` | `127.0.0.1/5432/SistemaFinanciero/sigsf_app` | rol de `db/seguridad.sql` |
| `SIGSF_DB_PASSWORD` | secreto | en `/etc/sigsf/sigsf.env` (chmod 600) o secreto de GitHub; nunca en Git |
| `SIGSF_SERVER_PORT` | `8091` | solo interno; Nginx publica 443 |
| `SIGSF_SESION_MINUTOS` | `30` | expiración de sesión |
| `SIGSF_BACKUP_AUTOMATICO_HORAS` | `24` | respaldo `pg_dump` programado |
| `-Dsigsf.log.dir` | `/opt/sigsf/shared/logs` | Logback: `sigsf.log`, acceso, seguridad, errores, por área |

Archivos: `deploy/sigsf.env.ejemplo` (plantilla), `deploy/sigsf.service` (systemd endurecido: `NoNewPrivileges`, `ProtectSystem=strict`, `ReadWritePaths` solo `shared/`), `deploy/nginx-sigsf.conf` (TLS 1.2/1.3, HSTS, rangos para video), `docker-compose.yml` (app + PostgreSQL, `read_only`, `cap_drop: ALL`, `no-new-privileges`).

## 5. Procedimientos de despliegue

### A. Contenedores (recomendado)
```sh
cp deploy/sigsf.env.ejemplo deploy/sigsf.env      # cambiar SIGSF_DB_PASSWORD
docker compose --env-file deploy/sigsf.env up -d  # PostgreSQL se inicializa con db/*.sql en orden
docker compose ps                                 # app y db en estado healthy
```

### B. Servidor Linux con systemd
```sh
sudo useradd -r -s /usr/sbin/nologin sigsf && sudo mkdir -p /etc/sigsf
sudo cp deploy/sigsf.env.ejemplo /etc/sigsf/sigsf.env && sudo chmod 600 /etc/sigsf/sigsf.env
sudo cp deploy/sigsf.service /etc/systemd/system/
sudo sh scripts/desplegar.sh release/sigsf-5.11.0.tar.gz --destino /opt/sigsf --env /etc/sigsf/sigsf.env --systemd
```

### C. Windows (demostración)
`INICIAR_DEMO.bat` (Java 17 + Chrome, datos en memoria). Para un servidor Windows se recomienda Docker Desktop con el mismo `docker-compose.yml`.

### D. Base de datos
Base nueva: `schema.sql → datos_ejemplo.sql → portal_cliente.sql → seguridad.sql → version_esquema.sql → pagos_qr.sql`. Al arrancar, `VerificadorEsquema` compara la versión del esquema (5.2.0) con la esperada por el código. Antes de cada despliegue a producción: `scripts/backup_bd.sh`.

### Pasos que ejecuta `scripts/desplegar.sh`
1. Verifica SHA-256 del paquete (si no coincide, aborta con código 3).
2. Extrae en `releases/<versión>-<fecha>` (nunca sobrescribe).
3. Enlaza `logs/` y `respaldos/` a `shared/`.
4. Detiene la versión activa.
5. Cambia `current` de forma atómica.
6. Arranca (proceso o `systemctl start sigsf`).
7. Ejecuta `scripts/smoke_test.py`.
8. Si la prueba de humo falla, vuelve a la versión anterior y la verifica. Conserva las últimas 5 versiones.

## 6. Automatización CI/CD

| Workflow | Disparador | Etapas |
|---|---|---|
| `ci.yml` | push / PR a `main`, `develop` | JDK 17 → 229 JUnit. PR en rojo no se fusiona. |
| `plan-pruebas.yml` | push / PR / manual | JUnit + Newman (63 aserciones) + Playwright (14 E2E) + PostgreSQL 16 de servicio con 7 JDBC; reportes como artefacto. |
| **`cd.yml` (nuevo)** | etiqueta `v*.*.*` o manual | **1** compilar, JUnit, empaquetar, smoke del JAR → **2** imagen Docker, smoke + healthcheck, push a `ghcr.io/miguelwaa/sigsf:<versión>` y `latest` → **3** GitHub Release con `.tar.gz`, `.zip`, `SHA256SUMS`, `MANIFIESTO.json` → **4** despliegue por SSH con `desplegar.sh --systemd` en el entorno `produccion` (aprobación manual; se omite si faltan secretos). |

Controles del CD: la etiqueta debe coincidir con `VERSION`; `concurrency` evita dos despliegues a la vez; permisos mínimos por job (`packages: write` solo para la imagen y `contents: write` solo para el Release); secretos `SIGSF_SSH_HOST`, `SIGSF_SSH_USER`, `SIGSF_SSH_KEY`.

`scripts/pipeline_local.py` ejecuta **las mismas etapas en el equipo local** para ensayar antes de etiquetar y para dejar evidencia cuando no hay acceso a GitHub.

Flujo de ramas: `feature/despliegue-5.11.0` → merge `--no-ff` en `release/5.11.0` → etiqueta `v5.11.0` → `git push origin v5.11.0` dispara el CD.

## 7. Verificación posterior y monitoreo

`scripts/smoke_test.py` (biblioteca estándar de Python) comprueba 9 puntos y guarda JSON:

1. `GET /api/health` = 200 y `status: ok` (con reintentos durante el arranque).
2. `/login.html` servido.
3. CSP con `script-src 'self'`.
4. `X-Content-Type-Options: nosniff`.
5. `/api/usuarios` sin token = 401.
6. `text/plain` = 415.
7. Inicio de sesión con token.
8. `/api/version` igual a la versión del paquete.
9. Dashboard autenticado.

Monitoreo: `HEALTHCHECK` Docker y `ExecStartPost` de systemd usan `SondaSalud` (Java, sin curl, solo URL locales). Logs rotados por Logback en `shared/logs`, con `historial_despliegues.log` de cada operación.

## 8. Reversión (rollback)

| Caso | Acción |
|---|---|
| Prueba de humo fallida | Automática en `desplegar.sh`: vuelve al enlace anterior y re-verifica. |
| Error detectado después | `sh scripts/rollback.sh --destino /opt/sigsf [--a releases/<id>] [--login U:C]` |
| Contenedores | `SIGSF_VERSION=5.10.0 docker compose up -d` (imagen anterior en GHCR) |
| Código | `git revert <commit>` en una rama `hotfix/*` y nueva etiqueta de parche |
| Base de datos | `scripts/restaurar_bd.bat` / `pg_restore` del respaldo previo |

## 9. Herramientas y codificación propuesta

| Herramienta | Uso | Archivo |
|---|---|---|
| JDK 17 (`javac`, `jar`, `jlink`) | Compilar, empaquetar, JRE mínima | `scripts/empaquetar.py`, `crear_imagen_base_local.sh` |
| Python 3 | Orquestar pipeline, paquete y smoke | `empaquetar_release.py`, `pipeline_local.py`, `smoke_test.py` |
| Docker / Docker Compose | Imagen y orquestación app + BD | `Dockerfile`, `.dockerignore`, `docker-compose.yml` |
| GitHub Actions | CI y CD | `.github/workflows/ci.yml`, `plan-pruebas.yml`, `cd.yml` |
| GHCR | Registro de imágenes | `ghcr.io/miguelwaa/sigsf` |
| systemd | Servicio en Linux | `deploy/sigsf.service` |
| Nginx + Let's Encrypt | HTTPS | `deploy/nginx-sigsf.conf` |
| PostgreSQL 16 | Persistencia | `db/*.sql` |
| Git | Ramas, merge, etiquetas, reversión | `feature/despliegue-5.11.0`, `release/5.11.0`, `v5.11.0` |
| Playwright (Python) + Chrome | Capturas de la app desplegada | `scripts/capturar_evidencias.py` |
| JUnit 4 | Pruebas de la sonda de salud | `test/.../despliegue/SondaSaludTest.java` |

## 10. Resultados obtenidos (18-11-2026, Linux, JDK 21 con `--release 17`)

| Prueba | Resultado | Evidencia |
|---|---|---|
| Compilación + JUnit | **229/229 aprobadas** (224 anteriores + 5 de `SondaSalud`) | `evidencias/v5.11.0/pipeline/01_compilar_junit.txt` |
| PostgreSQL 16.15 real + 7 JDBC | **7/7 aprobadas** (antes pendiente) | `pipeline/02_postgresql_jdbc.txt`, `pruebas_postgresql/` |
| Paquete de despliegue | 113 archivos, 5,4 MB, SHA-256 por archivo | `release/MANIFIESTO.json`, `pipeline/04_empaquetar_release.txt` |
| Imagen Docker | Construida, 133 MB, usuario 10001, raíz de solo lectura, `cap_drop ALL` | `pipeline/05_docker_build.txt`, `docker_inspect.json` |
| Smoke del contenedor (memoria) | **9/9** · healthcheck **healthy** | `pipeline/smoke_docker.json` |
| Contenedor + PostgreSQL | **9/9** · "Conexión a PostgreSQL correcta", esquema 5.2.0 | `docker_postgresql/` |
| Despliegue staging 5.10.0 → 5.11.0 | **9/9** en cada despliegue | `despliegue/01…02`, `historial_despliegues.log` |
| Paquete defectuoso 5.11.1 | Smoke 7/9 → **reversión automática** a 5.11.0 (6/6) | `despliegue/03_despliegue_fallido_5.11.1_reversion_automatica.txt` |
| Paquete alterado | SHA-256 no coincide → **rechazado** (código 3), sin tocar la versión activa | `despliegue/04_paquete_alterado_rechazado.txt` |
| Rollback manual a 5.10.0 y regreso | **9/9** en ambos sentidos | `despliegue/05…06` |
| Validación de configuración | 4 YAML válidos, `docker compose config` correcto, `systemd-analyze verify` sin errores, `sh -n` correcto | `configuracion/` |
| Capturas de la app desplegada | 5.10.0, 5.11.0, rollback y contenedor con PostgreSQL | `capturas/` |

**Hallazgo corregido durante las pruebas:** con PostgreSQL y volumen de logs, Logback registraba `Permission denied` en `/app/logs` porque Docker creaba la carpeta como root. Se corrigió creando `/app/logs` y `/app/respaldos` con dueño 10001 antes de `VOLUME`. Se añadió la etapa `docker_logs_escritura` al pipeline. Evidencia: `docker_postgresql/HALLAZGO_logs_permiso_denegado_antes_de_corregir.txt`.

**Limitaciones declaradas:**
- Sin Internet en el entorno de ensayo: la imagen se construyó con el mismo `Dockerfile` sobre una base local generada con `jlink` (JRE 21; el bytecode es Java 17). En CI se usa `eclipse-temurin:17-jre-jammy`.
- Por esa misma razón no se ejecutaron en GitHub los workflows (`cd.yml` se validó y sus etapas se ejecutaron con `pipeline_local.py`), ni Newman/Playwright JS (sin `npm ci`), ni el push a GHCR.
- Nginx/TLS y systemd real quedan configurados y verificados sintácticamente, no instalados.

## 11. Rutas compartidas

| Recurso | Ruta |
|---|---|
| Repositorio | https://github.com/MiguelWaa/SistemaFinanciero |
| Ejecuciones CI/CD | https://github.com/MiguelWaa/SistemaFinanciero/actions |
| Workflow CD | https://github.com/MiguelWaa/SistemaFinanciero/blob/release/5.11.0/.github/workflows/cd.yml |
| Releases (paquetes + SHA-256) | https://github.com/MiguelWaa/SistemaFinanciero/releases/tag/v5.11.0 |
| Imagen de contenedor | `ghcr.io/miguelwaa/sigsf:5.11.0` · https://github.com/MiguelWaa/SistemaFinanciero/pkgs/container/sigsf |
| Red de ramas | https://github.com/MiguelWaa/SistemaFinanciero/network |

Las rutas de Release, Actions y paquete se activan al ejecutar `git push origin release/5.11.0 v5.11.0`.

## 12. Lista de verificación del despliegue

- [ ] `VERSION`, `CHANGELOG.md` y etiqueta coinciden
- [ ] CI en verde (229 JUnit, 7 JDBC)
- [ ] Respaldo de la base antes de desplegar
- [ ] `sigsf.env` con contraseña real (chmod 600), rol `sigsf_app`
- [ ] Paquete con SHA-256 verificado
- [ ] Smoke 9/9 después de desplegar
- [ ] Healthcheck `healthy`, logs sin errores
- [ ] Versión anterior disponible para rollback
