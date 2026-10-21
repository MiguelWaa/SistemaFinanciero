package com.utp.sistemafinanciero.dto;

/**
 * DTO de salida del inicio de sesión del personal. Incluye el token de
 * sesión que el navegador envía luego en la cabecera
 * {@code Authorization: Bearer <token>}.
 */
public class SesionDTO {

    private final String token;
    private final int expiraEnMinutos;
    private final Integer idUsuario;
    private final String nombre;
    private final String apellido;
    private final String email;
    private final Integer idRol;
    private final String rol;
    private final String estado;

    public SesionDTO(String token, int expiraEnMinutos, UsuarioDTO u, String rol) {
        this.token = token;
        this.expiraEnMinutos = expiraEnMinutos;
        this.idUsuario = u.getIdUsuario();
        this.nombre = u.getNombre();
        this.apellido = u.getApellido();
        this.email = u.getEmail();
        this.idRol = u.getIdRol();
        this.rol = rol;
        this.estado = u.getEstado();
    }

    public String getToken() { return token; }
    public int getExpiraEnMinutos() { return expiraEnMinutos; }
    public Integer getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getEmail() { return email; }
    public Integer getIdRol() { return idRol; }
    public String getRol() { return rol; }
    public String getEstado() { return estado; }
}
