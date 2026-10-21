package com.utp.sistemafinanciero.security;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/** RNF-GEN-03 · Protección de credenciales. */
public class Pbkdf2PasswordHasherTest {

    private Pbkdf2PasswordHasher hasher;

    @Before
    public void preparar() {
        hasher = new Pbkdf2PasswordHasher(1000);
    }

    @Test
    public void elHashNoContieneLaClaveYUsaFormatoPbkdf2() {
        String h = hasher.hash("admin123");
        assertTrue(h.startsWith("pbkdf2$1000$"));
        assertFalse(h.contains("admin123"));
    }

    @Test
    public void dosHashesDeLaMismaClaveSonDistintosPorLaSal() {
        assertNotEquals(hasher.hash("admin123"), hasher.hash("admin123"));
    }

    @Test
    public void verificaClaveCorrectaYRechazaIncorrecta() {
        String h = hasher.hash("lucia123");
        assertTrue(hasher.verificar("lucia123", h));
        assertFalse(hasher.verificar("Lucia123", h));
        assertFalse(hasher.verificar("", h));
        assertFalse(hasher.verificar(null, h));
    }

    @Test
    public void aceptaHashSha256HeredadoYPideActualizarlo() {
        // Hash SHA-256 de "admin123" que traen los datos de ejemplo antiguos.
        String heredado = "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9";
        assertTrue(hasher.verificar("admin123", heredado));
        assertFalse(hasher.verificar("otra", heredado));
        assertTrue(hasher.requiereActualizar(heredado));
        assertFalse(hasher.requiereActualizar(hasher.hash("admin123")));
    }

    @Test
    public void pideActualizarSiSeAumentanLasIteraciones() {
        String debil = hasher.hash("clave123");
        assertTrue(new Pbkdf2PasswordHasher(5000).requiereActualizar(debil));
    }
}
