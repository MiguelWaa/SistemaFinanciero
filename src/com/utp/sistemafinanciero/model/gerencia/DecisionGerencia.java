package com.utp.sistemafinanciero.model.gerencia;

import java.time.LocalDateTime;

/**
 * Entidad que representa la tabla decisiones_gerencia del
 * Sistema Integral de Gestion de Servicios Financieros.
 */
public class DecisionGerencia {

    // Identificador único de la decisión (PK) -> columna: id_decision_gerencia
    private Integer idDecisionGerencia;

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

    public DecisionGerencia() {
    }

    public DecisionGerencia(Integer idDecisionGerencia, Integer idExpediente, String tipoDecision, String descripcion, LocalDateTime fechaDecision, String estado) {
        this.idDecisionGerencia = idDecisionGerencia;
        this.idExpediente = idExpediente;
        this.tipoDecision = tipoDecision;
        this.descripcion = descripcion;
        this.fechaDecision = fechaDecision;
        this.estado = estado;
    }

    public Integer getIdDecisionGerencia() {
        return idDecisionGerencia;
    }

    public void setIdDecisionGerencia(Integer idDecisionGerencia) {
        this.idDecisionGerencia = idDecisionGerencia;
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
        return "DecisionGerencia{" + "idDecisionGerencia=" + idDecisionGerencia + ", " + "idExpediente=" + idExpediente + ", " + "tipoDecision=" + tipoDecision + ", " + "descripcion=" + descripcion + ", " + "fechaDecision=" + fechaDecision + ", " + "estado=" + estado + "}";
    }
}
