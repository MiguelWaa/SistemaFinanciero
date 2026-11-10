#!/usr/bin/env python3
"""Empaqueta las clases validadas en dist/SIGSF.jar. No compila ni instala dependencias."""
from pathlib import Path
import os,subprocess,datetime
ROOT=Path(__file__).resolve().parents[1];classes=ROOT/'build/integrado/classes'
if not (classes/'com/utp/sistemafinanciero/ServerMain.class').exists():raise SystemExit('Primero ejecute scripts/validar_proyecto.py')
def git(*args):
 try:return subprocess.check_output(['git',*args],cwd=ROOT,text=True,stderr=subprocess.DEVNULL).strip()
 except Exception:return 'no-disponible'
version=(ROOT/'VERSION').read_text().strip()
(classes/'version.properties').write_text('version='+version+'\ncommit='+git('rev-parse','HEAD')+'\nrama='+git('branch','--show-current')+'\nfecha='+datetime.datetime.now(datetime.timezone.utc).isoformat()+'\n',encoding='utf8')
manifest=ROOT/'build/integrado/MANIFEST.MF'
manifest.write_text('Manifest-Version: 1.0\nMain-Class: com.utp.sistemafinanciero.ServerMain\nClass-Path: ../lib/postgresql.jar ../lib/slf4j-api-2.0.16.jar ../lib/logback-core-1.5.12.jar ../lib/logback-classic-1.5.12.jar\n\n')
(ROOT/'dist').mkdir(exist_ok=True)
subprocess.run([os.getenv('SIGSF_JAVA','java'),'-m','jdk.jartool/sun.tools.jar.Main','--create','--file',str(ROOT/'dist/SIGSF.jar'),'--manifest',str(manifest),'-C',str(classes),'.'],cwd=ROOT,check=True)
print(ROOT/'dist/SIGSF.jar')
