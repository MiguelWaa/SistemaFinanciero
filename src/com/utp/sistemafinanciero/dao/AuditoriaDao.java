package com.utp.sistemafinanciero.dao;

import com.utp.sistemafinanciero.model.seguridad.Auditoria;

import java.util.List;

/**
 * Contrato DAO para la bitácora de auditoría (tabla {@code auditoria}).
 * Solo se inserta y se consulta: la bitácora no se edita ni se borra
 * desde la aplicación (integridad de la evidencia).
 */
public interface AuditoriaDao {

    /** Registra un evento y devuelve su id. */
    Integer registrar(Auditoria evento);

    /** Últimos eventos, del más reciente al más antiguo. */
    List<Auditoria> recientes(int limite);
}
