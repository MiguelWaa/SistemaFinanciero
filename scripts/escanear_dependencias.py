#!/usr/bin/env python3
"""Ejecuta SCA con herramientas instaladas; nunca inventa un informe si falta el escáner."""
import argparse,datetime,hashlib,json,os,pathlib,shutil,subprocess,zipfile
ROOT=pathlib.Path(__file__).resolve().parents[1]
def main():
 p=argparse.ArgumentParser(description=__doc__)
 p.add_argument('--online',action='store_true',help='Autoriza consultar servicios externos: npm recibe el árbol de dependencias. Requiere consentimiento del propietario.')
 a=p.parse_args()
 out=ROOT/'pruebas/reportes/sca';out.mkdir(parents=True,exist_ok=True);results=[]
 inventory=[]
 for f in sorted((ROOT/'lib').rglob('*.jar')):
  with zipfile.ZipFile(f) as jar:
   props={}
   for n in jar.namelist():
    if n.endswith('pom.properties'):
     for line in jar.read(n).decode(errors='replace').splitlines():
      if '=' in line:
       k,v=line.split('=',1);props[k]=v
  inventory.append({'archivo':f.relative_to(ROOT).as_posix(),'maven':props,'sha256':hashlib.sha256(f.read_bytes()).hexdigest()})
 lock=json.loads((ROOT/'pruebas/e2e/package-lock.json').read_text())
 npm=[{'paquete':k.removeprefix('node_modules/'),'version':v.get('version'),'integrity':v.get('integrity')} for k,v in lock.get('packages',{}).items() if k]
 (out/'inventario_local.json').write_text(json.dumps({'java':inventory,'npm':npm,'nota':'Inventario local, no es un escaneo de vulnerabilidades.'},ensure_ascii=False,indent=2))
 if not a.online:
  (out/'resultado.json').write_text(json.dumps({'estado':'INVENTARIO LOCAL GENERADO','vulnerabilidades':'NO EVALUADAS','motivo':'El modo predeterminado no envía datos. El escaneo en línea requiere autorización explícita.'},ensure_ascii=False,indent=2))
  print(out/'resultado.json');return 0
 def run(name,cmd,cwd):
  if not cmd[0] or not shutil.which(cmd[0]):results.append({'herramienta':name,'estado':'BLOQUEADO','motivo':'Instale la herramienta y agréguela a PATH'});return
  try:
   with (out/(name+'.txt')).open('w') as log:r=subprocess.run(cmd,cwd=cwd,stdout=log,stderr=subprocess.STDOUT,timeout=1800)
   results.append({'herramienta':name,'estado':'APROBADO' if r.returncode==0 else 'REVISAR INFORME','salida':r.returncode})
  except (OSError,subprocess.TimeoutExpired) as e:results.append({'herramienta':name,'estado':'BLOQUEADO','motivo':str(e)})
 dc=shutil.which('dependency-check') or shutil.which('dependency-check.sh') or shutil.which('dependency-check.bat')
 run('dependency-check',[dc,'--project','SIGSF','--scan',str(ROOT/'lib'),'--out',str(out),'--format','HTML','--format','JSON','--failOnCVSS','7'],ROOT)
 run('npm-audit',[shutil.which('npm') or shutil.which('npm.cmd'),'audit','--package-lock-only','--json','--audit-level=high','--fetch-retries=0','--fetch-timeout=10000'],ROOT/'pruebas/e2e')
 payload={'fecha_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'resultados':results,'criterio':'CVSS >= 7 / npm high requieren revisión, corrección y repetición; fallos de descarga no equivalen a cero CVE.'}
 (out/'resultado.json').write_text(json.dumps(payload,ensure_ascii=False,indent=2));print(out/'resultado.json')
 return 0 if all(r['estado']=='APROBADO' for r in results) else 1
if __name__=='__main__':raise SystemExit(main())
