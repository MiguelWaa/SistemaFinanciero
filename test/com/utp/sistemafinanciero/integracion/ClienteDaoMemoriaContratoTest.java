package com.utp.sistemafinanciero.integracion;

import com.utp.sistemafinanciero.dao.ClienteDao;
import com.utp.sistemafinanciero.dao.memoria.ClienteDaoMemoria;

/** Contrato DAO ejecutado sobre la implementación en memoria. */
public class ClienteDaoMemoriaContratoTest extends ContratoClienteDao {
    @Override
    protected ClienteDao crearDao() {
        return new ClienteDaoMemoria();
    }
}
