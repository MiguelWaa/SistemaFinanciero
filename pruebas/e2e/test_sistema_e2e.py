"""
SIGSF · Plan de Pruebas · PRUEBAS DE SISTEMA (extremo a extremo, caja negra)

Se usa el sistema como lo usa una persona: navegador Chromium real →
frontend HTML/JS → API → servicios → PostgreSQL (SistemaFinanciero_test).
Cada prueba corresponde a un caso PS-xx del plan y deja su captura en
evidencias/pruebas/e2e/.

Ejecutar:  pytest pruebas/e2e -v   (con el servidor en marcha)
"""
import re
import zipfile
from decimal import Decimal, ROUND_HALF_UP

import pytest
from playwright.sync_api import expect

from conftest import (URL, aprobar_solicitud, cerrar_sesion, evidencia, ingresar_portal, iniciar_sesion, ir, pestana_portal,
                      solicitar_prestamo, tramites)


def test_PS_01_login_fallido_muestra_mensaje_generico(pagina):
    ir(pagina, "login.html")
    pagina.fill("#user", "admin@sigsf.com")
    pagina.fill("#pass", "clave-incorrecta")
    pagina.click("#btnEntrar")
    expect(pagina.locator("#aviso")).to_contain_text("incorrectos")
    expect(pagina).to_have_url(re.compile(r".*/login\.html"))
    evidencia(pagina, "e2e/PS-01_login_fallido.png")


def test_PS_02_login_admin_muestra_panel_con_indicadores(pagina):
    iniciar_sesion(pagina, "ADMIN")
    expect(pagina.locator("#saludo")).to_be_visible()
    expect(pagina.get_by_text("Clientes activos")).to_be_visible()
    expect(pagina.locator("#kpiClientes")).not_to_have_text("—")
    expect(pagina.locator("#navRol")).to_contain_text("Admin")
    evidencia(pagina, "e2e/PS-02_panel_admin.png")


def test_PS_03_sin_sesion_el_panel_redirige_al_login(pagina):
    ir(pagina, "index.html")
    pagina.wait_for_url(re.compile(r".*/login\.html.*"))
    evidencia(pagina, "e2e/PS-03_sin_sesion_redirige.png")


def test_PS_04_registrar_cliente_desde_el_modulo(pagina):
    iniciar_sesion(pagina)
    ir(pagina, "clientes y servicios financieros.html")
    documento = "6" + str(abs(hash(pagina)) % 10_000_000).zfill(7)
    pagina.fill("#sec0_documento", documento)
    pagina.fill("#sec0_nombres", "Rosa")
    pagina.fill("#sec0_apellidos", "Prueba Sistema")
    pagina.fill("#sec0_correo", "rosa.e2e@sigsf.test")
    pagina.click("#sec0_guardar")
    pagina.fill("#sec0_buscar", documento)
    expect(pagina.locator("#sec0_tabla")).to_contain_text("Rosa Prueba Sistema")
    evidencia(pagina, "e2e/PS-04_cliente_registrado.png")


def test_PS_05_cambio_de_moneda_calcula_el_total_en_el_servidor(pagina):
    iniciar_sesion(pagina, "CAJERO")
    ir(pagina, "operaciones financieras.html")
    combo = pagina.locator("#sec0_idTipoCambio")
    expect(combo.locator("option").nth(1)).to_be_attached()
    opcion = combo.locator("option", has_text="USD").first
    texto = opcion.inner_text()
    venta = Decimal(re.search(r"venta ([0-9.]+)", texto).group(1))
    combo.select_option(value=opcion.get_attribute("value"))
    pagina.select_option("#sec0_idCliente", index=1)   # v5.2+: la primera opción es "Selecciona…"
    pagina.fill("#sec0_monto", "250")
    pagina.click("#sec0_guardar")
    esperado = (Decimal("250") * venta).quantize(Decimal("0.01"), ROUND_HALF_UP)
    primera = pagina.locator("#sec0_tabla tr").first
    expect(primera).to_contain_text("S/ 250.00")
    expect(primera).to_contain_text(f"{esperado:,.2f}")
    evidencia(pagina, "e2e/PS-05_cambio_calculado.png")


