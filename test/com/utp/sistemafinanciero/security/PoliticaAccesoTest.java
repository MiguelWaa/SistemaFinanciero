package com.utp.sistemafinanciero.security;

import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.*;

/** Control de acceso por roles (RBAC) y aislamiento del Portal del Cliente. */
public class PoliticaAccesoTest {

    private final PoliticaAcceso politica = new PoliticaAcceso();

    private static Sesion personal(String rol) {
        return new Sesion("t", Sesion.Tipo.PERSONAL, 1, "x@sigsf.com", rol, Instant.now(), Instant.now());
    }

    private static Sesion cliente(int id) {
        return new Sesion("t", Sesion.Tipo.CLIENTE, id, "7000000" + id, "CLIENTE", Instant.now(), Instant.now());
    }

    @Test
    public void rutasPublicasNoRequierenSesion() {
        assertTrue(politica.esPublica("POST", "/api/auth/login"));
        assertTrue(politica.esPublica("GET", "/api/health"));
        assertFalse(politica.esPublica("GET", "/api/clientes"));
    }

    @Test
    public void adminPuedeTodo() {
        Sesion admin = personal("ADMIN");
        assertTrue(politica.permite(admin, "DELETE", "/api/usuarios/3"));
        assertTrue(politica.permite(admin, "POST", "/api/respaldos"));
    }

    @Test
    public void cajeroOperaCajaPeroNoAdministraUsuariosNiRespaldos() {
        Sesion cajero = personal("CAJERO");
        assertTrue(politica.permite(cajero, "POST", "/api/operaciones"));
        assertTrue(politica.permite(cajero, "POST", "/api/ventas"));
        assertTrue(politica.permite(cajero, "GET", "/api/tipos-cambio"));
        assertFalse(politica.permite(cajero, "PUT", "/api/tipos-cambio/1"));
        assertFalse(politica.permite(cajero, "GET", "/api/usuarios"));
        assertFalse(politica.permite(cajero, "POST", "/api/respaldos"));
        assertFalse(politica.permite(cajero, "POST", "/api/campanias"));
    }

    @Test
    public void analistaConsultaOperacionesYGestionaMarketing() {
        Sesion analista = personal("ANALISTA");
        assertTrue(politica.permite(analista, "GET", "/api/operaciones"));
        assertFalse(politica.permite(analista, "POST", "/api/operaciones"));
        assertTrue(politica.permite(analista, "POST", "/api/campanias"));
        assertTrue(politica.permite(analista, "PUT", "/api/expedientes/2"));
    }

    @Test
    public void rolPersonalizadoSoloLee() {
        Sesion auditor = personal("AUDITOR");
        assertTrue(politica.permite(auditor, "GET", "/api/ventas"));
        assertFalse(politica.permite(auditor, "DELETE", "/api/ventas/1"));
        assertFalse(politica.permite(auditor, "GET", "/api/auditoria"));
    }

    @Test
    public void clienteSoloAccedeASusPropiosTramites() {
        Sesion c = cliente(1);
        assertTrue(politica.permite(c, "GET", "/api/portal/1/operaciones"));
        assertFalse("IDOR: no debe ver trámites de otro cliente",
                politica.permite(c, "GET", "/api/portal/2/operaciones"));
        assertFalse(politica.permite(c, "GET", "/api/clientes"));
        assertTrue(politica.permite(c, "GET", "/api/tipos-cambio"));
    }

    @Test
    public void autorizarLanzaAccesoDenegado() {
        assertThrows(AccesoDenegadoException.class,
                () -> politica.autorizar(personal("CAJERO"), "DELETE", "/api/usuarios/1"));
    }
}
