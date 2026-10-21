package com.utp.sistemafinanciero.dao.memoria;

import com.utp.sistemafinanciero.dao.AuditoriaDao;
import com.utp.sistemafinanciero.model.seguridad.Auditoria;

import java.util.List;
import java.util.stream.Collectors;

/** Implementación en memoria de la bitácora de auditoría. */
public class AuditoriaDaoMemoria extends RepositorioMemoria<Auditoria> implements AuditoriaDao {

    public AuditoriaDaoMemoria() {
        super(Auditoria::getIdAuditoria, Auditoria::setIdAuditoria, true);
    }

    @Override
    public Integer registrar(Auditoria evento) {
        return crear(evento);
    }

    @Override
    public List<Auditoria> recientes(int limite) {
        return listar().stream().limit(limite).collect(Collectors.toList());
    }
}
