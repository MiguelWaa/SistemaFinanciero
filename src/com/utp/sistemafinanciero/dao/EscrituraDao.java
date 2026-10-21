package com.utp.sistemafinanciero.dao;

/**
 * Operaciones de alta y baja de un DAO.
 *
 * @param <T> entidad del modelo
 */
public interface EscrituraDao<T> {

    /** Inserta la entidad y devuelve la clave primaria generada. */
    Integer crear(T entidad);

    /** Elimina por clave primaria y devuelve las filas afectadas (0 si no existía). */
    int eliminar(int id);
}
