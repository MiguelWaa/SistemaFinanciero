#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SIGSF - Generador determinista de datos de demostracion (100 registros por modulo).

Salidas:
  db/datos_100_registros.sql                       (PostgreSQL, re-ejecutable)
  src/.../dao/memoria/semilla_100.tsv               (modo demostracion sin PostgreSQL)

Reglas del SQL generado:
  * Solo ASCII (igual que db/datos_ejemplo.sql).
  * "Completa" cada tabla hasta su objetivo (100 filas; 300 en operacion):
    LIMIT = objetivo - filas actuales  ->  se puede ejecutar varias veces.
  * Las claves foraneas se resuelven por clave natural (documento, codigo,
    email...), asi que no depende de los ids que tenga la base.
Uso:  python3 generar_datos_100.py <raiz_del_proyecto>
"""
import hashlib, random, sys, os, re

R = random.Random(20261025)
OBJ = 100

# ------------------------------------------------------------------ valores
class D:   # fecha relativa a hoy (dias)
    def __init__(s, k): s.k = k
    def sql(s): return "CURRENT_DATE" if s.k == 0 else "CURRENT_DATE %s %d" % ("-" if s.k < 0 else "+", abs(s.k))
    def tsv(s): return "D%+d" % s.k
class T:   # instante relativo a ahora (dias, horas, minutos hacia atras)
    def __init__(s, d=0, h=0, m=0): s.d, s.h, s.m = d, h, m
    def sql(s): return "CURRENT_TIMESTAMP - INTERVAL '%d days %d hours %d minutes'" % (s.d, s.h, s.m)
    def tsv(s): return "T-%d-%d-%d" % (s.d, s.h, s.m)
NULL = None

def q(v):
    if v is None: return "NULL"
    if isinstance(v, (D, T)): return v.sql()
    if isinstance(v, bool): return "TRUE" if v else "FALSE"
    if isinstance(v, int): return str(v)
    if isinstance(v, float): return repr(round(v, 4))
    return "'" + str(v).replace("'", "''") + "'"

def tv(v):
    if v is None: return r"\N"
    if isinstance(v, (D, T)): return v.tsv()
    return str(v).replace("\t", " ")

def sha(s): return hashlib.sha256(s.encode()).hexdigest()
def money(x): return round(x + 1e-9, 2)

# ------------------------------------------------------------------ listas
HOMBRES = "Luis Carlos Jorge Miguel Diego Fernando Ricardo Andres Javier Raul Mario Cesar Hugo Pedro Jose Victor Edwin Percy Wilber Nestor Alvaro Marco Oscar Renzo Sergio".split()
MUJERES = "Maria Lucia Sofia Ana Rosa Carmen Patricia Gabriela Valeria Milagros Katherine Yesenia Flor Elena Daniela Paola Nancy Judith Karen Silvia Lourdes Monica Rocio Fiorella Jimena".split()
APELL = "Quispe Mamani Huanca Condori Flores Ramos Torres Vargas Chavez Rojas Gutierrez Salazar Paredes Cornejo Zegarra Medina Caceres Valdivia Mendoza Choque Apaza Ccori Lazo Palomino Delgado Rivera Castillo Cruz Pinto Ticona Nina Callo Sucasaca Yucra Zapata Benavides Llerena Arenas Bellido Portugal Ccama Cutipa Huamani Yanque Pacheco Soncco".split()
DISTRITOS = ["Cercado", "Yanahuara", "Cayma", "Cerro Colorado", "Miraflores", "Paucarpata", "Sachaca", "Socabaya",
             "J.L. Bustamante y Rivero", "Hunter", "Mariano Melgar", "Characato", "Tiabaya"]
CALLES = ["Av. Ejercito", "Calle Mercaderes", "Av. Dolores", "Calle San Francisco", "Av. Lima", "Calle Jerusalen",
          "Av. Venezuela", "Calle Bolivar", "Av. Parra", "Calle Santa Catalina", "Av. Kennedy", "Calle Puente Bolognesi",
          "Av. Goyeneche", "Calle Peral", "Av. Progreso", "Jr. Union"]

def nombre_completo(i):
    hombre = R.random() < 0.5
    n = R.choice(HOMBRES if hombre else MUJERES)
    return n, R.choice(APELL) + " " + R.choice(APELL)

def direccion():
    return "%s %d, %s" % (R.choice(CALLES), R.randint(100, 2400), R.choice(DISTRITOS))

def tel(): return "9%08d" % R.randint(10000000, 99999999)
def tel_fijo(): return "054%06d" % R.randint(200000, 399999)

def pick(pesos):
    """pesos: lista de (valor, peso)"""
    tot = sum(p for _, p in pesos); r = R.uniform(0, tot); a = 0
    for v, p in pesos:
        a += p
        if r <= a: return v
    return pesos[-1][0]

def sin_tilde(s):
    return s.replace("á","a").replace("é","e").replace("í","i").replace("ó","o").replace("ú","u").replace("ñ","n")

# ------------------------------------------------------------------ tablas
TABLAS = []   # (nombre, objetivo, filtro_conteo_sql, columnas, filas, sql_builder)

# --- 1. roles
ROL_BASE = [("ADMIN", "Administrador del sistema"), ("SUPERVISOR", "Supervisa operaciones y reportes"),
            ("CAJERO", "Registra operaciones de caja y cambio"), ("ANALISTA", "Analiza prestamos, empenos y riesgos")]
CARGOS = [("CAJERO", "registra operaciones de caja y cambio"), ("ANALISTA", "analiza prestamos, empenos y riesgos"),
          ("SUPERVISOR", "supervisa operaciones y aprueba solicitudes"), ("AUDITOR", "consulta reportes y bitacora"),
          ("TESORERO", "controla la liquidez y los tipos de cambio"), ("EJECUTIVO_VENTAS", "registra ventas de servicios financieros"),
          ("JEFE_ALMACEN", "administra productos y stock"), ("JEFE_LOGISTICA", "gestiona proveedores y abastecimiento"),
          ("JEFE_PRODUCCION", "gestiona lineas y ordenes de produccion"), ("GERENTE", "resuelve expedientes de gerencia"),
          ("ASESOR_MARKETING", "gestiona campanias y prospectos"), ("TASADOR", "evalua articulos en garantia")]
SEDES = [("AQP_CENTRO", "Arequipa Centro"), ("AQP_YANAHUARA", "Arequipa Yanahuara"), ("AQP_CAYMA", "Arequipa Cayma"),
         ("AQP_PAUCARPATA", "Arequipa Paucarpata"), ("LIMA_MIRAFLORES", "Lima Miraflores"), ("CUSCO", "Cusco"),
         ("JULIACA", "Juliaca"), ("TACNA", "Tacna")]
roles = list(ROL_BASE)
for c, dsc in CARGOS:
    for s, sn in SEDES:
        roles.append(("%s_%s" % (c, s), "Sucursal %s: %s" % (sn, dsc)))
assert len(roles) == 100

# --- 2. permisos
MODS = "USUARIOS ROLES CLIENTES SERVICIOS TIPOS_CAMBIO OPERACIONES PRESTAMOS EMPENOS VENTAS PRODUCTOS PROVEEDORES CAMPANIAS PROSPECTOS LINEAS ORDENES EXPEDIENTES SOLICITUDES REPORTES AUDITORIA RESPALDOS".split()
ACCS = [("VER", "Consultar"), ("CREAR", "Registrar"), ("EDITAR", "Modificar"), ("ELIMINAR", "Eliminar"), ("EXPORTAR", "Exportar a Excel/CSV")]
permisos = [("%s_%s" % (a, m), "%s registros de %s" % (d, m.lower().replace("_", " "))) for m in MODS for a, d in ACCS]
assert len(permisos) == 100

# --- 3. rol_permiso (100 pares unicos; solo permisos del indice <= 96)
perm_ok = [p[0] for p in permisos[:96]]
rp = []
def add_rp(r, p):
    if (r, p) not in rp and p in perm_ok: rp.append((r, p))
for p in perm_ok:                                   # SUPERVISOR: ver todo (lo mas amplio)
    if p.startswith("VER_"): add_rp("SUPERVISOR", p)
for m in "CLIENTES OPERACIONES PRESTAMOS EMPENOS VENTAS SOLICITUDES".split():
    for a in ("VER", "CREAR", "EDITAR"): add_rp("CAJERO", "%s_%s" % (a, m))
for m in "CAMPANIAS PROSPECTOS PRODUCTOS PROVEEDORES LINEAS ORDENES EXPEDIENTES".split():
    for a in ("VER", "CREAR", "EDITAR"): add_rp("ANALISTA", "%s_%s" % (a, m))
for m in MODS[:8]:
    for a, _ in ACCS: add_rp("ADMIN", "%s_%s" % (a, m))
for c, _ in CARGOS:
    for s, _ in SEDES:
        if len(rp) >= 100: break
        add_rp("%s_%s" % (c, s), "VER_CLIENTES")
        if len(rp) >= 100: break
        add_rp("%s_%s" % (c, s), "VER_REPORTES")
rp = rp[:100]
assert len(rp) == 100, len(rp)

# --- 4. usuarios
HASH_USR = sha("sigsf123")
usuarios = []
usados = set()
ROLES_USR = [("ADMIN", 2), ("SUPERVISOR", 14), ("CAJERO", 50), ("ANALISTA", 34)]
for i in range(1, 101):
    n, a = nombre_completo(i)
    ap1 = a.split()[0]
    email = "%s.%s%02d@sigsf.com" % (n.lower(), ap1.lower(), i)
    est = pick([("ACTIVO", 80), ("INACTIVO", 8), ("PENDIENTE", 6), ("BLOQUEADO", 6)])
    usuarios.append((n, a, email, HASH_USR, pick(ROLES_USR), est, T(R.randint(1, 160), R.randint(0, 23), R.randint(0, 59))))

# --- 5. servicios
BASES = [("Cambio de moneda USD", "CAMBIO", "Compra y venta de dolares"), ("Cambio de moneda EUR", "CAMBIO", "Compra y venta de euros"),
         ("Cambio de moneda GBP", "CAMBIO", "Compra y venta de libras"), ("Prestamo personal", "CREDITO", "Prestamo con interes simple"),
         ("Prestamo de libre disponibilidad", "CREDITO", "Credito sin garantia"), ("Prestamo para capital de trabajo", "CREDITO", "Credito para negocios"),
         ("Empeno de joyas", "EMPENO", "Prestamo con garantia de joyas"), ("Empeno de electronicos", "EMPENO", "Prestamo con garantia de equipos"),
         ("Empeno de herramientas", "EMPENO", "Prestamo con garantia de herramientas"), ("Giro nacional", "GIRO", "Envio de dinero nacional"),
         ("Envio de remesas", "GIRO", "Recepcion y envio de remesas"), ("Deposito a plazo fijo", "AHORRO", "Deposito con tasa fija"),
         ("Cuenta de ahorros", "AHORRO", "Cuenta de ahorro personal"), ("Seguro de vida", "SEGURO", "Cobertura de vida"),
         ("Seguro de articulos empenados", "SEGURO", "Cobertura de articulos en garantia"), ("Tarjeta de debito", "TARJETA", "Emision de tarjeta de debito"),
         ("Alquiler de caja de seguridad", "CUSTODIA", "Custodia de valores"), ("Pago de servicios", "PAGOS", "Recaudacion de servicios"),
         ("Asesoria financiera", "ASESORIA", "Asesoria personalizada"), ("Tasacion de articulos", "ASESORIA", "Tasacion profesional")]
CANALES = ["Sucursal Centro", "Sucursal Yanahuara", "Sucursal Cayma", "Agencia Paucarpata", "Portal del Cliente"]
servicios = []
for c in CANALES:
    for b, t, d in BASES:
        servicios.append(("%s - %s" % (b, c), t, d + " (" + c + ")", pick([("ACTIVO", 92), ("INACTIVO", 8)])))
assert len(servicios) == 100 and len({s[0] for s in servicios}) == 100

# --- 6. clientes
clientes = []
for i in range(1, 101):
    n, a = nombre_completo(i)
    ce = (i % 12 == 0)
    doc = ("00%07d" % (2000000 + i)) if ce else "71%06d" % i
    nac = D(-R.randint(20 * 365, 62 * 365))
    est = pick([("ACTIVO", 88), ("INACTIVO", 6), ("BLOQUEADO", 6)])
    corr = "%s.%s%d@%s" % (n.lower(), a.split()[0].lower(), i, R.choice(["gmail.com", "hotmail.com", "outlook.com", "yahoo.es"]))
    clientes.append(("CE" if ce else "DNI", doc, n, a, nac, tel(), corr, direccion(), est, T(R.randint(0, 170), R.randint(0, 23), R.randint(0, 59))))
assert len({c[1] for c in clientes}) == 100

# --- 7. cliente_servicio
cli_srv = []
for i in range(1, 101):
    ini = -R.randint(10, 400)
    fin = NULL if R.random() < 0.7 else D(ini + R.randint(30, 300))
    cli_srv.append((clientes[(i - 1) % 90][1], servicios[R.randint(0, 89)][0], D(ini), fin, "ACTIVO" if fin is None else "FINALIZADO"))

# --- 8. tipos de cambio
PARES = [("USD", 3.62, 0.06, False), ("EUR", 3.98, 0.10, False), ("GBP", 4.62, 0.14, True), ("CHF", 4.08, 0.10, True), ("CAD", 2.68, 0.07, True)]
tcs = []; tc_activo = {"USD": 3.78, "EUR": 4.15}
hist = {}
for mon, base, spr, activo_hoy in PARES:
    v = base; rows = []
    for k in range(20):
        v = max(base * 0.97, min(base * 1.03, v + R.uniform(-0.02, 0.02)))
        compra = round(v, 4); venta = round(v + spr, 4)
        rows.append((mon, "PEN", compra, venta, D(-(k if activo_hoy else k + 1)), "ACTIVO" if (activo_hoy and k == 0) else "INACTIVO"))
    hist[mon] = rows
    if activo_hoy: tc_activo[mon] = rows[0][3]
for mon in ("GBP", "CHF", "CAD", "USD", "EUR"): tcs += hist[mon]      # los ACTIVOS primero
assert len(tcs) == 100

# --- 9. operaciones (CAMBIO / PRESTAMO / EMPENO, 100 c/u)
usados_monto = {100.0, 5000.0, 800.0, 2000.0}
def monto_unico(base):
    m = round(base, 2)
    while m in usados_monto: m = round(m + 0.01, 2)
    usados_monto.add(m); return m

MONEDAS = [("USD", 55), ("EUR", 25), ("GBP", 8), ("CHF", 6), ("CAD", 6)]
def estado_op(i):
    return pick([("COMPLETADA", 66), ("PENDIENTE", 22), ("ANULADA", 12)])
def tiempo_op(est, i):
    if est == "PENDIENTE": return T(R.randint(0, 9), R.randint(0, 23), R.randint(0, 59))
    if i < 8: return T(0, R.randint(0, 8), R.randint(0, 59))        # "operaciones de hoy"
    return T(R.randint(1, 75), R.randint(0, 23), R.randint(0, 59))

ops_cambio, ops_prest, ops_emp = [], [], []
for i in range(1, 101):
    est = estado_op(i); mon = pick(MONEDAS); venta = tc_activo[mon]
    m = monto_unico(R.choice([50, 80, 100, 150, 200, 300, 500, 800, 1000, 1500, 2000, 3000]) + R.randint(1, 99) / 100)
    cli = clientes[R.randint(0, 89)][1]
    ops_cambio.append((cli, "CAMBIO", mon, "PEN", m, venta, m, money(m * venta), est, tiempo_op(est, i)))
for i in range(1, 101):
    est = estado_op(i)
    m = monto_unico(R.choice([500, 800, 1000, 1500, 2000, 3000, 4000, 5000, 8000, 10000, 15000]) + R.randint(1, 99) / 100)
    ops_prest.append((clientes[R.randint(0, 89)][1], "PRESTAMO", "PEN", "PEN", m, NULL, NULL, NULL, est, tiempo_op(est, i)))
for i in range(1, 101):
    est = estado_op(i)
    m = monto_unico(R.choice([200, 300, 500, 800, 1000, 1200, 1800, 2500, 3500]) + R.randint(1, 99) / 100)
    ops_emp.append((clientes[R.randint(0, 89)][1], "EMPENO", "PEN", "PEN", m, NULL, NULL, NULL, est, tiempo_op(est, i)))

# --- 10. prestamos (n-esimo prestamo <-> n-esima operacion PRESTAMO)
prestamos = []
for i, o in enumerate(ops_prest, 1):
    m = o[4]; tasa = R.choice([1.2, 1.5, 1.8, 2.0, 2.5, 3.0]); plazo = R.choice([6, 9, 12, 18, 24, 36])
    total = m * (1 + tasa / 100 * plazo); cuota = money(total / plazo); saldo = money(total)
    est_op = o[8]; ini = o[9].d if isinstance(o[9], T) else 0
    est = {"COMPLETADA": pick([("ACTIVO", 80), ("CANCELADO", 20)]), "PENDIENTE": "PENDIENTE", "ANULADA": "ANULADO"}[est_op]
    if est == "CANCELADO": saldo = 0.0
    prestamos.append((m, tasa, plazo, cuota, D(-ini), D(-ini + plazo * 30), saldo, est))

# --- 11. articulos
CATS = [("Electronica", [("Laptop", ["HP", "Lenovo", "Dell", "Asus"]), ("Celular", ["Samsung", "Xiaomi", "Apple", "Motorola"]),
                          ("Televisor", ["LG", "Samsung", "Sony"]), ("Tablet", ["Apple", "Samsung", "Lenovo"]), ("Consola", ["Sony", "Nintendo", "Microsoft"])]),
        ("Joyeria", [("Anillo de oro 18k", ["-"]), ("Cadena de oro 18k", ["-"]), ("Reloj", ["Casio", "Citizen", "Seiko"]), ("Pulsera de plata", ["-"])]),
        ("Herramientas", [("Taladro", ["Bosch", "Makita", "DeWalt"]), ("Amoladora", ["Bosch", "Truper"]), ("Soldadora", ["Lincoln", "Miller"])]),
        ("Electrodomesticos", [("Licuadora", ["Oster", "Philips"]), ("Cocina", ["Mabe", "Indurama"]), ("Refrigeradora", ["LG", "Samsung", "Indurama"])])]
FISICO = [("MUY BUENO", 30), ("BUENO", 45), ("REGULAR", 20), ("DETERIORADO", 5)]
articulos = []
for i in range(1, 101):
    cat, items = R.choice(CATS); it, marcas = R.choice(items); mar = R.choice(marcas)
    val = money(R.choice([300, 500, 800, 1200, 1800, 2500, 3500, 5000]) * R.uniform(0.8, 1.3))
    articulos.append((clientes[R.randint(0, 89)][1], ("%s %s" % (it, mar if mar != "-" else "")).strip(), cat, mar,
                      "M%04d" % R.randint(1, 9999), "SN-%s-%05d" % (cat[:3].upper(), i), pick(FISICO), val,
                      D(-R.randint(1, 70)), "ACTIVO"))

# --- 12. empenos (n-esimo empeno <-> n-esima operacion EMPENO; articulo (n-1)%98+1)
empenos = []
for i, o in enumerate(ops_emp, 1):
    m = o[4]; tasa = R.choice([8.0, 10.0, 12.0, 15.0]); plazo = R.choice([30, 45, 60, 90])
    est_op = o[8]; ini = o[9].d
    est = {"COMPLETADA": pick([("ACTIVO", 70), ("CANCELADO", 30)]), "PENDIENTE": "PENDIENTE", "ANULADA": "ANULADO"}[est_op]
    if i >= 99: est = "CANCELADO"
    saldo = 0.0 if est == "CANCELADO" else m
    empenos.append((articulos[(i - 1) % 98][5], m, tasa, plazo, D(-ini), D(-ini + plazo), saldo, est))

# --- 13. comprobantes (operaciones COMPLETADAS de entre las 99 primeras de cada tipo)
comprob = []
def total_op(tipo, o): return o[7] if tipo == "CAMBIO" else o[4]
for tipo, ops, k in (("CAMBIO", ops_cambio, 34), ("PRESTAMO", ops_prest, 33), ("EMPENO", ops_emp, 33)):
    for o in [x for x in ops[:99] if x[8] == "COMPLETADA"][:k]:
        comprob.append((tipo, o[4], total_op(tipo, o)))
for o in [x for x in ops_cambio[:99] if x[8] == "COMPLETADA"][34:]:
    if len(comprob) >= 100: break
    comprob.append(("CAMBIO", o[4], total_op("CAMBIO", o)))
assert len(comprob) == 100, len(comprob)
comprob = [(c[0], c[1], "%s%03d-%06d" % ("B" if i % 3 else "F", 1 if i % 2 else 2, i + 1), "BOLETA" if i % 3 else "FACTURA",
            c[2], pick([("EMITIDO", 92), ("ANULADO", 8)]), T(R.randint(0, 60), R.randint(0, 23), R.randint(0, 59)))
           for i, c in enumerate(comprob)]

# --- 14. ventas
ventas = []; usados_imp = {378.0, 5750.0, 800.0, 305.0}
for i in range(1, 101):
    s = servicios[R.randint(0, 89)]; base = {"CAMBIO": 400, "CREDITO": 3000, "EMPENO": 900, "GIRO": 350, "AHORRO": 1500,
                                          "SEGURO": 120, "TARJETA": 30, "CUSTODIA": 80, "PAGOS": 150, "ASESORIA": 200}[s[1]]
    monto = money(base * R.uniform(0.4, 2.2))
    imp = money(monto * (1 + R.choice([0, 0, 0.01, 0.02, 0.05])) + R.randint(1, 99) / 100)
    while imp in usados_imp: imp = money(imp + 0.01)
    usados_imp.add(imp)
    if i <= 28:   t = T(R.randint(0, 24), R.randint(0, 23), R.randint(0, 59))        # este mes
    else:         t = T(R.randint(25, 120), R.randint(0, 23), R.randint(0, 59))
    if i <= 3: t = T(0, R.randint(0, 6), R.randint(0, 59))
    ventas.append((clientes[R.randint(0, 89)][1], s[0], t, monto, imp, pick([("REGISTRADA", 90), ("ANULADA", 10)])))

# --- 15. detalle_venta (100 lineas sobre ventas <= 96)
detalles = []
for i in range(1, 101):
    v = ventas[(i - 1) % 96] if i <= 96 else ventas[R.randint(0, 95)]
    cant = R.randint(1, 4); pu = money(v[4] / cant)
    detalles.append((v[4], "Servicio: " + v[1].split(" - ")[0], cant, pu, money(cant * pu)))

# --- 16. productos (codigo PRD-101..200)
PRODS = [("Formularios de operacion", "Papeleria"), ("Sobres de seguridad", "Seguridad"), ("Cinta para contadora", "Insumos"), ("Tinta para sellos", "Insumos"),
         ("Calculadora financiera", "Equipos"), ("Contadora de billetes", "Equipos"), ("Detector de billetes falsos", "Equipos"), ("Papel bond A4", "Papeleria"),
         ("Toner para impresora", "Tecnologia"), ("Lapicero azul", "Papeleria"), ("Archivador de palanca", "Papeleria"), ("Bolsa de seguridad numerada", "Seguridad"),
         ("Candado de seguridad", "Seguridad"), ("Balanza de precision", "Equipos"), ("Lupa de joyero", "Equipos"), ("Etiquetas adhesivas", "Papeleria"),
         ("Cinta de embalaje", "Insumos"), ("Guantes de latex", "Limpieza"), ("Desinfectante", "Limpieza"), ("Silla ergonomica", "Mobiliario"),
         ("Escritorio de oficina", "Mobiliario"), ("Camara de seguridad", "Tecnologia"), ("Disco duro externo", "Tecnologia"), ("Memoria USB 64GB", "Tecnologia"),
         ("Sello automatico", "Insumos"), ("Almohadilla para sellos", "Insumos"), ("Folder manila", "Papeleria"), ("Clips metalicos", "Papeleria"),
         ("Grapas", "Papeleria"), ("Perforador", "Equipos")]
UND = {"Papeleria": ["UND", "PAQUETE", "MILLAR"], "Insumos": ["UND", "ROLLO"], "Equipos": ["UND"], "Seguridad": ["UND", "PAQUETE"],
       "Tecnologia": ["UND"], "Limpieza": ["UND", "CAJA"], "Mobiliario": ["UND"]}
productos = []
for i in range(1, 101):
    nombre, cat = PRODS[(i - 1) % len(PRODS)]
    if i > len(PRODS): nombre = "%s (lote %d)" % (nombre, (i - 1) // len(PRODS) + 1)
    minimo = R.choice([5, 10, 15, 20, 25, 40, 50]); bajo = (i % 8 == 0)
    stock = R.randint(0, max(1, minimo - 1)) if bajo else R.randint(minimo, minimo * 5)
    productos.append(("PRD-%03d" % (100 + i), nombre, "Producto de %s para las sucursales" % cat.lower(), cat, R.choice(UND[cat]), stock, minimo, "ACTIVO" if i % 17 else "INACTIVO"))

# --- 17. inventario (100): 1 por producto (<=95) + 5 segundas ubicaciones
UBIC = ["Almacen central", "Almacen Yanahuara", "Almacen Cayma", "Boveda principal", "Deposito Paucarpata"]
inventario = []
for i in range(1, 101):
    p = productos[i - 1] if i <= 95 else productos[i - 96]
    inventario.append((p[0], UBIC[0] if i <= 95 else UBIC[1 + (i % 4)], p[5] if i <= 95 else R.randint(1, 20), T(R.randint(0, 30), R.randint(0, 23), R.randint(0, 59))))

# --- 18. movimiento_inventario (100)
MOT = {"ENTRADA": ["Compra a proveedor", "Devolucion de sucursal", "Reposicion mensual"], "SALIDA": ["Consumo de caja", "Entrega a sucursal", "Baja por deterioro"], "AJUSTE": ["Ajuste por inventario fisico"]}
mov_inv = []
for i in range(1, 101):
    tp = pick([("ENTRADA", 45), ("SALIDA", 45), ("AJUSTE", 10)])
    mov_inv.append((productos[R.randint(0, 94)][0], tp, R.randint(1, 60), R.choice(MOT[tp]), T(R.randint(0, 90), R.randint(0, 23), R.randint(0, 59))))

# --- 19. proveedores
RUBROS = ["Distribuidora", "Importaciones", "Comercial", "Servicios Graficos", "Suministros", "Tecnologia", "Seguridad", "Papelera", "Mobiliario", "Limpieza"]
ZONAS = ["Andina", "del Sur", "Misti", "Chachani", "Volcan", "Arequipa", "Colca", "Pacifico", "Sillar", "Campina", "Cañon", "Altiplano", "Imperial", "Nacional", "Peruana"]
SUF = ["SAC", "EIRL", "SRL", "SA"]
proveedores = []; usados_nom = set()
for i in range(1, 101):
    while True:
        rz = "%s %s %s" % (R.choice(RUBROS), R.choice(ZONAS), R.choice(SUF)); rz = sin_tilde(rz)
        if rz not in usados_nom: usados_nom.add(rz); break
    n, a = nombre_completo(i)
    proveedores.append((rz, "20%09d" % (500000000 + i * 7919 % 400000000 + i), "%s %s" % (n, a.split()[0]), tel_fijo(),
                        "ventas%d@%s.pe" % (i, rz.split()[1].lower().replace(" ", "")), direccion(), "ACTIVO" if i % 9 else "INACTIVO"))
assert len({p[1] for p in proveedores}) == 100

# --- 20. movimiento_logistica
mov_log = []
for i in range(1, 101):
    tp = pick([("COMPRA", 45), ("ENTREGA", 35), ("DEVOLUCION", 10), ("COTIZACION", 10)])
    mov_log.append((proveedores[R.randint(0, 97)][1], tp, "%s de insumos para las sucursales (pedido %04d)" % (tp.capitalize(), i), R.randint(5, 400),
                    T(R.randint(0, 100), R.randint(0, 23), R.randint(0, 59)), pick([("REGISTRADO", 30), ("RECIBIDO", 55), ("ANULADO", 5), ("PENDIENTE", 10)])))

# --- 21. prospectos
prospectos = []
for i in range(1, 101):
    n, a = nombre_completo(i)
    prospectos.append((n, a, "72%06d" % i, tel(), "%s.%s%d@gmail.com" % (n.lower(), a.split()[0].lower(), 300 + i),
                       R.choice(["WEB", "FERIA", "REFERIDO", "REDES", "VOLANTES", "LLAMADA"]),
                       pick([("NUEVO", 35), ("CONTACTADO", 30), ("INTERESADO", 15), ("CONVERTIDO", 12), ("DESCARTADO", 8)]),
                       T(R.randint(0, 120), R.randint(0, 23), R.randint(0, 59))))

# --- 22. campanias
PROMOS = ["Cambio sin comision", "Prestamos rapidos", "Empeno con mejor tasa", "Giros a todo el Peru", "Deposito a plazo", "Seguro de vida", "Tarjeta de debito",
          "Dia del cliente", "Campana escolar", "Campana navidena", "Fiestas patrias", "Aniversario de Arequipa", "Remesas familiares", "Credito para negocios", "Ahorro joven",
          "Cuenta de ahorros", "Caja de seguridad", "Asesoria gratuita", "Tasacion sin costo", "Referidos premiados"]
CAN = ["REDES", "VOLANTES", "RADIO", "WEB", "EMAIL", "TV", "EVENTO"]
EDIC = ["Enero", "Marzo", "Mayo", "Julio", "Setiembre"]
campanias = []
for ed in EDIC:
    for p in PROMOS:
        est = pick([("ACTIVA", 35), ("FINALIZADA", 50), ("PROGRAMADA", 15)])
        ini = {"ACTIVA": -R.randint(1, 20), "FINALIZADA": -R.randint(40, 200), "PROGRAMADA": R.randint(3, 40)}[est]
        campanias.append(("%s - %s 2026" % (p, ed), "Difusion de %s" % p.lower(), D(ini), D(ini + R.choice([15, 30, 45, 60])),
                          money(R.choice([500, 800, 1200, 1500, 2500, 4000, 6000, 9000, 12000])), R.choice(CAN), est))
campanias.sort(key=lambda c: 0 if c[6] == "ACTIVA" else 1)
assert len(campanias) == 100

# --- 23. campania_prospecto / actividad_marketing
camp_pros = []; actividades = []
for i in range(1, 101):
    camp = campanias[R.randint(0, 96)][0]; pro = prospectos[R.randint(0, 95)][2]
    camp_pros.append((camp, pro, D(-R.randint(0, 90)), R.choice(["Interesado", "Solicito informacion", "Sin respuesta", "Agendo visita", "Contratado", "No interesado"]),
                      pick([("PENDIENTE", 25), ("CONTACTADO", 40), ("CONVERTIDO", 20), ("DESCARTADO", 15)])))
    actividades.append((pro, camp, R.choice(["LLAMADA", "CORREO", "VISITA", "WHATSAPP", "EVENTO"]), "Seguimiento comercial nro. %03d" % i, T(R.randint(0, 90), R.randint(0, 23), R.randint(0, 59))))

# --- 24. lineas / ordenes
LB = ["Emision de tarjetas", "Impresion de formularios", "Sellado de bolsas de seguridad", "Troquelado de comprobantes", "Encuadernacion de expedientes",
      "Impresion de talonarios", "Plastificado de carnets", "Etiquetado de articulos empenados", "Embalaje de remesas", "Personalizacion de chequeras"]
TURN = ["Turno manana", "Turno tarde", "Turno noche", "Sede Centro", "Sede Yanahuara", "Sede Cayma", "Sede Paucarpata", "Sede Miraflores", "Planta 1", "Planta 2"]
lineas = []
for t in TURN:
    for b in LB:
        lineas.append(("LIN-%03d" % (len(lineas) + 1), "%s - %s" % (b, t), "Linea de %s (%s)" % (b.lower(), t.lower()), pick([("ACTIVA", 85), ("MANTENIMIENTO", 8), ("INACTIVA", 7)])))
ordenes = []
for i in range(1, 101):
    est = pick([("PENDIENTE", 15), ("EN_PROCESO", 38), ("TERMINADA", 42), ("ANULADA", 5)])
    ini = -R.randint(1, 90); dur = R.choice([5, 7, 10, 15, 20]); cant = R.choice([100, 200, 300, 500, 800, 1000, 2000, 5000])
    av = {"PENDIENTE": 0, "EN_PROCESO": R.choice([10, 20, 35, 40, 55, 60, 75, 90]), "TERMINADA": 100, "ANULADA": R.choice([0, 15])}[est]
    real = D(ini + dur - R.randint(0, 2)) if est == "TERMINADA" else NULL
    ordenes.append((productos[R.randint(0, 94)][0], lineas[R.randint(0, 97)][0], cant, D(ini), D(ini + dur), real, av, est, "Lote %03d - %s" % (i, "urgente" if i % 11 == 0 else "programado")))

# --- 25. expedientes + decisiones + resoluciones + reportes
AREAS = ["OPERACIONES", "MARKETING", "ALMACEN", "LOGISTICA", "PRODUCCION", "VENTAS", "SISTEMAS", "FINANZAS"]
ASUNTOS = ["Ampliacion de limite de caja", "Presupuesto adicional para campania", "Compra de insumos de caja", "Renovacion de equipos de computo", "Contratacion de personal temporal",
           "Apertura de nueva agencia", "Cambio de proveedor de seguridad", "Aumento de tasa de prestamos", "Mantenimiento de linea de produccion", "Adquisicion de contadoras de billetes"]
expedientes = []
for i in range(1, 101):
    est = pick([("PENDIENTE", 30), ("APROBADO", 40), ("RECHAZADO", 12), ("OBSERVADO", 18)])
    sol = T(R.randint(0, 120), R.randint(0, 23), R.randint(0, 59))
    res = NULL if est == "PENDIENTE" else T(max(0, sol.d - R.randint(1, 5)), R.randint(0, 23), R.randint(0, 59))
    n, a = nombre_completo(i)
    expedientes.append(("EXP-%03d" % (3 + i), R.choice(AREAS), "%s %s" % (n, a.split()[0]), money(R.choice([800, 1500, 3000, 5000, 8000, 12000, 20000, 35000, 60000])),
                        R.choice(["BAJO", "MEDIO", "ALTO"]), "%s (area %s)" % (R.choice(ASUNTOS), "solicitante"), est, sol, res))
dec_dir = []; dec_ger = []; resol = []; rep_dir = []
for i in range(1, 101):
    e = expedientes[(i - 1) % 97][0]
    dec_dir.append((e, pick([("APROBAR", 45), ("OBSERVAR", 25), ("RECHAZAR", 15), ("DERIVAR", 15)]), "DD-%04d: decision de direccion sobre %s" % (i, e),
                    T(R.randint(0, 100), R.randint(0, 23), R.randint(0, 59)), pick([("VIGENTE", 90), ("ANULADA", 10)])))
    dec_ger.append((e, pick([("APROBAR", 50), ("OBSERVAR", 25), ("RECHAZAR", 25)]), "DG-%04d: decision de gerencia general sobre %s" % (i, e),
                    T(R.randint(0, 100), R.randint(0, 23), R.randint(0, 59)), pick([("VIGENTE", 90), ("ANULADA", 10)])))
    resol.append(("DG-%04d: decision de gerencia general sobre %s" % (i, e), "RG-2026-%03d" % i, "Resolucion gerencial sobre el expediente %s" % e,
                  "Se resuelve conforme a lo evaluado por la gerencia general para el expediente %s." % e, D(-R.randint(0, 100)), pick([("VIGENTE", 90), ("ANULADA", 10)])))
    rep_dir.append((R.choice(["MENSUAL", "TRIMESTRAL", "SEMESTRAL", "ANUAL", "ESPECIAL"]),
                    R.choice(["VENTAS", "OPERACIONES", "MARKETING", "ALMACEN", "LOGISTICA", "PRODUCCION", "CLIENTES", "FINANZAS"]),
                    "Reporte de gestion nro. %03d para la direccion" % i, T(R.randint(0, 150), R.randint(0, 23), R.randint(0, 59))))

# =================================================================== EMISION SQL
def limite(tabla, obj, where=""):
    cnt = "(SELECT COUNT(*) FROM %s%s)" % (tabla, (" WHERE " + where) if where else "")
    return "LIMIT CASE WHEN %s < %d THEN %d - %s ELSE 0 END" % (cnt, obj, obj, cnt)

def values(cols, filas):
    out = ["WITH v (%s) AS (VALUES" % ", ".join(["n"] + cols)]
    for i, f in enumerate(filas, 1):
        out.append("  (%d, %s)%s" % (i, ", ".join(q(x) for x in f), "," if i < len(filas) else ""))
    out.append(")")
    return "\n".join(out)

def cast(col, tipo): return "CAST(v.%s AS %s)" % (col, tipo)

SQL = []
def titulo(t, extra=""):
    SQL.append("\n-- ---- %s %s----" % (t, extra))

def insert(tabla, objetivo, cols_vals, cols_v, filas, joins="", where="", where_cnt="", unica=None):
    """cols_vals: lista (columna_destino, expresion SELECT). cols_v: nombres de columnas de v."""
    s = values(cols_v, filas) + "\nINSERT INTO %s (%s)\nSELECT %s\nFROM v%s" % (
        tabla, ", ".join(c for c, _ in cols_vals), ", ".join(e for _, e in cols_vals), joins)
    w = []
    if where: w.append(where)
    if unica: w.append("NOT EXISTS (SELECT 1 FROM %s x WHERE %s)" % (tabla, unica))
    if w: s += "\nWHERE " + " AND ".join(w)
    s += "\nORDER BY v.n\n" + limite(tabla, objetivo, where_cnt) + ";"
    SQL.append(s)

def fk(tabla, alias, col_nat, col_v):
    return "\nJOIN %s %s ON %s.%s = v.%s" % (tabla, alias, alias, col_nat, col_v)

SQL.append("""-- ============================================================
--  SIGSF - Datos de demostracion: 100 registros por modulo   (v5.1.0)
--  Generado por scripts/datos/generar_datos_100.py (semilla fija 20261025).
--
--  EJECUTAR DESPUES de: schema.sql, datos_ejemplo.sql,
--  portal_cliente.sql y seguridad.sql (pgAdmin > Query Tool).
--
--  * Completa cada tabla hasta 100 filas (operacion: 300 = 100 cambios +
--    100 prestamos + 100 empenos). Se puede ejecutar varias veces: si la
--    tabla ya tiene el objetivo, no inserta nada.
--  * No borra ni modifica datos existentes. No cambia el esquema.
--  * Las claves foraneas se resuelven por clave natural (documento,
--    email, codigo, RUC...), no por id.
--  * Usuarios de prueba generados:  <correo>  /  sigsf123
--  * Clientes del portal generados: documento 71000001..71000100
--    (clave inicial = su documento).
-- ============================================================
""")

# 1 roles
titulo("Seguridad: roles (100)")
insert("rol", 100, [("nombre", cast("nombre", "VARCHAR(50)")), ("descripcion", cast("descripcion", "VARCHAR(200)"))],
       ["nombre", "descripcion"], roles, unica="x.nombre = v.nombre")
titulo("Seguridad: permisos (100)")
insert("permiso", 100, [("nombre", cast("nombre", "VARCHAR(80)")), ("descripcion", cast("descripcion", "VARCHAR(200)"))],
       ["nombre", "descripcion"], permisos, unica="x.nombre = v.nombre")
titulo("Seguridad: rol_permiso (100)")
insert("rol_permiso", 100, [("id_rol", "r.id_rol"), ("id_permiso", "p.id_permiso")], ["rol", "permiso"], rp,
       joins="\nJOIN rol r ON r.nombre = v.rol\nJOIN permiso p ON p.nombre = v.permiso",
       unica="x.id_rol = r.id_rol AND x.id_permiso = p.id_permiso")
titulo("Usuarios (100)", "contrasena de los generados: sigsf123 ")
insert("usuario", 100, [("nombre", cast("nombre", "VARCHAR(80)")), ("apellido", cast("apellido", "VARCHAR(80)")), ("email", cast("email", "VARCHAR(120)")),
                        ("password", cast("password", "VARCHAR(200)")), ("id_rol", "r.id_rol"), ("estado", cast("estado", "VARCHAR(20)")),
                        ("fecha_registro", cast("fecha", "TIMESTAMP"))],
       ["nombre", "apellido", "email", "password", "rol", "estado", "fecha"], [(u[0], u[1], u[2], u[3], u[4], u[5], u[6]) for u in usuarios],
       joins="\nJOIN rol r ON r.nombre = v.rol", unica="x.email = v.email")
titulo("Servicios financieros (100)")
insert("servicio_financiero", 100, [("nombre", cast("nombre", "VARCHAR(120)")), ("tipo_servicio", cast("tipo", "VARCHAR(60)")),
                                    ("descripcion", cast("descripcion", "TEXT")), ("estado", cast("estado", "VARCHAR(20)"))],
       ["nombre", "tipo", "descripcion", "estado"], servicios, unica="x.nombre = v.nombre")
titulo("Clientes (100)")
insert("cliente", 100, [("tipo_documento", cast("tipo", "VARCHAR(20)")), ("documento", cast("documento", "VARCHAR(20)")), ("nombres", cast("nombres", "VARCHAR(120)")),
                        ("apellidos", cast("apellidos", "VARCHAR(120)")), ("fecha_nacimiento", cast("nacimiento", "DATE")), ("telefono", cast("telefono", "VARCHAR(30)")),
                        ("correo", cast("correo", "VARCHAR(120)")), ("direccion", cast("direccion", "VARCHAR(200)")), ("estado", cast("estado", "VARCHAR(20)")),
                        ("fecha_registro", cast("fecha", "TIMESTAMP"))],
       ["tipo", "documento", "nombres", "apellidos", "nacimiento", "telefono", "correo", "direccion", "estado", "fecha"], clientes, unica="x.documento = v.documento")
titulo("Portal del Cliente: accesos de los clientes generados (clave inicial = documento)")
SQL.append("""CREATE TABLE IF NOT EXISTS cliente_acceso (
    id_cliente INTEGER PRIMARY KEY REFERENCES cliente(id_cliente) ON DELETE CASCADE,
    password   VARCHAR(200) NOT NULL
);""")
acc = [(c[1], sha(c[1])) for c in clientes]
SQL.append(values(["documento", "clave"], acc) + "\nINSERT INTO cliente_acceso (id_cliente, password)\nSELECT c.id_cliente, CAST(v.clave AS VARCHAR(200))\nFROM v\nJOIN cliente c ON c.documento = v.documento\nWHERE NOT EXISTS (SELECT 1 FROM cliente_acceso a WHERE a.id_cliente = c.id_cliente);")
titulo("Clientes: servicios contratados (100)")
insert("cliente_servicio", 100, [("id_cliente", "c.id_cliente"), ("id_servicio", "s.id_servicio"), ("fecha_inicio", cast("inicio", "DATE")),
                                  ("fecha_fin", cast("fin", "DATE")), ("estado", cast("estado", "VARCHAR(20)"))],
       ["documento", "servicio", "inicio", "fin", "estado"], cli_srv,
       joins="\nJOIN cliente c ON c.documento = v.documento\nJOIN servicio_financiero s ON s.nombre = v.servicio")
titulo("Operaciones: tipos de cambio (100)")
insert("tipo_cambio", 100, [("moneda_origen", cast("origen", "VARCHAR(10)")), ("moneda_destino", cast("destino", "VARCHAR(10)")), ("compra", cast("compra", "NUMERIC(12,4)")),
                            ("venta", cast("venta", "NUMERIC(12,4)")), ("fecha_inicio", cast("inicio", "DATE")), ("estado", cast("estado", "VARCHAR(20)"))],
       ["origen", "destino", "compra", "venta", "inicio", "estado"], tcs,
       unica="x.moneda_origen = v.origen AND x.moneda_destino = v.destino AND x.fecha_inicio = CAST(v.inicio AS DATE)")

def insert_ops(tipo, ops):
    titulo("Operaciones financieras: %s (100)" % tipo)
    cols = ["documento", "tipo", "origen", "destino", "monto", "tc", "entregar", "recibir", "estado", "fecha"]
    insert("operacion", 100, [("id_cliente", "c.id_cliente"),
           ("id_tipo_cambio", "(SELECT t.id_tipo_cambio FROM tipo_cambio t WHERE t.moneda_origen = v.origen AND t.moneda_destino = v.destino AND t.estado = 'ACTIVO' ORDER BY t.fecha_inicio DESC, t.id_tipo_cambio DESC LIMIT 1)" if tipo == "CAMBIO" else "CAST(NULL AS INTEGER)"),
           ("tipo_operacion", cast("tipo", "VARCHAR(30)")), ("moneda_origen", cast("origen", "VARCHAR(10)")), ("moneda_destino", cast("destino", "VARCHAR(10)")),
           ("monto", cast("monto", "NUMERIC(14,2)")), ("tipo_cambio", cast("tc", "NUMERIC(12,4)")), ("total_entregar", cast("entregar", "NUMERIC(14,2)")),
           ("total_recibir", cast("recibir", "NUMERIC(14,2)")), ("estado", cast("estado", "VARCHAR(20)")), ("fecha_operacion", cast("fecha", "TIMESTAMP"))],
           cols, ops, joins="\nJOIN cliente c ON c.documento = v.documento",
           unica="x.tipo_operacion = v.tipo AND x.monto = CAST(v.monto AS NUMERIC(14,2))", where_cnt="tipo_operacion = '%s'" % tipo)
for tipo, ops in (("CAMBIO", ops_cambio), ("PRESTAMO", ops_prest), ("EMPENO", ops_emp)):
    insert_ops(tipo, ops)

titulo("Operaciones: prestamos (100)", "(interes simple: total = monto x (1 + tasa/100 x meses)) ")
insert("prestamo", 100, [("id_operacion", "o.id_operacion"), ("monto_prestamo", cast("monto", "NUMERIC(14,2)")), ("tasa_interes", cast("tasa", "NUMERIC(6,3)")),
                         ("plazo_meses", cast("plazo", "INTEGER")), ("cuota_mensual", cast("cuota", "NUMERIC(14,2)")), ("fecha_inicio", cast("inicio", "DATE")),
                         ("fecha_vencimiento", cast("vence", "DATE")), ("saldo_pendiente", cast("saldo", "NUMERIC(14,2)")), ("estado", cast("estado", "VARCHAR(20)"))],
       ["monto", "tasa", "plazo", "cuota", "inicio", "vence", "saldo", "estado"], prestamos,
       joins="\nJOIN operacion o ON o.tipo_operacion = 'PRESTAMO' AND o.monto = CAST(v.monto AS NUMERIC(14,2))",
       unica="x.id_operacion = o.id_operacion")
titulo("Operaciones: articulos en garantia (100)")
insert("articulo", 100, [("id_cliente", "c.id_cliente"), ("descripcion", cast("descripcion", "VARCHAR(200)")), ("categoria", cast("categoria", "VARCHAR(60)")),
                         ("marca", cast("marca", "VARCHAR(60)")), ("modelo", cast("modelo", "VARCHAR(60)")), ("numero_serie", cast("serie", "VARCHAR(80)")),
                         ("estado_fisico", cast("fisico", "VARCHAR(60)")), ("valor_estimado", cast("valor", "NUMERIC(14,2)")), ("fecha_registro", cast("fecha", "DATE")),
                         ("estado", cast("estado", "VARCHAR(20)"))],
       ["documento", "descripcion", "categoria", "marca", "modelo", "serie", "fisico", "valor", "fecha", "estado"], articulos,
       joins="\nJOIN cliente c ON c.documento = v.documento", unica="x.numero_serie = v.serie")
titulo("Operaciones: empenos (100)")
insert("empeno", 100, [("id_operacion", "o.id_operacion"), ("id_articulo", "a.id_articulo"), ("monto_prestamo", cast("monto", "NUMERIC(14,2)")),
                       ("tasa_interes", cast("tasa", "NUMERIC(6,3)")), ("plazo_dias", cast("plazo", "INTEGER")), ("fecha_empeno", cast("inicio", "DATE")),
                       ("fecha_vencimiento", cast("vence", "DATE")), ("saldo_pendiente", cast("saldo", "NUMERIC(14,2)")), ("estado", cast("estado", "VARCHAR(20)"))],
       ["serie", "monto", "tasa", "plazo", "inicio", "vence", "saldo", "estado"], empenos,
       joins="\nJOIN operacion o ON o.tipo_operacion = 'EMPENO' AND o.monto = CAST(v.monto AS NUMERIC(14,2))\nJOIN articulo a ON a.numero_serie = v.serie",
       unica="x.id_operacion = o.id_operacion")
titulo("Operaciones: comprobantes (100)")
insert("comprobante", 100, [("id_operacion", "o.id_operacion"), ("numero_comprobante", cast("numero", "VARCHAR(40)")), ("tipo_comprobante", cast("tipo_comp", "VARCHAR(30)")),
                            ("fecha_emision", cast("fecha", "TIMESTAMP")), ("monto_total", cast("total", "NUMERIC(14,2)")), ("estado", cast("estado", "VARCHAR(20)"))],
       ["tipo", "monto", "numero", "tipo_comp", "total", "estado", "fecha"], comprob,
       joins="\nJOIN operacion o ON o.tipo_operacion = v.tipo AND o.monto = CAST(v.monto AS NUMERIC(14,2))", unica="x.numero_comprobante = v.numero")
titulo("Ventas (100)")
insert("venta", 100, [("id_cliente", "c.id_cliente"), ("id_servicio", "s.id_servicio"), ("fecha_venta", cast("fecha", "TIMESTAMP")), ("monto", cast("monto", "NUMERIC(14,2)")),
                      ("importe", cast("importe", "NUMERIC(14,2)")), ("estado", cast("estado", "VARCHAR(20)"))],
       ["documento", "servicio", "fecha", "monto", "importe", "estado"], ventas,
       joins="\nJOIN cliente c ON c.documento = v.documento\nJOIN servicio_financiero s ON s.nombre = v.servicio",
       unica="x.importe = CAST(v.importe AS NUMERIC(14,2)) AND x.id_cliente = c.id_cliente")
titulo("Ventas: detalle (100)")
insert("detalle_venta", 100, [("id_venta", "ve.id_venta"), ("descripcion", cast("descripcion", "VARCHAR(200)")), ("cantidad", cast("cantidad", "INTEGER")),
                              ("precio_unitario", cast("precio", "NUMERIC(14,2)")), ("subtotal", cast("subtotal", "NUMERIC(14,2)"))],
       ["importe", "descripcion", "cantidad", "precio", "subtotal"], detalles,
       joins="\nJOIN venta ve ON ve.importe = CAST(v.importe AS NUMERIC(14,2))")
titulo("Almacen: productos (100)")
insert("producto", 100, [("codigo", cast("codigo", "VARCHAR(40)")), ("nombre", cast("nombre", "VARCHAR(120)")), ("descripcion", cast("descripcion", "TEXT")),
                         ("categoria", cast("categoria", "VARCHAR(60)")), ("unidad_medida", cast("unidad", "VARCHAR(30)")), ("stock_actual", cast("stock", "INTEGER")),
                         ("stock_minimo", cast("minimo", "INTEGER")), ("estado", cast("estado", "VARCHAR(20)"))],
       ["codigo", "nombre", "descripcion", "categoria", "unidad", "stock", "minimo", "estado"], productos, unica="x.codigo = v.codigo")
titulo("Almacen: inventario por ubicacion (100)")
insert("inventario", 100, [("id_producto", "p.id_producto"), ("ubicacion", cast("ubicacion", "VARCHAR(80)")), ("cantidad", cast("cantidad", "INTEGER")),
                           ("fecha_actualizacion", cast("fecha", "TIMESTAMP"))],
       ["codigo", "ubicacion", "cantidad", "fecha"], inventario, joins="\nJOIN producto p ON p.codigo = v.codigo")
titulo("Almacen: movimientos de inventario (100)")
insert("movimiento_inventario", 100, [("id_producto", "p.id_producto"), ("tipo_movimiento", cast("tipo", "VARCHAR(20)")), ("cantidad", cast("cantidad", "INTEGER")),
                                       ("motivo", cast("motivo", "VARCHAR(200)")), ("fecha_movimiento", cast("fecha", "TIMESTAMP"))],
       ["codigo", "tipo", "cantidad", "motivo", "fecha"], mov_inv, joins="\nJOIN producto p ON p.codigo = v.codigo")
titulo("Logistica: proveedores (100)")
insert("proveedor", 100, [("razon_social", cast("razon", "VARCHAR(150)")), ("ruc", cast("ruc", "VARCHAR(20)")), ("contacto", cast("contacto", "VARCHAR(120)")),
                          ("telefono", cast("telefono", "VARCHAR(30)")), ("correo", cast("correo", "VARCHAR(120)")), ("direccion", cast("direccion", "VARCHAR(200)")),
                          ("estado", cast("estado", "VARCHAR(20)"))],
       ["razon", "ruc", "contacto", "telefono", "correo", "direccion", "estado"], proveedores, unica="x.ruc = v.ruc")
titulo("Logistica: movimientos (100)")
insert("movimiento_logistica", 100, [("id_proveedor", "p.id_proveedor"), ("tipo_movimiento", cast("tipo", "VARCHAR(30)")), ("descripcion", cast("descripcion", "TEXT")),
                                     ("cantidad", cast("cantidad", "INTEGER")), ("fecha_movimiento", cast("fecha", "TIMESTAMP")), ("estado", cast("estado", "VARCHAR(20)"))],
       ["ruc", "tipo", "descripcion", "cantidad", "fecha", "estado"], mov_log, joins="\nJOIN proveedor p ON p.ruc = v.ruc")
titulo("Marketing: prospectos (100)")
insert("prospecto", 100, [("nombres", cast("nombres", "VARCHAR(120)")), ("apellidos", cast("apellidos", "VARCHAR(120)")), ("documento", cast("documento", "VARCHAR(20)")),
                          ("telefono", cast("telefono", "VARCHAR(30)")), ("correo", cast("correo", "VARCHAR(120)")), ("origen", cast("origen", "VARCHAR(60)")),
                          ("estado", cast("estado", "VARCHAR(20)")), ("fecha_registro", cast("fecha", "TIMESTAMP"))],
       ["nombres", "apellidos", "documento", "telefono", "correo", "origen", "estado", "fecha"], prospectos, unica="x.documento = v.documento")
titulo("Marketing: campanias (100)")
insert("campania", 100, [("nombre", cast("nombre", "VARCHAR(120)")), ("descripcion", cast("descripcion", "TEXT")), ("fecha_inicio", cast("inicio", "DATE")),
                         ("fecha_fin", cast("fin", "DATE")), ("presupuesto", cast("presupuesto", "NUMERIC(14,2)")), ("canal", cast("canal", "VARCHAR(60)")),
                         ("estado", cast("estado", "VARCHAR(20)"))],
       ["nombre", "descripcion", "inicio", "fin", "presupuesto", "canal", "estado"], campanias, unica="x.nombre = v.nombre")
titulo("Marketing: campanias y prospectos contactados (100)")
insert("campania_prospecto", 100, [("id_campania", "c.id_campania"), ("id_prospecto", "p.id_prospecto"), ("fecha_contacto", cast("fecha", "DATE")),
                                   ("resultado", cast("resultado", "VARCHAR(120)")), ("estado", cast("estado", "VARCHAR(20)"))],
       ["campania", "documento", "fecha", "resultado", "estado"], camp_pros,
       joins="\nJOIN campania c ON c.nombre = v.campania\nJOIN prospecto p ON p.documento = v.documento")
titulo("Marketing: actividades (100)")
insert("actividad_marketing", 100, [("id_prospecto", "p.id_prospecto"), ("id_campania", "c.id_campania"), ("tipo_actividad", cast("tipo", "VARCHAR(60)")),
                                    ("descripcion", cast("descripcion", "TEXT")), ("fecha_actividad", cast("fecha", "TIMESTAMP"))],
       ["documento", "campania", "tipo", "descripcion", "fecha"], actividades,
       joins="\nJOIN prospecto p ON p.documento = v.documento\nJOIN campania c ON c.nombre = v.campania")
titulo("Produccion: lineas (100)")
insert("linea_produccion", 100, [("codigo", cast("codigo", "VARCHAR(40)")), ("nombre", cast("nombre", "VARCHAR(120)")), ("descripcion", cast("descripcion", "TEXT")),
                                 ("estado", cast("estado", "VARCHAR(20)"))],
       ["codigo", "nombre", "descripcion", "estado"], lineas, unica="x.codigo = v.codigo")
titulo("Produccion: ordenes (100)", "(TERMINADA = avance 100) ")
insert("orden_produccion", 100, [("id_producto", "p.id_producto"), ("id_linea", "l.id_linea"), ("cantidad", cast("cantidad", "INTEGER")), ("fecha_inicio", cast("inicio", "DATE")),
                                 ("fecha_fin_estimada", cast("fin_est", "DATE")), ("fecha_fin_real", cast("fin_real", "DATE")), ("avance", cast("avance", "NUMERIC(5,2)")),
                                 ("estado", cast("estado", "VARCHAR(20)")), ("observaciones", cast("obs", "TEXT"))],
       ["producto", "linea", "cantidad", "inicio", "fin_est", "fin_real", "avance", "estado", "obs"], ordenes,
       joins="\nJOIN producto p ON p.codigo = v.producto\nJOIN linea_produccion l ON l.codigo = v.linea")
titulo("Direccion y Gerencia: expedientes (100)")
insert("expediente_aprobacion", 100, [("codigo", cast("codigo", "VARCHAR(40)")), ("area", cast("area", "VARCHAR(80)")), ("solicitante", cast("solicitante", "VARCHAR(120)")),
                                      ("monto", cast("monto", "NUMERIC(14,2)")), ("impacto", cast("impacto", "VARCHAR(60)")), ("descripcion", cast("descripcion", "TEXT")),
                                      ("estado", cast("estado", "VARCHAR(20)")), ("fecha_solicitud", cast("solicitud", "TIMESTAMP")), ("fecha_resolucion", cast("resolucion", "TIMESTAMP"))],
       ["codigo", "area", "solicitante", "monto", "impacto", "descripcion", "estado", "solicitud", "resolucion"], expedientes, unica="x.codigo = v.codigo")
titulo("Direccion: decisiones (100)")
insert("decision_direccion", 100, [("id_expediente", "e.id_expediente"), ("tipo_decision", cast("tipo", "VARCHAR(40)")), ("descripcion", cast("descripcion", "TEXT")),
                                   ("fecha_decision", cast("fecha", "TIMESTAMP")), ("estado", cast("estado", "VARCHAR(20)"))],
       ["codigo", "tipo", "descripcion", "fecha", "estado"], dec_dir, joins="\nJOIN expediente_aprobacion e ON e.codigo = v.codigo")
titulo("Gerencia General: decisiones (100)")
insert("decision_gerencia", 100, [("id_expediente", "e.id_expediente"), ("tipo_decision", cast("tipo", "VARCHAR(40)")), ("descripcion", cast("descripcion", "TEXT")),
                                  ("fecha_decision", cast("fecha", "TIMESTAMP")), ("estado", cast("estado", "VARCHAR(20)"))],
       ["codigo", "tipo", "descripcion", "fecha", "estado"], dec_ger, joins="\nJOIN expediente_aprobacion e ON e.codigo = v.codigo",
       unica="x.descripcion = v.descripcion")
titulo("Gerencia General: resoluciones (100)")
insert("resolucion_gerencial", 100, [("id_decision_gerencia", "d.id_decision_gerencia"), ("numero_resolucion", cast("numero", "VARCHAR(40)")), ("titulo", cast("titulo", "VARCHAR(150)")),
                                     ("contenido", cast("contenido", "TEXT")), ("fecha_emision", cast("fecha", "DATE")), ("estado", cast("estado", "VARCHAR(20)"))],
       ["decision", "numero", "titulo", "contenido", "fecha", "estado"], resol, joins="\nJOIN decision_gerencia d ON d.descripcion = v.decision",
       unica="x.numero_resolucion = v.numero")
titulo("Direccion: reportes (100)")
insert("reporte_direccion", 100, [("tipo_reporte", cast("tipo", "VARCHAR(60)")), ("modulo", cast("modulo", "VARCHAR(60)")), ("descripcion", cast("descripcion", "TEXT")),
                                  ("fecha_generacion", cast("fecha", "TIMESTAMP"))],
       ["tipo", "modulo", "descripcion", "fecha"], rep_dir)

SQL.append("\n-- Verificacion rapida (conteo por tabla):\n-- SELECT 'cliente' t, COUNT(*) FROM cliente UNION ALL SELECT 'operacion', COUNT(*) FROM operacion UNION ALL SELECT 'venta', COUNT(*) FROM venta;\n-- Fin de los datos de demostracion.\n")

# =================================================================== TSV (modo memoria)
TSV = []
def sec(nombre, filas):
    TSV.append("@" + nombre)
    for f in filas: TSV.append("\t".join(tv(x) for x in f))
sec("rol", roles); sec("usuario", usuarios); sec("servicio", servicios)
sec("cliente", clientes); sec("tipo_cambio", tcs)
sec("producto", productos); sec("proveedor", proveedores); sec("prospecto", prospectos); sec("campania", campanias)
sec("articulo", articulos)
sec("operacion_cambio", ops_cambio); sec("operacion_prestamo", ops_prest); sec("operacion_empeno", ops_emp)
sec("prestamo", prestamos); sec("empeno", empenos)
sec("venta", ventas); sec("linea", lineas); sec("orden", ordenes); sec("expediente", expedientes)

# =================================================================== ESCRITURA
raiz = sys.argv[1] if len(sys.argv) > 1 else "."
with open(os.path.join(raiz, "db", "datos_100_registros.sql"), "w", encoding="ascii", newline="\n") as f:
    f.write("\n".join(SQL))
dest = os.path.join(raiz, "src", "com", "utp", "sistemafinanciero", "dao", "memoria")
if os.path.isdir(dest):
    with open(os.path.join(dest, "semilla_100.tsv"), "w", encoding="ascii", newline="\n") as f:
        f.write("# SIGSF - datos de demostracion (modo memoria). Generado por scripts/datos/generar_datos_100.py\n" + "\n".join(TSV) + "\n")
print("OK sql:", sum(len(s) for s in SQL), "bytes")
