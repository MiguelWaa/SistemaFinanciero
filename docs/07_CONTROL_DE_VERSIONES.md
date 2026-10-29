# SIGSF · Sistema de control de versiones (SCV)

Repositorio Git del **Sistema Integral de Gestión de Servicios Financieros**.
Este documento describe cómo el equipo gestiona el código fuente, la base de
datos y la documentación con Git: ramas, commits, fusiones (merge),
resolución de conflictos, retrocesos (rollback), etiquetas de versión,
repositorio remoto, automatizaciones y seguridad del repositorio.

| | |
|---|---|
| Herramienta | Git 2.x (Git for Windows / NetBeans → *Team › Git*) |
| Repositorio remoto | `origin` (GitHub privado del equipo; ver §9) |
| Estrategia de ramas | **Git Flow** simplificado |
| Mensajes de commit | **Conventional Commits** (validado por hook `commit-msg`) |
| Numeración | **SemVer** `MAYOR.MENOR.PARCHE` (archivo `VERSION` + etiquetas `vX.Y.Z`) |
| Registro de cambios | `CHANGELOG.md` (formato *Keep a Changelog*) |
| Integración continua | GitHub Actions: `.github/workflows/ci.yml` |

---

## 1. Estrategia de ramas (Git Flow)

```
main      ●──────────────────────────●──────────────●      producción (solo versiones publicadas, con etiqueta)
          v4.0.0                      v5.0.0         v5.0.1
           \                         /   \          / \
develop     ●──●────●────●────●─────●     \   hotfix/5.0.1 ──► develop
               \  / \  /  \  /  \  /       \
feature/*       ●●   ●●    ●●    ●●         release/5.0.0
```

| Rama | Nace de | Se fusiona en | Uso | Ejemplos en este repositorio |
|---|---|---|---|---|
| `main` | — | — | Código en producción. Cada commit de `main` es una versión etiquetada. **Nadie hace commit directo.** | `v4.0.0`, `v5.0.0`, `v5.0.1` |
| `develop` | `main` | `release/*` | Integración del trabajo del equipo; siempre debe compilar y pasar las pruebas. | — |
| `feature/<tema>` | `develop` | `develop` (merge `--no-ff`) | Una funcionalidad o cambio por rama, por integrante. | `feature/seguridad-repositorio`, `feature/version-backend`, `feature/version-frontend`, `feature/integracion-continua`, `feature/inventario-documentacion`, `feature/prestamos-redondeo` |
| `release/<x.y.z>` | `develop` | `main` **y** `develop` | Preparar la entrega: número de versión, CHANGELOG, últimos ajustes. | `release/5.0.0` |
| `hotfix/<x.y.z>` | `main` | `main` **y** `develop` | Corrección urgente sobre la versión publicada. | `hotfix/5.0.1` |

**Por qué `--no-ff`:** fuerza un commit de merge aunque se pudiera avanzar en
línea recta; así el grafo (*network*) muestra cada rama como un bloque y se
puede revertir una funcionalidad completa con un solo `git revert -m 1`.

## 2. Responsables (autoría de los commits)

| Integrante | Ramas principales |
|---|---|
| Miguel Ángel Machaca Machaca | línea base v4.0, `feature/seguridad-repositorio`, `feature/inventario-documentacion`, `release/5.0.0`, integración de merges |
| Ricardo Gabriel Cervantes Tejada | `feature/version-backend`, `feature/integracion-continua`, rollback (revert) de `feature/prestamos-redondeo` |
| Cesar Huaripaucar Huaman | `feature/version-frontend`, `hotfix/5.0.1` |

`git shortlog -sn --all` muestra los commits de cada integrante.

## 3. Convención de commits (Conventional Commits)

```
tipo(ámbito): descripción en minúsculas, en imperativo
<línea en blanco>
cuerpo opcional: qué y por qué (no cómo)
```

| Tipo | Cuándo | Ejemplo real del repositorio |
|---|---|---|
| `feat` | Funcionalidad nueva | `feat(api): endpoints GET /api/version y GET /api/versiones` |
| `fix` | Corrección de error | `fix(version): ramas, etiquetas y commit raíz vacíos en la pantalla de versiones` |
| `docs` | Documentación | `docs: agregar CHANGELOG.md y archivo VERSION (SemVer)` |
| `test` | Pruebas | `test(version): 15 pruebas del control de versiones (99 -> 114)` |
| `build` | Compilación (Ant) | `build(ant): generar version.properties con datos de Git al compilar` |
| `ci` | Integración continua | `ci: integración continua con GitHub Actions…` |
| `chore` | Mantenimiento | `chore(git): hooks pre-commit y commit-msg versionados en .githooks/` |
| `perf` / `refactor` | Rendimiento / reestructuración | `perf(prestamos): truncar la cuota mensual…` (luego revertido) |

