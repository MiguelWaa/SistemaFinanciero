package com.utp.sistemafinanciero.dao.jdbc;

import com.utp.sistemafinanciero.dao.CampaniaDao;

import com.utp.sistemafinanciero.model.marketing.Campania;
import com.utp.sistemafinanciero.util.Db;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Implementación JDBC/PostgreSQL del DAO de la tabla {@code campania}. */
public class CampaniaDaoJdbc implements CampaniaDao {

    private static final Db.RowMapper<Campania> MAPPER = rs -> new Campania(
            rs.getObject("id_campania", Integer.class),
            rs.getString("nombre"),
            rs.getString("descripcion"),
            rs.getObject("fecha_inicio", LocalDate.class),
            rs.getObject("fecha_fin", LocalDate.class),
            rs.getObject("presupuesto", BigDecimal.class),
            rs.getString("canal"),
            rs.getString("estado"));

    private static final String COLS =
            "id_campania, nombre, descripcion, fecha_inicio, fecha_fin, presupuesto, canal, estado";

    @Override
    public List<Campania> listar() {
        return Db.query("SELECT " + COLS + " FROM campania ORDER BY id_campania DESC", MAPPER);
    }

    @Override
    public Campania buscar(int idCampania) {
        return Db.queryOne("SELECT " + COLS + " FROM campania WHERE id_campania = ?", MAPPER, idCampania);
    }

    @Override
    public Integer crear(Campania c) {
        return Db.insert(
                "INSERT INTO campania (nombre, descripcion, fecha_inicio, fecha_fin, presupuesto, canal, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                c.getNombre(), c.getDescripcion(), c.getFechaInicio(), c.getFechaFin(),
                c.getPresupuesto(), c.getCanal(), c.getEstado());
    }

    @Override
    public int actualizar(Campania c) {
        return Db.update(
                "UPDATE campania SET nombre = ?, descripcion = ?, fecha_inicio = ?, fecha_fin = ?, "
                + "presupuesto = ?, canal = ?, estado = ? WHERE id_campania = ?",
                c.getNombre(), c.getDescripcion(), c.getFechaInicio(), c.getFechaFin(),
                c.getPresupuesto(), c.getCanal(), c.getEstado(), c.getIdCampania());
    }

    @Override
    public int eliminar(int idCampania) {
        return Db.update("DELETE FROM campania WHERE id_campania = ?", idCampania);
    }
}
