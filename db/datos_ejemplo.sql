-- ============================================================
--  SIGSF · Datos de ejemplo para pruebas
--  Ejecutar DESPUES de schema.sql, sobre la base sistema_financiero.
--
--  Usuarios de acceso (email / contrasena):
--    admin@sigsf.com        / admin123    (ADMIN)
--    lucia.ramos@sigsf.com  / lucia123    (CAJERO)
--    jorge.torres@sigsf.com / jorge123    (ANALISTA)
--  Las contrasenas se guardan como hash SHA-256.
-- ============================================================

-- ---- Roles ----
INSERT INTO rol (nombre, descripcion) VALUES
  ('ADMIN',      'Administrador del sistema'),
  ('SUPERVISOR', 'Supervisa operaciones y reportes'),
  ('CAJERO',     'Registra operaciones de caja y cambio'),
  ('ANALISTA',   'Analiza prestamos, empenos y riesgos');

-- ---- Permisos ----
INSERT INTO permiso (nombre, descripcion) VALUES
  ('GESTIONAR_USUARIOS',   'Crear y administrar usuarios'),
  ('REGISTRAR_OPERACION',  'Registrar operaciones financieras'),
  ('VER_REPORTES',         'Consultar reportes e indicadores'),
  ('APROBAR_EXPEDIENTES',  'Aprobar expedientes de direccion');

-- ---- Rol - Permiso (el ADMIN tiene todos) ----
INSERT INTO rol_permiso (id_rol, id_permiso) VALUES
  (1,1),(1,2),(1,3),(1,4),
  (2,3),(3,2),(4,3);

