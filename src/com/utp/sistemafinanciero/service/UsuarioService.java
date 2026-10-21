package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.UsuarioDao;
import com.utp.sistemafinanciero.dto.UsuarioDTO;
import com.utp.sistemafinanciero.model.seguridad.Usuario;
import com.utp.sistemafinanciero.security.PasswordHasher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Reglas de negocio para la gestión de usuarios: validaciones, unicidad
 * del correo, política de contraseñas, cifrado PBKDF2 y valores por
 * defecto. Devuelve siempre {@link UsuarioDTO} (nunca la contraseña).
 */
public class UsuarioService implements ServicioCrud<Usuario, UsuarioDTO> {

    /** Contraseña temporal cuando el administrador no indica una. */
    public static final String CLAVE_TEMPORAL = "cambiar123";
    private static final Set<String> ESTADOS = Set.of("ACTIVO", "INACTIVO", "PENDIENTE", "BLOQUEADO");

    private final UsuarioDao dao;
    private final PasswordHasher hasher;

    /** El DAO y el algoritmo de cifrado se reciben por constructor (DIP). */
    public UsuarioService(UsuarioDao dao, PasswordHasher hasher) {
        this.dao = dao;
        this.hasher = hasher;
    }

    public List<UsuarioDTO> listar() {
        return dao.listar().stream().map(UsuarioDTO::de).collect(Collectors.toList());
    }

    public UsuarioDTO buscar(int id) {
        return UsuarioDTO.de(exigir(id));
    }

    public UsuarioDTO crear(Usuario u) {
        validar(u);
        if (dao.buscarPorEmail(u.getEmail().trim()) != null) {
            throw new ReglaNegocioException("Ya existe un usuario con el correo " + u.getEmail() + ".");
        }
        String clave = (u.getPassword() == null || u.getPassword().isBlank()) ? CLAVE_TEMPORAL : u.getPassword();
        if (!CLAVE_TEMPORAL.equals(clave)) {
            validarClave(clave);
        }
        u.setEmail(u.getEmail().trim());
        u.setPassword(hasher.hash(clave));
        if (u.getEstado() == null || u.getEstado().isBlank()) {
            u.setEstado("PENDIENTE");
        }
        u.setFechaRegistro(LocalDateTime.now());
        u.setIdUsuario(dao.crear(u));
        return UsuarioDTO.de(u);
    }

    public UsuarioDTO actualizar(int id, Usuario u) {
        Usuario actual = exigir(id);
        u.setIdUsuario(id);
        if (u.getEstado() == null || u.getEstado().isBlank()) {
            u.setEstado(actual.getEstado());
        }
        validar(u);
        Usuario otro = dao.buscarPorEmail(u.getEmail().trim());
        if (otro != null && otro.getIdUsuario() != id) {
            throw new ReglaNegocioException("Ya existe otro usuario con el correo " + u.getEmail() + ".");
        }
        dao.actualizar(u);
        // Si se escribió una contraseña nueva, se cambia por separado.
        if (u.getPassword() != null && !u.getPassword().isBlank()) {
            cambiarPassword(id, u.getPassword());
        }
        u.setFechaRegistro(actual.getFechaRegistro());
        return UsuarioDTO.de(u);
    }

    public void cambiarPassword(int id, String nuevaClave) {
        exigir(id);
        validarClave(nuevaClave);
        dao.actualizarPassword(id, hasher.hash(nuevaClave));
    }

    public void eliminar(int id) {
        if (dao.eliminar(id) == 0) {
            throw new ReglaNegocioException("No existe el usuario con id " + id + ".");
        }
    }

    /** Política de contraseñas: mínimo 8 caracteres, con letras y números. */
    static void validarClave(String clave) {
        if (clave == null || clave.length() < 8
                || !clave.matches(".*[A-Za-z].*") || !clave.matches(".*[0-9].*")) {
            throw new ReglaNegocioException(
                    "La contraseña debe tener al menos 8 caracteres e incluir letras y números.");
        }
    }

    private Usuario exigir(int id) {
        Usuario u = dao.buscar(id);
        if (u == null) {
            throw new ReglaNegocioException("No existe el usuario con id " + id + ".");
        }
        return u;
    }

    private void validar(Usuario u) {
        if (u.getNombre() == null || u.getNombre().isBlank()) {
            throw new ReglaNegocioException("El nombre es obligatorio.");
        }
        if (u.getEmail() == null || u.getEmail().isBlank()) {
            throw new ReglaNegocioException("El correo es obligatorio.");
        }
        if (!u.getEmail().trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new ReglaNegocioException("El correo no tiene un formato válido.");
        }
        if (u.getIdRol() == null) {
            throw new ReglaNegocioException("Debe asignar un rol al usuario.");
        }
        if (u.getEstado() != null && !u.getEstado().isBlank() && !ESTADOS.contains(u.getEstado())) {
            throw new ReglaNegocioException("Estado de usuario no válido: " + u.getEstado() + ".");
        }
    }
}
