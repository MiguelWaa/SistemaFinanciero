package com.utp.sistemafinanciero.dao.memoria;

import com.utp.sistemafinanciero.dao.AccesoClienteDao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Implementación en memoria del {@link AccesoClienteDao} (credenciales del portal). */
public class AccesoClienteDaoMemoria implements AccesoClienteDao {

    private final Map<Integer, String> accesos = new ConcurrentHashMap<>();

    @Override
    public String passwordDe(int idCliente) {
        return accesos.get(idCliente);
    }

    @Override
    public void guardar(int idCliente, String password) {
        accesos.put(idCliente, password);
    }
}
