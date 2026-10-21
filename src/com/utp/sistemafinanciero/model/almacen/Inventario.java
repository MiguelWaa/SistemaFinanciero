package com.utp.sistemafinanciero.model.almacen;

import java.time.LocalDateTime;

/**
 * Entidad que representa la tabla inventarios del
 * Sistema Integral de Gestion de Servicios Financieros.
 *
 * Proyecto: Curso Integrador I Sistemas - Software (UTP)
 */
public class Inventario {

    // Identificador único del registro (PK) -> columna: id_inventario
    private Integer idInventario;

    // Producto registrado (FK) -> columna: id_producto
    private Integer idProducto;

    // Ubicación física en almacén -> columna: ubicacion
    private String ubicacion;

    // Cantidad disponible -> columna: cantidad
    private Integer cantidad;

    // Fecha de la última actualización -> columna: fecha_actualizacion
    private LocalDateTime fechaActualizacion;

    public Inventario() {
    }

    public Inventario(Integer idInventario, Integer idProducto, String ubicacion, Integer cantidad, LocalDateTime fechaActualizacion) {
        this.idInventario = idInventario;
        this.idProducto = idProducto;
        this.ubicacion = ubicacion;
        this.cantidad = cantidad;
        this.fechaActualizacion = fechaActualizacion;
    }

    public Integer getIdInventario() {
        return idInventario;
    }

    public void setIdInventario(Integer idInventario) {
        this.idInventario = idInventario;
    }

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    @Override
    public String toString() {
        return "Inventario{" + "idInventario=" + idInventario + ", " + "idProducto=" + idProducto + ", " + "ubicacion=" + ubicacion + ", " + "cantidad=" + cantidad + ", " + "fechaActualizacion=" + fechaActualizacion + "}";
    }
}