El hook `.githooks/commit-msg` rechaza mensajes que no siguen el formato.

## 4. Versionamiento semántico y etiquetas

* `VERSION` contiene la versión en curso: `x.y.z-SNAPSHOT` en `develop`, `x.y.z` en `main`.
* Cada publicación en `main` lleva una **etiqueta anotada**: `git tag -a v5.0.0 -m "…"`.
* La aplicación muestra la versión en ejecución (pantalla **Control de versiones** y pie del panel)
  usando, en este orden: Git → `version.properties` (generado por `build.xml` al compilar) → `VERSION`.

| Etiqueta | Tipo | Contenido |
|---|---|---|
| `v4.0.0` | línea base | Estado entregado en el taller de recursos de código fuente |
| `v5.0.0` | MENOR→MAYOR | Control de versiones: Git Flow, hooks, API y pantalla de versiones, versionamiento del esquema BD, CI, inventario v5 |
| `v5.0.1` | PARCHE | `hotfix/5.0.1`: nombre de la base (`SistemaFinanciero`) y puerto (8091) unificados en código y documentación |

## 5. Flujo de trabajo diario (comandos)

```bash
# 1. Clonar una vez y activar los hooks del equipo
git clone <url-del-repositorio> SistemaFinanciero
cd SistemaFinanciero
scripts/git/instalar_hooks.sh            # Windows: scripts\git\instalar_hooks.bat

# 2. Empezar una funcionalidad desde develop actualizado
git switch develop
git pull
git switch -c feature/mi-cambio

# 3. Trabajar en pasos pequeños
git status
git add src/… web/…
git commit -m "feat(ventas): exportar ventas a CSV"
git push -u origin feature/mi-cambio      # y abrir un Pull Request hacia develop

# 4. Integrar (o desde GitHub con el Pull Request aprobado y CI en verde)
git switch develop
git merge --no-ff feature/mi-cambio
git push
```

## 6. Resolución de conflictos (caso real)

`feature/seguridad-repositorio` y `feature/version-backend` se desarrollaron en
paralelo y ambas agregaron líneas en la misma sección de `CHANGELOG.md`
(«Sin publicar › Agregado»). Al fusionar la segunda en `develop`:

```text
$ git merge --no-ff feature/version-backend
CONFLICT (content): Merge conflict in CHANGELOG.md
Automatic merge failed; fix conflicts and then commit the result.
```

```text
### Agregado
<<<<<<< HEAD
- Plantilla `db.properties.ejemplo`; `db.properties` sale del repositorio (credenciales).
- Hooks Git `pre-commit` (bloquea credenciales y archivos generados) y `commit-msg`.
=======
- Servicio de versión (`VersionService`) y API `GET /api/version`, `GET /api/versiones` (solo ADMIN).
- Versionamiento del esquema de la base: tabla `version_esquema` y `db/version_esquema.sql`.
- `version.properties` generado por Ant al compilar (commit, rama, etiqueta).
>>>>>>> feature/version-backend
```

**Resolución:** se conservaron las dos listas (ambos cambios son válidos), se
quitaron los marcadores, se verificó que el proyecto compila y se confirmó:
`git add CHANGELOG.md && git commit` → commit de merge con la nota del conflicto.

## 7. Rollback: cómo deshacer cambios

| Situación | Comando | ¿Reescribe historial? | Uso en el equipo |
|---|---|---|---|
| Descartar cambios de un archivo sin commit | `git restore <archivo>` | No | Libre |
| Sacar un archivo del área de preparación | `git restore --staged <archivo>` | No | Libre |
| Guardar cambios a medias para cambiar de rama | `git stash` / `git stash pop` | No | Libre |
| Deshacer el último commit **local** (no publicado) | `git reset --soft HEAD~1` (conserva cambios) · `git reset --hard HEAD~1` (los borra) | **Sí** | Solo en ramas propias sin `push` |
| Deshacer un commit **ya publicado** | `git revert <commit>` | No (crea un commit inverso) | **Forma obligatoria en `develop` y `main`** |
| Deshacer una funcionalidad completa (merge) | `git revert -m 1 <merge>` | No | Caso real abajo |
| Recuperar algo "perdido" tras un reset | `git reflog` + `git reset --hard <hash>` | — | Emergencias |
| Volver a ver una versión anterior | `git switch --detach v4.0.0` | No | Consultas / demostraciones |

