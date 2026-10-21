package com.utp.sistemafinanciero.controller;

import com.utp.sistemafinanciero.dao.DaoFactory;
import com.utp.sistemafinanciero.service.DashboardService;
import com.utp.sistemafinanciero.util.Json;
import com.utp.sistemafinanciero.web.ApiServer;

/** Estado del servidor e indicadores del panel principal (Dirección / Gerencia). */
public class SistemaController implements Controlador {

    private final DashboardService dashboard;
    private final DaoFactory bd;

    public SistemaController(DashboardService dashboard, DaoFactory bd) {
        this.dashboard = dashboard;
        this.bd = bd;
    }

    @Override
    public void registrar(ApiServer app) {
        app.get("/api/health", ctx -> Json.map(
                "status", "ok",
                "servicio", "SIGSF API",
                "persistencia", bd.nombre()));
        app.get("/api/dashboard", ctx -> dashboard.indicadores());
    }
}
