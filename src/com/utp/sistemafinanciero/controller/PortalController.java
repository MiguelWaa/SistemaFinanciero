package com.utp.sistemafinanciero.controller;

import com.utp.sistemafinanciero.dto.LoginDTO;
import com.utp.sistemafinanciero.service.AuditoriaService;
import com.utp.sistemafinanciero.service.PortalService;
import com.utp.sistemafinanciero.util.Json;
import com.utp.sistemafinanciero.web.ApiServer;

import java.util.Map;

/**
 * Portal del Cliente (parte pública). El {@code FiltroSeguridad}
 * garantiza que cada cliente solo acceda a {@code /api/portal/{suId}/...}.
 */
public class PortalController implements Controlador {

    private final PortalService portal;

    public PortalController(PortalService portal) {
        this.portal = portal;
    }

    @Override
    public void registrar(ApiServer app) {
        app.post("/api/portal/registro", ctx -> {
            Map<String, Object> b = ctx.bodyMap();
            ctx.atributo(AuditoriaService.USUARIO_LOGIN, Json.str(b, "documento"));
            return portal.registrar(MapeadorEntrada.aCliente(b), Json.str(b, "password"));
        });
        app.post("/api/portal/login", ctx -> {
            LoginDTO datos = LoginDTO.cliente(ctx.bodyMap());
            ctx.atributo(AuditoriaService.USUARIO_LOGIN, datos.getUsuario());
            return portal.login(datos);
        });
        app.put("/api/portal/{id}/datos", ctx -> portal.actualizarDatos(ctx.paramInt("id"), ctx.bodyMap()));
        app.get("/api/portal/{id}/operaciones", ctx -> portal.misOperaciones(ctx.paramInt("id")));
        app.post("/api/portal/{id}/cambio", ctx -> {
            Map<String, Object> b = ctx.bodyMap();
            return portal.solicitarCambio(ctx.paramInt("id"), Json.intOrNull(b, "idTipoCambio"),
                    Json.decimalOrNull(b, "monto"));
        });
        app.post("/api/portal/{id}/prestamo", ctx -> {
            Map<String, Object> b = ctx.bodyMap();
            // La tasa NO se acepta del cliente: la fija la entidad (evita que el cliente la manipule).
            return portal.solicitarPrestamo(ctx.paramInt("id"), Json.decimalOrNull(b, "montoPrestamo"),
                    Json.intOrNull(b, "plazoMeses"), null);
        });
        app.post("/api/portal/{id}/empeno", ctx -> {
            Map<String, Object> b = ctx.bodyMap();
            return portal.solicitarEmpeno(ctx.paramInt("id"), Json.str(b, "descripcionArticulo"),
                    Json.decimalOrNull(b, "valorEstimado"), Json.decimalOrNull(b, "montoPrestamo"),
                    Json.intOrNull(b, "plazoDias"));
        });
    }
}
