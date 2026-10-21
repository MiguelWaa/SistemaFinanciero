package com.utp.sistemafinanciero.dao;

import java.util.List;

/**
 * Operaciones de solo lectura de un DAO.
 *
 * <p>Las interfaces genéricas {@link LecturaDao}, {@link EscrituraDao} y
 * {@link ActualizableDao} aplican el principio de segregación de
 * interfaces (ISP): cada DAO implementa solo las capacidades que
 * realmente ofrece (por ejemplo, una operación financiera se registra y
 * se consulta, pero no se edita).</p>
 *
 * @param <T> entidad del modelo
 */
public interface LecturaDao<T> {

    /** Devuelve todos los registros de la tabla. */
    List<T> listar();

    /** Busca un registro por su clave primaria; devuelve null si no existe. */
    T buscar(int id);
}
