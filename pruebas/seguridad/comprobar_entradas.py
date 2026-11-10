"""Comprobaciones locales acotadas para SIGSF v5.6.0. Python 3, sin dependencias.
Uso: python pruebas_seguridad_adicionales.py --base-url http://localhost:8091 --output resultado.json
Requiere servidor de demostración en memoria y credenciales de prueba del ZIP original.
No ejecuta cargas, comandos remotos, ataques de disponibilidad ni escaneos externos.
"""
import argparse,json,os,urllib.request,urllib.error,urllib.parse
p=argparse.ArgumentParser();p.add_argument('--base-url',default='http://localhost:8091');p.add_argument('--output',default='resultado_seguridad.json');a=p.parse_args()
u=urllib.parse.urlparse(a.base_url)
if u.scheme!='http' or u.hostname not in ('localhost','127.0.0.1','::1'):raise SystemExit('Utilizar exclusivamente el servidor HTTP local de demostracion.')
def request(body,ctype='application/json'):
 body=body if isinstance(body,bytes) else body.encode()
 try:
  r=urllib.request.urlopen(urllib.request.Request(a.base_url.rstrip('/')+'/api/auth/login',data=body,headers={'Content-Type':ctype}),timeout=5);return r.status,r.read().decode()
 except urllib.error.HTTPError as e:return e.code,e.read().decode()
corpus=['','{','[','{"email":}','{"email":"x"','{"email":null}','{"email":[]}','{"email":{}}','{"email":1}','{"email":true}','{}','[]','null','true','1','{"email":"x","password":}','{"email":"x","password":"\\uZZZZ"}','{"email":"x","password":"\\x"}','\x00','{"email":"fuzz@example.test","password":"x"} trailing']
results=[]
for i,body in enumerate(corpus,1):
 code,text=request(body);results.append({'id':f'FZ-{i:02}','http':code,'expected':'4xx y sin token','passed':400<=code<500 and '"token"' not in text})
credentials={'email':os.getenv('SIGSF_TEST_EMAIL','admin@sigsf.com'),'password':os.getenv('SIGSF_TEST_PASSWORD','admin123')}
good=json.dumps(credentials)
for id,body,ctype,expected in [('JSON-SUF',good+' trailing','application/json',400),('DES-JAVA',bytes.fromhex('aced000570'),'application/x-java-serialized-object',415),('CFG-TYPE',good,'text/plain',415)]:
 code,text=request(body,ctype);results.append({'id':id,'http':code,'expected':expected,'passed':code==expected,'token_present':'"token"' in text})
code,text=request(json.dumps({**credentials,'@class':'java.lang.String'}));results.append({'id':'DES-META','http':code,'expected':'200; revisar tambien que el parser solo crea Map/List','passed':code==200})
result={'scope':'local, memoria, credenciales de demostracion','pass':sum(x['passed'] for x in results),'total':len(results),'tests':results}
with open(a.output,'w',encoding='utf-8') as f:json.dump(result,f,ensure_ascii=False,indent=2)
print(json.dumps({'total':result['total'],'cumplen':result['pass'],'observaciones':[x['id'] for x in results if not x['passed']]},ensure_ascii=False))
raise SystemExit(0 if result['pass']==result['total'] else 1)
