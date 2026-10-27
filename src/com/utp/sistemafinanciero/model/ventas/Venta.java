package com.utp.sistemafinanciero.model.ventas;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa la tabla ventas del
 * Sistema Integral de Gestion de Servicios Financieros.
 */
public class Venta {

    // Identificador único de la venta (PK) -> columna: id_venta
    private Integer idVenta;

    // Cliente que realiza la compra (FK) -> columna: id_cliente
    private Integer idCliente;

    // Servicio financiero vendido (FK) -> columna: id_servicio
    private Integer idServicio;

    // Fecha y hora de la venta -> columna: fecha_venta
    private LocalDateTime fechaVenta;

    // Monto de la venta -> columna: monto
    private BigDecimal monto;

    // Importe total de la venta -> columna: importe
    private BigDecimal importe;

    // Estado de la venta -> columna: estado
    private String estado;

    public Venta() {
    }

    public Venta(Integer idVenta, Integer idCliente, Integer idServicio, LocalDateTime fechaVenta, BigDecimal monto, BigDecimal importe, String estado) {
        this.idVenta = idVenta;
        this.idCliente = idCliente;
        this.idServicio = idServicio;
        this.fechaVenta = fechaVenta;
        this.monto = monto;
        this.importe = importe;
        this.estado = estado;
    }

    public Integer getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(Integer idVenta) {
        this.idVenta = idVenta;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public Integer getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(Integer idServicio) {
        this.idServicio = idServicio;
    }

    public LocalDateTime getFechaVenta() {
        return fechaVenta;
    }

    public void setFechaVenta(LocalDateTime fechaVenta) {
        this.fechaVenta = fechaVenta;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Venta{" + "idVenta=" + idVenta + ", " + "idCliente=" + idCliente + ", " + "idServicio=" + idServicio + ", " + "fechaVenta=" + fechaVenta + ", " + "monto=" + monto + ", " + "importe=" + importe + ", " + "estado=" + estado + "}";
    }
}
