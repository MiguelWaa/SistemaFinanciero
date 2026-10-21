-- ============================================================
--  SIGSF · Portal del Cliente — tabla de accesos
--
--  EJECUTAR UNA SOLA VEZ sobre tu base de datos existente.
--  NO borra ni modifica ninguna información que ya tengas.
--
--  Crea la tabla donde se guarda la contraseña con la que cada
--  cliente entra al Portal del Cliente.
-- ============================================================

CREATE TABLE IF NOT EXISTS cliente_acceso (
    id_cliente INTEGER PRIMARY KEY REFERENCES cliente(id_cliente) ON DELETE CASCADE,
    password   VARCHAR(200) NOT NULL
);

-- A los clientes que ya existen se les habilita el acceso usando
-- su propio número de documento como contraseña inicial.
INSERT INTO cliente_acceso (id_cliente, password)
SELECT c.id_cliente, c.documento
FROM cliente c
WHERE c.documento IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM cliente_acceso a WHERE a.id_cliente = c.id_cliente);

-- Consulta para ver con qué documento puede entrar cada cliente:
-- SELECT c.documento AS usuario, a.password AS contrasena, c.nombres, c.apellidos
-- FROM cliente c JOIN cliente_acceso a ON a.id_cliente = c.id_cliente;
