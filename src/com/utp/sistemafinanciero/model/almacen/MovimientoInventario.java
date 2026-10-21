package com.utp.sistemafinanciero.model.almacen;

import java.time.LocalDateTime;

/**
 * Entidad que representa la tabla movimientos_inventario del
 * Sistema Integral de Gestion de Servicios Financieros.
 *
 * Proyecto: Curso Integrador I Sistemas - Software (UTP)
 */
public class MovimientoInventario {

    // Identificador único del movimiento (PK) -> columna: id_movimiento
    private Integer idMovimiento;

    // Producto asociado (FK) -> columna: id_producto
    private Integer idProducto;

    // Tipo de movimiento (INGRESO, SALIDA, etc.) -> columna: tipo_movimiento
    private String tipoMovimiento;

    // Cantidad involucrada -> columna: cantidad
    private Integer cantidad;

    // Motivo del movimiento -> columna: motivo
    private String motivo;

    // Fecha y hora del movimiento -> columna: fecha_movimiento
    private LocalDateTime fechaMovimiento;

    public MovimientoInventario() {
    }

    public MovimientoInventario(Integer idMovimiento, Integer idProducto, String tipoMovimiento, Integer cantidad, String motivo, LocalDateTime fechaMovimiento) {
        this.idMovimiento = idMovimiento;
        this.idProducto = idProducto;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidad = cantidad;
        this.motivo = motivo;
        this.fechaMovimiento = fechaMovimiento;
    }

    public Integer getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(Integer idMovimiento) {
        this.idMovimiento = idMovimiento;
    }

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public LocalDateTime getFechaMovimiento() {
        return fechaMovimiento;
    }

    public void setFechaMovimiento(LocalDateTime fechaMovimiento) {
        this.fechaMovimiento = fechaMovimiento;
    }

    @Override
    public String toString() {
        return "MovimientoInventario{" + "idMovimiento=" + idMovimiento + ", " + "idProducto=" + idProducto + ", " + "tipoMovimiento=" + tipoMovimiento + ", " + "cantidad=" + cantidad + ", " + "motivo=" + motivo + ", " + "fechaMovimiento=" + fechaMovimiento + "}";
    }
}