-- ---- Usuarios (contrasenas SHA-256) ----
INSERT INTO usuario (nombre, apellido, email, password, id_rol, estado) VALUES
  ('Miguel', 'Machaca', 'admin@sigsf.com',
     '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 1, 'ACTIVO'),
  ('Lucia',  'Ramos',   'lucia.ramos@sigsf.com',
     '5e96a6cf706c46feaeda94715b59ffa61c09f2d46c122ce7456f385de6c51a06', 3, 'ACTIVO'),
  ('Jorge',  'Torres',  'jorge.torres@sigsf.com',
     'f69ed3a744195d7d1429b167f449ed9a76f36ba6a74f997a7bfe179649de32c4', 4, 'ACTIVO');

-- ---- Servicios financieros ----
INSERT INTO servicio_financiero (nombre, tipo_servicio, descripcion, estado) VALUES
  ('Cambio de moneda',  'CAMBIO',   'Compra y venta de divisas', 'ACTIVO'),
  ('Prestamo personal', 'CREDITO',  'Prestamos con interes',     'ACTIVO'),
  ('Empeno de articulos','EMPENO',  'Prestamo con garantia',     'ACTIVO'),
  ('Giros y envios',    'GIRO',     'Envio de dinero nacional',  'ACTIVO');

-- ---- Clientes ----
INSERT INTO cliente (tipo_documento, documento, nombres, apellidos, fecha_nacimiento, telefono, correo, direccion, estado) VALUES
  ('DNI','70000001','Juan',   'Perez Salas',   '1995-05-20','987654321','juan.perez@gmail.com','Av. Los Olivos 123','ACTIVO'),
  ('DNI','70000002','Maria',  'Lopez Diaz',    '1990-11-02','987111222','maria.lopez@gmail.com','Jr. Union 456','ACTIVO'),
  ('DNI','70000003','Carlos', 'Quispe Mamani', '1988-03-15','987333444','carlos.quispe@gmail.com','Calle Real 789','ACTIVO'),
  ('CE', '001234567','Ana',   'Torres Vega',   '1998-07-30','987555666','ana.torres@gmail.com','Av. Grau 1010','ACTIVO');

-- ---- Tipos de cambio ----
INSERT INTO tipo_cambio (moneda_origen, moneda_destino, compra, venta, fecha_inicio, estado) VALUES
  ('USD','PEN',3.7200,3.7800, CURRENT_DATE, 'ACTIVO'),
  ('EUR','PEN',4.0500,4.1500, CURRENT_DATE, 'ACTIVO');

-- ---- Productos (almacen). Dos por debajo del stock minimo. ----
INSERT INTO producto (codigo, nombre, descripcion, categoria, unidad_medida, stock_actual, stock_minimo, estado) VALUES
  ('PRD-001','Formularios de operacion','Talonarios preimpresos','Papeleria','UND', 120, 50, 'ACTIVO'),
  ('PRD-002','Sobres de seguridad',      'Sobres para valores',  'Papeleria','UND',  15, 40, 'ACTIVO'),
  ('PRD-003','Cinta para contadora',     'Insumo de contadora',  'Insumos',  'UND',   8, 20, 'ACTIVO'),
  ('PRD-004','Tinta para sellos',        'Tinta indeleble',      'Insumos',  'UND',  60, 25, 'ACTIVO'),
  ('PRD-005','Calculadora financiera',   'Equipo de caja',       'Equipos',  'UND',  10, 10, 'ACTIVO');

-- ---- Proveedores ----
INSERT INTO proveedor (razon_social, ruc, contacto, telefono, correo, direccion, estado) VALUES
  ('Distribuidora Andina SAC','20512345678','Pedro Ramos','014567890','ventas@andina.com','Av. Industrial 200','ACTIVO'),
  ('Importaciones del Sur EIRL','20598765432','Rosa Diaz','015678901','contacto@delsur.com','Jr. Comercio 45','ACTIVO');

-- ---- Prospectos (marketing) ----
INSERT INTO prospecto (nombres, apellidos, documento, telefono, correo, origen, estado) VALUES
  ('Luis','Gonzales','70011122','987000111','luis.g@gmail.com','FERIA','NUEVO'),
  ('Sofia','Rojas','70022233','987000222','sofia.r@gmail.com','WEB','CONTACTADO'),
  ('Diego','Flores','70033344','987000333','diego.f@gmail.com','REFERIDO','NUEVO');

-- ---- Campanias (marketing) ----
INSERT INTO campania (nombre, descripcion, fecha_inicio, fecha_fin, presupuesto, canal, estado) VALUES
  ('Cambio sin comision','Promocion de cambio de dolares', CURRENT_DATE, CURRENT_DATE + INTERVAL '30 days', 1500.00, 'REDES', 'ACTIVA'),
  ('Prestamos rapidos',   'Difusion de prestamos personales', CURRENT_DATE, CURRENT_DATE + INTERVAL '45 days', 2500.00, 'VOLANTES', 'ACTIVA');

-- ---- Articulos (para empeno) ----
INSERT INTO articulo (id_cliente, descripcion, categoria, marca, modelo, numero_serie, estado_fisico, valor_estimado, estado) VALUES
  (1,'Laptop 14 pulgadas','Electronica','HP','245 G8','SN-HP-0099','BUENO',1800.00,'ACTIVO'),
  (3,'Anillo de oro 18k','Joyeria','-','-','-','MUY BUENO',1200.00,'ACTIVO');

-- ---- Operaciones (hoy) ----
-- op 1: cambio de moneda; op 2: base de prestamo; op 3: base de empeno
INSERT INTO operacion (id_cliente, id_tipo_cambio, tipo_operacion, moneda_origen, moneda_destino, monto, tipo_cambio, total_entregar, total_recibir, estado) VALUES
  (1, 1, 'CAMBIO',   'USD','PEN', 100.00, 3.7800, 100.00, 378.00, 'COMPLETADA'),
  (2, NULL,'PRESTAMO','PEN','PEN', 5000.00, NULL, NULL, NULL, 'COMPLETADA'),
  (1, NULL,'EMPENO',  'PEN','PEN',  800.00, NULL, NULL, NULL, 'COMPLETADA');

-- ---- Prestamo (sobre la operacion 2) ----
INSERT INTO prestamo (id_operacion, monto_prestamo, tasa_interes, plazo_meses, cuota_mensual, fecha_inicio, fecha_vencimiento, saldo_pendiente, estado) VALUES
  (2, 5000.00, 15.000, 12, 479.17, CURRENT_DATE, CURRENT_DATE + INTERVAL '12 months', 5750.00, 'ACTIVO');

-- ---- Empeno (sobre la operacion 3 y el articulo 1) ----
INSERT INTO empeno (id_operacion, id_articulo, monto_prestamo, tasa_interes, plazo_dias, fecha_empeno, fecha_vencimiento, saldo_pendiente, estado) VALUES
  (3, 1, 800.00, 10.000, 30, CURRENT_DATE, CURRENT_DATE + INTERVAL '30 days', 800.00, 'ACTIVO');

-- ---- Ventas (este mes) ----
INSERT INTO venta (id_cliente, id_servicio, monto, importe, estado) VALUES
  (1, 1, 378.00, 378.00, 'REGISTRADA'),
  (2, 2, 5000.00, 5750.00, 'REGISTRADA'),
  (3, 3, 800.00, 800.00, 'REGISTRADA'),
  (4, 4, 300.00, 305.00, 'REGISTRADA');

-- ---- Expedientes (direccion) ----
INSERT INTO expediente_aprobacion (codigo, area, solicitante, monto, impacto, descripcion, estado) VALUES
  ('EXP-001','OPERACIONES','Lucia Ramos', 20000.00,'ALTO','Ampliacion de limite de caja para cambio','PENDIENTE'),
  ('EXP-002','MARKETING','Jorge Torres',  5000.00,'MEDIO','Presupuesto adicional para campania','PENDIENTE'),
  ('EXP-003','ALMACEN','Miguel Machaca',  3000.00,'BAJO','Compra de insumos de caja','APROBADO');

-- Fin de los datos de ejemplo.
