#!/usr/bin/env python3
"""Capturas de pantalla de la aplicación desplegada (Playwright para Python + Chrome).

  python scripts/capturar_evidencias.py --url http://127.0.0.1:8095 --salida evidencias/v5.11.0/capturas \
         --prefijo 5.11.0 [--paginas login,index,versiones]

Inicia sesión con la cuenta de demostración, recorre las páginas indicadas y
guarda PNG a 1440x900. Registra errores de consola y respuestas >= 400.
"""
import argparse, json
from pathlib import Path
from playwright.sync_api import sync_playwright

PAGINAS = {'login': 'login.html', 'index': 'index.html', 'versiones': 'versiones.html',
           'operaciones': 'operaciones financieras.html', 'clientes': 'clientes y servicios financieros.html',
           'reportes': 'reportes.html', 'seguridad': 'seguridad.html', 'respaldos': 'respaldos.html',
           'pagos': 'pagos.html', 'usuarios': 'usuarios.html', 'welcome': 'welcome.html'}

def main():
    p = argparse.ArgumentParser(); p.add_argument('--url', required=True); p.add_argument('--salida', required=True)
    p.add_argument('--prefijo', default=''); p.add_argument('--paginas', default='login,index,versiones')
    p.add_argument('--chrome', default='/usr/bin/google-chrome'); p.add_argument('--usuario', default='admin@sigsf.com')
    p.add_argument('--clave', default='admin123'); a = p.parse_args()
    out = Path(a.salida); out.mkdir(parents=True, exist_ok=True); reg = {'url': a.url, 'capturas': [], 'errores': []}
    with sync_playwright() as pw:
        b = pw.chromium.launch(executable_path=a.chrome, args=['--no-sandbox', '--autoplay-policy=no-user-gesture-required'])
        pg = b.new_page(viewport={'width': 1440, 'height': 900}, device_scale_factor=1)
        pg.on('console', lambda m: m.type == 'error' and reg['errores'].append('consola: ' + m.text))
        pg.on('response', lambda r: r.status >= 400 and reg['errores'].append(f'HTTP {r.status} {r.url}'))
        base = a.url.rstrip('/') + '/'
        for i, nombre in enumerate(a.paginas.split(','), 1):
            if nombre == 'login':
                pg.goto(base + 'login.html'); pg.wait_for_timeout(1200)
                pg.fill('#user', a.usuario); pg.fill('#pass', a.clave)
                f = out / f'{a.prefijo}{i:02d}_login.png'; pg.screenshot(path=str(f)); reg['capturas'].append(f.name)
                pg.click('#btnEntrar'); pg.wait_for_timeout(2500)
                continue
            pg.goto(base + PAGINAS.get(nombre, nombre)); pg.wait_for_load_state('networkidle'); pg.wait_for_timeout(1800)
            f = out / f'{a.prefijo}{i:02d}_{nombre}.png'; pg.screenshot(path=str(f)); reg['capturas'].append(f.name)
        b.close()
    (out / f'{a.prefijo}capturas.json').write_text(json.dumps(reg, ensure_ascii=False, indent=2), encoding='utf8')
    print(json.dumps(reg, ensure_ascii=False, indent=2))

if __name__ == '__main__':
    main()
