package com.utp.sistemafinanciero.dao.memoria;

import com.utp.sistemafinanciero.dao.OrdenProduccionDao;
import com.utp.sistemafinanciero.model.produccion.OrdenProduccion;

/** Implementación en memoria del {@link OrdenProduccionDao} (pruebas y modo demostración). */
public class OrdenProduccionDaoMemoria extends RepositorioMemoria<OrdenProduccion> implements OrdenProduccionDao {

    public OrdenProduccionDaoMemoria() {
        super(OrdenProduccion::getIdOrden, OrdenProduccion::setIdOrden, true);
    }
}
