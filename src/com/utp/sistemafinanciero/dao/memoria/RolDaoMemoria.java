package com.utp.sistemafinanciero.dao.memoria;

import com.utp.sistemafinanciero.dao.RolDao;
import com.utp.sistemafinanciero.model.seguridad.Rol;

/** Implementación en memoria del {@link RolDao} (pruebas y modo demostración). */
public class RolDaoMemoria extends RepositorioMemoria<Rol> implements RolDao {

    public RolDaoMemoria() {
        super(Rol::getIdRol, Rol::setIdRol, false);
    }
}
