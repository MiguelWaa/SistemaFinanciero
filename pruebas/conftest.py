"""
SIGSF · Plan de Pruebas · Configuración común de las pruebas de SISTEMA (e2e/)
y de ACEPTACIÓN (aceptacion/) con Playwright + pytest.

Requisitos (una sola vez):
    pip install -r pruebas/requirements.txt
    playwright install chromium

El servidor debe estar en marcha sobre la base de pruebas (ver
scripts/pruebas/ejecutar_pruebas_sistema.*). URL por defecto:
http://localhost:8092 (variable SIGSF_URL para cambiarla).
"""
import os
import pathlib
import re

import pytest

RAIZ = pathlib.Path(__file__).resolve().parent.parent
EVIDENCIAS = RAIZ / "evidencias" / "pruebas"
URL = os.environ.get("SIGSF_URL", "http://localhost:8092").rstrip("/")

# Laboratorio sin internet: si se indica una copia local de Font Awesome 6.5
# (npm i @fortawesome/fontawesome-free@6.5.1), los íconos se sirven desde
# ahí para que las capturas se vean igual que en producción.
FA_LOCAL = os.environ.get("SIGSF_FA_LOCAL", "")

USUARIOS = {
    "ADMIN": ("admin@sigsf.com", "admin123"),
    "CAJERO": ("lucia.ramos@sigsf.com", "lucia123"),
    "ANALISTA": ("jorge.torres@sigsf.com", "jorge123"),
}


@pytest.fixture(scope="session")
def browser_context_args(browser_context_args):
    return {**browser_context_args, "viewport": {"width": 1366, "height": 800},
            "locale": "es-PE", "timezone_id": "America/Lima", "accept_downloads": True}


@pytest.fixture
def pagina(page):
    """Página de Playwright con tiempos de espera del plan (10 s) y sin depender de CDNs."""
    page.set_default_timeout(10_000)
    if FA_LOCAL:
        def servir_iconos(route):
            ruta = route.request.url.split("/font-awesome/")[1].split("/", 1)[1].split("?")[0]
            route.fulfill(path=os.path.join(FA_LOCAL, ruta))
        page.route(re.compile(r"https://cdnjs\.cloudflare\.com/ajax/libs/font-awesome/.*"), servir_iconos)
    page.route(re.compile(r"https://fonts\.(googleapis|gstatic)\.com/.*"),
               lambda r: r.fulfill(body="", content_type="text/css"))
    return page


def ir(page, ruta):
    page.goto(URL + "/" + ruta.lstrip("/"), wait_until="domcontentloaded")


def iniciar_sesion(page, rol="ADMIN"):
    usuario, clave = USUARIOS[rol]
    ir(page, "login.html")
    page.fill("#user", usuario)
    page.fill("#pass", clave)
    page.click("#btnEntrar")
    page.wait_for_url(re.compile(r".*/index\.html"))


def ingresar_portal(page, documento="71000004"):
    ir(page, "portal.html")
    page.fill("#liDoc", documento)
    page.fill("#liPass", documento)
    page.click("#btnLogin")
    page.wait_for_url(re.compile(r".*/portal-inicio\.html"))
    # Esperar a que el historial termine de cargar (puede estar en una pestaña oculta).
    page.locator("#histTabla").filter(has_not_text="Cargando").wait_for(state="attached")
    page.wait_for_function("!document.querySelector('#histTabla').innerText.includes('Cargando')")


def tramites(page):
    """Filas reales del historial del portal (excluye la fila vacía o de carga)."""
    return page.locator("#histTabla tr:not(.empty-row):has(td:nth-child(2))")


def pestana_portal(page, nombre):
    """Portal v5.2+: las secciones son pestañas (inicio, pagar, cotizar, tramites, historial, datos)."""
    page.click(f'#tabs [data-tab="{nombre}"]')


def solicitar_prestamo(page, monto, plazo="12"):
    pestana_portal(page, "tramites")
    page.fill("#prMonto", str(monto))
    page.select_option("#prPlazo", str(plazo))
    page.click("#fPrestamo button[type=submit]")


def confirmar(page, boton):
    """v5.2+: las acciones delicadas piden confirmación en una ventana modal de Bootstrap."""
    modal = page.locator(".modal.show")
    modal.wait_for()
    page.wait_for_timeout(300)                     # termina la animación de entrada (fade)
    modal.get_by_role("button", name=boton, exact=True).click()
    modal.wait_for(state="detached")


def aprobar_solicitud(page, numero):
    """Aprueba una solicitud de la bandeja y espera la respuesta del servidor (PUT …/aprobar)."""
    with page.expect_response(lambda r: f"/solicitudes/{numero}/aprobar" in r.url) as resp:
        page.locator(f'#lista [data-ok="{numero}"]').click()
        confirmar(page, "Aprobar")
    assert resp.value.ok, resp.value.text()


def cerrar_sesion(page):
    """El cierre de sesión está en el menú del usuario (barra superior)."""
    page.locator("#navUser").click()
    page.click("#logoutBtn")


def evidencia(page, nombre, completa=False):
    """Guarda la captura de pantalla del caso en evidencias/pruebas/<carpeta>/."""
    destino = EVIDENCIAS / nombre
    destino.parent.mkdir(parents=True, exist_ok=True)
    page.screenshot(path=str(destino), full_page=completa)
    return destino
