package com.utp.sistemafinanciero.model.logistica;

import java.time.LocalDateTime;

/**
 * Entidad que representa la tabla movimientos_logistica del
 * Sistema Integral de Gestion de Servicios Financieros.
 *
 * Proyecto: Curso Integrador I Sistemas - Software (UTP)
 */
public class MovimientoLogistica {

    // Identificador único del movimiento (PK) -> columna: id_movimiento_logistica
    private Integer idMovimientoLogistica;

    // Proveedor asociado (FK) -> columna: id_proveedor
    private Integer idProveedor;

    // Tipo de movimiento logístico -> columna: tipo_movimiento
    private String tipoMovimiento;

    // Descripción del movimiento -> columna: descripcion
    private String descripcion;

    // Cantidad involucrada -> columna: cantidad
    private Integer cantidad;

    // Fecha y hora del movimiento -> columna: fecha_movimiento
    private LocalDateTime fechaMovimiento;

    // Estado del movimiento -> columna: estado
    private String estado;

    public MovimientoLogistica() {
    }

    public MovimientoLogistica(Integer idMovimientoLogistica, Integer idProveedor, String tipoMovimiento, String descripcion, Integer cantidad, LocalDateTime fechaMovimiento, String estado) {
        this.idMovimientoLogistica = idMovimientoLogistica;
        this.idProveedor = idProveedor;
        this.tipoMovimiento = tipoMovimiento;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.fechaMovimiento = fechaMovimiento;
        this.estado = estado;
    }

    public Integer getIdMovimientoLogistica() {
        return idMovimientoLogistica;
    }

    public void setIdMovimientoLogistica(Integer idMovimientoLogistica) {
        this.idMovimientoLogistica = idMovimientoLogistica;
    }

    public Integer getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(Integer idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public LocalDateTime getFechaMovimiento() {
        return fechaMovimiento;
    }

    public void setFechaMovimiento(LocalDateTime fechaMovimiento) {
        this.fechaMovimiento = fechaMovimiento;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "MovimientoLogistica{" + "idMovimientoLogistica=" + idMovimientoLogistica + ", " + "idProveedor=" + idProveedor + ", " + "tipoMovimiento=" + tipoMovimiento + ", " + "descripcion=" + descripcion + ", " + "cantidad=" + cantidad + ", " + "fechaMovimiento=" + fechaMovimiento + ", " + "estado=" + estado + "}";
    }
}
