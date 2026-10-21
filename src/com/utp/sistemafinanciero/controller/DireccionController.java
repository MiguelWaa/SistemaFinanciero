package com.utp.sistemafinanciero.controller;

import com.utp.sistemafinanciero.service.ExpedienteService;
import com.utp.sistemafinanciero.util.Json;
import com.utp.sistemafinanciero.web.ApiServer;

/** Módulos Dirección y Gerencia General: expedientes de aprobación. */
public class DireccionController implements Controlador {

    private final ExpedienteService expedientes;

    public DireccionController(ExpedienteService expedientes) {
        this.expedientes = expedientes;
    }

    @Override
    public void registrar(ApiServer app) {
        app.get("/api/expedientes", ctx -> expedientes.listar());
        app.get("/api/expedientes/{id}", ctx -> expedientes.buscar(ctx.paramInt("id")));
        app.post("/api/expedientes", ctx -> expedientes.crear(MapeadorEntrada.aExpediente(ctx.bodyMap())));
        // Resolver: aprobar / observar / rechazar (RF-DIR-03)
        app.put("/api/expedientes/{id}", ctx ->
                expedientes.resolver(ctx.paramInt("id"), Json.str(ctx.bodyMap(), "estado")));
        app.delete("/api/expedientes/{id}", ctx -> { expedientes.eliminar(ctx.paramInt("id")); return Json.map("ok", true); });
    }
}
