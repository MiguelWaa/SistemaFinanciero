package com.utp.sistemafinanciero.model.operaciones;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad que representa la tabla tipos_cambio del
 * Sistema Integral de Gestion de Servicios Financieros.
 *
 * Proyecto: Curso Integrador I Sistemas - Software (UTP)
 */
public class TipoCambio {

    // Identificador único del tipo de cambio (PK) -> columna: id_tipo_cambio
    private Integer idTipoCambio;

    // Moneda de origen -> columna: moneda_origen
    private String monedaOrigen;

    // Moneda de destino -> columna: moneda_destino
    private String monedaDestino;

    // Precio de compra -> columna: compra
    private BigDecimal compra;

    // Precio de venta -> columna: venta
    private BigDecimal venta;

    // Fecha desde la cual rige -> columna: fecha_inicio
    private LocalDate fechaInicio;

    // Estado del tipo de cambio -> columna: estado
    private String estado;

    public TipoCambio() {
    }

    public TipoCambio(Integer idTipoCambio, String monedaOrigen, String monedaDestino, BigDecimal compra, BigDecimal venta, LocalDate fechaInicio, String estado) {
        this.idTipoCambio = idTipoCambio;
        this.monedaOrigen = monedaOrigen;
        this.monedaDestino = monedaDestino;
        this.compra = compra;
        this.venta = venta;
        this.fechaInicio = fechaInicio;
        this.estado = estado;
    }

    public Integer getIdTipoCambio() {
        return idTipoCambio;
    }

    public void setIdTipoCambio(Integer idTipoCambio) {
        this.idTipoCambio = idTipoCambio;
    }

    public String getMonedaOrigen() {
        return monedaOrigen;
    }

    public void setMonedaOrigen(String monedaOrigen) {
        this.monedaOrigen = monedaOrigen;
    }

    public String getMonedaDestino() {
        return monedaDestino;
    }

    public void setMonedaDestino(String monedaDestino) {
        this.monedaDestino = monedaDestino;
    }

    public BigDecimal getCompra() {
        return compra;
    }

    public void setCompra(BigDecimal compra) {
        this.compra = compra;
    }

    public BigDecimal getVenta() {
        return venta;
    }

    public void setVenta(BigDecimal venta) {
        this.venta = venta;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "TipoCambio{" + "idTipoCambio=" + idTipoCambio + ", " + "monedaOrigen=" + monedaOrigen + ", " + "monedaDestino=" + monedaDestino + ", " + "compra=" + compra + ", " + "venta=" + venta + ", " + "fechaInicio=" + fechaInicio + ", " + "estado=" + estado + "}";
    }
}
