package com.utp.sistemafinanciero.model.direccion;

import java.time.LocalDateTime;

/**
 * Entidad que representa la tabla decisiones_direccion del
 * Sistema Integral de Gestion de Servicios Financieros.
 *
 * Proyecto: Curso Integrador I Sistemas - Software (UTP)
 */
public class DecisionDireccion {

    // Identificador único de la decisión (PK) -> columna: id_decision
    private Integer idDecision;

    // Expediente relacionado (FK) -> columna: id_expediente
    private Integer idExpediente;

    // Tipo de decisión tomada -> columna: tipo_decision
    private String tipoDecision;

    // Descripción de la decisión -> columna: descripcion
    private String descripcion;

    // Fecha y hora de la decisión -> columna: fecha_decision
    private LocalDateTime fechaDecision;

    // Estado de la decisión -> columna: estado
    private String estado;

    public DecisionDireccion() {
    }

    public DecisionDireccion(Integer idDecision, Integer idExpediente, String tipoDecision, String descripcion, LocalDateTime fechaDecision, String estado) {
        this.idDecision = idDecision;
        this.idExpediente = idExpediente;
        this.tipoDecision = tipoDecision;
        this.descripcion = descripcion;
        this.fechaDecision = fechaDecision;
        this.estado = estado;
    }

    public Integer getIdDecision() {
        return idDecision;
    }

    public void setIdDecision(Integer idDecision) {
        this.idDecision = idDecision;
    }

    public Integer getIdExpediente() {
        return idExpediente;
    }

    public void setIdExpediente(Integer idExpediente) {
        this.idExpediente = idExpediente;
    }

    public String getTipoDecision() {
        return tipoDecision;
    }

    public void setTipoDecision(String tipoDecision) {
        this.tipoDecision = tipoDecision;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFechaDecision() {
        return fechaDecision;
    }

    public void setFechaDecision(LocalDateTime fechaDecision) {
        this.fechaDecision = fechaDecision;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "DecisionDireccion{" + "idDecision=" + idDecision + ", " + "idExpediente=" + idExpediente + ", " + "tipoDecision=" + tipoDecision + ", " + "descripcion=" + descripcion + ", " + "fechaDecision=" + fechaDecision + ", " + "estado=" + estado + "}";
    }
}
