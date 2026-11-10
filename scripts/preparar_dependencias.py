#!/usr/bin/env python3
"""Descarga únicamente JAR ausentes, con versiones explícitas, desde Maven Central."""
from pathlib import Path
import urllib.request
ROOT=Path(__file__).resolve().parents[1]
FILES={
 'lib/postgresql.jar':'org/postgresql/postgresql/42.7.4/postgresql-42.7.4.jar',
 'lib/slf4j-api-2.0.16.jar':'org/slf4j/slf4j-api/2.0.16/slf4j-api-2.0.16.jar',
 'lib/logback-core-1.5.12.jar':'ch/qos/logback/logback-core/1.5.12/logback-core-1.5.12.jar',
 'lib/logback-classic-1.5.12.jar':'ch/qos/logback/logback-classic/1.5.12/logback-classic-1.5.12.jar',
 'lib/test/junit-4.13.2.jar':'junit/junit/4.13.2/junit-4.13.2.jar',
 'lib/test/hamcrest-core-1.3.jar':'org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar'}
if __name__=='__main__':
 for target,artifact in FILES.items():
  p=ROOT/target
  if p.is_file():continue
  print('Descargando '+target,flush=True);p.parent.mkdir(parents=True,exist_ok=True)
  data=urllib.request.urlopen('https://repo.maven.apache.org/maven2/'+artifact,timeout=60).read()
  # Publicación atómica: una descarga interrumpida no deja un .jar aparentemente válido.
  tmp=p.with_suffix('.tmp');tmp.write_bytes(data);tmp.replace(p)
 print('Dependencias Java disponibles.')
