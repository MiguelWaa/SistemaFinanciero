# Cómo trabajar en el repositorio SIGSF

Guía corta para los integrantes del equipo. El detalle está en
[`docs/07_CONTROL_DE_VERSIONES.md`](docs/07_CONTROL_DE_VERSIONES.md).

## Primera vez

```bash
git clone <url> SistemaFinanciero && cd SistemaFinanciero
scripts/git/instalar_hooks.sh          # Windows: scripts\git\instalar_hooks.bat
copy db.properties.ejemplo db.properties   # y poner la contraseña local de PostgreSQL
```

En NetBeans: *File › Open Project* sobre la carpeta clonada; *Team › Git* muestra
ramas, commits y diferencias.

## Cada cambio

1. `git switch develop && git pull`
2. `git switch -c feature/<tema-corto>` (o `hotfix/<x.y.z>` desde `main` si es urgente)
3. Commits pequeños con [Conventional Commits](https://www.conventionalcommits.org/es/v1.0.0/):
   `feat(ventas): …`, `fix(bd): …`, `docs: …`, `test: …`
4. Anotar el cambio en `CHANGELOG.md` › «Sin publicar».
5. Si cambia la base: script nuevo en `db/` que termine con `INSERT INTO version_esquema …`
   y agregar la versión en `version/HistorialEsquema.java`.
6. `sh scripts/compilar_y_probar.sh` (o Alt+F6 en NetBeans): todo en verde.
7. `git push -u origin feature/<tema>` y abrir un **Pull Request hacia `develop`**.
   Otro integrante revisa; se fusiona con *merge commit* (`--no-ff`) cuando la CI está en verde.

## Reglas

* Nunca commit directo en `main`; en `develop` solo merges y documentación menor.
* Nunca `git push --force` en `main` ni `develop`. Para deshacer algo publicado: `git revert`.
* Nunca subir `db.properties`, respaldos, logs ni `build/`/`dist/` (el hook lo impide).
* Publicar versión: `release/x.y.z` → actualizar `VERSION` y `CHANGELOG.md` → merge en `main`
  → `git tag -a vx.y.z` → merge de vuelta en `develop`.
