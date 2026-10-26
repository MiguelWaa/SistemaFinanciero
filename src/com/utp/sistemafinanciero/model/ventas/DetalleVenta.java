package com.utp.sistemafinanciero.model.ventas;

import java.math.BigDecimal;

/**
 * Entidad que representa la tabla detalle_venta del
 * Sistema Integral de Gestion de Servicios Financieros.
 */
public class DetalleVenta {

    // Identificador único del detalle (PK) -> columna: id_detalle
    private Integer idDetalle;

    // Venta a la que pertenece (FK) -> columna: id_venta
    private Integer idVenta;

    // Descripción del ítem vendido -> columna: descripcion
    private String descripcion;

    // Cantidad vendida -> columna: cantidad
    private Integer cantidad;

    // Precio unitario -> columna: precio_unitario
    private BigDecimal precioUnitario;

    // Subtotal del ítem -> columna: subtotal
    private BigDecimal subtotal;

    public DetalleVenta() {
    }

    public DetalleVenta(Integer idDetalle, Integer idVenta, String descripcion, Integer cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {
        this.idDetalle = idDetalle;
        this.idVenta = idVenta;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    public Integer getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(Integer idDetalle) {
        this.idDetalle = idDetalle;
    }

    public Integer getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(Integer idVenta) {
        this.idVenta = idVenta;
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

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    @Override
    public String toString() {
        return "DetalleVenta{" + "idDetalle=" + idDetalle + ", " + "idVenta=" + idVenta + ", " + "descripcion=" + descripcion + ", " + "cantidad=" + cantidad + ", " + "precioUnitario=" + precioUnitario + ", " + "subtotal=" + subtotal + "}";
    }
}
