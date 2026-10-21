package com.utp.sistemafinanciero.model.logistica;

/**
 * Entidad que representa la tabla proveedores del
 * Sistema Integral de Gestion de Servicios Financieros.
 *
 * Proyecto: Curso Integrador I Sistemas - Software (UTP)
 */
public class Proveedor {

    // Identificador único del proveedor (PK) -> columna: id_proveedor
    private Integer idProveedor;

    // Razón social -> columna: razon_social
    private String razonSocial;

    // Número de RUC único -> columna: ruc
    private String ruc;

    // Persona de contacto -> columna: contacto
    private String contacto;

    // Número de teléfono -> columna: telefono
    private String telefono;

    // Correo electrónico -> columna: correo
    private String correo;

    // Dirección del proveedor -> columna: direccion
    private String direccion;

    // Estado del proveedor -> columna: estado
    private String estado;

    public Proveedor() {
    }

    public Proveedor(Integer idProveedor, String razonSocial, String ruc, String contacto, String telefono, String correo, String direccion, String estado) {
        this.idProveedor = idProveedor;
        this.razonSocial = razonSocial;
        this.ruc = ruc;
        this.contacto = contacto;
        this.telefono = telefono;
        this.correo = correo;
        this.direccion = direccion;
        this.estado = estado;
    }

    public Integer getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(Integer idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Proveedor{" + "idProveedor=" + idProveedor + ", " + "razonSocial=" + razonSocial + ", " + "ruc=" + ruc + ", " + "contacto=" + contacto + ", " + "telefono=" + telefono + ", " + "correo=" + correo + ", " + "direccion=" + direccion + ", " + "estado=" + estado + "}";
    }
}
