package com.utp.sistemafinanciero.controller;

import com.utp.sistemafinanciero.dao.VentaDao;
import com.utp.sistemafinanciero.dto.VentaReporteDTO;
import com.utp.sistemafinanciero.model.ventas.Venta;
import com.utp.sistemafinanciero.service.VentaService;
import com.utp.sistemafinanciero.util.ExcelExporter;
import com.utp.sistemafinanciero.util.Json;
import com.utp.sistemafinanciero.web.ApiServer;
import com.utp.sistemafinanciero.web.Descarga;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Módulo Ventas, con el reporte Excel que se actualiza en cada venta (RF-VNT-05/06). */
public class VentaController implements Controlador {

    private static final Logger LOG = LoggerFactory.getLogger(VentaController.class);

    private static final String[] ENCABEZADOS = {"N° Venta", "Cliente", "Servicio", "Fecha", "Importe (S/)", "Estado"};

    private final VentaService ventas;
    private final VentaDao ventaDao;

    public VentaController(VentaService ventas, VentaDao ventaDao) {
        this.ventas = ventas;
        this.ventaDao = ventaDao;
    }

    @Override
    public void registrar(ApiServer app) {
        app.get("/api/ventas/excel", ctx -> {
            byte[] datos = excel();
            guardarEnDisco(datos);
            return new Descarga("ventas.xlsx", Exportar.TIPO_EXCEL, datos);
        });
        app.get("/api/ventas/reporte", ctx -> ventaDao.reporte());
        app.get("/api/ventas", ctx -> ventas.listar());
        app.get("/api/ventas/{id}", ctx -> ventas.buscar(ctx.paramInt("id")));
        app.post("/api/ventas", ctx -> {
            Venta creada = ventas.crear(MapeadorEntrada.aVenta(ctx.bodyMap()));
            try {
                guardarEnDisco(excel());
            } catch (Exception ignore) {
                // El Excel en disco es opcional; la venta ya quedó registrada.
            }
            return creada;
        });
        app.delete("/api/ventas/{id}", ctx -> { ventas.eliminar(ctx.paramInt("id")); return Json.map("ok", true); });
    }

    private byte[] excel() {
        return ExcelExporter.generar("Ventas", ENCABEZADOS,
                ventaDao.reporte().stream().map(VentaReporteDTO::comoFila).collect(Collectors.toList()));
    }

    private static void guardarEnDisco(byte[] datos) {
        try {
            Path dir = Paths.get("reportes");
            Files.createDirectories(dir);
            Files.write(dir.resolve("ventas.xlsx"), datos);
        } catch (Exception e) {
            LOG.warn("No se pudo escribir reportes/ventas.xlsx: {}", e.getMessage());
        }
    }
}
