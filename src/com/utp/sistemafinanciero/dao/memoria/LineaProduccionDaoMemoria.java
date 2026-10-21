package com.utp.sistemafinanciero.dao.memoria;

import com.utp.sistemafinanciero.dao.LineaProduccionDao;
import com.utp.sistemafinanciero.model.produccion.LineaProduccion;

/** Implementación en memoria del {@link LineaProduccionDao} (pruebas y modo demostración). */
public class LineaProduccionDaoMemoria extends RepositorioMemoria<LineaProduccion> implements LineaProduccionDao {

    public LineaProduccionDaoMemoria() {
        super(LineaProduccion::getIdLinea, LineaProduccion::setIdLinea, true);
    }
}
