package com.utp.sistemafinanciero.dao;

import com.utp.sistemafinanciero.model.produccion.LineaProduccion;

/**
 * Contrato DAO (Data Access Object) para la tabla {@code linea_produccion}.
 *
 * <p>La capa de servicios depende de esta interfaz y no de una
 * implementación concreta (principio de inversión de dependencias, DIP).
 * Implementaciones: {@code dao.jdbc.LineaProduccionDaoJdbc} (PostgreSQL) y
 * {@code dao.memoria.LineaProduccionDaoMemoria} (pruebas / demostración).</p>
 */
public interface LineaProduccionDao extends CrudDao<LineaProduccion> {
}
