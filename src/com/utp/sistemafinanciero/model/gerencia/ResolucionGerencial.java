package com.utp.sistemafinanciero.model.gerencia;

import java.time.LocalDate;

/**
 * Entidad que representa la tabla resoluciones_gerenciales del
 * Sistema Integral de Gestion de Servicios Financieros.
 *
 * Proyecto: Curso Integrador I Sistemas - Software (UTP)
 */
public class ResolucionGerencial {

    // Identificador único de la resolución (PK) -> columna: id_resolucion
    private Integer idResolucion;

    // Decisión gerencial que origina la resolución (FK) -> columna: id_decision_gerencia
    private Integer idDecisionGerencia;

    // Número único de la resolución -> columna: numero_resolucion
    private String numeroResolucion;

    // Título de la resolución -> columna: titulo
    private String titulo;

    // Contenido completo de la resolución -> columna: contenido
    private String contenido;

    // Fecha de emisión -> columna: fecha_emision
    private LocalDate fechaEmision;

    // Estado de la resolución -> columna: estado
    private String estado;

    public ResolucionGerencial() {
    }

    public ResolucionGerencial(Integer idResolucion, Integer idDecisionGerencia, String numeroResolucion, String titulo, String contenido, LocalDate fechaEmision, String estado) {
        this.idResolucion = idResolucion;
        this.idDecisionGerencia = idDecisionGerencia;
        this.numeroResolucion = numeroResolucion;
        this.titulo = titulo;
        this.contenido = contenido;
        this.fechaEmision = fechaEmision;
        this.estado = estado;
    }

    public Integer getIdResolucion() {
        return idResolucion;
    }

    public void setIdResolucion(Integer idResolucion) {
        this.idResolucion = idResolucion;
    }

    public Integer getIdDecisionGerencia() {
        return idDecisionGerencia;
    }

    public void setIdDecisionGerencia(Integer idDecisionGerencia) {
        this.idDecisionGerencia = idDecisionGerencia;
    }

    public String getNumeroResolucion() {
        return numeroResolucion;
    }

    public void setNumeroResolucion(String numeroResolucion) {
        this.numeroResolucion = numeroResolucion;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDate fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "ResolucionGerencial{" + "idResolucion=" + idResolucion + ", " + "idDecisionGerencia=" + idDecisionGerencia + ", " + "numeroResolucion=" + numeroResolucion + ", " + "titulo=" + titulo + ", " + "contenido=" + contenido + ", " + "fechaEmision=" + fechaEmision + ", " + "estado=" + estado + "}";
    }
}
