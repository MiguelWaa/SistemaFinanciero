package com.utp.sistemafinanciero.security;

import com.utp.sistemafinanciero.service.ReglaNegocioException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Protección contra ataques de fuerza bruta: tras N intentos fallidos
 * consecutivos para una misma cuenta, la bloquea temporalmente.
 */
public class LimitadorIntentos {

    private static final class Registro {
        int fallos;
        Instant bloqueadoHasta;
    }

    private final Map<String, Registro> registros = new ConcurrentHashMap<>();
    private final int maxIntentos;
    private final Duration bloqueo;
    private final Clock reloj;

    public LimitadorIntentos(int maxIntentos, Duration bloqueo, Clock reloj) {
        this.maxIntentos = maxIntentos;
        this.bloqueo = bloqueo;
        this.reloj = reloj;
    }

    public LimitadorIntentos(int maxIntentos, int minutosBloqueo) {
        this(maxIntentos, Duration.ofMinutes(minutosBloqueo), Clock.systemUTC());
    }

    /** Lanza una excepción si la cuenta está bloqueada en este momento. */
    public void verificar(String cuenta) {
        Registro r = registros.get(clave(cuenta));
        if (r != null && r.bloqueadoHasta != null) {
            Instant ahora = reloj.instant();
            if (ahora.isBefore(r.bloqueadoHasta)) {
                long min = Math.max(1, Duration.between(ahora, r.bloqueadoHasta).toMinutes() + 1);
                throw new ReglaNegocioException("Cuenta bloqueada temporalmente por varios intentos fallidos. "
                        + "Intente de nuevo en " + min + " minuto(s).");
            }
            registros.remove(clave(cuenta));
        }
    }

    /** Registra un intento fallido; devuelve true si la cuenta quedó bloqueada. */
    public boolean registrarFallo(String cuenta) {
        Registro r = registros.computeIfAbsent(clave(cuenta), k -> new Registro());
        synchronized (r) {
            r.fallos++;
            if (r.fallos >= maxIntentos) {
                r.bloqueadoHasta = reloj.instant().plus(bloqueo);
                return true;
            }
            return false;
        }
    }

    /** Un acceso correcto reinicia el contador. */
    public void registrarExito(String cuenta) {
        registros.remove(clave(cuenta));
    }

    public int fallos(String cuenta) {
        Registro r = registros.get(clave(cuenta));
        return r == null ? 0 : r.fallos;
    }

    private static String clave(String cuenta) {
        return cuenta == null ? "" : cuenta.trim().toLowerCase();
    }
}
