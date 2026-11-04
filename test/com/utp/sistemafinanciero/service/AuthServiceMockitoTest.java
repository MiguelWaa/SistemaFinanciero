package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.RolDao;
import com.utp.sistemafinanciero.dao.UsuarioDao;
import com.utp.sistemafinanciero.dto.LoginDTO;
import com.utp.sistemafinanciero.dto.SesionDTO;
import com.utp.sistemafinanciero.model.seguridad.Rol;
import com.utp.sistemafinanciero.model.seguridad.Usuario;
import com.utp.sistemafinanciero.security.GestorSesiones;
import com.utp.sistemafinanciero.security.LimitadorIntentos;
import com.utp.sistemafinanciero.security.PasswordHasher;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Plan de Pruebas · PU-SEG-M · AuthService con Mockito (RF-SEG-01,
 * RNF-SEG-01). Se aísla el servicio de la base de datos y del cifrado
 * PBKDF2 para comprobar las decisiones de seguridad por su interacción.
 */
public class AuthServiceMockitoTest {

    private static final String HASH_FICTICIO = "hash-ficticio";
    private UsuarioDao usuarios;
    private RolDao roles;
    private PasswordHasher hasher;
    private LimitadorIntentos limitador;
    private AuthService auth;

    @Before
    public void preparar() {
        usuarios = mock(UsuarioDao.class);
        roles = mock(RolDao.class);
        hasher = mock(PasswordHasher.class);
        limitador = spy(new LimitadorIntentos(5, 5));
        when(hasher.hash("sigsf-ficticio")).thenReturn(HASH_FICTICIO);
        auth = new AuthService(usuarios, roles, hasher, new GestorSesiones(30), limitador);
    }

    private Usuario usuario(String hash, String estado) {
        Usuario u = new Usuario(3, "Lucía", "Ramos", "lucia@sigsf.com", hash, 2, estado, null);
        when(usuarios.buscarPorEmail("lucia@sigsf.com")).thenReturn(u);
        when(roles.buscar(2)).thenReturn(new Rol(2, "CAJERO", "Caja"));
        return u;
    }

    @Test
    public void correoInexistenteVerificaContraHashFicticioParaIgualarTiempos() {
        when(hasher.verificar(eq("x"), eq(HASH_FICTICIO))).thenReturn(false);

        assertThrows(ReglaNegocioException.class, () -> auth.login(new LoginDTO("nadie@sigsf.com", "x")));

        verify(hasher).verificar("x", HASH_FICTICIO);
        verify(limitador).registrarFallo("nadie@sigsf.com");
    }

    @Test
    public void cuentaBloqueadaNoConsultaLaBaseNiElCifrado() {
        for (int i = 0; i < 5; i++) limitador.registrarFallo("lucia@sigsf.com");
        clearInvocations(limitador);

        assertThrows(ReglaNegocioException.class, () -> auth.login(new LoginDTO("lucia@sigsf.com", "lucia123")));

        verify(usuarios, never()).buscarPorEmail(anyString());
        verify(hasher, never()).verificar(eq("lucia123"), anyString());
    }

    @Test
    public void loginCorrectoReiniciaElContadorYAbreSesionConRol() {
        usuario("pbkdf2$ok", "ACTIVO");
        when(hasher.verificar("lucia123", "pbkdf2$ok")).thenReturn(true);

        SesionDTO s = auth.login(new LoginDTO("lucia@sigsf.com", "lucia123"));

        assertNotNull(s.getToken());
        verify(limitador).registrarExito("lucia@sigsf.com");
        verify(usuarios, never()).actualizarPassword(anyInt(), anyString());
    }

    @Test
    public void hashAntiguoSeMigraUnaSolaVez() {
        usuario("sha256-antiguo", "ACTIVO");
        when(hasher.verificar("lucia123", "sha256-antiguo")).thenReturn(true);
        when(hasher.requiereActualizar("sha256-antiguo")).thenReturn(true);
        when(hasher.hash("lucia123")).thenReturn("pbkdf2$nuevo");

        auth.login(new LoginDTO("lucia@sigsf.com", "lucia123"));

        verify(usuarios, times(1)).actualizarPassword(3, "pbkdf2$nuevo");
    }

    @Test
    public void cuentaPendienteNoAbreSesion() {
        usuario("pbkdf2$ok", "PENDIENTE");
        when(hasher.verificar("lucia123", "pbkdf2$ok")).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () -> auth.login(new LoginDTO("lucia@sigsf.com", "lucia123")));
        verify(limitador, never()).registrarExito(anyString());
    }
}
