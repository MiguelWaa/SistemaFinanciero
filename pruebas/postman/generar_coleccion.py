"""Genera SIGSF_API.postman_collection.json (Postman v2.1).

Se mantiene en Python para que los casos (id, petición, aserciones) se lean
como una tabla del Plan de Pruebas. Ejecutar:  python generar_coleccion.py
"""
import json

def req(caso, nombre, metodo, ruta, token=None, cuerpo=None, pruebas=(), pre=None):
    h = [{"key": "Content-Type", "value": "application/json"}]
    if token:
        h.append({"key": "Authorization", "value": "Bearer {{" + token + "}}"})
    item = {
        "name": f"{caso} · {nombre}",
        "request": {
            "method": metodo,
            "header": h,
            "url": {"raw": "{{base}}" + ruta, "host": ["{{base}}"], "path": [p for p in ruta.strip("/").split("/")]},
        },
        "event": [{"listen": "test", "script": {"type": "text/javascript", "exec": list(pruebas)}}],
    }
    if cuerpo is not None:
        item["request"]["body"] = {"mode": "raw", "raw": json.dumps(cuerpo, ensure_ascii=False),
                                   "options": {"raw": {"language": "json"}}}
    if pre:
        item["event"].append({"listen": "prerequest", "script": {"type": "text/javascript", "exec": pre}})
    return item

def codigo(n):
    return f'pm.test("HTTP {n}", () => pm.response.to.have.status({n}));'

TIEMPO = 'pm.test("Responde en menos de 800 ms", () => pm.expect(pm.response.responseTime).to.be.below(800));'
NO_500 = 'pm.test("Nunca error 500", () => pm.expect(pm.response.code).to.be.below(500));'

