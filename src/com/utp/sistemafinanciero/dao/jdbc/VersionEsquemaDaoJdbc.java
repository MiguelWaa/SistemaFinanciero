package com.utp.sistemafinanciero.dao.jdbc;

import com.utp.sistemafinanciero.dao.VersionEsquemaDao;
import com.utp.sistemafinanciero.model.sistema.VersionEsquema;
import com.utp.sistemafinanciero.util.Db;

import java.time.LocalDateTime;
import java.util.List;

/** Implementación JDBC/PostgreSQL del historial {@code version_esquema}. */
public class VersionEsquemaDaoJdbc implements VersionEsquemaDao {

    private static final String COLUMNAS =
            "SELECT id_version, version, descripcion, script, aplicado_en, aplicado_por FROM version_esquema ";

    private static final Db.RowMapper<VersionEsquema> MAPPER = rs -> {
        VersionEsquema v = new VersionEsquema();
        v.setIdVersion(rs.getObject("id_version", Integer.class));
        v.setVersion(rs.getString("version"));
        v.setDescripcion(rs.getString("descripcion"));
        v.setScript(rs.getString("script"));
        v.setAplicadoEn(rs.getObject("aplicado_en", LocalDateTime.class));
        v.setAplicadoPor(rs.getString("aplicado_por"));
        return v;
    };

    @Override
    public List<VersionEsquema> listar() {
        return Db.query(COLUMNAS + "ORDER BY id_version DESC", MAPPER);
    }

    @Override
    public VersionEsquema actual() {
        return Db.queryOne(COLUMNAS + "ORDER BY id_version DESC LIMIT 1", MAPPER);
    }

    @Override
    public Integer registrar(VersionEsquema v) {
        return Db.insert(
                "INSERT INTO version_esquema (version, descripcion, script) VALUES (?, ?, ?) "
                + "ON CONFLICT (version) DO NOTHING",
                v.getVersion(), v.getDescripcion(), v.getScript());
    }
}
