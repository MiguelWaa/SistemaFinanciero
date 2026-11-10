#!/usr/bin/env python3
"""Arranca el proyecto en memoria, sin cambiar db.properties ni datos de PostgreSQL."""
import argparse,os,pathlib,subprocess,sys
ROOT=pathlib.Path(__file__).resolve().parents[1]
p=argparse.ArgumentParser(description=__doc__);p.add_argument('--source-target-17',action='store_true');p.add_argument('--puerto',type=int,default=8091);a=p.parse_args()
r=subprocess.call([sys.executable,str(ROOT/'scripts/validar_proyecto.py'),'--solo-junit']+(['--source-target-17'] if a.source_target_17 else []),cwd=ROOT)
if r:raise SystemExit(r)
env=os.environ.copy();env.update(SIGSF_PERSISTENCIA='memoria',SIGSF_SERVER_PORT=str(a.puerto),SIGSF_BACKUP_AUTOMATICO_HORAS='0')
cp=os.pathsep.join(map(str,[ROOT/'build/integrado/classes']+list((ROOT/'lib').glob('*.jar'))))
print(f'Abrir http://localhost:{a.puerto}/ - Ctrl+C para detener.',flush=True)
raise SystemExit(subprocess.call([os.getenv('SIGSF_JAVA','java'),'-cp',cp,'com.utp.sistemafinanciero.ServerMain'],cwd=ROOT,env=env))
