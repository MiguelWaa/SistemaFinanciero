package com.utp.sistemafinanciero.security;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Administra las sesiones activas mediante <b>tokens opacos</b> de 256
 * bits generados con {@link SecureRandom}.
 *
 * <ul>
 *   <li>Expiración por inactividad (por defecto 30 minutos), renovada en
 *       cada petición válida (expiración deslizante).</li>
 *   <li>Cierre de sesión real: el token se revoca en el servidor.</li>
 *   <li>El reloj es inyectable para poder probar la expiración (TDD).</li>
 * </ul>
 */
public class GestorSesiones {

    private final Map<String, Sesion> sesiones = new ConcurrentHashMap<>();
    private final SecureRandom aleatorio = new SecureRandom();
    private final Duration duracion;
    private final Clock reloj;

    public GestorSesiones(Duration duracion, Clock reloj) {
        this.duracion = duracion;
        this.reloj = reloj;
    }

    public GestorSesiones(int minutos) {
        this(Duration.ofMinutes(minutos), Clock.systemUTC());
    }

    /** Crea una sesión nueva y devuelve sus datos (incluido el token). */
    public Sesion abrir(Sesion.Tipo tipo, int idSujeto, String usuario, String rol) {
        byte[] bytes = new byte[32];
        aleatorio.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant ahora = reloj.instant();
        Sesion s = new Sesion(token, tipo, idSujeto, usuario, rol, ahora, ahora.plus(duracion));
        sesiones.put(token, s);
        return s;
    }

    /** Valida un token: si existe y no expiró, renueva su vigencia y la devuelve. */
    public Optional<Sesion> validar(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        Sesion s = sesiones.get(token);
        if (s == null) {
            return Optional.empty();
        }
        Instant ahora = reloj.instant();
        if (ahora.isAfter(s.getExpira())) {
            sesiones.remove(token);
            return Optional.empty();
        }
        s.renovar(ahora.plus(duracion));
        return Optional.of(s);
    }

    /** Cierra (revoca) la sesión. */
    public void cerrar(String token) {
        if (token != null) {
            sesiones.remove(token);
        }
    }

    /** Elimina las sesiones vencidas (se invoca periódicamente). */
    public int purgarVencidas() {
        Instant ahora = reloj.instant();
        int antes = sesiones.size();
        sesiones.values().removeIf(s -> ahora.isAfter(s.getExpira()));
        return antes - sesiones.size();
    }

    public int activas() {
        return sesiones.size();
    }

    public long minutos() {
        return duracion.toMinutes();
    }
}
