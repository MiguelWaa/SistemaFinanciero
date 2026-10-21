package com.utp.sistemafinanciero.dto;

import com.utp.sistemafinanciero.util.Json;

import java.util.Map;

/**
 * DTO de entrada para el inicio de sesión. Sirve tanto para el personal
 * (correo + contraseña) como para el Portal del Cliente (documento +
 * contraseña): el campo {@code usuario} guarda el identificador usado.
 */
public class LoginDTO {

    private final String usuario;
    private final String password;

    public LoginDTO(String usuario, String password) {
        this.usuario = usuario == null ? null : usuario.trim();
        this.password = password;
    }

    /** Construye el DTO a partir del cuerpo JSON {@code {"email":..,"password":..}}. */
    public static LoginDTO personal(Map<String, Object> cuerpo) {
        return new LoginDTO(Json.str(cuerpo, "email"), Json.str(cuerpo, "password"));
    }

    /** Construye el DTO a partir del cuerpo JSON {@code {"documento":..,"password":..}}. */
    public static LoginDTO cliente(Map<String, Object> cuerpo) {
        return new LoginDTO(Json.str(cuerpo, "documento"), Json.str(cuerpo, "password"));
    }

    public boolean incompleto() {
        return usuario == null || usuario.isBlank() || password == null || password.isBlank();
    }

    public String getUsuario() { return usuario; }

    /** La contraseña no tiene getter "público" para que nunca se serialice. */
    public String password() { return password; }
}
