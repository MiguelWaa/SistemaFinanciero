package com.utp.sistemafinanciero.dao.memoria;

import com.utp.sistemafinanciero.dao.ClienteDao;
import com.utp.sistemafinanciero.model.clientes.Cliente;

/** Implementación en memoria del {@link ClienteDao}. */
public class ClienteDaoMemoria extends RepositorioMemoria<Cliente> implements ClienteDao {

    public ClienteDaoMemoria() {
        super(Cliente::getIdCliente, Cliente::setIdCliente, false);
    }

    @Override
    public Cliente buscarPorDocumento(String documento) {
        return filtrar(c -> documento != null && documento.equals(c.getDocumento()))
                .stream().findFirst().orElse(null);
    }

    @Override
    public int contar() {
        return filtrar(c -> "ACTIVO".equals(c.getEstado())).size();
    }
}
