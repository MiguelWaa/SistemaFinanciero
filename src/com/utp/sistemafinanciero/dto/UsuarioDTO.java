package com.utp.sistemafinanciero.dto;

import com.utp.sistemafinanciero.model.seguridad.Usuario;

import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) de salida para un usuario del sistema.
 *
 * <p>Es la "vista pública" de la entidad {@link Usuario}: transporta solo
 * los datos que el frontend necesita y <b>nunca</b> incluye la contraseña
 * ni su hash (RNF-GEN-03). Así la capa web no expone la entidad de
 * persistencia directamente.</p>
 */
public class UsuarioDTO {

    private final Integer idUsuario;
    private final String nombre;
    private final String apellido;
    private final String email;
    private final Integer idRol;
    private final String estado;
    private final LocalDateTime fechaRegistro;

    public UsuarioDTO(Integer idUsuario, String nombre, String apellido, String email,
                      Integer idRol, String estado, LocalDateTime fechaRegistro) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.idRol = idRol;
        this.estado = estado;
        this.fechaRegistro = fechaRegistro;
    }

    /** Convierte la entidad en DTO (método fábrica). */
    public static UsuarioDTO de(Usuario u) {
        if (u == null) return null;
        return new UsuarioDTO(u.getIdUsuario(), u.getNombre(), u.getApellido(), u.getEmail(),
                u.getIdRol(), u.getEstado(), u.getFechaRegistro());
    }

    public Integer getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getEmail() { return email; }
    public Integer getIdRol() { return idRol; }
    public String getEstado() { return estado; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
}
