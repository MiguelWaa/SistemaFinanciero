package com.utp.sistemafinanciero.security;

/**
 * Estrategia para proteger contraseñas (patrón <b>Strategy</b>).
 *
 * <p>Los servicios dependen de esta interfaz y no de un algoritmo
 * concreto (DIP). Si mañana se quiere usar BCrypt o Argon2 solo se crea
 * otra implementación, sin tocar los servicios (OCP).</p>
 */
public interface PasswordHasher {

    /** Devuelve el valor protegido (con sal) que se guarda en la base de datos. */
    String hash(String plano);

    /** Indica si la contraseña escrita corresponde al valor almacenado. */
    boolean verificar(String plano, String almacenado);

    /**
     * Indica si el valor almacenado usa un formato antiguo o débil y debe
     * regenerarse (migración transparente al iniciar sesión).
     */
    boolean requiereActualizar(String almacenado);
}
