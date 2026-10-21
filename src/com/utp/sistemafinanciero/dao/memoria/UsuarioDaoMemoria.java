package com.utp.sistemafinanciero.dao.memoria;

import com.utp.sistemafinanciero.dao.UsuarioDao;
import com.utp.sistemafinanciero.model.seguridad.Usuario;

/** Implementación en memoria del {@link UsuarioDao}. */
public class UsuarioDaoMemoria extends RepositorioMemoria<Usuario> implements UsuarioDao {

    public UsuarioDaoMemoria() {
        super(Usuario::getIdUsuario, Usuario::setIdUsuario, false);
    }

    @Override
    public Usuario buscarPorEmail(String email) {
        return filtrar(u -> email != null && email.equalsIgnoreCase(u.getEmail()))
                .stream().findFirst().orElse(null);
    }

    /** Igual que el DAO JDBC, actualizar() NO toca la contraseña. */
    @Override
    public int actualizar(Usuario u) {
        Usuario actual = buscar(u.getIdUsuario() == null ? -1 : u.getIdUsuario());
        if (actual == null) return 0;
        Usuario copia = copiar(u);
        copia.setPassword(actual.getPassword());
        copia.setFechaRegistro(actual.getFechaRegistro());
        return super.actualizar(copia);
    }

    @Override
    public int actualizarPassword(int idUsuario, String passwordHash) {
        return modificar(u -> u.getIdUsuario() == idUsuario, u -> u.setPassword(passwordHash));
    }

    @Override
    public int contar() {
        return total();
    }
}
