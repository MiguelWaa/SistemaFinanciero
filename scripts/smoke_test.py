#!/usr/bin/env python3
"""Prueba de humo posterior al despliegue de SIGSF (solo biblioteca estándar).

Comprueba que la versión desplegada responde, sirve el frontend, aplica las
cabeceras de seguridad y protege la API. Escribe un JSON con cada verificación.

  python scripts/smoke_test.py --url http://127.0.0.1:8091 --version 5.11.0 \
         --salida evidencias/v5.11.0/smoke.json [--login admin@sigsf.com:admin123]

Código de salida 0 = todas aprobadas; 1 = alguna falló (el despliegue debe
revertirse). --esperar N reintenta la salud durante N segundos.
"""
import argparse, datetime, json, sys, time, urllib.error, urllib.request

def pedir(url, metodo='GET', datos=None, cabeceras=None, espera=5):
    req = urllib.request.Request(url, data=datos, method=metodo, headers=cabeceras or {})
    try:
        with urllib.request.urlopen(req, timeout=espera) as r:
            return r.status, dict(r.headers), r.read(65536)
    except urllib.error.HTTPError as e:
        return e.code, dict(e.headers), e.read(65536)

def main():
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument('--url', default='http://127.0.0.1:8091')
    p.add_argument('--version', help='Versión esperada en /api/version')
    p.add_argument('--login', help='usuario:clave para probar inicio de sesión (solo datos de demostración)')
    p.add_argument('--esperar', type=int, default=60)
    p.add_argument('--salida')
    a = p.parse_args()
    base = a.url.rstrip('/')
    checks = []

    def check(nombre, ok, detalle):
        checks.append({'verificacion': nombre, 'estado': 'APROBADO' if ok else 'FALLIDO', 'detalle': detalle})
        print(('[OK]   ' if ok else '[FALLO]') + ' ' + nombre + ' - ' + detalle, flush=True)

    # 1. Salud con reintentos (arranque en frío)
    inicio = time.time(); estado = None; cuerpo = b''
    while time.time() - inicio < a.esperar:
        try:
            estado, _, cuerpo = pedir(base + '/api/health', espera=3)
            if estado == 200: break
        except Exception:
            pass
        time.sleep(1)
    salud = {}
    try: salud = json.loads(cuerpo or b'{}')
    except ValueError: pass
    check('GET /api/health', estado == 200 and salud.get('status') == 'ok',
          f'HTTP {estado} {salud} en {time.time()-inicio:.1f}s')
    if estado != 200:
        return terminar(a, base, checks)

    # 2. Frontend y cabeceras de seguridad
    estado, cab, cuerpo = pedir(base + '/login.html')
    cab = {k.lower(): v for k, v in cab.items()}
    check('Frontend /login.html', estado == 200 and b'<html' in cuerpo.lower(), f'HTTP {estado}, {len(cuerpo)} bytes')
    csp = cab.get('content-security-policy', '')
    check('Cabecera CSP', "script-src 'self'" in csp, csp[:90] or 'ausente')
    check('Cabecera X-Content-Type-Options', cab.get('x-content-type-options') == 'nosniff', cab.get('x-content-type-options', 'ausente'))

    # 3. API protegida sin sesión
    estado, _, _ = pedir(base + '/api/usuarios')
    check('API protegida sin token', estado in (401, 403), f'GET /api/usuarios -> HTTP {estado}')

    # 4. Content-Type estricto
    estado, _, _ = pedir(base + '/api/auth/login', 'POST', b'x', {'Content-Type': 'text/plain'})
    check('Content-Type no admitido', estado == 415, f'POST text/plain -> HTTP {estado}')

    # 5. Inicio de sesión y versión (opcional: cuenta de demostración o de monitoreo)
    if a.login:
        usuario, clave = a.login.split(':', 1)
        datos = json.dumps({'email': usuario, 'password': clave}).encode()
        estado, _, cuerpo = pedir(base + '/api/auth/login', 'POST', datos, {'Content-Type': 'application/json'})
        token = ''
        try: token = json.loads(cuerpo).get('token', '')
        except ValueError: pass
        check('Login de demostración', estado == 200 and len(token) > 20, f'HTTP {estado}, token {"emitido" if token else "ausente"}')
        if token:
            aut = {'Authorization': 'Bearer ' + token}
            # /api/version exige sesión: confirma que corre la versión recién desplegada
            estado, _, cuerpo = pedir(base + '/api/version', cabeceras=aut)
            info = json.loads(cuerpo or b'{}') if estado == 200 else {}
            check('Versión desplegada (/api/version)', estado == 200 and (not a.version or info.get('version') == a.version),
                  f'HTTP {estado} esperada={a.version} recibida={info.get("version")} etiqueta={info.get("etiqueta")}')
            estado, _, cuerpo = pedir(base + '/api/dashboard', cabeceras=aut)
            check('Dashboard autenticado', estado == 200, f'HTTP {estado}, {len(cuerpo)} bytes')
    return terminar(a, base, checks)

def terminar(a, base, checks):
    ok = all(c['estado'] == 'APROBADO' for c in checks) and len(checks) > 1
    res = {'herramienta': 'scripts/smoke_test.py', 'url': base, 'version_esperada': a.version,
           'fecha_utc': datetime.datetime.now(datetime.timezone.utc).isoformat(timespec='seconds'),
           'estado': 'APROBADO' if ok else 'FALLIDO',
           'aprobadas': sum(c['estado'] == 'APROBADO' for c in checks), 'total': len(checks), 'verificaciones': checks}
    if a.salida:
        from pathlib import Path
        Path(a.salida).parent.mkdir(parents=True, exist_ok=True)
        Path(a.salida).write_text(json.dumps(res, ensure_ascii=False, indent=2), encoding='utf8')
    print(f"Resultado: {res['estado']} ({res['aprobadas']}/{res['total']})")
    return 0 if ok else 1

if __name__ == '__main__':
    sys.exit(main())
