package com.utp.sistemafinanciero.controller;

import com.utp.sistemafinanciero.service.ProveedorService;
import com.utp.sistemafinanciero.web.ApiServer;

/** Módulo Logística: proveedores. */
public class LogisticaController implements Controlador {

    private final ProveedorService proveedores;

    public LogisticaController(ProveedorService proveedores) {
        this.proveedores = proveedores;
    }

    @Override
    public void registrar(ApiServer app) {
        new ControladorCrud<>("proveedores", proveedores, MapeadorEntrada::aProveedor).registrar(app);
    }
}
