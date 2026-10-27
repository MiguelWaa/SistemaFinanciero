package com.utp.sistemafinanciero.dao;

import com.utp.sistemafinanciero.model.pagos.Billetera;

/**
 * Contrato DAO para la tabla {@code billetera}: las cuentas de Yape, Plin
 * y Tunki con las que la entidad cobra.
 */
public interface BilleteraDao extends LecturaDao<Billetera>, ActualizableDao<Billetera> {

    Integer crear(Billetera billetera);

    /** Billetera por su código (YAPE, PLIN, TUNKI); null si no existe. */
    Billetera buscarPorCodigo(String codigo);
}
