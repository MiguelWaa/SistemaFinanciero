package com.utp.sistemafinanciero.model.marketing;

import java.time.LocalDateTime;

/**
 * Entidad que representa la tabla prospectos del
 * Sistema Integral de Gestion de Servicios Financieros.
 *
 * Proyecto: Curso Integrador I Sistemas - Software (UTP)
 */
public class Prospecto {

    // Identificador único del prospecto (PK) -> columna: id_prospecto
    private Integer idProspecto;

    // Nombres del prospecto -> columna: nombres
    private String nombres;

    // Apellidos del prospecto -> columna: apellidos
    private String apellidos;

    // Número de documento -> columna: documento
    private String documento;

    // Número de teléfono -> columna: telefono
    private String telefono;

    // Correo electrónico -> columna: correo
    private String correo;

    // Canal de origen del prospecto -> columna: origen
    private String origen;

    // Estado del prospecto -> columna: estado
    private String estado;

    // Fecha y hora de registro -> columna: fecha_registro
    private LocalDateTime fechaRegistro;

    public Prospecto() {
    }

    public Prospecto(Integer idProspecto, String nombres, String apellidos, String documento, String telefono, String correo, String origen, String estado, LocalDateTime fechaRegistro) {
        this.idProspecto = idProspecto;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.documento = documento;
        this.telefono = telefono;
        this.correo = correo;
        this.origen = origen;
        this.estado = estado;
        this.fechaRegistro = fechaRegistro;
    }

    public Integer getIdProspecto() {
        return idProspecto;
    }

    public void setIdProspecto(Integer idProspecto) {
        this.idProspecto = idProspecto;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
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

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    @Override
    public String toString() {
        return "Prospecto{" + "idProspecto=" + idProspecto + ", " + "nombres=" + nombres + ", " + "apellidos=" + apellidos + ", " + "documento=" + documento + ", " + "telefono=" + telefono + ", " + "correo=" + correo + ", " + "origen=" + origen + ", " + "estado=" + estado + ", " + "fechaRegistro=" + fechaRegistro + "}";
    }
}
