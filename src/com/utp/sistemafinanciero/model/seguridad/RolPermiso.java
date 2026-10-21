package com.utp.sistemafinanciero.model.seguridad;

/**
 * Entidad que representa la tabla rol_permiso del
 * Sistema Integral de Gestion de Servicios Financieros.
 *
 * Proyecto: Curso Integrador I Sistemas - Software (UTP)
 */
public class RolPermiso {

    // Referencia al rol (PK compuesta / FK) (PK) -> columna: id_rol
    private Integer idRol;

    // Referencia al permiso (PK compuesta / FK) (PK) -> columna: id_permiso
    private Integer idPermiso;

    public RolPermiso() {
    }

    public RolPermiso(Integer idRol, Integer idPermiso) {
        this.idRol = idRol;
        this.idPermiso = idPermiso;
    }

    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
    }

    public Integer getIdPermiso() {
        return idPermiso;
    }

    public void setIdPermiso(Integer idPermiso) {
        this.idPermiso = idPermiso;
    }

    @Override
    public String toString() {
        return "RolPermiso{" + "idRol=" + idRol + ", " + "idPermiso=" + idPermiso + "}";
    }
}
