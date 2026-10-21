package com.utp.sistemafinanciero.dao;

import com.utp.sistemafinanciero.model.seguridad.Usuario;

/**
 * Contrato DAO (Data Access Object) para la tabla {@code usuario}.
 *
 * <p>La capa de servicios depende de esta interfaz y no de una
 * implementación concreta (principio de inversión de dependencias, DIP).
 * Implementaciones: {@code dao.jdbc.UsuarioDaoJdbc} (PostgreSQL) y
 * {@code dao.memoria.UsuarioDaoMemoria} (pruebas / demostración).</p>
 */
public interface UsuarioDao extends CrudDao<Usuario> {
    Usuario buscarPorEmail(String email);

    int actualizarPassword(int idUsuario, String passwordHash);

    int contar();
}
