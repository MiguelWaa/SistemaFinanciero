"""
SIGSF · Plan de Pruebas · PRUEBAS DE ACEPTACIÓN (BDD)

Los criterios de aceptación de las historias de usuario están escritos en
Gherkin en español (features/*.feature) para que el usuario los lea y los
firme; este archivo los convierte en pruebas automáticas con pytest-bdd +
Playwright contra el sistema en marcha.

Ejecutar:  pytest pruebas/aceptacion -v
"""
import re
import time
from decimal import Decimal, ROUND_HALF_UP

import pytest
from playwright.sync_api import expect
from pytest_bdd import given, parsers, scenarios, then, when

from conftest import URL, USUARIOS, aprobar_solicitud, evidencia, ingresar_portal, iniciar_sesion, ir, pestana_portal, solicitar_prestamo, tramites

scenarios("HU01_cambio_de_moneda.feature", "HU02_prestamo_desde_el_portal.feature",
          "HU03_control_de_acceso.feature", "HU04_bandeja_de_solicitudes.feature")


@pytest.fixture
def ctx():
    return {}


def api_token(page, rol):
    usuario, clave = USUARIOS[rol]
    r = page.request.post(URL + "/api/auth/login", data={"email": usuario, "password": clave})
    assert r.ok, r.text()
    return r.json()["token"]


def token_portal(page, documento):
    r = page.request.post(URL + "/api/portal/login", data={"documento": documento, "password": documento})
    assert r.ok, r.text()
    return r.json()


# ------------------------------- Dado -------------------------------

@given(parsers.parse('que inicié sesión como "{rol}"'))
def sesion(pagina, rol):
    iniciar_sesion(pagina, rol)


@given(parsers.parse('abrí el módulo "{modulo}"'))
@when(parsers.parse('abro el módulo "{modulo}"'))
def abrir_modulo(pagina, modulo):
    ir(pagina, modulo)


@given(parsers.parse('que tengo un token de "{rol}"'), target_fixture="token")
def token(pagina, rol):
    return api_token(pagina, rol)


@given(parsers.parse('que ingresé al portal con el documento "{documento}"'))
def portal(pagina, documento, ctx):
    ingresar_portal(pagina, documento)
    ctx["filas"] = tramites(pagina).count()
    ctx["documento"] = documento


@given("una cuenta de prueba nueva", target_fixture="cuenta")
def cuenta_nueva(pagina):
    t = api_token(pagina, "ADMIN")
    correo = f"bloqueo.{int(time.time() * 1000)}@sigsf.test"
    r = pagina.request.post(URL + "/api/usuarios", headers={"Authorization": "Bearer " + t},
                            data={"nombre": "Prueba", "apellido": "Bloqueo", "email": correo,
                                  "password": "Clave.2026", "idRol": 3, "estado": "ACTIVO"})
    assert r.ok, r.text()
    return {"correo": correo, "clave": "Clave.2026"}


@given(parsers.parse('que tengo un token del portal del documento "{documento}"'), target_fixture="sesion_portal")
def sesion_portal(pagina, documento):
    return token_portal(pagina, documento)


@given(parsers.parse('que el cliente "{documento}" solicitó un préstamo de "{monto}" a "{plazo}" meses'))
def solicitud_previa(pagina, documento, monto, plazo, ctx):
    s = token_portal(pagina, documento)
    r = pagina.request.post(f"{URL}/api/portal/{s['idCliente']}/prestamo",
                            headers={"Authorization": "Bearer " + s["token"]},
                            data={"montoPrestamo": float(monto), "plazoMeses": int(plazo)})
    assert r.ok, r.text()
    ctx["operacion"] = r.json()["operacion"]["idOperacion"]
    ctx["portal"] = s


# ------------------------------- Cuando -------------------------------

@when(parsers.parse('registro un cambio de "{monto}" en "{moneda}" para el primer cliente'))
def registrar_cambio(pagina, monto, moneda, ctx):
    combo = pagina.locator("#sec0_idTipoCambio")
    expect(combo.locator("option").nth(1)).to_be_attached()
    opcion = combo.locator("option", has_text=moneda).first
    ctx["venta"] = Decimal(re.search(r"venta ([0-9.]+)", opcion.inner_text()).group(1))
    ctx["monto"] = Decimal(monto)
    combo.select_option(value=opcion.get_attribute("value"))
    pagina.select_option("#sec0_idCliente", index=1)   # v5.2+: la primera opción es "Selecciona…"
    pagina.fill("#sec0_monto", monto)
    pagina.click("#sec0_guardar")


