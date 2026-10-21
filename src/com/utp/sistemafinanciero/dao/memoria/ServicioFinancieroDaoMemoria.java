package com.utp.sistemafinanciero.dao.memoria;

import com.utp.sistemafinanciero.dao.ServicioFinancieroDao;
import com.utp.sistemafinanciero.model.clientes.ServicioFinanciero;

/** Implementación en memoria del {@link ServicioFinancieroDao} (pruebas y modo demostración). */
public class ServicioFinancieroDaoMemoria extends RepositorioMemoria<ServicioFinanciero> implements ServicioFinancieroDao {

    public ServicioFinancieroDaoMemoria() {
        super(ServicioFinanciero::getIdServicio, ServicioFinanciero::setIdServicio, false);
    }
}
