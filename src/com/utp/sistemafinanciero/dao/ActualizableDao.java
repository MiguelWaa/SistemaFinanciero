package com.utp.sistemafinanciero.dao;

/**
 * Operación de modificación de un DAO.
 *
 * @param <T> entidad del modelo
 */
public interface ActualizableDao<T> {

    /** Actualiza la entidad (identificada por su id) y devuelve las filas afectadas. */
    int actualizar(T entidad);
}
