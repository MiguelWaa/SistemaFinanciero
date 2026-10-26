package com.utp.sistemafinanciero.model.produccion;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad que representa la tabla orden_produccion del
 * Sistema Integral de Gestion de Servicios Financieros.
 */
public class OrdenProduccion {

    // Identificador único de la orden (PK) -> columna: id_orden
    private Integer idOrden;

    // Producto a fabricar (FK) -> columna: id_producto
    private Integer idProducto;

    // Línea de producción asignada (FK) -> columna: id_linea
    private Integer idLinea;

    // Cantidad a producir -> columna: cantidad
    private Integer cantidad;

    // Fecha de inicio -> columna: fecha_inicio
    private LocalDate fechaInicio;

    // Fecha estimada de finalización -> columna: fecha_fin_estimada
    private LocalDate fechaFinEstimada;

    // Fecha real de finalización -> columna: fecha_fin_real
    private LocalDate fechaFinReal;

    // Porcentaje de avance -> columna: avance
    private BigDecimal avance;

    // Estado de la orden -> columna: estado
    private String estado;

    // Observaciones adicionales -> columna: observaciones
    private String observaciones;

    public OrdenProduccion() {
    }

    public OrdenProduccion(Integer idOrden, Integer idProducto, Integer idLinea, Integer cantidad, LocalDate fechaInicio, LocalDate fechaFinEstimada, LocalDate fechaFinReal, BigDecimal avance, String estado, String observaciones) {
        this.idOrden = idOrden;
        this.idProducto = idProducto;
        this.idLinea = idLinea;
        this.cantidad = cantidad;
        this.fechaInicio = fechaInicio;
        this.fechaFinEstimada = fechaFinEstimada;
        this.fechaFinReal = fechaFinReal;
        this.avance = avance;
        this.estado = estado;
        this.observaciones = observaciones;
    }

    public Integer getIdOrden() {
        return idOrden;
    }

    public void setIdOrden(Integer idOrden) {
        this.idOrden = idOrden;
    }

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public Integer getIdLinea() {
        return idLinea;
    }

    public void setIdLinea(Integer idLinea) {
        this.idLinea = idLinea;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFinEstimada() {
        return fechaFinEstimada;
    }

    public void setFechaFinEstimada(LocalDate fechaFinEstimada) {
        this.fechaFinEstimada = fechaFinEstimada;
    }

    public LocalDate getFechaFinReal() {
        return fechaFinReal;
    }

    public void setFechaFinReal(LocalDate fechaFinReal) {
        this.fechaFinReal = fechaFinReal;
    }

    public BigDecimal getAvance() {
        return avance;
    }

    public void setAvance(BigDecimal avance) {
        this.avance = avance;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    @Override
    public String toString() {
        return "OrdenProduccion{" + "idOrden=" + idOrden + ", " + "idProducto=" + idProducto + ", " + "idLinea=" + idLinea + ", " + "cantidad=" + cantidad + ", " + "fechaInicio=" + fechaInicio + ", " + "fechaFinEstimada=" + fechaFinEstimada + ", " + "fechaFinReal=" + fechaFinReal + ", " + "avance=" + avance + ", " + "estado=" + estado + ", " + "observaciones=" + observaciones + "}";
    }
}
