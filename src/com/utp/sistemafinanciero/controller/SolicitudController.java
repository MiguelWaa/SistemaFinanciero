package com.utp.sistemafinanciero.controller;

import com.utp.sistemafinanciero.service.SolicitudService;
import com.utp.sistemafinanciero.web.ApiServer;

/** Bandeja de solicitudes de clientes (lo que piden desde el Portal). */
public class SolicitudController implements Controlador {

    private final SolicitudService solicitudes;

    public SolicitudController(SolicitudService solicitudes) {
        this.solicitudes = solicitudes;
    }

    @Override
    public void registrar(ApiServer app) {
        app.get("/api/solicitudes", ctx -> solicitudes.listar(null));
        app.get("/api/solicitudes/pendientes", ctx -> solicitudes.listar("PENDIENTE"));
        app.put("/api/solicitudes/{id}/aprobar", ctx -> solicitudes.aprobar(ctx.paramInt("id")));
        app.put("/api/solicitudes/{id}/rechazar", ctx -> solicitudes.rechazar(ctx.paramInt("id")));
    }
}
