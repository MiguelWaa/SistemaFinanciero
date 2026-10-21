package com.utp.sistemafinanciero.model.operaciones;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad que representa la tabla prestamos del
 * Sistema Integral de Gestion de Servicios Financieros.
 *
 * Proyecto: Curso Integrador I Sistemas - Software (UTP)
 */
public class Prestamo {

    // Identificador único del préstamo (PK) -> columna: id_prestamo
    private Integer idPrestamo;

    // Operación asociada (FK) -> columna: id_operacion
    private Integer idOperacion;

    // Monto otorgado -> columna: monto_prestamo
    private BigDecimal montoPrestamo;

    // Tasa de interés aplicada -> columna: tasa_interes
    private BigDecimal tasaInteres;

    // Plazo en meses -> columna: plazo_meses
    private Integer plazoMeses;

    // Cuota mensual a pagar -> columna: cuota_mensual
    private BigDecimal cuotaMensual;

    // Fecha de inicio -> columna: fecha_inicio
    private LocalDate fechaInicio;

    // Fecha de vencimiento -> columna: fecha_vencimiento
    private LocalDate fechaVencimiento;

    // Saldo pendiente de pago -> columna: saldo_pendiente
    private BigDecimal saldoPendiente;

    // Estado del préstamo -> columna: estado
    private String estado;

    public Prestamo() {
    }

    public Prestamo(Integer idPrestamo, Integer idOperacion, BigDecimal montoPrestamo, BigDecimal tasaInteres, Integer plazoMeses, BigDecimal cuotaMensual, LocalDate fechaInicio, LocalDate fechaVencimiento, BigDecimal saldoPendiente, String estado) {
        this.idPrestamo = idPrestamo;
        this.idOperacion = idOperacion;
        this.montoPrestamo = montoPrestamo;
        this.tasaInteres = tasaInteres;
        this.plazoMeses = plazoMeses;
        this.cuotaMensual = cuotaMensual;
        this.fechaInicio = fechaInicio;
        this.fechaVencimiento = fechaVencimiento;
        this.saldoPendiente = saldoPendiente;
        this.estado = estado;
    }

    public Integer getIdPrestamo() {
        return idPrestamo;
    }

    public void setIdPrestamo(Integer idPrestamo) {
        this.idPrestamo = idPrestamo;
    }

    public Integer getIdOperacion() {
        return idOperacion;
    }

    public void setIdOperacion(Integer idOperacion) {
        this.idOperacion = idOperacion;
    }

    public BigDecimal getMontoPrestamo() {
        return montoPrestamo;
    }

    public void setMontoPrestamo(BigDecimal montoPrestamo) {
        this.montoPrestamo = montoPrestamo;
    }

    public BigDecimal getTasaInteres() {
        return tasaInteres;
    }

    public void setTasaInteres(BigDecimal tasaInteres) {
        this.tasaInteres = tasaInteres;
    }

    public Integer getPlazoMeses() {
        return plazoMeses;
    }

    public void setPlazoMeses(Integer plazoMeses) {
        this.plazoMeses = plazoMeses;
    }

    public BigDecimal getCuotaMensual() {
        return cuotaMensual;
    }

    public void setCuotaMensual(BigDecimal cuotaMensual) {
        this.cuotaMensual = cuotaMensual;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public BigDecimal getSaldoPendiente() {
        return saldoPendiente;
    }

    public void setSaldoPendiente(BigDecimal saldoPendiente) {
        this.saldoPendiente = saldoPendiente;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Prestamo{" + "idPrestamo=" + idPrestamo + ", " + "idOperacion=" + idOperacion + ", " + "montoPrestamo=" + montoPrestamo + ", " + "tasaInteres=" + tasaInteres + ", " + "plazoMeses=" + plazoMeses + ", " + "cuotaMensual=" + cuotaMensual + ", " + "fechaInicio=" + fechaInicio + ", " + "fechaVencimiento=" + fechaVencimiento + ", " + "saldoPendiente=" + saldoPendiente + ", " + "estado=" + estado + "}";
    }
}
