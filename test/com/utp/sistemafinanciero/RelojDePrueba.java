package com.utp.sistemafinanciero;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** Reloj controlable para probar expiraciones sin esperar tiempo real. */
public class RelojDePrueba extends Clock {

    private Instant ahora = Instant.parse("2026-10-10T12:00:00Z");

    public void avanzar(Duration d) {
        ahora = ahora.plus(d);
    }

    @Override public ZoneId getZone() { return ZoneOffset.UTC; }
    @Override public Clock withZone(ZoneId zone) { return this; }
    @Override public Instant instant() { return ahora; }
}
