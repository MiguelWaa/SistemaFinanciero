# Convención de ramas por integrante · SIGSF

## Formato

```
<tipo>/<tema>-<usuario>
```

* **tipo**: `feature`, `fix`, `docs`, `hotfix` o `release` (Git Flow, ver `07_CONTROL_DE_VERSIONES.md`).
* **tema**: palabras cortas en minúsculas separadas por guiones.
* **usuario**: sufijo propio de cada integrante. Evita que dos personas creen la misma rama.

## Sufijos del equipo

| Integrante | Sufijo | Ejemplo |
|---|---|---|
| Cesar Huaripaucar | `-cesarhuaripaucar` | `feature/version-frontend-cesarhuaripaucar` |
| Compañero 2 | `-borregop` | `feature/datos-100-registros-borregop` |

## Antes de crear una rama

```bash
git fetch --all
git branch -r | grep <tema>      # comprobar que no exista
git switch -c <tipo>/<tema>-<usuario> origin/develop
```

## Reglas

1. Cada integrante trabaja solo en ramas con su sufijo.
2. Toda rama sale de `develop` (salvo `hotfix`, que sale de `main`).
3. Se fusiona con Pull Request hacia `develop`; la CI debe estar en verde.
4. Una rama, un tema: no mezclar cambios sin relación.
