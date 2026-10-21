package com.utp.sistemafinanciero.dao;

import com.utp.sistemafinanciero.model.marketing.Prospecto;

/**
 * Contrato DAO (Data Access Object) para la tabla {@code prospecto}.
 *
 * <p>La capa de servicios depende de esta interfaz y no de una
 * implementación concreta (principio de inversión de dependencias, DIP).
 * Implementaciones: {@code dao.jdbc.ProspectoDaoJdbc} (PostgreSQL) y
 * {@code dao.memoria.ProspectoDaoMemoria} (pruebas / demostración).</p>
 */
public interface ProspectoDao extends CrudDao<Prospecto> {
}
