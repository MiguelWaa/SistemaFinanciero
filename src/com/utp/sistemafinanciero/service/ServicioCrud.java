package com.utp.sistemafinanciero.service;

import java.util.List;

/**
 * Contrato común de los servicios que ofrecen el mantenimiento completo
 * (CRUD) de una entidad.
 *
 * <p>Gracias a esta abstracción, un único controlador genérico
 * ({@code controller.ControladorCrud}) publica los 5 endpoints REST de
 * cualquier servicio que la implemente (principios OCP y LSP): agregar un
 * nuevo mantenimiento no exige escribir rutas nuevas.</p>
 *
 * @param <E> tipo que recibe (entidad)
 * @param <S> tipo que devuelve (entidad o DTO)
 */
public interface ServicioCrud<E, S> {

    List<S> listar();

    S buscar(int id);

    S crear(E entidad);

    S actualizar(int id, E entidad);

    void eliminar(int id);
}
