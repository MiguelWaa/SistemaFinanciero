#!/usr/bin/env python3
"""Genera el paquete de despliegue release/sigsf-<versión>.tar.gz (+ .zip y SHA-256).

Requiere dist/SIGSF.jar (scripts/empaquetar.py). Solo biblioteca estándar.
El paquete contiene lo necesario para ejecutar, nunca fuentes de prueba,
db.properties, logs ni respaldos:

  dist/SIGSF.jar  lib/*.jar  web/  db/  deploy/  scripts/{smoke_test.py,desplegar.sh,rollback.sh}
  VERSION  db.properties.ejemplo  INICIAR_DEMO.*  README.md  docs/19_PLAN_DE_DESPLIEGUE.md
  MANIFIESTO.json (versión, commit, SHA-256 por archivo)
"""
import datetime, hashlib, io, json, os, subprocess, sys, tarfile, zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
VERSION = (ROOT / 'VERSION').read_text().strip()
SALIDA = ROOT / 'release'
INCLUIR = ['dist/SIGSF.jar', 'lib', 'web', 'db', 'deploy', 'VERSION', 'db.properties.ejemplo',
           'INICIAR_DEMO.bat', 'INICIAR_DEMO.sh', 'README.md', 'docs/19_PLAN_DE_DESPLIEGUE.md',
           'scripts/smoke_test.py', 'scripts/desplegar.sh', 'scripts/rollback.sh', 'scripts/iniciar_demo_windows.ps1']
EXCLUIR = {'lib/test', 'deploy/sigsf.env'}

def git(*a):
    try: return subprocess.check_output(['git', *a], cwd=ROOT, text=True, stderr=subprocess.DEVNULL).strip()
    except Exception: return 'no-disponible'

def archivos():
    for entrada in INCLUIR:
        p = ROOT / entrada
        if not p.exists():
            if entrada == 'dist/SIGSF.jar': sys.exit('Falta dist/SIGSF.jar: ejecute scripts/empaquetar.py')
            continue
        for f in ([p] if p.is_file() else sorted(x for x in p.rglob('*') if x.is_file())):
            rel = f.relative_to(ROOT).as_posix()
            if any(rel == e or rel.startswith(e + '/') for e in EXCLUIR) or '__pycache__' in rel: continue
            yield rel, f

def sha(p):
    h = hashlib.sha256()
    with open(p, 'rb') as f:
        for b in iter(lambda: f.read(1 << 20), b''): h.update(b)
    return h.hexdigest()

def main():
    SALIDA.mkdir(exist_ok=True)
    lista = list(archivos())
    manifiesto = {'producto': 'SIGSF', 'version': VERSION, 'commit': git('rev-parse', 'HEAD'),
                  'etiqueta': git('describe', '--tags', '--always'), 'rama': git('branch', '--show-current'),
                  'fecha_utc': datetime.datetime.now(datetime.timezone.utc).isoformat(timespec='seconds'),
                  'java_minimo': '17', 'archivos': {rel: sha(f) for rel, f in lista}}
    datos_m = json.dumps(manifiesto, ensure_ascii=False, indent=2).encode()
    base = f'sigsf-{VERSION}'
    tgz, zp = SALIDA / f'{base}.tar.gz', SALIDA / f'{base}.zip'
    with tarfile.open(tgz, 'w:gz') as t:
        for rel, f in lista:
            info = t.gettarinfo(str(f), arcname='./' + rel)
            info.uid = info.gid = 0; info.uname = info.gname = 'sigsf'
            if rel.endswith('.sh'): info.mode = 0o755
            with open(f, 'rb') as fh: t.addfile(info, fh)
        info = tarfile.TarInfo('./MANIFIESTO.json'); info.size = len(datos_m); info.mtime = int(datetime.datetime.now().timestamp())
        t.addfile(info, io.BytesIO(datos_m))
    with zipfile.ZipFile(zp, 'w', zipfile.ZIP_DEFLATED) as z:
        for rel, f in lista: z.write(f, f'{base}/{rel}')
        z.writestr(f'{base}/MANIFIESTO.json', datos_m)
    for p in (tgz, zp):
        (p.parent / (p.name + '.sha256')).write_text(f'{sha(p)}  {p.name}\n')
    (SALIDA / 'SHA256SUMS').write_text(''.join((p.parent / (p.name + '.sha256')).read_text() for p in (tgz, zp)))
    (SALIDA / 'MANIFIESTO.json').write_bytes(datos_m)
    print(json.dumps({'paquete': str(tgz.relative_to(ROOT)), 'zip': str(zp.relative_to(ROOT)),
                      'archivos': len(lista), 'tamano_mb': round(tgz.stat().st_size / 1e6, 2),
                      'sha256': sha(tgz)}, indent=2))

if __name__ == '__main__':
    main()
