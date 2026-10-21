package com.utp.sistemafinanciero.model.seguridad;

import java.time.LocalDateTime;

/**
 * Entidad que representa la tabla usuarios del
 * Sistema Integral de Gestion de Servicios Financieros.
 *
 * Proyecto: Curso Integrador I Sistemas - Software (UTP)
 */
public class Usuario {

    // Identificador único del usuario (PK) -> columna: id_usuario
    private Integer idUsuario;

    // Nombre del usuario -> columna: nombre
    private String nombre;

    // Apellido del usuario -> columna: apellido
    private String apellido;

    // Correo electrónico único del usuario -> columna: email
    private String email;

    // Contraseña encriptada del usuario -> columna: password
    private String password;

    // Rol asignado al usuario (FK) -> columna: id_rol
    private Integer idRol;

    // Estado del usuario (ACTIVO/INACTIVO) -> columna: estado
    private String estado;

    // Fecha y hora de registro -> columna: fecha_registro
    private LocalDateTime fechaRegistro;

    public Usuario() {
    }

    public Usuario(Integer idUsuario, String nombre, String apellido, String email, String password, Integer idRol, String estado, LocalDateTime fechaRegistro) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.password = password;
        this.idRol = idRol;
        this.estado = estado;
        this.fechaRegistro = fechaRegistro;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
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
        return "Usuario{" + "idUsuario=" + idUsuario + ", " + "nombre=" + nombre + ", " + "apellido=" + apellido + ", " + "email=" + email + ", " + "password=" + password + ", " + "idRol=" + idRol + ", " + "estado=" + estado + ", " + "fechaRegistro=" + fechaRegistro + "}";
    }
}
