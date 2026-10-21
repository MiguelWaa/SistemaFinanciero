package com.utp.sistemafinanciero.security;

import com.utp.sistemafinanciero.RelojDePrueba;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.*;

/** RNF-SEG-02 · Sesiones con token y expiración por inactividad. */
public class GestorSesionesTest {

    private RelojDePrueba reloj;
    private GestorSesiones gestor;

    @Before
    public void preparar() {
        reloj = new RelojDePrueba();
        gestor = new GestorSesiones(Duration.ofMinutes(30), reloj);
    }

    @Test
    public void abrirGeneraTokenUnicoDe256Bits() {
        Sesion a = gestor.abrir(Sesion.Tipo.PERSONAL, 1, "admin@sigsf.com", "admin");
        Sesion b = gestor.abrir(Sesion.Tipo.PERSONAL, 1, "admin@sigsf.com", "ADMIN");
        assertNotEquals(a.getToken(), b.getToken());
        assertEquals(43, a.getToken().length());
        assertEquals("ADMIN", a.getRol());
    }

    @Test
    public void tokenInventadoNoEsValido() {
        assertFalse(gestor.validar("token-falso").isPresent());
        assertFalse(gestor.validar(null).isPresent());
    }

    @Test
    public void laSesionExpiraTras30MinutosDeInactividad() {
        Sesion s = gestor.abrir(Sesion.Tipo.PERSONAL, 1, "admin@sigsf.com", "ADMIN");
        reloj.avanzar(Duration.ofMinutes(31));
        assertFalse(gestor.validar(s.getToken()).isPresent());
    }

    @Test
    public void cadaPeticionRenuevaLaVigencia() {
        Sesion s = gestor.abrir(Sesion.Tipo.PERSONAL, 1, "admin@sigsf.com", "ADMIN");
        reloj.avanzar(Duration.ofMinutes(20));
        assertTrue(gestor.validar(s.getToken()).isPresent());
        reloj.avanzar(Duration.ofMinutes(20)); // 40 min desde el login, 20 desde el último uso
        assertTrue(gestor.validar(s.getToken()).isPresent());
    }

    @Test
    public void cerrarSesionRevocaElToken() {
        Sesion s = gestor.abrir(Sesion.Tipo.CLIENTE, 7, "70000001", "CLIENTE");
        gestor.cerrar(s.getToken());
        assertFalse(gestor.validar(s.getToken()).isPresent());
        assertEquals(0, gestor.activas());
    }
}
