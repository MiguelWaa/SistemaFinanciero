package com.utp.sistemafinanciero.model.marketing;

import java.time.LocalDateTime;

/**
 * Entidad que representa la tabla actividades_marketing del
 * Sistema Integral de Gestion de Servicios Financieros.
 *
 * Proyecto: Curso Integrador I Sistemas - Software (UTP)
 */
public class ActividadMarketing {

    // Identificador único de la actividad (PK) -> columna: id_actividad
    private Integer idActividad;

    // Prospecto relacionado (FK) -> columna: id_prospecto
    private Integer idProspecto;

    // Campaña relacionada (FK) -> columna: id_campania
    private Integer idCampania;

    // Tipo de actividad realizada -> columna: tipo_actividad
    private String tipoActividad;

    // Descripción de la actividad -> columna: descripcion
    private String descripcion;

    // Fecha y hora de la actividad -> columna: fecha_actividad
    private LocalDateTime fechaActividad;

    public ActividadMarketing() {
    }

    public ActividadMarketing(Integer idActividad, Integer idProspecto, Integer idCampania, String tipoActividad, String descripcion, LocalDateTime fechaActividad) {
        this.idActividad = idActividad;
        this.idProspecto = idProspecto;
        this.idCampania = idCampania;
        this.tipoActividad = tipoActividad;
        this.descripcion = descripcion;
        this.fechaActividad = fechaActividad;
    }

    public Integer getIdActividad() {
        return idActividad;
    }

    public void setIdActividad(Integer idActividad) {
        this.idActividad = idActividad;
    }

    public Integer getIdProspecto() {
        return idProspecto;
    }

    public void setIdProspecto(Integer idProspecto) {
        this.idProspecto = idProspecto;
    }

    public Integer getIdCampania() {
        return idCampania;
    }

    public void setIdCampania(Integer idCampania) {
        this.idCampania = idCampania;
    }

    public String getTipoActividad() {
        return tipoActividad;
    }

    public void setTipoActividad(String tipoActividad) {
        this.tipoActividad = tipoActividad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFechaActividad() {
        return fechaActividad;
    }

    public void setFechaActividad(LocalDateTime fechaActividad) {
        this.fechaActividad = fechaActividad;
    }

    @Override
    public String toString() {
        return "ActividadMarketing{" + "idActividad=" + idActividad + ", " + "idProspecto=" + idProspecto + ", " + "idCampania=" + idCampania + ", " + "tipoActividad=" + tipoActividad + ", " + "descripcion=" + descripcion + ", " + "fechaActividad=" + fechaActividad + "}";
    }
}
