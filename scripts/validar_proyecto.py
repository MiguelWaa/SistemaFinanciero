#!/usr/bin/env python3
"""Plan integrado SIGSF. Python 3 estándar; JDK 17+, Node y npm ci en pruebas/e2e.
No instala herramientas ni modifica bases de datos. Cada ejecución conserva su evidencia.
"""
import argparse,datetime,hashlib,json,os,pathlib,re,shutil,socket,subprocess,sys,time,urllib.request
ROOT=pathlib.Path(__file__).resolve().parents[1]

def main():
 p=argparse.ArgumentParser(description=__doc__)
 p.add_argument('--solo-junit',action='store_true')
 p.add_argument('--omitir-e2e',action='store_true',help='Registra E2E como NO EJECUTADO, nunca aprobado')
 p.add_argument('--source-target-17',action='store_true',help='Compatibilidad de bytecode sin verificar APIs --release; solo runtime incompleto')
 p.add_argument('--salida',default='pruebas/reportes/'+datetime.datetime.now(datetime.timezone.utc).strftime('%Y%m%dT%H%M%SZ'))
 a=p.parse_args();os.chdir(ROOT)
 out=(ROOT/a.salida).resolve();out.mkdir(parents=True,exist_ok=True)
 summary={'version':(ROOT/'VERSION').read_text().strip(),'fecha_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'alcance':'localhost; datos de prueba; DAO en memoria','compilacion':'-source 17 -target 17' if a.source_target_17 else '--release 17','etapas':[], 'pendientes':['PostgreSQL (scripts/probar_postgresql.py)','ZAP (scripts/escanear_zap.py)','SCA (scripts/escanear_dependencias.py)']}
 try:summary['commit']=subprocess.check_output(['git','rev-parse','HEAD'],text=True).strip();summary['cambios_locales']=bool(subprocess.check_output(['git','status','--porcelain'],text=True).strip())
 except (OSError,subprocess.CalledProcessError):summary['commit']='sin .git'
 sha=hashlib.sha256()
 for carpeta in ['src','test','web','scripts','pruebas/seguridad']:
  for f in sorted((ROOT/carpeta).rglob('*')):
   if f.is_file() and '__pycache__' not in f.parts:
    sha.update(f.relative_to(ROOT).as_posix().encode());sha.update(f.read_bytes())
 summary['sha256_fuentes']=sha.hexdigest()
 def etapa(nombre,cmd,timeout=180,env=None,cwd=ROOT):
  log=out/(nombre+'.txt');print('Ejecutando '+nombre,flush=True)
  try:
   with log.open('w',encoding='utf8') as f:
    r=subprocess.run([str(x) for x in cmd],cwd=cwd,env=env,stdout=f,stderr=subprocess.STDOUT,timeout=timeout)
   item={'nombre':nombre,'estado':'APROBADO' if r.returncode==0 else 'FALLIDO','salida':r.returncode,'registro':log.name}
  except (OSError,subprocess.TimeoutExpired) as e:
   with log.open('a',encoding='utf8') as f:f.write('\n'+str(e))
   item={'nombre':nombre,'estado':'BLOQUEADO','registro':log.name}
  summary['etapas'].append(item);return item['estado']=='APROBADO'
 def pendiente(nombre,motivo):summary['etapas'].append({'nombre':nombre,'estado':'NO EJECUTADO','motivo':motivo})
 def guardar():
  summary['seleccion_aprobada']=all(x['estado']=='APROBADO' for x in summary['etapas'] if x['estado']!='NO EJECUTADO')
  summary['evaluacion_integral_completa']=False # SCA/DAST/BD tienen informes y aceptación independientes.
  (out/'resultado.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2),encoding='utf8')
  (out/'RESUMEN.txt').write_text('\n'.join([f"SIGSF {summary['version']}",f"Fuentes SHA256: {summary['sha256_fuentes']}"]+[x['nombre']+': '+x['estado'] for x in summary['etapas']]+['Pendientes: '+', '.join(summary['pendientes'])]),encoding='utf8')
  print('Resultado: '+str(out/'resultado.json'),flush=True)
 libs=[ROOT/'lib'/n for n in ['postgresql.jar','slf4j-api-2.0.16.jar','logback-core-1.5.12.jar','logback-classic-1.5.12.jar']]
 jt=[ROOT/'lib/test'/n for n in ['junit-4.13.2.jar','hamcrest-core-1.3.jar']]
 missing=[str(f.relative_to(ROOT)) for f in libs+jt if not f.is_file()]
 if missing:
  summary['etapas'].append({'nombre':'dependencias','estado':'BLOQUEADO','motivo':'Ejecute python scripts/preparar_dependencias.py. Faltan: '+', '.join(missing)});guardar();return 1
 build=ROOT/'build/integrado';classes=build/'classes';tests=build/'test'
 if build.exists():shutil.rmtree(build)
 classes.mkdir(parents=True);tests.mkdir()
 java=os.getenv('SIGSF_JAVA','java');javac=shutil.which('javac')
 compiler=[javac] if javac else [java,'-m','jdk.compiler/com.sun.tools.javac.Main']
 flags=['-source','17','-target','17'] if a.source_target_17 else ['--release','17']
 cp=os.pathsep.join(map(str,libs))
 for carpeta,dest,name,classpath in [('src',classes,'compilar_fuentes',cp),('test',tests,'compilar_pruebas',cp+os.pathsep+str(classes)+os.pathsep+os.pathsep.join(map(str,jt)))]:
  files=sorted((ROOT/carpeta).rglob('*.java'));args=build/(carpeta+'.args')
  args.write_text('\n'.join('"'+f.as_posix()+'"' for f in files),encoding='utf8')
  if not etapa(name,compiler+['-encoding','UTF-8']+flags+['-cp',classpath,'-d',dest,'@'+str(args)]):guardar();return 1
  for f in (ROOT/carpeta).rglob('*'):
   if f.is_file() and f.suffix!='.java':
    target=dest/f.relative_to(ROOT/carpeta);target.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(f,target)
 fullcp=os.pathsep.join(map(str,[tests,classes]+libs+jt))
 names=['.'.join(f.relative_to(ROOT/'test').with_suffix('').parts) for f in sorted((ROOT/'test').rglob('*Test.java'))]
 ok=etapa('junit',[java,'-cp',fullcp,'org.junit.runner.JUnitCore']+names,300)
 content=(out/'junit.txt').read_text(errors='replace');m=re.search(r'OK \((\d+) tests?\)',content)
 if m:summary['junit_casos_aprobados']=int(m.group(1))
 if a.solo_junit:pendiente('api_fuzzing_e2e','Selección --solo-junit');guardar();return 0 if ok else 1
 node=os.getenv('SIGSF_NODE','node');syntax=True
 with (out/'frontend_sintaxis.txt').open('w') as log:
  try:
   for f in sorted((ROOT/'web').rglob('*.js')):
    if 'vendor' in f.relative_to(ROOT/'web').parts:continue
    r=subprocess.run([node,'--check',str(f)],stdout=log,stderr=subprocess.STDOUT,timeout=15);syntax &= r.returncode==0
  except (OSError,subprocess.TimeoutExpired) as e:log.write(str(e));syntax=False
 summary['etapas'].append({'nombre':'frontend_sintaxis','estado':'APROBADO' if syntax else 'FALLIDO','registro':'frontend_sintaxis.txt'})
 with socket.socket() as reserve:reserve.bind(('127.0.0.1',0));port=reserve.getsockname()[1]
 url=f'http://127.0.0.1:{port}';summary['url_prueba']=url
 env=os.environ.copy();env.update(SIGSF_PERSISTENCIA='memoria',SIGSF_SERVER_PORT=str(port),SIGSF_BACKUP_AUTOMATICO_HORAS='0',SIGSF_BACKUP_DIR=str(out/'respaldos'),SIGSF_URL=url)
 server=None
 try:
  with (out/'servidor.txt').open('w') as log:
   server=subprocess.Popen([java,'-cp',os.pathsep.join(map(str,[classes]+libs)),'com.utp.sistemafinanciero.ServerMain'],cwd=ROOT,env=env,stdout=log,stderr=subprocess.STDOUT)
   ready=False
   for _ in range(150):
    if server.poll() is not None:break
    try:
     with urllib.request.urlopen(url+'/api/health',timeout=1) as r:ready=r.status==200
     if ready:break
    except OSError:time.sleep(.2)
   if not ready:summary['etapas'].append({'nombre':'servidor','estado':'BLOQUEADO','registro':'servidor.txt'});guardar();return 1
   modules=pathlib.Path(os.getenv('SIGSF_NODE_MODULES',str(ROOT/'pruebas/e2e/node_modules')))
   newman=modules/'newman/bin/newman.js'
   if newman.is_file():
    etapa('newman',[node,newman,'run',ROOT/'pruebas/postman/SIGSF_API.postman_collection.json','-e',ROOT/'pruebas/postman/SIGSF_local.postman_environment.json','--env-var','baseUrl='+url,'-r','cli,junit','--reporter-junit-export',out/'newman.xml'],120,env)
   else:summary['etapas'].append({'nombre':'newman','estado':'BLOQUEADO','motivo':'Ejecute npm ci en pruebas/e2e'})
   etapa('entradas_seguridad',[sys.executable,ROOT/'pruebas/seguridad/comprobar_entradas.py','--base-url',url,'--output',out/'entradas_seguridad.json'],90,env)
   if a.omitir_e2e:pendiente('playwright','Omitido expresamente; se requiere Chromium compatible')
   else:etapa('playwright',[node,modules/'@playwright/test/cli.js','test'],240,env,ROOT/'pruebas/e2e')
 finally:
  if server is not None:
   server.terminate()
   try:server.wait(timeout=5)
   except subprocess.TimeoutExpired:server.kill();server.wait()
 guardar();return 0 if summary['seleccion_aprobada'] else 1

if __name__=='__main__':raise SystemExit(main())
