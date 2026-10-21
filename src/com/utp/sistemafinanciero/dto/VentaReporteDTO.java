package com.utp.sistemafinanciero.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de una fila del reporte de ventas (Excel): muestra el nombre del
 * cliente y del servicio en lugar de sus identificadores.
 */
public class VentaReporteDTO {

    private final Integer idVenta;
    private final String cliente;
    private final String servicio;
    private final LocalDateTime fecha;
    private final BigDecimal importe;
    private final String estado;

    public VentaReporteDTO(Integer idVenta, String cliente, String servicio,
                           LocalDateTime fecha, BigDecimal importe, String estado) {
        this.idVenta = idVenta;
        this.cliente = cliente;
        this.servicio = servicio;
        this.fecha = fecha;
        this.importe = importe;
        this.estado = estado;
    }

    /** Fila de texto para el {@code ExcelExporter}. */
    public String[] comoFila() {
        return new String[]{
                String.valueOf(idVenta),
                cliente == null ? "" : cliente,
                servicio == null ? "" : servicio,
                fecha == null ? "" : fecha.toString().replace("T", " ").substring(0, Math.min(16, fecha.toString().length())),
                importe == null ? "0" : importe.toPlainString(),
                estado == null ? "" : estado};
    }

    public Integer getIdVenta() { return idVenta; }
    public String getCliente() { return cliente; }
    public String getServicio() { return servicio; }
    public LocalDateTime getFecha() { return fecha; }
    public BigDecimal getImporte() { return importe; }
    public String getEstado() { return estado; }
}
