package com.utp.sistemafinanciero.dao.memoria;

import com.utp.sistemafinanciero.dao.ProveedorDao;
import com.utp.sistemafinanciero.model.logistica.Proveedor;

/** Implementación en memoria del {@link ProveedorDao} (pruebas y modo demostración). */
public class ProveedorDaoMemoria extends RepositorioMemoria<Proveedor> implements ProveedorDao {

    public ProveedorDaoMemoria() {
        super(Proveedor::getIdProveedor, Proveedor::setIdProveedor, false);
    }
}
