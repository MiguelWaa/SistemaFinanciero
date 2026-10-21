package com.utp.sistemafinanciero.dao.memoria;

import com.utp.sistemafinanciero.dao.ExpedienteAprobacionDao;
import com.utp.sistemafinanciero.model.direccion.ExpedienteAprobacion;

/** Implementación en memoria del {@link ExpedienteAprobacionDao}. */
public class ExpedienteAprobacionDaoMemoria extends RepositorioMemoria<ExpedienteAprobacion>
        implements ExpedienteAprobacionDao {

    public ExpedienteAprobacionDaoMemoria() {
        super(ExpedienteAprobacion::getIdExpediente, ExpedienteAprobacion::setIdExpediente, true);
    }

    @Override
    public int contarPendientes() {
        return filtrar(e -> "PENDIENTE".equals(e.getEstado())).size();
    }
}
