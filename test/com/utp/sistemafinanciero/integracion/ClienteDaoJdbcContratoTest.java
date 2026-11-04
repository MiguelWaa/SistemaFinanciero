package com.utp.sistemafinanciero.integracion;

import com.utp.sistemafinanciero.dao.ClienteDao;
import com.utp.sistemafinanciero.dao.jdbc.ClienteDaoJdbc;

/** Contrato DAO ejecutado sobre PostgreSQL real (SistemaFinanciero_test). */
public class ClienteDaoJdbcContratoTest extends ContratoClienteDao {
    @Override
    protected ClienteDao crearDao() {
        BdPruebas.requerir();
        return new ClienteDaoJdbc();
    }
}
