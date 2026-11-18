#!/usr/bin/env python3
"""Ejecuta en el equipo local las mismas etapas del workflow CD (.github/workflows/cd.yml).

  python scripts/pipeline_local.py [--destino /tmp/sigsf-staging] [--puerto 8095]
         [--base-image eclipse-temurin:17-jre-jammy] [--sin-docker] [--evidencia DIR]

Etapas: 1 compilar + JUnit (igual que CI) · 2 PostgreSQL (si hay SIGSF_DB_PASSWORD)
3 empaquetar JAR y release · 4 imagen Docker + smoke + healthcheck
5 desplegar en staging con desplegar.sh + smoke con login.
Cada etapa deja su registro y el resumen queda en <evidencia>/pipeline_local.json.
"""
import argparse, datetime, json, os, shutil, subprocess, sys, time
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
VERSION = (ROOT / 'VERSION').read_text().strip()

def main():
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument('--destino', default='/tmp/sigsf-staging')
    p.add_argument('--puerto', type=int, default=8095)
    p.add_argument('--puerto-docker', type=int, default=8096)
    p.add_argument('--base-image', default='eclipse-temurin:17-jre-jammy')
    p.add_argument('--sin-docker', action='store_true')
    p.add_argument('--evidencia', default=str(ROOT / 'evidencias' / f'v{VERSION}' / 'pipeline'))
    a = p.parse_args()
    ev = Path(a.evidencia); ev.mkdir(parents=True, exist_ok=True)
    py = sys.executable
    res = {'pipeline': 'CD SIGSF (ejecución local)', 'version': VERSION,
           'inicio_utc': datetime.datetime.now(datetime.timezone.utc).isoformat(timespec='seconds'), 'etapas': []}

    def guardar():
        (ev / 'pipeline_local.json').write_text(json.dumps(res, ensure_ascii=False, indent=2), encoding='utf8')

    def etapa(nombre, cmd, env=None, timeout=900, obligatoria=True):
        t = time.time(); log = ev / f'{len(res["etapas"])+1:02d}_{nombre}.txt'
        with log.open('w') as f:
            f.write('$ ' + ' '.join(map(str, cmd)) + '\n\n'); f.flush()
            r = subprocess.run(list(map(str, cmd)), cwd=ROOT, env={**os.environ, **(env or {})},
                               stdout=f, stderr=subprocess.STDOUT, timeout=timeout)
        e = {'etapa': nombre, 'estado': 'APROBADO' if r.returncode == 0 else 'FALLIDO',
             'segundos': round(time.time() - t, 1), 'registro': log.name}
        res['etapas'].append(e); guardar()
        print(f"[{e['estado']}] {nombre} ({e['segundos']} s)", flush=True)
        if r.returncode and obligatoria:
            res['estado'] = 'FALLIDO'; guardar(); sys.exit(1)
        return r.returncode == 0

    def omitida(nombre, motivo):
        res['etapas'].append({'etapa': nombre, 'estado': 'OMITIDO', 'motivo': motivo}); guardar()
        print(f'[OMITIDO] {nombre}: {motivo}', flush=True)

    # 1. Integración continua: compilar + pruebas
    etapa('compilar_junit', [py, 'scripts/validar_proyecto.py', '--solo-junit'])
    # 2. Integración con PostgreSQL real
    if os.getenv('SIGSF_DB_PASSWORD') and shutil.which('psql'):
        etapa('postgresql_jdbc', [py, 'scripts/probar_postgresql.py'])
    else:
        omitida('postgresql_jdbc', 'Sin SIGSF_DB_PASSWORD o sin cliente psql')
    # 3. Empaquetado
    etapa('empaquetar_jar', [py, 'scripts/empaquetar.py'])
    etapa('empaquetar_release', [py, 'scripts/empaquetar_release.py'])
    paquete = ROOT / 'release' / f'sigsf-{VERSION}.tar.gz'
    # 4. Contenedor
    if not a.sin_docker and shutil.which('docker'):
        tag = f'sigsf:{VERSION}'
        commit = subprocess.run(['git', 'rev-parse', '--short', 'HEAD'], cwd=ROOT, capture_output=True, text=True).stdout.strip()
        etapa('docker_build', ['docker', 'build', '--build-arg', f'BASE_IMAGE={a.base_image}',
                               '--build-arg', f'VERSION={VERSION}', '--build-arg', f'COMMIT={commit}', '-t', tag, '.'])
        subprocess.run(['docker', 'rm', '-f', 'sigsf-pipeline'], capture_output=True)
        etapa('docker_run', ['docker', 'run', '-d', '--name', 'sigsf-pipeline', '--read-only', '--tmpfs', '/tmp',
                             '--cap-drop', 'ALL', '--security-opt', 'no-new-privileges:true',
                             '-p', f'{a.puerto_docker}:8091', '-e', 'SIGSF_PERSISTENCIA=memoria',
                             '-v', 'sigsf-pipeline-logs:/app/logs', tag])
        etapa('docker_smoke', [py, 'scripts/smoke_test.py', '--url', f'http://127.0.0.1:{a.puerto_docker}',
                               '--version', VERSION, '--login', 'admin@sigsf.com:admin123',
                               '--salida', ev / 'smoke_docker.json'], obligatoria=False)
        espera = 'for i in $(seq 1 30); do s=$(docker inspect -f "{{.State.Health.Status}}" sigsf-pipeline); ' \
                 'echo "intento $i: $s"; [ "$s" = healthy ] && exit 0; sleep 3; done; exit 1'
        etapa('docker_healthcheck', ['sh', '-c', espera], obligatoria=False)
        subprocess.run(['sh', '-c', f'docker inspect sigsf-pipeline > "{ev}/docker_inspect.json"; '
                                    f'docker image inspect {tag} > "{ev}/docker_image.json"; '
                                    f'docker images {tag.split(":")[0]} > "{ev}/docker_images.txt"; '
                                    f'docker logs sigsf-pipeline > "{ev}/docker_logs.txt" 2>&1; '
                                    f'docker history {tag} > "{ev}/docker_history.txt"'])
    else:
        omitida('docker', 'Docker no disponible o --sin-docker')
    # 5. Despliegue en staging con reversión automática
    destino = Path(a.destino); (destino / 'shared').mkdir(parents=True, exist_ok=True)
    envf = destino / 'shared' / 'sigsf.env'
    envf.write_text(f'SIGSF_PERSISTENCIA=memoria\nSIGSF_SERVER_PORT={a.puerto}\nSIGSF_BACKUP_AUTOMATICO_HORAS=0\n')
    etapa('desplegar_staging', ['sh', 'scripts/desplegar.sh', paquete, '--destino', destino,
                                '--login', 'admin@sigsf.com:admin123'])
    res['estado'] = 'APROBADO' if all(e['estado'] in ('APROBADO', 'OMITIDO') for e in res['etapas']) else 'CON OBSERVACIONES'
    res['fin_utc'] = datetime.datetime.now(datetime.timezone.utc).isoformat(timespec='seconds')
    guardar(); print('Pipeline:', res['estado'], '->', ev / 'pipeline_local.json')

if __name__ == '__main__':
    main()