**Caso real — `feature/prestamos-redondeo`:** se fusionó en `develop` un cambio
que truncaba la cuota del préstamo (`RoundingMode.DOWN`). Las pruebas fallaron
(`PrestamoServiceTest.redondeaLaCuotaADosDecimales`: 1166.66 en vez de
1166.67) y el saldo quedaba descuadrado (12 × 1166.66 = 13 999.92 ≠ 14 000).
Como el merge ya estaba publicado, **no** se usó `reset`: se ejecutó

```bash
git revert -m 1 <hash-del-merge>     # -m 1: conservar el lado de develop
```

Resultado: un commit `Revert "Merge branch 'feature/prestamos-redondeo'…"`,
las pruebas en verde y el error visible en el historial como evidencia.

## 8. Archivos que no se versionan y seguridad del repositorio

* `.gitignore`: `build/`, `dist/`, `logs/`, `respaldos/`, `reportes/`, `nbproject/private/`, `lib/test/` y **`db.properties`**.
* `db.properties.ejemplo` es la plantilla versionada (valores de laboratorio). Cada integrante crea su `db.properties` local; el programa carga primero el ejemplo y luego el local.
* Hook `.githooks/pre-commit`: bloquea `db.properties`, archivos generados, archivos > 5 MB y contraseñas o claves privadas escritas en el código.
* `.gitattributes`: fin de línea normalizado (LF; `.bat` con CRLF), binarios (`.jar`, `.png`, `.xlsx`) sin conversión y `nbproject/` byte a byte.
* **Lección aprendida:** la línea base `v4.0.0` incluía `db.properties` con la contraseña de laboratorio. Quitarlo del índice no lo borra del historial; si hubiera sido una contraseña real se debe **cambiar la contraseña** (rotarla), no solo borrar el archivo.
* Ramas protegidas en GitHub (*Settings › Branches*): `main` y `develop` exigen Pull Request y CI en verde.

## 9. Repositorio remoto (GitHub)

```bash
# Publicar el repositorio por primera vez (repositorio privado vacío creado en GitHub)
git remote add origin https://github.com/<usuario>/SistemaFinanciero.git   # o: git remote set-url origin …
git push -u origin --all          # todas las ramas
git push origin --tags            # todas las etiquetas (v4.0.0 … v5.4.0)
```

* **Network:** *Insights › Network* muestra el mismo grafo que `git log --all --graph`.
* El repositorio también se entrega empaquetado como `SIGSF.bundle`
  (`git clone SIGSF.bundle SistemaFinanciero` reconstruye todas las ramas y etiquetas).

## 10. Versionamiento de la base de datos

La versión del esquema es independiente de la versión de la aplicación y se
registra en la tabla `version_esquema` (script `db/version_esquema.sql`).

| Versión | Script | Cambio |
|---|---|---|
| 1.0.0 | `db/schema.sql` + `db/datos_ejemplo.sql` | 31 tablas de las 9 áreas |
| 1.1.0 | `db/portal_cliente.sql` | Portal del cliente (`cliente_acceso`) |
| 2.0.0 | `db/seguridad.sql` | Auditoría inmutable, CHECK, índices, rol `sigsf_app` |
| 5.0.0 | `db/version_esquema.sql` | Tabla `version_esquema` |
| 5.2.0 | `db/pagos_qr.sql` | Pagos con QR: tablas `billetera`, `pago` y `cotizacion` |

Al arrancar, `VerificadorEsquema` crea la tabla si falta, registra las versiones
detectadas y avisa en el log si la base está atrasada. La pantalla **Control de
versiones › Esquema BD** muestra el historial. Regla del equipo: **todo script
SQL nuevo termina con su `INSERT INTO version_esquema`**.

## 11. Integración continua

`.github/workflows/ci.yml` se ejecuta en cada `push` a `main`/`develop` y en cada
Pull Request: instala JDK 17, compila y ejecuta las **151 pruebas** con
`scripts/compilar_y_probar.sh`. Un Pull Request con pruebas en rojo no se
fusiona (eso habría detenido el caso del §7 antes de llegar a `develop`).

## 12. Comandos de consulta útiles

```bash
git log --oneline --graph --all --decorate     # red de ramas (network)
git log --oneline v4.0.0..v5.0.0               # qué cambió entre dos versiones
git diff v5.0.0 v5.0.1 --stat                  # archivos cambiados por el parche
git show v4.0.0:web/_respaldo_diseno_anterior/login.html   # ver un archivo antiguo
git blame src/com/utp/sistemafinanciero/service/PrestamoService.java
git shortlog -sn --all                         # commits por integrante
git describe --tags                            # versión más cercana al commit actual
```
