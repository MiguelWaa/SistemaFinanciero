package com.utp.sistemafinanciero.dao;

import com.utp.sistemafinanciero.model.operaciones.Articulo;

/**
 * Contrato DAO (Data Access Object) para la tabla {@code articulo}.
 *
 * <p>La capa de servicios depende de esta interfaz y no de una
 * implementación concreta (principio de inversión de dependencias, DIP).
 * Implementaciones: {@code dao.jdbc.ArticuloDaoJdbc} (PostgreSQL) y
 * {@code dao.memoria.ArticuloDaoMemoria} (pruebas / demostración).</p>
 */
public interface ArticuloDao extends LecturaDao<Articulo>, EscrituraDao<Articulo> {
}
