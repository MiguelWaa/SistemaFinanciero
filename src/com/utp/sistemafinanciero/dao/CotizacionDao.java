package com.utp.sistemafinanciero.dao;

import com.utp.sistemafinanciero.model.pagos.Cotizacion;

import java.util.List;

/** Contrato DAO para la tabla {@code cotizacion}. */
public interface CotizacionDao extends LecturaDao<Cotizacion> {

    Integer crear(Cotizacion cotizacion);

    /** Actualiza el estado y la operación asociada (al convertirla en solicitud). */
    int actualizar(Cotizacion cotizacion);

    List<Cotizacion> listarPorCliente(int idCliente);
}
