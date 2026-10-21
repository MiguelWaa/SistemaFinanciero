package com.utp.sistemafinanciero.model.clientes;

/**
 * Entidad que representa la tabla servicios_financieros del
 * Sistema Integral de Gestion de Servicios Financieros.
 *
 * Proyecto: Curso Integrador I Sistemas - Software (UTP)
 */
public class ServicioFinanciero {

    // Identificador único del servicio (PK) -> columna: id_servicio
    private Integer idServicio;

    // Nombre del servicio financiero -> columna: nombre
    private String nombre;

    // Tipo de servicio (CAMBIO, PRESTAMO, EMPENO) -> columna: tipo_servicio
    private String tipoServicio;

    // Descripción del servicio -> columna: descripcion
    private String descripcion;

    // Estado del servicio -> columna: estado
    private String estado;

    public ServicioFinanciero() {
    }

    public ServicioFinanciero(Integer idServicio, String nombre, String tipoServicio, String descripcion, String estado) {
        this.idServicio = idServicio;
        this.nombre = nombre;
        this.tipoServicio = tipoServicio;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    public Integer getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(Integer idServicio) {
        this.idServicio = idServicio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipoServicio() {
        return tipoServicio;
    }

    public void setTipoServicio(String tipoServicio) {
        this.tipoServicio = tipoServicio;
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

    @Override
    public String toString() {
        return "ServicioFinanciero{" + "idServicio=" + idServicio + ", " + "nombre=" + nombre + ", " + "tipoServicio=" + tipoServicio + ", " + "descripcion=" + descripcion + ", " + "estado=" + estado + "}";
    }
}
