package com.utp.sistemafinanciero.security;

import com.utp.sistemafinanciero.RelojDePrueba;
import com.utp.sistemafinanciero.service.ReglaNegocioException;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.*;

/** Protección contra fuerza bruta: bloqueo tras 5 intentos fallidos. */
public class LimitadorIntentosTest {

    private RelojDePrueba reloj;
    private LimitadorIntentos limitador;

    @Before
    public void preparar() {
        reloj = new RelojDePrueba();
        limitador = new LimitadorIntentos(5, Duration.ofMinutes(5), reloj);
    }

    @Test
    public void bloqueaAlQuintoFallo() {
        for (int i = 0; i < 4; i++) {
            assertFalse(limitador.registrarFallo("admin@sigsf.com"));
        }
        assertTrue(limitador.registrarFallo("ADMIN@sigsf.com")); // no distingue mayúsculas
        assertThrows(ReglaNegocioException.class, () -> limitador.verificar("admin@sigsf.com"));
    }

    @Test
    public void elBloqueoTerminaTrasElTiempoConfigurado() {
        for (int i = 0; i < 5; i++) limitador.registrarFallo("u@sigsf.com");
        reloj.avanzar(Duration.ofMinutes(6));
        limitador.verificar("u@sigsf.com"); // no lanza
        assertEquals(0, limitador.fallos("u@sigsf.com"));
    }

    @Test
    public void unAccesoCorrectoReiniciaElContador() {
        limitador.registrarFallo("u@sigsf.com");
        limitador.registrarFallo("u@sigsf.com");
        limitador.registrarExito("u@sigsf.com");
        assertEquals(0, limitador.fallos("u@sigsf.com"));
    }
}
