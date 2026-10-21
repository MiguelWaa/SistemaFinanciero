package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.memoria.MemoriaDaoFactory;
import com.utp.sistemafinanciero.dto.LoginDTO;
import com.utp.sistemafinanciero.dto.SesionDTO;
import com.utp.sistemafinanciero.model.seguridad.Rol;
import com.utp.sistemafinanciero.model.seguridad.Usuario;
import com.utp.sistemafinanciero.security.GestorSesiones;
import com.utp.sistemafinanciero.security.LimitadorIntentos;
import com.utp.sistemafinanciero.security.Pbkdf2PasswordHasher;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

/** RF-SEG-01 · RNF-SEG-01 · Inicio de sesión del personal. */
public class AuthServiceTest {

    private MemoriaDaoFactory bd;
    private GestorSesiones sesiones;
    private AuthService auth;
    private Pbkdf2PasswordHasher hasher;

    @Before
    public void preparar() {
        bd = new MemoriaDaoFactory();
        hasher = new Pbkdf2PasswordHasher(1000);
        sesiones = new GestorSesiones(30);
        auth = new AuthService(bd.usuarios(), bd.roles(), hasher, sesiones, new LimitadorIntentos(5, 5));
        bd.roles().crear(new Rol(null, "ADMIN", "Administrador"));
        bd.usuarios().crear(new Usuario(null, "Miguel", "Machaca", "admin@sigsf.com",
                hasher.hash("admin123"), 1, "ACTIVO", LocalDateTime.now()));
    }

    @Test
    public void loginCorrectoDevuelveTokenYRol() {
        SesionDTO s = auth.login(new LoginDTO("admin@sigsf.com", "admin123"));
        assertNotNull(s.getToken());
        assertEquals("ADMIN", s.getRol());
        assertTrue(sesiones.validar(s.getToken()).isPresent());
    }

    @Test
    public void mensajeGenericoNoRevelaSiElCorreoExiste() {
        String m1 = assertThrows(ReglaNegocioException.class,
                () -> auth.login(new LoginDTO("admin@sigsf.com", "mala"))).getMessage();
        String m2 = assertThrows(ReglaNegocioException.class,
                () -> auth.login(new LoginDTO("noexiste@sigsf.com", "mala"))).getMessage();
        assertEquals(m1, m2);
    }

    @Test
    public void cuentaNoActivaNoPuedeEntrar() {
        bd.usuarios().crear(new Usuario(null, "Nuevo", "", "nuevo@sigsf.com",
                hasher.hash("nuevo123"), 1, "PENDIENTE", LocalDateTime.now()));
        ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
                () -> auth.login(new LoginDTO("nuevo@sigsf.com", "nuevo123")));
        assertTrue(e.getMessage().contains("no está activa"));
    }

    @Test
    public void bloqueaTrasCincoIntentosAunqueLuegoLaClaveSeaCorrecta() {
        for (int i = 0; i < 5; i++) {
            assertThrows(ReglaNegocioException.class, () -> auth.login(new LoginDTO("admin@sigsf.com", "x")));
        }
        assertThrows(ReglaNegocioException.class, () -> auth.login(new LoginDTO("admin@sigsf.com", "admin123")));
    }

    @Test
    public void migraHashAntiguoSha256APbkdf2AlIniciarSesion() {
        int id = bd.usuarios().crear(new Usuario(null, "Viejo", "", "viejo@sigsf.com",
                Pbkdf2PasswordHasher.sha256Hex("viejo123"), 1, "ACTIVO", LocalDateTime.now()));
        auth.login(new LoginDTO("viejo@sigsf.com", "viejo123"));
        assertTrue(bd.usuarios().buscar(id).getPassword().startsWith("pbkdf2$"));
    }

    @Test
    public void logoutRevocaLaSesion() {
        SesionDTO s = auth.login(new LoginDTO("admin@sigsf.com", "admin123"));
        auth.logout(s.getToken());
        assertFalse(sesiones.validar(s.getToken()).isPresent());
    }
}
