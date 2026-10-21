package com.utp.sistemafinanciero.dao.memoria;

import com.utp.sistemafinanciero.dao.VersionEsquemaDao;
import com.utp.sistemafinanciero.model.sistema.VersionEsquema;

import java.util.List;

/** Implementación en memoria del historial de versiones del esquema. */
public class VersionEsquemaDaoMemoria extends RepositorioMemoria<VersionEsquema> implements VersionEsquemaDao {

    public VersionEsquemaDaoMemoria() {
        super(VersionEsquema::getIdVersion, VersionEsquema::setIdVersion, true);
    }

    @Override
    public VersionEsquema actual() {
        List<VersionEsquema> todas = listar();
        return todas.isEmpty() ? null : todas.get(0);
    }

    @Override
    public Integer registrar(VersionEsquema v) {
        boolean repetida = listar().stream().anyMatch(x -> x.getVersion().equals(v.getVersion()));
        return repetida ? null : crear(v);
    }
}
