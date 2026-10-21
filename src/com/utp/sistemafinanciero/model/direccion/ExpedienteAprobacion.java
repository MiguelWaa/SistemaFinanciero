package com.utp.sistemafinanciero.model.direccion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa la tabla expedientes_aprobacion del
 * Sistema Integral de Gestion de Servicios Financieros.
 *
 * Proyecto: Curso Integrador I Sistemas - Software (UTP)
 */
public class ExpedienteAprobacion {

    // Identificador único del expediente (PK) -> columna: id_expediente
    private Integer idExpediente;

    // Código identificador del expediente -> columna: codigo
    private String codigo;

    // Área solicitante -> columna: area
    private String area;

    // Nombre del solicitante -> columna: solicitante
    private String solicitante;

    // Monto involucrado -> columna: monto
    private BigDecimal monto;

    // Impacto estimado -> columna: impacto
    private String impacto;

    // Descripción del expediente -> columna: descripcion
    private String descripcion;

    // Estado del expediente -> columna: estado
    private String estado;

    // Fecha y hora de solicitud -> columna: fecha_solicitud
    private LocalDateTime fechaSolicitud;

    // Fecha y hora de resolución -> columna: fecha_resolucion
    private LocalDateTime fechaResolucion;

    public ExpedienteAprobacion() {
    }

    public ExpedienteAprobacion(Integer idExpediente, String codigo, String area, String solicitante, BigDecimal monto, String impacto, String descripcion, String estado, LocalDateTime fechaSolicitud, LocalDateTime fechaResolucion) {
        this.idExpediente = idExpediente;
        this.codigo = codigo;
        this.area = area;
        this.solicitante = solicitante;
        this.monto = monto;
        this.impacto = impacto;
        this.descripcion = descripcion;
        this.estado = estado;
        this.fechaSolicitud = fechaSolicitud;
        this.fechaResolucion = fechaResolucion;
    }

    public Integer getIdExpediente() {
        return idExpediente;
    }

    public void setIdExpediente(Integer idExpediente) {
        this.idExpediente = idExpediente;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(String solicitante) {
        this.solicitante = solicitante;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getImpacto() {
        return impacto;
    }

    public void setImpacto(String impacto) {
        this.impacto = impacto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public LocalDateTime getFechaResolucion() {
        return fechaResolucion;
    }

    public void setFechaResolucion(LocalDateTime fechaResolucion) {
        this.fechaResolucion = fechaResolucion;
    }

    @Override
    public String toString() {
        return "ExpedienteAprobacion{" + "idExpediente=" + idExpediente + ", " + "codigo=" + codigo + ", " + "area=" + area + ", " + "solicitante=" + solicitante + ", " + "monto=" + monto + ", " + "impacto=" + impacto + ", " + "descripcion=" + descripcion + ", " + "estado=" + estado + ", " + "fechaSolicitud=" + fechaSolicitud + ", " + "fechaResolucion=" + fechaResolucion + "}";
    }
}