carpetas = [
    {"name": "1 · Salud y autenticación", "item": [
        req("PI-NEW-01", "Health público", "GET", "/api/health", pruebas=[codigo(200), TIEMPO,
            'pm.test("Persistencia PostgreSQL", () => pm.expect(pm.response.json().persistencia).to.include("PostgreSQL"));']),
        req("PI-NEW-02", "Sin token responde 401", "GET", "/api/clientes", pruebas=[codigo(401)]),
        req("PI-NEW-03", "Login con clave errónea: mensaje genérico", "POST", "/api/auth/login",
            cuerpo={"email": "admin@sigsf.com", "password": "clave-incorrecta"},
            pruebas=[codigo(400), 'pm.test("No revela si el correo existe", () => pm.expect(pm.response.json().error).to.eql("Usuario o contraseña incorrectos."));']),
        req("PI-NEW-04", "Login ADMIN", "POST", "/api/auth/login",
            cuerpo={"email": "admin@sigsf.com", "password": "admin123"},
            pruebas=[codigo(200), TIEMPO, 'const j = pm.response.json();',
                     'pm.test("Devuelve token y rol", () => { pm.expect(j.token).to.be.a("string"); pm.expect(j.rol).to.eql("ADMIN"); });',
                     'pm.test("No expone la contraseña", () => pm.expect(pm.response.text()).to.not.include("pbkdf2"));',
                     'pm.collectionVariables.set("token", j.token);']),
        req("PI-NEW-05", "Login CAJERO", "POST", "/api/auth/login",
            cuerpo={"email": "lucia.ramos@sigsf.com", "password": "lucia123"},
            pruebas=[codigo(200), 'pm.collectionVariables.set("tokenCajero", pm.response.json().token);']),
        req("PI-NEW-06", "Inyección SQL en el login", "POST", "/api/auth/login",
            cuerpo={"email": "admin@sigsf.com' OR '1'='1", "password": "x' OR '1'='1"},
            pruebas=[codigo(400), 'pm.test("Sin token", () => pm.expect(pm.response.json().token).to.be.undefined);']),
    ]},
    {"name": "2 · Clientes (CRUD)", "item": [
        req("PI-NEW-07", "Listar clientes", "GET", "/api/clientes", token="token",
            pruebas=[codigo(200), TIEMPO, 'pm.test("Al menos 100 clientes", () => pm.expect(pm.response.json().length).to.be.at.least(100));']),
        req("PI-NEW-08", "Registrar cliente", "POST", "/api/clientes", token="token",
            cuerpo={"nombres": "Rosa", "apellidos": "Newman", "documento": "{{doc}}"},
            pre=['pm.collectionVariables.set("doc", "6" + Math.floor(1000000 + Math.random() * 8999999));'],
            pruebas=[codigo(200), 'const j = pm.response.json();',
                     'pm.test("Estado ACTIVO y fecha automática", () => { pm.expect(j.estado).to.eql("ACTIVO"); pm.expect(j.fechaRegistro).to.be.a("string"); });',
                     'pm.collectionVariables.set("idCliente", j.idCliente);']),
        req("PI-NEW-09", "Documento duplicado", "POST", "/api/clientes", token="token",
            cuerpo={"nombres": "Otra", "documento": "{{doc}}"},
            pruebas=[codigo(400), 'pm.test("Mensaje claro", () => pm.expect(pm.response.json().error).to.include("Ya existe"));']),
        req("PI-NEW-10", "Estado inicial inventado", "POST", "/api/clientes", token="token",
            cuerpo={"nombres": "Mal", "documento": "69999999", "estado": "VIP_SIN_CONTROL"},
            pruebas=[codigo(400)]),
        req("PI-NEW-11", "Bloquear cliente", "PUT", "/api/clientes/{{idCliente}}/estado", token="token",
            cuerpo={"estado": "bloqueado"},
            pruebas=[codigo(200), 'pm.test("Normaliza a BLOQUEADO", () => pm.expect(pm.response.json().estado).to.eql("BLOQUEADO"));']),
        req("PI-NEW-12", "Eliminar cliente", "DELETE", "/api/clientes/{{idCliente}}", token="token",
            pruebas=[codigo(200)]),
    ]},
    {"name": "3 · Operaciones y préstamos", "item": [
        req("PI-NEW-13", "Cambio: total = monto × tipo de cambio", "POST", "/api/operaciones", token="token",
            cuerpo={"idCliente": 1, "monto": 100, "tipoCambio": 3.75, "totalRecibir": 999999},
            pruebas=[codigo(200), 'const j = pm.response.json();',
                     'pm.test("375.00 calculado en el servidor (ignora el total enviado)", () => pm.expect(j.totalRecibir).to.eql(375));',
                     'pm.collectionVariables.set("idOperacion", j.idOperacion);']),
        req("PI-NEW-14", "Estado de operación inventado", "POST", "/api/operaciones", token="token",
            cuerpo={"idCliente": 1, "monto": 100, "tipoCambio": 3.7, "estado": "HACKEADO"}, pruebas=[codigo(400)]),
        req("PI-NEW-15", "Monto 1e20 no provoca 500", "POST", "/api/operaciones", token="token",
            cuerpo={"idCliente": 1, "monto": 1e20, "tipoCambio": 3.7}, pruebas=[codigo(400), NO_500]),
        req("PI-NEW-16", "Cliente inexistente", "POST", "/api/operaciones", token="token",
            cuerpo={"idCliente": 999999, "monto": 100, "tipoCambio": 3.7},
            pruebas=[codigo(400), 'pm.test("Mensaje claro", () => pm.expect(pm.response.json().error).to.include("no existe"));']),
        req("PI-NEW-17", "Préstamo: cuota con interés simple", "POST", "/api/prestamos", token="token",
            cuerpo={"idOperacion": "{{idOperacion}}", "montoPrestamo": 5000, "tasaInteres": 15, "plazoMeses": 12},
            pruebas=[codigo(200), 'const j = pm.response.json();',
                     'pm.test("Cuota 1166.67 y saldo 14000.00", () => { pm.expect(j.cuotaMensual).to.eql(1166.67); pm.expect(j.saldoPendiente).to.eql(14000); });',
                     'pm.collectionVariables.set("idPrestamo", j.idPrestamo);']),
        req("PI-NEW-18", "Préstamo con tasa 1500 %", "POST", "/api/prestamos", token="token",
            cuerpo={"idOperacion": 1, "montoPrestamo": 1000, "tasaInteres": 1500, "plazoMeses": 12},
            pruebas=[codigo(400), NO_500]),
        req("PI-NEW-19", "Préstamo a 361 meses", "POST", "/api/prestamos", token="token",
            cuerpo={"idOperacion": 1, "montoPrestamo": 1000, "tasaInteres": 2, "plazoMeses": 361},
            pruebas=[codigo(400)]),
        req("PI-NEW-20", "Eliminar préstamo de prueba", "DELETE", "/api/prestamos/{{idPrestamo}}", token="token",
            pruebas=[codigo(200)]),
    ]},
    {"name": "4 · Roles (RBAC) y auditoría", "item": [
        req("PI-NEW-21", "CAJERO no administra usuarios", "GET", "/api/usuarios", token="tokenCajero", pruebas=[codigo(403)]),
        req("PI-NEW-22", "CAJERO sí opera caja", "GET", "/api/operaciones", token="tokenCajero", pruebas=[codigo(200)]),
        req("PI-NEW-23", "ADMIN consulta auditoría", "GET", "/api/auditoria", token="token",
            pruebas=[codigo(200), 'pm.test("Registra el acceso denegado del cajero", () => pm.expect(pm.response.text()).to.include("lucia.ramos@sigsf.com"));']),
        req("PI-NEW-24", "Indicadores del panel", "GET", "/api/dashboard", token="token",
            pruebas=[codigo(200), TIEMPO, 'pm.test("Clientes totales > 0", () => pm.expect(pm.response.json().clientesTotales).to.be.above(0));']),
    ]},
    {"name": "5 · Portal del Cliente", "item": [
        req("PI-NEW-25", "Login del cliente 71000001", "POST", "/api/portal/login",
            cuerpo={"documento": "71000001", "password": "71000001"},
            pruebas=[codigo(200), 'const j = pm.response.json();',
                     'pm.collectionVariables.set("tokenPortal", j.token); pm.collectionVariables.set("idPortal", j.idCliente);']),
        req("PI-NEW-26", "Ve sus propios trámites", "GET", "/api/portal/{{idPortal}}/operaciones", token="tokenPortal",
            pruebas=[codigo(200)]),
        req("PI-NEW-27", "No ve los trámites de otro cliente (IDOR)", "GET", "/api/portal/1/operaciones", token="tokenPortal",
            pruebas=[codigo(403)]),
        req("PI-NEW-28", "Préstamo menor al mínimo no deja operación huérfana", "POST", "/api/portal/{{idPortal}}/prestamo", token="tokenPortal",
            cuerpo={"montoPrestamo": 50, "plazoMeses": 12}, pruebas=[codigo(400)]),
        req("PI-NEW-29", "Token de cliente no entra al sistema interno", "GET", "/api/clientes", token="tokenPortal",
            pruebas=[codigo(403)]),
    ]},
    {"name": "6 · Cierre de sesión", "item": [
        req("PI-NEW-30", "Logout", "POST", "/api/auth/logout", token="token", pruebas=[codigo(200)]),
        req("PI-NEW-31", "El token cerrado ya no sirve", "GET", "/api/clientes", token="token", pruebas=[codigo(401)]),
    ]},
]

coleccion = {
    "info": {
        "name": "SIGSF · Pruebas de integración de la API (Avance 4)",
        "description": "Plan de Pruebas SIGSF v6.0.0 · casos PI-NEW-01…31. Ejecutar con el servidor en marcha "
                       "sobre la base SistemaFinanciero_test:  newman run SIGSF_API.postman_collection.json "
                       "-e SIGSF_local.postman_environment.json",
        "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
    },
    "item": carpetas,
    "variable": [{"key": k, "value": ""} for k in
                 ["token", "tokenCajero", "tokenPortal", "idPortal", "doc", "idCliente", "idOperacion", "idPrestamo"]],
}
entorno = {
    "name": "SIGSF local (pruebas)",
    "values": [{"key": "base", "value": "http://localhost:8092", "enabled": True}],
}
with open("SIGSF_API.postman_collection.json", "w", encoding="utf-8") as f:
    json.dump(coleccion, f, ensure_ascii=False, indent=2)
with open("SIGSF_local.postman_environment.json", "w", encoding="utf-8") as f:
    json.dump(entorno, f, ensure_ascii=False, indent=2)
print("Casos:", sum(len(c["item"]) for c in carpetas))
