package com.utp.sistemafinanciero.model.operaciones;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa la tabla comprobantes del
 * Sistema Integral de Gestion de Servicios Financieros.
 *
 * Proyecto: Curso Integrador I Sistemas - Software (UTP)
 */
public class Comprobante {

    // Identificador único del comprobante (PK) -> columna: id_comprobante
    private Integer idComprobante;

    // Operación asociada (FK) -> columna: id_operacion
    private Integer idOperacion;

    // Número único del comprobante -> columna: numero_comprobante
    private String numeroComprobante;

    // Tipo de comprobante emitido -> columna: tipo_comprobante
    private String tipoComprobante;

    // Fecha y hora de emisión -> columna: fecha_emision
    private LocalDateTime fechaEmision;

    // Monto total del comprobante -> columna: monto_total
    private BigDecimal montoTotal;

    // Estado del comprobante -> columna: estado
    private String estado;

    public Comprobante() {
    }

    public Comprobante(Integer idComprobante, Integer idOperacion, String numeroComprobante, String tipoComprobante, LocalDateTime fechaEmision, BigDecimal montoTotal, String estado) {
        this.idComprobante = idComprobante;
        this.idOperacion = idOperacion;
        this.numeroComprobante = numeroComprobante;
        this.tipoComprobante = tipoComprobante;
        this.fechaEmision = fechaEmision;
        this.montoTotal = montoTotal;
        this.estado = estado;
    }

    public Integer getIdComprobante() {
        return idComprobante;
    }

    public void setIdComprobante(Integer idComprobante) {
        this.idComprobante = idComprobante;
    }

    public Integer getIdOperacion() {
        return idOperacion;
    }

    public void setIdOperacion(Integer idOperacion) {
        this.idOperacion = idOperacion;
    }

    public String getNumeroComprobante() {
        return numeroComprobante;
    }

    public void setNumeroComprobante(String numeroComprobante) {
        this.numeroComprobante = numeroComprobante;
    }

    public String getTipoComprobante() {
        return tipoComprobante;
    }

    public void setTipoComprobante(String tipoComprobante) {
        this.tipoComprobante = tipoComprobante;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDateTime fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Comprobante{" + "idComprobante=" + idComprobante + ", " + "idOperacion=" + idOperacion + ", " + "numeroComprobante=" + numeroComprobante + ", " + "tipoComprobante=" + tipoComprobante + ", " + "fechaEmision=" + fechaEmision + ", " + "montoTotal=" + montoTotal + ", " + "estado=" + estado + "}";
    }
}
