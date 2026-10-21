package com.utp.sistemafinanciero.dao;

/**
 * DAO con el ciclo completo de mantenimiento (CRUD): crear, leer,
 * actualizar y eliminar. Es la suma de las tres interfaces segregadas.
 *
 * @param <T> entidad del modelo
 */
public interface CrudDao<T> extends LecturaDao<T>, EscrituraDao<T>, ActualizableDao<T> {
}
