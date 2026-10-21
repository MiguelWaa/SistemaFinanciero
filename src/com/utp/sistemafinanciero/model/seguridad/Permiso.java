package com.utp.sistemafinanciero.model.seguridad;

/**
 * Entidad que representa la tabla permisos del
 * Sistema Integral de Gestion de Servicios Financieros.
 *
 * Proyecto: Curso Integrador I Sistemas - Software (UTP)
 */
public class Permiso {

    // Identificador único del permiso (PK) -> columna: id_permiso
    private Integer idPermiso;

    // Nombre del permiso -> columna: nombre
    private String nombre;

    // Descripción del permiso -> columna: descripcion
    private String descripcion;

    public Permiso() {
    }

    public Permiso(Integer idPermiso, String nombre, String descripcion) {
        this.idPermiso = idPermiso;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public Integer getIdPermiso() {
        return idPermiso;
    }

    public void setIdPermiso(Integer idPermiso) {
        this.idPermiso = idPermiso;
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
        return "Permiso{" + "idPermiso=" + idPermiso + ", " + "nombre=" + nombre + ", " + "descripcion=" + descripcion + "}";
    }
}
