package com.utp.sistemafinanciero.model.seguridad;

/**
 * Entidad que representa la tabla roles del
 * Sistema Integral de Gestion de Servicios Financieros.
 *
 * Proyecto: Curso Integrador I Sistemas - Software (UTP)
 */
public class Rol {

    // Identificador único del rol (PK) -> columna: id_rol
    private Integer idRol;

    // Nombre del rol -> columna: nombre
    private String nombre;

    // Descripción de las funciones del rol -> columna: descripcion
    private String descripcion;

    public Rol() {
    }

    public Rol(Integer idRol, String nombre, String descripcion) {
        this.idRol = idRol;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
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

    @Override
    public String toString() {
        return "Rol{" + "idRol=" + idRol + ", " + "nombre=" + nombre + ", " + "descripcion=" + descripcion + "}";
    }
}
