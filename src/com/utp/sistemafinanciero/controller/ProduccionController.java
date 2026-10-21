package com.utp.sistemafinanciero.controller;

import com.utp.sistemafinanciero.service.ProduccionService;
import com.utp.sistemafinanciero.util.Json;
import com.utp.sistemafinanciero.web.ApiServer;

/** Módulo Producción: líneas y órdenes de producción. */
public class ProduccionController implements Controlador {

    private final ProduccionService produccion;

    public ProduccionController(ProduccionService produccion) {
        this.produccion = produccion;
    }

    @Override
    public void registrar(ApiServer app) {
        app.get("/api/lineas", ctx -> produccion.listarLineas());
        app.post("/api/lineas", ctx -> produccion.crearLinea(MapeadorEntrada.aLinea(ctx.bodyMap())));
        app.put("/api/lineas/{id}", ctx -> produccion.actualizarLinea(ctx.paramInt("id"), MapeadorEntrada.aLinea(ctx.bodyMap())));
        app.delete("/api/lineas/{id}", ctx -> { produccion.eliminarLinea(ctx.paramInt("id")); return Json.map("ok", true); });

        app.get("/api/ordenes", ctx -> produccion.listarOrdenes());
        app.post("/api/ordenes", ctx -> produccion.crearOrden(MapeadorEntrada.aOrden(ctx.bodyMap())));
        app.put("/api/ordenes/{id}", ctx -> produccion.actualizarOrden(ctx.paramInt("id"), MapeadorEntrada.aOrden(ctx.bodyMap())));
        app.delete("/api/ordenes/{id}", ctx -> { produccion.eliminarOrden(ctx.paramInt("id")); return Json.map("ok", true); });
    }
}
