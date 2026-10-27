package com.utp.sistemafinanciero.dao.jdbc;

import com.utp.sistemafinanciero.dao.BilleteraDao;
import com.utp.sistemafinanciero.model.pagos.Billetera;
import com.utp.sistemafinanciero.util.Db;

import java.time.LocalDateTime;
import java.util.List;

/** Implementación JDBC/PostgreSQL del DAO de la tabla {@code billetera}. */
public class BilleteraDaoJdbc implements BilleteraDao {

    private static final String COLS = "id_billetera, codigo, nombre, numero_celular, titular, qr_imagen, "
            + "instrucciones, activo, fecha_actualizacion";

    private static final Db.RowMapper<Billetera> MAPPER = rs -> new Billetera(
            rs.getObject("id_billetera", Integer.class),
            rs.getString("codigo"),
            rs.getString("nombre"),
            rs.getString("numero_celular"),
            rs.getString("titular"),
            rs.getString("qr_imagen"),
            rs.getString("instrucciones"),
            rs.getObject("activo", Boolean.class),
            rs.getObject("fecha_actualizacion", LocalDateTime.class));

    @Override
    public List<Billetera> listar() {
        return Db.query("SELECT " + COLS + " FROM billetera ORDER BY id_billetera", MAPPER);
    }

    @Override
    public Billetera buscar(int id) {
        return Db.queryOne("SELECT " + COLS + " FROM billetera WHERE id_billetera = ?", MAPPER, id);
    }

    @Override
    public Billetera buscarPorCodigo(String codigo) {
        return Db.queryOne("SELECT " + COLS + " FROM billetera WHERE codigo = ?", MAPPER,
                codigo == null ? null : codigo.toUpperCase());
    }

    @Override
    public Integer crear(Billetera b) {
        return Db.insert("INSERT INTO billetera (codigo, nombre, numero_celular, titular, qr_imagen, instrucciones, "
                        + "activo, fecha_actualizacion) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                b.getCodigo(), b.getNombre(), b.getNumeroCelular(), b.getTitular(), b.getQrImagen(),
                b.getInstrucciones(), b.getActivo(), b.getFechaActualizacion());
    }

    @Override
    public int actualizar(Billetera b) {
        return Db.update("UPDATE billetera SET nombre = ?, numero_celular = ?, titular = ?, qr_imagen = ?, "
                        + "instrucciones = ?, activo = ?, fecha_actualizacion = ? WHERE id_billetera = ?",
                b.getNombre(), b.getNumeroCelular(), b.getTitular(), b.getQrImagen(), b.getInstrucciones(),
                b.getActivo(), b.getFechaActualizacion(), b.getIdBilletera());
    }
}
