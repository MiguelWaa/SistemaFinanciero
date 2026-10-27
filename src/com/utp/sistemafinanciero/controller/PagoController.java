package com.utp.sistemafinanciero.controller;

import com.utp.sistemafinanciero.service.CotizacionService;
import com.utp.sistemafinanciero.service.PagoService;
import com.utp.sistemafinanciero.util.Json;
import com.utp.sistemafinanciero.web.ApiServer;
import com.utp.sistemafinanciero.web.Ctx;

import java.util.Map;

/**
 * Pagos (Yape, Plin, Tunki y caja), billeteras de cobro y cotizaciones
 * para el <b>personal</b>. Los permisos los aplica {@code PoliticaAcceso}:
 * <ul>
 *   <li>{@code /api/pagos}: CAJERO y SUPERVISOR verifican y registran; ANALISTA consulta.</li>
 *   <li>{@code /api/billeteras}: todos consultan; solo el ADMIN modifica el número y el QR.</li>
 *   <li>{@code /api/cotizaciones}: CAJERO cotiza en ventanilla; el resto consulta.</li>
 * </ul>
 */
public class PagoController implements Controlador {

    private final PagoService pagos;
    private final CotizacionService cotizaciones;

    public PagoController(PagoService pagos, CotizacionService cotizaciones) {
        this.pagos = pagos;
        this.cotizaciones = cotizaciones;
    }

    @Override
    public void registrar(ApiServer app) {
        // ---- Pagos ----
        app.get("/api/pagos", ctx -> pagos.listar(ctx.query("estado")));
        app.get("/api/pagos/resumen", ctx -> pagos.resumen());
        app.get("/api/pagos/{id}", ctx -> pagos.buscar(ctx.paramInt("id")));
        app.get("/api/pagos/pagables/{idCliente}", ctx -> pagos.pagables(ctx.paramInt("idCliente")));
        app.get("/api/pagos/{id}/constancia", ctx -> pagos.constancia(ctx.paramInt("id")));
        app.post("/api/pagos", ctx -> pagos.registrarEnCaja(ctx.bodyMap(), usuario(ctx)));
        app.put("/api/pagos/{id}/confirmar", ctx -> pagos.confirmar(ctx.paramInt("id"), usuario(ctx)));
        app.put("/api/pagos/{id}/rechazar", ctx -> {
            Map<String, Object> b = ctx.bodyMap();
            return pagos.rechazar(ctx.paramInt("id"), Json.str(b, "motivo"), usuario(ctx));
        });

        // ---- Billeteras de cobro ----
        app.get("/api/billeteras", ctx -> pagos.billeteras());
        app.put("/api/billeteras/{id}", ctx -> pagos.actualizarBilletera(ctx.paramInt("id"), ctx.bodyMap()));

        // ---- Cotizaciones ----
        app.get("/api/cotizaciones", ctx -> cotizaciones.listar());
        app.get("/api/cotizaciones/{id}", ctx -> cotizaciones.detalle(ctx.paramInt("id")));
        app.post("/api/cotizaciones", ctx -> {
            Map<String, Object> b = ctx.bodyMap();
            return cotizaciones.cotizar(Json.intOrNull(b, "idCliente"), b, "CAJA");
        });
    }

    private static String usuario(Ctx ctx) {
        return ctx.sesion() == null ? "sistema" : ctx.sesion().getUsuario();
    }
}
