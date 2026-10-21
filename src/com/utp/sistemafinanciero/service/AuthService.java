package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.RolDao;
import com.utp.sistemafinanciero.dao.UsuarioDao;
import com.utp.sistemafinanciero.dto.LoginDTO;
import com.utp.sistemafinanciero.dto.SesionDTO;
import com.utp.sistemafinanciero.dto.UsuarioDTO;
import com.utp.sistemafinanciero.model.seguridad.Rol;
import com.utp.sistemafinanciero.model.seguridad.Usuario;
import com.utp.sistemafinanciero.security.GestorSesiones;
import com.utp.sistemafinanciero.security.LimitadorIntentos;
import com.utp.sistemafinanciero.security.PasswordHasher;
import com.utp.sistemafinanciero.security.Sesion;

/**
 * Autenticación del personal.
 *
 * <ul>
 *   <li>Verifica la contraseña con PBKDF2 (y migra los hashes antiguos).</li>
 *   <li>Bloquea la cuenta tras varios intentos fallidos (fuerza bruta).</li>
 *   <li>Usa un mensaje genérico de error para no revelar qué correos existen.</li>
 *   <li>Solo las cuentas ACTIVAS pueden entrar (RF-SEG-01).</li>
 *   <li>Abre una sesión en el servidor y devuelve su token.</li>
 * </ul>
 */
public class AuthService {

    private static final String CREDENCIALES_INVALIDAS = "Usuario o contraseña incorrectos.";

    private final UsuarioDao usuarioDao;
    private final RolDao rolDao;
    private final PasswordHasher hasher;
    private final GestorSesiones sesiones;
    private final LimitadorIntentos limitador;
    /** Hash ficticio: se verifica aunque el correo no exista, para igualar tiempos de respuesta. */
    private final String hashFicticio;

    public AuthService(UsuarioDao usuarioDao, RolDao rolDao, PasswordHasher hasher,
                       GestorSesiones sesiones, LimitadorIntentos limitador) {
        this.usuarioDao = usuarioDao;
        this.rolDao = rolDao;
        this.hasher = hasher;
        this.sesiones = sesiones;
        this.limitador = limitador;
        this.hashFicticio = hasher.hash("sigsf-ficticio");
    }

    public SesionDTO login(LoginDTO datos) {
        if (datos.incompleto()) {
            throw new ReglaNegocioException("Debe ingresar usuario y contraseña.");
        }
        String email = datos.getUsuario();
        limitador.verificar(email);

        Usuario u = usuarioDao.buscarPorEmail(email);
        boolean claveOk = hasher.verificar(datos.password(), u == null ? hashFicticio : u.getPassword());
        if (u == null || !claveOk) {
            boolean bloqueada = limitador.registrarFallo(email);
            throw new ReglaNegocioException(bloqueada
                    ? "Demasiados intentos fallidos. La cuenta quedó bloqueada temporalmente."
                    : CREDENCIALES_INVALIDAS);
        }
        if (u.getEstado() != null && !"ACTIVO".equalsIgnoreCase(u.getEstado())) {
            throw new ReglaNegocioException("La cuenta no está activa. Contacte al administrador.");
        }
        limitador.registrarExito(email);

        // Migración transparente: SHA-256 antiguo → PBKDF2 con sal.
        if (hasher.requiereActualizar(u.getPassword())) {
            usuarioDao.actualizarPassword(u.getIdUsuario(), hasher.hash(datos.password()));
        }

        String rolNombre = "";
        if (u.getIdRol() != null) {
            Rol rol = rolDao.buscar(u.getIdRol());
            if (rol != null) {
                rolNombre = rol.getNombre();
            }
        }
        Sesion s = sesiones.abrir(Sesion.Tipo.PERSONAL, u.getIdUsuario(), u.getEmail(), rolNombre);
        return new SesionDTO(s.getToken(), (int) sesiones.minutos(), UsuarioDTO.de(u), s.getRol());
    }

    /** Cierra la sesión (revoca el token en el servidor). */
    public void logout(String token) {
        sesiones.cerrar(token);
    }
}
