package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.memoria.UsuarioDaoMemoria;
import com.utp.sistemafinanciero.dto.UsuarioDTO;
import com.utp.sistemafinanciero.model.seguridad.Usuario;
import com.utp.sistemafinanciero.security.Pbkdf2PasswordHasher;
import com.utp.sistemafinanciero.util.Json;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/** RF-SEG-02 · RNF-GEN-03/04 · Usuarios con contraseña cifrada y correo único. */
public class UsuarioServiceTest {

    private UsuarioDaoMemoria dao;
    private Pbkdf2PasswordHasher hasher;
    private UsuarioService servicio;

    @Before
    public void preparar() {
        dao = new UsuarioDaoMemoria();
        hasher = new Pbkdf2PasswordHasher(1000);
        servicio = new UsuarioService(dao, hasher);
    }

    private static Usuario usuario(String email, String clave) {
        Usuario u = new Usuario();
        u.setNombre("Lucía");
        u.setEmail(email);
        u.setPassword(clave);
        u.setIdRol(3);
        return u;
    }

    @Test
    public void guardaLaClaveCifradaYNuncaLaDevuelve() {
        UsuarioDTO creado = servicio.crear(usuario("lucia@sigsf.com", "Segura2026"));
        String almacenado = dao.buscar(creado.getIdUsuario()).getPassword();
        assertTrue(almacenado.startsWith("pbkdf2$"));
        assertTrue(hasher.verificar("Segura2026", almacenado));
        String json = Json.stringify(servicio.listar());
        assertFalse("La API no debe exponer el hash", json.contains("pbkdf2") || json.contains("password"));
    }

    @Test
    public void usuarioNuevoQuedaPendiente() {
        assertEquals("PENDIENTE", servicio.crear(usuario("a@sigsf.com", "Segura2026")).getEstado());
    }

    @Test
    public void elCorreoEsUnico() {
        servicio.crear(usuario("a@sigsf.com", "Segura2026"));
        assertThrows(ReglaNegocioException.class, () -> servicio.crear(usuario("A@sigsf.com", "Segura2026")));
    }

    @Test
    public void aplicaPoliticaDeContrasenas() {
        assertThrows(ReglaNegocioException.class, () -> servicio.crear(usuario("b@sigsf.com", "123")));
        assertThrows(ReglaNegocioException.class, () -> servicio.crear(usuario("c@sigsf.com", "soloLetras")));
    }

    @Test
    public void sinClaveSeAsignaLaTemporal() {
        UsuarioDTO u = servicio.crear(usuario("d@sigsf.com", null));
        assertTrue(hasher.verificar(UsuarioService.CLAVE_TEMPORAL, dao.buscar(u.getIdUsuario()).getPassword()));
    }

    @Test
    public void validaFormatoDeCorreoYRol() {
        assertThrows(ReglaNegocioException.class, () -> servicio.crear(usuario("sin-arroba", "Segura2026")));
        Usuario sinRol = usuario("e@sigsf.com", "Segura2026");
        sinRol.setIdRol(null);
        assertThrows(ReglaNegocioException.class, () -> servicio.crear(sinRol));
    }
}
