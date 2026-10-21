package com.utp.sistemafinanciero.model.marketing;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad que representa la tabla campanias del
 * Sistema Integral de Gestion de Servicios Financieros.
 *
 * Proyecto: Curso Integrador I Sistemas - Software (UTP)
 */
public class Campania {

    // Identificador único de la campaña (PK) -> columna: id_campania
    private Integer idCampania;

    // Nombre de la campaña -> columna: nombre
    private String nombre;

    // Descripción de la campaña -> columna: descripcion
    private String descripcion;

    // Fecha de inicio -> columna: fecha_inicio
    private LocalDate fechaInicio;

    // Fecha de finalización -> columna: fecha_fin
    private LocalDate fechaFin;

    // Presupuesto asignado -> columna: presupuesto
    private BigDecimal presupuesto;

    // Canal de difusión -> columna: canal
    private String canal;

    // Estado de la campaña -> columna: estado
    private String estado;

    public Campania() {
    }

    public Campania(Integer idCampania, String nombre, String descripcion, LocalDate fechaInicio, LocalDate fechaFin, BigDecimal presupuesto, String canal, String estado) {
        this.idCampania = idCampania;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.presupuesto = presupuesto;
        this.canal = canal;
        this.estado = estado;
    }

    public Integer getIdCampania() {
        return idCampania;
    }

    public void setIdCampania(Integer idCampania) {
        this.idCampania = idCampania;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public BigDecimal getPresupuesto() {
        return presupuesto;
    }

    public void setPresupuesto(BigDecimal presupuesto) {
        this.presupuesto = presupuesto;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Campania{" + "idCampania=" + idCampania + ", " + "nombre=" + nombre + ", " + "descripcion=" + descripcion + ", " + "fechaInicio=" + fechaInicio + ", " + "fechaFin=" + fechaFin + ", " + "presupuesto=" + presupuesto + ", " + "canal=" + canal + ", " + "estado=" + estado + "}";
    }
}
