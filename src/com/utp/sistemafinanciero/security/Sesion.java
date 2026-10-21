package com.utp.sistemafinanciero.security;

import java.time.Instant;

/**
 * Sesión autenticada del servidor. Puede ser del <b>personal</b>
 * (usuario interno con rol ADMIN, CAJERO, ...) o de un <b>cliente</b>
 * del Portal (rol CLIENTE). El navegador solo conoce el token; los datos
 * de la sesión permanecen en el servidor y no pueden ser alterados.
 */
public class Sesion {

    public enum Tipo { PERSONAL, CLIENTE }

    private final String token;
    private final Tipo tipo;
    private final int idSujeto;
    private final String usuario;
    private final String rol;
    private final Instant creada;
    private volatile Instant expira;

    public Sesion(String token, Tipo tipo, int idSujeto, String usuario, String rol,
                  Instant creada, Instant expira) {
        this.token = token;
        this.tipo = tipo;
        this.idSujeto = idSujeto;
        this.usuario = usuario;
        this.rol = rol == null ? "" : rol.toUpperCase();
        this.creada = creada;
        this.expira = expira;
    }

    public String getToken() { return token; }
    public Tipo getTipo() { return tipo; }
    /** idUsuario (personal) o idCliente (portal). */
    public int getIdSujeto() { return idSujeto; }
    /** Correo del usuario o documento del cliente. */
    public String getUsuario() { return usuario; }
    public String getRol() { return rol; }
    public Instant getCreada() { return creada; }
    public Instant getExpira() { return expira; }

    void renovar(Instant nuevaExpiracion) { this.expira = nuevaExpiracion; }

    public boolean esCliente() { return tipo == Tipo.CLIENTE; }
}
