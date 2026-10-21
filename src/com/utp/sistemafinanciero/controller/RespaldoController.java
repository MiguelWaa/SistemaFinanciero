package com.utp.sistemafinanciero.controller;

import com.utp.sistemafinanciero.backup.RespaldoService;
import com.utp.sistemafinanciero.util.Json;
import com.utp.sistemafinanciero.web.ApiServer;
import com.utp.sistemafinanciero.web.Descarga;

/** Respaldos (backup) de la base de datos. Solo para el rol ADMIN. */
public class RespaldoController implements Controlador {

    private final RespaldoService respaldos;

    public RespaldoController(RespaldoService respaldos) {
        this.respaldos = respaldos;
    }

    @Override
    public void registrar(ApiServer app) {
        app.get("/api/respaldos", ctx -> respaldos.listar());
        app.post("/api/respaldos", ctx -> respaldos.generar());
        app.get("/api/respaldos/{nombre}/descargar", ctx -> {
            String n = ctx.param("nombre");
            String tipo = n.endsWith(".json") ? "application/json" : "application/sql";
            return new Descarga(n, tipo, respaldos.leer(n));
        });
        app.delete("/api/respaldos/{nombre}", ctx -> {
            respaldos.eliminar(ctx.param("nombre"));
            return Json.map("ok", true);
        });
    }
}
