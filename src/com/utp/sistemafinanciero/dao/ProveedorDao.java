package com.utp.sistemafinanciero.dao;

import com.utp.sistemafinanciero.model.logistica.Proveedor;

/**
 * Contrato DAO (Data Access Object) para la tabla {@code proveedor}.
 *
 * <p>La capa de servicios depende de esta interfaz y no de una
 * implementación concreta (principio de inversión de dependencias, DIP).
 * Implementaciones: {@code dao.jdbc.ProveedorDaoJdbc} (PostgreSQL) y
 * {@code dao.memoria.ProveedorDaoMemoria} (pruebas / demostración).</p>
 */
public interface ProveedorDao extends CrudDao<Proveedor> {
}
