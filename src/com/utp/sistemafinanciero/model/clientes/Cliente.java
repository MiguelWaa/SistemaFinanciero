package com.utp.sistemafinanciero.model.clientes;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa la tabla clientes del
 * Sistema Integral de Gestion de Servicios Financieros.
 *
 * Proyecto: Curso Integrador I Sistemas - Software (UTP)
 */
public class Cliente {

    // Identificador único del cliente (PK) -> columna: id_cliente
    private Integer idCliente;

    // Tipo de documento de identidad -> columna: tipo_documento
    private String tipoDocumento;

    // Número de documento único -> columna: documento
    private String documento;

    // Nombres del cliente -> columna: nombres
    private String nombres;

    // Apellidos del cliente -> columna: apellidos
    private String apellidos;

    // Fecha de nacimiento -> columna: fecha_nacimiento
    private LocalDate fechaNacimiento;

    // Número de teléfono -> columna: telefono
    private String telefono;

    // Correo electrónico -> columna: correo
    private String correo;

    // Dirección del cliente -> columna: direccion
    private String direccion;

    // Estado del cliente -> columna: estado
    private String estado;

    // Fecha y hora de registro -> columna: fecha_registro
    private LocalDateTime fechaRegistro;

    public Cliente() {
    }

    public Cliente(Integer idCliente, String tipoDocumento, String documento, String nombres, String apellidos, LocalDate fechaNacimiento, String telefono, String correo, String direccion, String estado, LocalDateTime fechaRegistro) {
        this.idCliente = idCliente;
        this.tipoDocumento = tipoDocumento;
        this.documento = documento;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.fechaNacimiento = fechaNacimiento;
        this.telefono = telefono;
        this.correo = correo;
        this.direccion = direccion;
        this.estado = estado;
        this.fechaRegistro = fechaRegistro;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
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

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
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

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    @Override
    public String toString() {
        return "Cliente{" + "idCliente=" + idCliente + ", " + "tipoDocumento=" + tipoDocumento + ", " + "documento=" + documento + ", " + "nombres=" + nombres + ", " + "apellidos=" + apellidos + ", " + "fechaNacimiento=" + fechaNacimiento + ", " + "telefono=" + telefono + ", " + "correo=" + correo + ", " + "direccion=" + direccion + ", " + "estado=" + estado + ", " + "fechaRegistro=" + fechaRegistro + "}";
    }
}
