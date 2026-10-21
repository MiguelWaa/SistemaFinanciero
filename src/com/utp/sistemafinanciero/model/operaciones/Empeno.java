package com.utp.sistemafinanciero.model.operaciones;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad que representa la tabla empenos del
 * Sistema Integral de Gestion de Servicios Financieros.
 *
 * Proyecto: Curso Integrador I Sistemas - Software (UTP)
 */
public class Empeno {

    // Identificador único del empeño (PK) -> columna: id_empeno
    private Integer idEmpeno;

    // Operación asociada (FK) -> columna: id_operacion
    private Integer idOperacion;

    // Artículo dado en garantía (FK) -> columna: id_articulo
    private Integer idArticulo;

    // Monto otorgado por el empeño -> columna: monto_prestamo
    private BigDecimal montoPrestamo;

    // Tasa de interés aplicada -> columna: tasa_interes
    private BigDecimal tasaInteres;

    // Plazo en días -> columna: plazo_dias
    private Integer plazoDias;

    // Fecha del empeño -> columna: fecha_empeno
    private LocalDate fechaEmpeno;

    // Fecha de vencimiento -> columna: fecha_vencimiento
    private LocalDate fechaVencimiento;

    // Saldo pendiente de pago -> columna: saldo_pendiente
    private BigDecimal saldoPendiente;

    // Estado del empeño -> columna: estado
    private String estado;

    public Empeno() {
    }

    public Empeno(Integer idEmpeno, Integer idOperacion, Integer idArticulo, BigDecimal montoPrestamo, BigDecimal tasaInteres, Integer plazoDias, LocalDate fechaEmpeno, LocalDate fechaVencimiento, BigDecimal saldoPendiente, String estado) {
        this.idEmpeno = idEmpeno;
        this.idOperacion = idOperacion;
        this.idArticulo = idArticulo;
        this.montoPrestamo = montoPrestamo;
        this.tasaInteres = tasaInteres;
        this.plazoDias = plazoDias;
        this.fechaEmpeno = fechaEmpeno;
        this.fechaVencimiento = fechaVencimiento;
        this.saldoPendiente = saldoPendiente;
        this.estado = estado;
    }

    public Integer getIdEmpeno() {
        return idEmpeno;
    }

    public void setIdEmpeno(Integer idEmpeno) {
        this.idEmpeno = idEmpeno;
    }

    public Integer getIdOperacion() {
        return idOperacion;
    }

    public void setIdOperacion(Integer idOperacion) {
        this.idOperacion = idOperacion;
    }

    public Integer getIdArticulo() {
        return idArticulo;
    }

    public void setIdArticulo(Integer idArticulo) {
        this.idArticulo = idArticulo;
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

    public Integer getPlazoDias() {
        return plazoDias;
    }

    public void setPlazoDias(Integer plazoDias) {
        this.plazoDias = plazoDias;
    }

    public LocalDate getFechaEmpeno() {
        return fechaEmpeno;
    }

    public void setFechaEmpeno(LocalDate fechaEmpeno) {
        this.fechaEmpeno = fechaEmpeno;
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
        return "Empeno{" + "idEmpeno=" + idEmpeno + ", " + "idOperacion=" + idOperacion + ", " + "idArticulo=" + idArticulo + ", " + "montoPrestamo=" + montoPrestamo + ", " + "tasaInteres=" + tasaInteres + ", " + "plazoDias=" + plazoDias + ", " + "fechaEmpeno=" + fechaEmpeno + ", " + "fechaVencimiento=" + fechaVencimiento + ", " + "saldoPendiente=" + saldoPendiente + ", " + "estado=" + estado + "}";
    }
}
