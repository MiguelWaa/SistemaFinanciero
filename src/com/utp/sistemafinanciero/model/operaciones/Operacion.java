package com.utp.sistemafinanciero.model.operaciones;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa la tabla operaciones del
 * Sistema Integral de Gestion de Servicios Financieros.
 *
 * Proyecto: Curso Integrador I Sistemas - Software (UTP)
 */
public class Operacion {

    // Identificador único de la operación (PK) -> columna: id_operacion
    private Integer idOperacion;

    // Cliente que realiza la operación (FK) -> columna: id_cliente
    private Integer idCliente;

    // Tipo de cambio usado (FK) -> columna: id_tipo_cambio
    private Integer idTipoCambio;

    // Tipo de operación (CAMBIO, PRESTAMO, EMPENO) -> columna: tipo_operacion
    private String tipoOperacion;

    // Moneda de origen -> columna: moneda_origen
    private String monedaOrigen;

    // Moneda de destino -> columna: moneda_destino
    private String monedaDestino;

    // Monto de la operación -> columna: monto
    private BigDecimal monto;

    // Tipo de cambio aplicado -> columna: tipo_cambio
    private BigDecimal tipoCambio;

    // Total a entregar al cliente -> columna: total_entregar
    private BigDecimal totalEntregar;

    // Total a recibir del cliente -> columna: total_recibir
    private BigDecimal totalRecibir;

    // Estado de la operación -> columna: estado
    private String estado;

    // Fecha y hora de la operación -> columna: fecha_operacion
    private LocalDateTime fechaOperacion;

    public Operacion() {
    }

    public Operacion(Integer idOperacion, Integer idCliente, Integer idTipoCambio, String tipoOperacion, String monedaOrigen, String monedaDestino, BigDecimal monto, BigDecimal tipoCambio, BigDecimal totalEntregar, BigDecimal totalRecibir, String estado, LocalDateTime fechaOperacion) {
        this.idOperacion = idOperacion;
        this.idCliente = idCliente;
        this.idTipoCambio = idTipoCambio;
        this.tipoOperacion = tipoOperacion;
        this.monedaOrigen = monedaOrigen;
        this.monedaDestino = monedaDestino;
        this.monto = monto;
        this.tipoCambio = tipoCambio;
        this.totalEntregar = totalEntregar;
        this.totalRecibir = totalRecibir;
        this.estado = estado;
        this.fechaOperacion = fechaOperacion;
    }

    public Integer getIdOperacion() {
        return idOperacion;
    }

    public void setIdOperacion(Integer idOperacion) {
        this.idOperacion = idOperacion;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public Integer getIdTipoCambio() {
        return idTipoCambio;
    }

    public void setIdTipoCambio(Integer idTipoCambio) {
        this.idTipoCambio = idTipoCambio;
    }

    public String getTipoOperacion() {
        return tipoOperacion;
    }

    public void setTipoOperacion(String tipoOperacion) {
        this.tipoOperacion = tipoOperacion;
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

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public BigDecimal getTipoCambio() {
        return tipoCambio;
    }

    public void setTipoCambio(BigDecimal tipoCambio) {
        this.tipoCambio = tipoCambio;
    }

    public BigDecimal getTotalEntregar() {
        return totalEntregar;
    }

    public void setTotalEntregar(BigDecimal totalEntregar) {
        this.totalEntregar = totalEntregar;
    }

    public BigDecimal getTotalRecibir() {
        return totalRecibir;
    }

    public void setTotalRecibir(BigDecimal totalRecibir) {
        this.totalRecibir = totalRecibir;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaOperacion() {
        return fechaOperacion;
    }

    public void setFechaOperacion(LocalDateTime fechaOperacion) {
        this.fechaOperacion = fechaOperacion;
    }

    @Override
    public String toString() {
        return "Operacion{" + "idOperacion=" + idOperacion + ", " + "idCliente=" + idCliente + ", " + "idTipoCambio=" + idTipoCambio + ", " + "tipoOperacion=" + tipoOperacion + ", " + "monedaOrigen=" + monedaOrigen + ", " + "monedaDestino=" + monedaDestino + ", " + "monto=" + monto + ", " + "tipoCambio=" + tipoCambio + ", " + "totalEntregar=" + totalEntregar + ", " + "totalRecibir=" + totalRecibir + ", " + "estado=" + estado + ", " + "fechaOperacion=" + fechaOperacion + "}";
    }
}
