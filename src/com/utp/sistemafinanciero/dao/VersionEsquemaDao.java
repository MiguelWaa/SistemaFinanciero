package com.utp.sistemafinanciero.dao;

import com.utp.sistemafinanciero.model.sistema.VersionEsquema;

import java.util.List;

/**
 * Contrato DAO para el historial de versiones del esquema de la base de
 * datos (tabla {@code version_esquema}). Solo se consulta y se agrega:
 * una versión aplicada no se modifica (igual que un commit en Git).
 */
public interface VersionEsquemaDao {

    /** Versiones aplicadas, de la más reciente a la más antigua. */
    List<VersionEsquema> listar();

    /** Versión vigente del esquema (la última aplicada) o {@code null}. */
    VersionEsquema actual();

    /** Registra una versión aplicada y devuelve su id. */
    Integer registrar(VersionEsquema v);
}
