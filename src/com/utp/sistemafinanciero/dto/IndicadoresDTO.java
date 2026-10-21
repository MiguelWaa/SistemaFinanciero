package com.utp.sistemafinanciero.dto;

import java.math.BigDecimal;

/**
 * DTO con los indicadores (KPI) del panel principal y de Dirección.
 * Cada valor se calcula por separado en {@code DashboardService}
 * (tolerancia a fallos, RNF-GEN-07).
 */
public class IndicadoresDTO {

    private BigDecimal ingresosDelMes = BigDecimal.ZERO;
    private int clientesActivos;
    private int clientesTotales;
    private int operacionesHoy;
    private BigDecimal montoOperacionesMes = BigDecimal.ZERO;
    private BigDecimal prestamosColocados = BigDecimal.ZERO;
    private int productosStockBajo;
    private int expedientesPendientes;
    private int usuariosTotales;
    private int solicitudesPendientes;
    private int ventasTotales;

    public BigDecimal getIngresosDelMes() { return ingresosDelMes; }
    public void setIngresosDelMes(BigDecimal v) { this.ingresosDelMes = v; }
    public int getClientesActivos() { return clientesActivos; }
    public void setClientesActivos(int v) { this.clientesActivos = v; }
    public int getClientesTotales() { return clientesTotales; }
    public void setClientesTotales(int v) { this.clientesTotales = v; }
    public int getOperacionesHoy() { return operacionesHoy; }
    public void setOperacionesHoy(int v) { this.operacionesHoy = v; }
    public BigDecimal getMontoOperacionesMes() { return montoOperacionesMes; }
    public void setMontoOperacionesMes(BigDecimal v) { this.montoOperacionesMes = v; }
    public BigDecimal getPrestamosColocados() { return prestamosColocados; }
    public void setPrestamosColocados(BigDecimal v) { this.prestamosColocados = v; }
    public int getProductosStockBajo() { return productosStockBajo; }
    public void setProductosStockBajo(int v) { this.productosStockBajo = v; }
    public int getExpedientesPendientes() { return expedientesPendientes; }
    public void setExpedientesPendientes(int v) { this.expedientesPendientes = v; }
    public int getUsuariosTotales() { return usuariosTotales; }
    public void setUsuariosTotales(int v) { this.usuariosTotales = v; }
    public int getSolicitudesPendientes() { return solicitudesPendientes; }
    public void setSolicitudesPendientes(int v) { this.solicitudesPendientes = v; }
    public int getVentasTotales() { return ventasTotales; }
    public void setVentasTotales(int v) { this.ventasTotales = v; }
}
