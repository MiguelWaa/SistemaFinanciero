package com.utp.sistemafinanciero.dao;

import com.utp.sistemafinanciero.model.almacen.Producto;

/**
 * Contrato DAO (Data Access Object) para la tabla {@code producto}.
 *
 * <p>La capa de servicios depende de esta interfaz y no de una
 * implementación concreta (principio de inversión de dependencias, DIP).
 * Implementaciones: {@code dao.jdbc.ProductoDaoJdbc} (PostgreSQL) y
 * {@code dao.memoria.ProductoDaoMemoria} (pruebas / demostración).</p>
 */
public interface ProductoDao extends CrudDao<Producto> {
    /** Productos cuyo stock actual esta en o por debajo del minimo. */
    int contarStockBajo();
}
