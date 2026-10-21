package com.utp.sistemafinanciero.controller;

import com.utp.sistemafinanciero.service.EmpenoService;
import com.utp.sistemafinanciero.service.OperacionService;
import com.utp.sistemafinanciero.service.PrestamoService;
import com.utp.sistemafinanciero.service.TipoCambioService;
import com.utp.sistemafinanciero.util.Json;
import com.utp.sistemafinanciero.web.ApiServer;

import static com.utp.sistemafinanciero.controller.Exportar.decimal;
import static com.utp.sistemafinanciero.controller.Exportar.texto;

/** Módulo Operaciones Financieras: cambio de moneda, tipos de cambio, préstamos y empeños. */
public class OperacionController implements Controlador {

    private final OperacionService operaciones;
    private final TipoCambioService tiposCambio;
    private final PrestamoService prestamos;
    private final EmpenoService empenos;

    public OperacionController(OperacionService operaciones, TipoCambioService tiposCambio,
                               PrestamoService prestamos, EmpenoService empenos) {
        this.operaciones = operaciones;
        this.tiposCambio = tiposCambio;
        this.prestamos = prestamos;
        this.empenos = empenos;
    }

    @Override
    public void registrar(ApiServer app) {
        // ---- Operaciones (se registran y consultan; no se editan) ----
        app.get("/api/operaciones/excel", ctx -> Exportar.excel("operaciones.xlsx", "Operaciones",
                new String[]{"N°", "Cliente", "Tipo", "Monto", "Total recibir", "Estado", "Fecha"},
                operaciones.listar(), o -> new String[]{
                        texto(o.getIdOperacion()), texto(o.getIdCliente()), texto(o.getTipoOperacion()),
                        decimal(o.getMonto()), decimal(o.getTotalRecibir()), texto(o.getEstado()),
                        o.getFechaOperacion() == null ? "" : o.getFechaOperacion().toString().replace("T", " ")}));
        app.get("/api/operaciones", ctx -> operaciones.listar());
        app.get("/api/operaciones/{id}", ctx -> operaciones.buscar(ctx.paramInt("id")));
        app.post("/api/operaciones", ctx -> operaciones.crear(MapeadorEntrada.aOperacion(ctx.bodyMap())));
        app.put("/api/operaciones/{id}/estado", ctx -> {
            String estado = Json.str(ctx.bodyMap(), "estado");
            return operaciones.cambiarEstado(ctx.paramInt("id"), estado);
        });
        app.delete("/api/operaciones/{id}", ctx -> { operaciones.eliminar(ctx.paramInt("id")); return Json.map("ok", true); });

        // ---- Tipos de cambio (mantenimiento completo) ----
        new ControladorCrud<>("tipos-cambio", tiposCambio, MapeadorEntrada::aTipoCambio).registrar(app);

        // ---- Préstamos ----
        app.get("/api/prestamos", ctx -> prestamos.listar());
        app.get("/api/prestamos/{id}", ctx -> prestamos.buscar(ctx.paramInt("id")));
        app.post("/api/prestamos", ctx -> prestamos.crear(MapeadorEntrada.aPrestamo(ctx.bodyMap())));
        app.delete("/api/prestamos/{id}", ctx -> { prestamos.eliminar(ctx.paramInt("id")); return Json.map("ok", true); });

        // ---- Empeños ----
        app.get("/api/empenos", ctx -> empenos.listar());
        app.get("/api/empenos/{id}", ctx -> empenos.buscar(ctx.paramInt("id")));
        app.post("/api/empenos", ctx -> empenos.crear(MapeadorEntrada.aEmpeno(ctx.bodyMap())));
        app.delete("/api/empenos/{id}", ctx -> { empenos.eliminar(ctx.paramInt("id")); return Json.map("ok", true); });
    }
}
