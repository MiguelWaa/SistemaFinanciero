package com.utp.sistemafinanciero.dao;

import com.utp.sistemafinanciero.model.marketing.Campania;

/**
 * Contrato DAO (Data Access Object) para la tabla {@code campania}.
 *
 * <p>La capa de servicios depende de esta interfaz y no de una
 * implementación concreta (principio de inversión de dependencias, DIP).
 * Implementaciones: {@code dao.jdbc.CampaniaDaoJdbc} (PostgreSQL) y
 * {@code dao.memoria.CampaniaDaoMemoria} (pruebas / demostración).</p>
 */
public interface CampaniaDao extends CrudDao<Campania> {
}
