package com.utp.sistemafinanciero.model.clientes;

import java.time.LocalDate;

/**
 * Entidad que representa la tabla cliente_servicio del
 * Sistema Integral de Gestion de Servicios Financieros.
 *
 * Proyecto: Curso Integrador I Sistemas - Software (UTP)
 */
public class ClienteServicio {

    // Identificador único del registro (PK) -> columna: id_cliente_servicio
    private Integer idClienteServicio;

    // Cliente asociado (FK) -> columna: id_cliente
    private Integer idCliente;

    // Servicio contratado (FK) -> columna: id_servicio
    private Integer idServicio;

    // Fecha de inicio del servicio -> columna: fecha_inicio
    private LocalDate fechaInicio;

    // Fecha de finalización del servicio -> columna: fecha_fin
    private LocalDate fechaFin;

    // Estado del servicio contratado -> columna: estado
    private String estado;

    public ClienteServicio() {
    }

    public ClienteServicio(Integer idClienteServicio, Integer idCliente, Integer idServicio, LocalDate fechaInicio, LocalDate fechaFin, String estado) {
        this.idClienteServicio = idClienteServicio;
        this.idCliente = idCliente;
        this.idServicio = idServicio;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
    }

    public Integer getIdClienteServicio() {
        return idClienteServicio;
    }

    public void setIdClienteServicio(Integer idClienteServicio) {
        this.idClienteServicio = idClienteServicio;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public Integer getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(Integer idServicio) {
        this.idServicio = idServicio;
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "ClienteServicio{" + "idClienteServicio=" + idClienteServicio + ", " + "idCliente=" + idCliente + ", " + "idServicio=" + idServicio + ", " + "fechaInicio=" + fechaInicio + ", " + "fechaFin=" + fechaFin + ", " + "estado=" + estado + "}";
    }
}