def test_PS_06_titulos_de_tabla_sin_texto_duplicado(pagina):
    # Defecto DEF-009 (visual): decía "Registros de registrar cambio de moneda".
    iniciar_sesion(pagina)
    for modulo in ["operaciones financieras.html", "clientes y servicios financieros.html", "almacen.html"]:
        ir(pagina, modulo)
        titulo = pagina.locator("h2", has_text="Registros").first
        expect(titulo).to_be_visible()
        assert "registrar" not in titulo.inner_text().lower(), titulo.inner_text()
    evidencia(pagina, "e2e/PS-06_titulo_tabla.png")


def test_PS_07_aprobar_solicitud_pendiente(pagina):
    iniciar_sesion(pagina, "ADMIN")
    ir(pagina, "solicitudes.html")
    pagina.wait_for_load_state("networkidle")          # la bandeja termina de cargar antes de actuar
    boton = pagina.locator("#lista [data-ok]").first
    expect(boton).to_be_visible()
    numero = boton.get_attribute("data-ok")
    aprobar_solicitud(pagina, numero)
    # Resultado verificable: la solicitud sale de la bandeja de pendientes.
    expect(pagina.locator(f'#lista [data-ok="{numero}"]')).to_have_count(0)
    evidencia(pagina, "e2e/PS-07_solicitud_aprobada.png")


def test_PS_08_cajero_no_puede_ver_usuarios(pagina):
    iniciar_sesion(pagina, "CAJERO")
    ir(pagina, "usuarios.html")
    expect(pagina.get_by_text("no tiene permiso")).to_be_visible()
    expect(pagina.locator("#sec0_tabla")).not_to_contain_text("@sigsf.com")
    evidencia(pagina, "e2e/PS-08_cajero_sin_permiso.png")


def test_PS_09_portal_solicita_prestamo_y_ve_su_cuota(pagina):
    ingresar_portal(pagina, "71000006")
    solicitar_prestamo(pagina, 3000, 12)
    # 3000 × (1 + 0.15 × 12) / 12 = 700.00
    expect(pagina.locator("#prMsg")).to_contain_text("700.00")
    pestana_portal(pagina, "historial")
    expect(pagina.locator("#histTabla")).to_contain_text(re.compile("pr[eé]stamo", re.I))
    evidencia(pagina, "e2e/PS-09_portal_prestamo.png", completa=True)


def test_PS_10_portal_rechaza_prestamo_menor_al_minimo(pagina):
    ingresar_portal(pagina, "71000004")
    filas_antes = tramites(pagina).count()
    solicitar_prestamo(pagina, 50)
    expect(pagina.locator("#prMsg")).to_contain_text("mínimo")
    pagina.wait_for_timeout(500)
    assert tramites(pagina).count() == filas_antes, "no debe quedar una operación huérfana"
    evidencia(pagina, "e2e/PS-10_portal_monto_minimo.png")


def test_PS_11_exportar_operaciones_a_excel(pagina):
    iniciar_sesion(pagina)
    ir(pagina, "operaciones financieras.html")
    pagina.get_by_role("button", name="Exportar").first.click()
    with pagina.expect_download() as descarga:
        pagina.get_by_role("button", name="Excel").first.click()
    archivo = descarga.value.path()
    assert zipfile.is_zipfile(archivo), "el .xlsx debe ser un ZIP de Office Open XML"
    with zipfile.ZipFile(archivo) as z:
        assert "xl/workbook.xml" in z.namelist()
    assert descarga.value.suggested_filename.endswith(".xlsx")


def test_PS_12_cerrar_sesion_revoca_el_token(pagina):
    iniciar_sesion(pagina)
    token = pagina.evaluate("sessionStorage.getItem('sigsf_token') || localStorage.getItem('sigsf_token')")
    assert token
    cerrar_sesion(pagina)
    pagina.wait_for_url(re.compile(r".*/login\.html.*"))
    respuesta = pagina.request.get(URL + "/api/clientes", headers={"Authorization": "Bearer " + token})
    assert respuesta.status == 401
    evidencia(pagina, "e2e/PS-12_sesion_cerrada.png")


@pytest.mark.parametrize("ancho", [390])
def test_PS_13_portal_usable_en_celular(pagina, ancho):
    pagina.set_viewport_size({"width": ancho, "height": 844})
    ingresar_portal(pagina, "71000007")
    desborde = pagina.evaluate("document.documentElement.scrollWidth - window.innerWidth")
    assert desborde <= 1, f"la página se desborda {desborde}px en horizontal"
    pestana_portal(pagina, "tramites")
    expect(pagina.locator("#fPrestamo button[type=submit]")).to_be_visible()
    evidencia(pagina, "e2e/PS-13_portal_movil.png")