@when(parsers.parse("envío a la API un cambio de 100 a 3.75 con total manipulado 999999"), target_fixture="respuesta")
def cambio_manipulado(pagina, token):
    r = pagina.request.post(URL + "/api/operaciones", headers={"Authorization": "Bearer " + token},
                            data={"idCliente": 1, "monto": 100, "tipoCambio": 3.75, "totalRecibir": 999999})
    assert r.ok, r.text()
    return r.json()


@when(parsers.parse('solicito un préstamo de "{monto}" a "{plazo}" meses'))
def solicitar(pagina, monto, plazo):
    solicitar_prestamo(pagina, monto, plazo)


@when("escribo una contraseña incorrecta 5 veces")
def cinco_fallos(pagina, cuenta):
    for _ in range(5):
        pagina.request.post(URL + "/api/auth/login", data={"email": cuenta["correo"], "password": "mala"})


@when("consulto los trámites del cliente con id 1", target_fixture="respuesta")
def tramites_ajenos(pagina, sesion_portal):
    otro = 1 if sesion_portal["idCliente"] != 1 else 2
    return pagina.request.get(f"{URL}/api/portal/{otro}/operaciones",
                              headers={"Authorization": "Bearer " + sesion_portal["token"]})


@when("apruebo su solicitud en la bandeja")
def aprobar(pagina, ctx):
    ir(pagina, "solicitudes.html")
    pagina.wait_for_load_state("networkidle")
    expect(pagina.locator(f'#lista [data-ok="{ctx["operacion"]}"]')).to_be_visible()
    aprobar_solicitud(pagina, ctx["operacion"])
    expect(pagina.locator(f'#lista [data-ok="{ctx["operacion"]}"]')).to_have_count(0)
    evidencia(pagina, "aceptacion/CA-04.1_aprobada.png")


# ------------------------------- Entonces -------------------------------

@then("la operación aparece primera en la tabla con el total calculado")
def total_en_tabla(pagina, ctx):
    esperado = (ctx["monto"] * ctx["venta"]).quantize(Decimal("0.01"), ROUND_HALF_UP)
    fila = pagina.locator("#sec0_tabla tr").first
    expect(fila).to_contain_text(f"{esperado:,.2f}")
    evidencia(pagina, "aceptacion/CA-01.1_total_calculado.png")


@then("el sistema guarda 375.00 como total a recibir")
def total_servidor(respuesta):
    assert Decimal(str(respuesta["totalRecibir"])) == Decimal("375.00")


@then(parsers.parse('veo el mensaje "{mensaje}"'))
def mensaje(pagina, mensaje, ctx):
    expect(pagina.locator("#prMsg")).to_contain_text(mensaje)
    evidencia(pagina, f"aceptacion/CA-02_{ctx['documento']}.png")


@then("mi historial muestra el préstamo")
def historial_con_prestamo(pagina, ctx):
    expect(tramites(pagina)).to_have_count(ctx["filas"] + 1)
    pestana_portal(pagina, "historial")
    expect(pagina.locator("#histTabla")).to_contain_text(re.compile("pr[eé]stamo", re.I))


@then("mi historial no cambia")
def historial_igual(pagina, ctx):
    pagina.wait_for_timeout(500)
    assert tramites(pagina).count() == ctx["filas"]


@then(parsers.parse('veo el aviso "{texto}"'))
def aviso(pagina, texto):
    expect(pagina.get_by_text(texto)).to_be_visible()
    evidencia(pagina, "aceptacion/CA-03.1_cajero_sin_permiso.png")


@then("no se muestra ningún correo de usuario")
def sin_correos(pagina):
    expect(pagina.locator("#sec0_tabla")).not_to_contain_text("@sigsf.com")


@then(parsers.parse('el sistema responde "{texto}" aunque luego use la contraseña correcta'))
def bloqueada(pagina, cuenta, texto):
    r = pagina.request.post(URL + "/api/auth/login", data={"email": cuenta["correo"], "password": cuenta["clave"]})
    assert r.status == 400
    assert texto in r.json()["error"].lower()


@then("la API responde 403")
def responde_403(respuesta):
    assert respuesta.status == 403


@then(parsers.parse('la operación del cliente queda "{estado}" en su historial'))
def historial_estado(pagina, ctx, estado):
    s = ctx["portal"]
    r = pagina.request.get(f"{URL}/api/portal/{s['idCliente']}/operaciones",
                           headers={"Authorization": "Bearer " + s["token"]})
    ops = {o["idOperacion"]: o for o in r.json()}
    assert ops[ctx["operacion"]]["estado"] == estado
