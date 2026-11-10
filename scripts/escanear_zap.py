#!/usr/bin/env python3
"""ZAP Docker contra SIGSF local de QA. --activo aplica ataques y exige datos desechables."""
import argparse,datetime,json,os,pathlib,platform,shutil,subprocess,urllib.parse,urllib.request
ROOT=pathlib.Path(__file__).resolve().parents[1]
def main():
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('--url',default='http://127.0.0.1:8091');p.add_argument('--activo',action='store_true');a=p.parse_args()
 u=urllib.parse.urlparse(a.url)
 if u.scheme!='http' or u.hostname not in ['127.0.0.1','localhost','::1'] or u.path not in ['','/'] or u.username or u.query or u.fragment:raise SystemExit('Use la raíz HTTP de su instancia local de QA.')
 out=ROOT/'pruebas/reportes/zap';out.mkdir(parents=True,exist_ok=True)
 result={'fecha_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'modalidad':'activo' if a.activo else 'pasivo','estado':'BLOQUEADO','alcance':'instancia local; una cuenta; verificar cobertura de rutas y roles en el informe'}
 def save(): (out/'resultado.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf8')
 if not shutil.which('docker'):result['motivo']='Docker no está instalado';save();print(result['motivo']);return 2
 base=a.url.rstrip('/');token=None
 def call(path,data=None):
  headers={'Content-Type':'application/json'}
  if token:headers['Authorization']='Bearer '+token
  with urllib.request.urlopen(urllib.request.Request(base+path,data=data,headers=headers),timeout=10) as r:return r.status,json.load(r)
 try:
  status,login=call('/api/auth/login',json.dumps({'email':os.getenv('SIGSF_TEST_EMAIL','admin@sigsf.com'),'password':os.getenv('SIGSF_TEST_PASSWORD','admin123')}).encode());token=login['token']
  status,session=call('/api/auth/sesion');result['sesion_inicial']=status;result['rol']=session.get('rol')
  linux=platform.system()=='Linux';host=u.hostname if linux else 'host.docker.internal';port=u.port or 80
  target=f'http://{host}:{port}/';env=os.environ.copy();env.update(ZAP_AUTH_HEADER='Authorization',ZAP_AUTH_HEADER_VALUE='Bearer '+token,ZAP_AUTH_HEADER_SITE=host)
  cmd=['docker','run','--rm']+(['--network','host'] if linux else [])+['-v',str(out)+':/zap/wrk:rw','-e','ZAP_AUTH_HEADER','-e','ZAP_AUTH_HEADER_VALUE','-e','ZAP_AUTH_HEADER_SITE',os.getenv('SIGSF_ZAP_IMAGE','ghcr.io/zaproxy/zaproxy:stable'),'zap-full-scan.py' if a.activo else 'zap-baseline.py','-t',target,'-m','2','-r','informe.html','-J','informe.json']
  with (out/'ejecucion.txt').open('w') as log:r=subprocess.run(cmd,env=env,stdout=log,stderr=subprocess.STDOUT,timeout=1800)
  result['salida']=r.returncode;result['estado']='APROBADO' if r.returncode==0 else 'REVISAR INFORME' if r.returncode in [1,2] else 'BLOQUEADO'
  result['sesion_final']=call('/api/auth/sesion')[0]
 except Exception as e:result['estado']='BLOQUEADO';result['motivo']=type(e).__name__+': '+str(e)
 finally:
  if token:
   try:call('/api/auth/logout',b'{}')
   except Exception:pass
 save();print(out/'resultado.json');return 0 if result['estado']=='APROBADO' else 1
if __name__=='__main__':raise SystemExit(main())
