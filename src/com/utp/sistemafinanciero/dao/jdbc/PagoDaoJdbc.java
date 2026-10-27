package com.utp.sistemafinanciero.dao.jdbc;

import com.utp.sistemafinanciero.dao.PagoDao;
import com.utp.sistemafinanciero.model.pagos.Pago;
import com.utp.sistemafinanciero.util.Db;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación JDBC/PostgreSQL del DAO de la tabla {@code pago}.
 * Los listados hacen JOIN con {@code cliente} y no leen la columna
 * {@code constancia} (imagen), que se consulta aparte.
 */
public class PagoDaoJdbc implements PagoDao {

    /** Tope de filas de los listados generales (protege al servidor con mucho volumen). */
    private static final int MAX_FILAS = 5000;

    private static final String SELECT =
            "SELECT p.id_pago, p.id_cliente, p.id_operacion, p.concepto, p.descripcion, p.metodo, p.monto, "
            + "p.moneda, p.numero_operacion, p.celular_origen, p.referencia, p.origen, p.estado, p.fecha_pago, "
            + "p.fecha_verificacion, p.verificado_por, p.observacion, (p.constancia IS NOT NULL) AS con_constancia, "
            + "c.nombres, c.apellidos, c.documento "
            + "FROM pago p LEFT JOIN cliente c ON c.id_cliente = p.id_cliente ";

    private static final Db.RowMapper<Pago> MAPPER = rs -> {
        Pago p = new Pago();
        p.setIdPago(rs.getObject("id_pago", Integer.class));
        p.setIdCliente(rs.getObject("id_cliente", Integer.class));
        p.setIdOperacion(rs.getObject("id_operacion", Integer.class));
        p.setConcepto(rs.getString("concepto"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setMetodo(rs.getString("metodo"));
        p.setMonto(rs.getObject("monto", BigDecimal.class));
        p.setMoneda(rs.getString("moneda"));
        p.setNumeroOperacion(rs.getString("numero_operacion"));
        p.setCelularOrigen(rs.getString("celular_origen"));
        p.setReferencia(rs.getString("referencia"));
        p.setOrigen(rs.getString("origen"));
        p.setEstado(rs.getString("estado"));
        p.setFechaPago(rs.getObject("fecha_pago", LocalDateTime.class));
        p.setFechaVerificacion(rs.getObject("fecha_verificacion", LocalDateTime.class));
        p.setVerificadoPor(rs.getString("verificado_por"));
        p.setObservacion(rs.getString("observacion"));
        p.setConConstancia(rs.getBoolean("con_constancia"));
        String nombres = rs.getString("nombres");
        String apellidos = rs.getString("apellidos");
        if (nombres != null) {
            p.setCliente((nombres + " " + (apellidos == null ? "" : apellidos)).trim());
        }
        p.setDocumento(rs.getString("documento"));
        return p;
    };

    @Override
    public List<Pago> listar() {
        return Db.query(SELECT + "ORDER BY p.id_pago DESC LIMIT " + MAX_FILAS, MAPPER);
    }

    @Override
    public Pago buscar(int idPago) {
        return Db.queryOne(SELECT + "WHERE p.id_pago = ?", MAPPER, idPago);
    }

    @Override
    public List<Pago> listarPorCliente(int idCliente) {
        return Db.query(SELECT + "WHERE p.id_cliente = ? ORDER BY p.id_pago DESC", MAPPER, idCliente);
    }

    @Override
    public List<Pago> listarPorEstado(String estado) {
        return Db.query(SELECT + "WHERE p.estado = ? ORDER BY p.id_pago DESC LIMIT " + MAX_FILAS, MAPPER, estado);
    }

    @Override
    public Integer crear(Pago p) {
        return Db.insert(
                "INSERT INTO pago (id_cliente, id_operacion, concepto, descripcion, metodo, monto, moneda, "
                + "numero_operacion, celular_origen, referencia, origen, estado, fecha_pago, fecha_verificacion, "
                + "verificado_por, observacion, constancia) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                p.getIdCliente(), p.getIdOperacion(), p.getConcepto(), p.getDescripcion(), p.getMetodo(),
                p.getMonto(), p.getMoneda(), p.getNumeroOperacion(), p.getCelularOrigen(), p.getReferencia(),
                p.getOrigen(), p.getEstado(), p.getFechaPago(), p.getFechaVerificacion(), p.getVerificadoPor(),
                p.getObservacion(), p.getConstancia());
    }

    @Override
    public int actualizar(Pago p) {
        return Db.update(
                "UPDATE pago SET estado = ?, fecha_verificacion = ?, verificado_por = ?, observacion = ? "
                + "WHERE id_pago = ?",
                p.getEstado(), p.getFechaVerificacion(), p.getVerificadoPor(), p.getObservacion(), p.getIdPago());
    }

    @Override
    public boolean existeNumeroOperacion(String metodo, String numeroOperacion) {
        Integer n = Db.queryOne(
                "SELECT COUNT(*) AS n FROM pago WHERE metodo = ? AND upper(numero_operacion) = upper(?)",
                rs -> rs.getInt("n"), metodo, numeroOperacion);
        return n != null && n > 0;
    }

    @Override
    public String constancia(int idPago) {
        return Db.queryOne("SELECT constancia FROM pago WHERE id_pago = ?", rs -> rs.getString("constancia"), idPago);
    }
}
