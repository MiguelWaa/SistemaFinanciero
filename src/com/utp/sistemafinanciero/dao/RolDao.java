package com.utp.sistemafinanciero.dao;

import com.utp.sistemafinanciero.model.seguridad.Rol;

/**
 * Contrato DAO (Data Access Object) para la tabla {@code rol}.
 *
 * <p>La capa de servicios depende de esta interfaz y no de una
 * implementación concreta (principio de inversión de dependencias, DIP).
 * Implementaciones: {@code dao.jdbc.RolDaoJdbc} (PostgreSQL) y
 * {@code dao.memoria.RolDaoMemoria} (pruebas / demostración).</p>
 */
public interface RolDao extends CrudDao<Rol> {
}
