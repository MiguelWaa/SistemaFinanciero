package com.utp.sistemafinanciero.model.direccion;

import java.time.LocalDateTime;

/**
 * Entidad que representa la tabla reportes_direccion del
 * Sistema Integral de Gestion de Servicios Financieros.
 *
 * Proyecto: Curso Integrador I Sistemas - Software (UTP)
 */
public class ReporteDireccion {

    // Identificador único del reporte (PK) -> columna: id_reporte
    private Integer idReporte;

    // Tipo de reporte generado -> columna: tipo_reporte
    private String tipoReporte;

    // Módulo al que pertenece -> columna: modulo
    private String modulo;

    // Descripción del reporte -> columna: descripcion
    private String descripcion;

    // Fecha y hora de generación -> columna: fecha_generacion
    private LocalDateTime fechaGeneracion;

    public ReporteDireccion() {
    }

    public ReporteDireccion(Integer idReporte, String tipoReporte, String modulo, String descripcion, LocalDateTime fechaGeneracion) {
        this.idReporte = idReporte;
        this.tipoReporte = tipoReporte;
        this.modulo = modulo;
        this.descripcion = descripcion;
        this.fechaGeneracion = fechaGeneracion;
    }

    public Integer getIdReporte() {
        return idReporte;
    }

    public void setIdReporte(Integer idReporte) {
        this.idReporte = idReporte;
    }

    public String getTipoReporte() {
        return tipoReporte;
    }

    public void setTipoReporte(String tipoReporte) {
        this.tipoReporte = tipoReporte;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    @Override
    public String toString() {
        return "ReporteDireccion{" + "idReporte=" + idReporte + ", " + "tipoReporte=" + tipoReporte + ", " + "modulo=" + modulo + ", " + "descripcion=" + descripcion + ", " + "fechaGeneracion=" + fechaGeneracion + "}";
    }
}
