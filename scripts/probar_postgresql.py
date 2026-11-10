#!/usr/bin/env python3
"""Crea una BD local NUEVA, instala SQL y ejecuta 7 pruebas JDBC. No reutiliza ni borra BD existentes."""
import datetime,json,os,pathlib,shutil,subprocess,sys
ROOT=pathlib.Path(__file__).resolve().parents[1]
def main():
 os.chdir(ROOT);out=ROOT/'pruebas/reportes/postgresql';out.mkdir(parents=True,exist_ok=True)
 result={'herramienta':'PostgreSQL + JDBC + JUnit','estado':'BLOQUEADO','pasos':[]}
 def save(): (out/'resultado.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf8')
 for tool in ['psql','createdb','java']:
  if not shutil.which(tool):result['motivo']='No está instalado '+tool;save();print(result['motivo']);return 2
 if not (ROOT/'build/integrado/test/com/utp/sistemafinanciero/integracion/PostgresqlSeguridadIT.class').exists():
  result['motivo']='Primero ejecute python scripts/validar_proyecto.py --solo-junit';save();print(result['motivo']);return 2
 host=os.getenv('SIGSF_DB_HOST','127.0.0.1')
 if host not in ['localhost','127.0.0.1','::1']:raise SystemExit('Este ejecutor solo prepara bases locales de pruebas.')
 if not os.getenv('SIGSF_DB_PASSWORD'):result['motivo']='Defina SIGSF_DB_PASSWORD del servidor local de pruebas';save();print(result['motivo']);return 2
 name='sigsf_test_'+datetime.datetime.now(datetime.timezone.utc).strftime('%Y%m%d%H%M%S%f');result['base_datos']=name
 env=os.environ.copy();env.update(SIGSF_DB_HOST=host,SIGSF_DB_PORT=os.getenv('SIGSF_DB_PORT','5432'),SIGSF_DB_USER=os.getenv('SIGSF_DB_USER','postgres'),SIGSF_DB_NAME=name,SIGSF_PERSISTENCIA='postgresql',PGPASSWORD=os.environ['SIGSF_DB_PASSWORD'])
 params=['-h',host,'-p',env['SIGSF_DB_PORT'],'-U',env['SIGSF_DB_USER']]
 def run(label,cmd):
  with (out/(label+'.txt')).open('w') as f:r=subprocess.run(cmd,cwd=ROOT,env=env,stdout=f,stderr=subprocess.STDOUT,timeout=120)
  result['pasos'].append({'nombre':label,'salida':r.returncode});save();return r.returncode==0
 # createdb falla si ya existe; schema.sql solo recibe una base recién creada.
 if not run('crear_base',['createdb']+params+[name]):result['motivo']='No se pudo crear una BD nueva; consulte crear_base.txt';save();return 1
 for f in ['schema.sql','datos_ejemplo.sql','portal_cliente.sql','seguridad.sql','version_esquema.sql','pagos_qr.sql']:
  if not run(f,['psql']+params+['-d',name,'-v','ON_ERROR_STOP=1','-f',str(ROOT/'db'/f)]):result['estado']='FALLIDO';save();return 1
 libs=list((ROOT/'lib').glob('*.jar'))+list((ROOT/'lib/test').glob('*.jar'))
 cp=os.pathsep.join(map(str,[ROOT/'build/integrado/test',ROOT/'build/integrado/classes']+libs))
 ok=run('junit_jdbc',['java','-cp',cp,'org.junit.runner.JUnitCore','com.utp.sistemafinanciero.integracion.PostgresqlSeguridadIT'])
 result['estado']='APROBADO' if ok else 'FALLIDO';result['nota']='La BD se conserva para inspección; este script no la elimina.';save();print('Resultado: '+str(out/'resultado.json'));return 0 if ok else 1
if __name__=='__main__':raise SystemExit(main())
