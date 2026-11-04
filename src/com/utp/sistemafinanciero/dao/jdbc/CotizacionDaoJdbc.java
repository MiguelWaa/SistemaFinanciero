package com.utp.sistemafinanciero.dao.jdbc;

import com.utp.sistemafinanciero.dao.CotizacionDao;
import com.utp.sistemafinanciero.model.pagos.Cotizacion;
import com.utp.sistemafinanciero.util.Db;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Implementación JDBC/PostgreSQL del DAO de la tabla {@code cotizacion}. */
public class CotizacionDaoJdbc implements CotizacionDao {

    private static final String SELECT =
            "SELECT q.id_cotizacion, q.id_cliente, q.tipo, q.monto, q.moneda, q.plazo, q.unidad_plazo, q.tasa, "
            + "q.cuota, q.total_interes, q.total_pagar, q.id_tipo_cambio, q.tipo_cambio, q.monto_recibir, "
            + "q.moneda_recibir, q.descripcion, q.valor_estimado, q.estado, q.fecha_emision, q.fecha_vencimiento, "
            + "q.id_operacion, q.origen, c.nombres, c.apellidos, c.documento "
            + "FROM cotizacion q LEFT JOIN cliente c ON c.id_cliente = q.id_cliente ";

    private static final Db.RowMapper<Cotizacion> MAPPER = rs -> {
        Cotizacion q = new Cotizacion();
        q.setIdCotizacion(rs.getObject("id_cotizacion", Integer.class));
        q.setIdCliente(rs.getObject("id_cliente", Integer.class));
        q.setTipo(rs.getString("tipo"));
        q.setMonto(rs.getObject("monto", BigDecimal.class));
        q.setMoneda(rs.getString("moneda"));
        q.setPlazo(rs.getObject("plazo", Integer.class));
        q.setUnidadPlazo(rs.getString("unidad_plazo"));
        q.setTasa(rs.getObject("tasa", BigDecimal.class));
        q.setCuota(rs.getObject("cuota", BigDecimal.class));
        q.setTotalInteres(rs.getObject("total_interes", BigDecimal.class));
        q.setTotalPagar(rs.getObject("total_pagar", BigDecimal.class));
        q.setIdTipoCambio(rs.getObject("id_tipo_cambio", Integer.class));
        q.setTipoCambio(rs.getObject("tipo_cambio", BigDecimal.class));
        q.setMontoRecibir(rs.getObject("monto_recibir", BigDecimal.class));
        q.setMonedaRecibir(rs.getString("moneda_recibir"));
        q.setDescripcion(rs.getString("descripcion"));
        q.setValorEstimado(rs.getObject("valor_estimado", BigDecimal.class));
        q.setEstado(rs.getString("estado"));
        q.setFechaEmision(rs.getObject("fecha_emision", LocalDateTime.class));
        q.setFechaVencimiento(rs.getObject("fecha_vencimiento", LocalDate.class));
        q.setIdOperacion(rs.getObject("id_operacion", Integer.class));
        q.setOrigen(rs.getString("origen"));
        String nombres = rs.getString("nombres");
        String apellidos = rs.getString("apellidos");
        if (nombres != null) {
            q.setCliente((nombres + " " + (apellidos == null ? "" : apellidos)).trim());
        }
        q.setDocumento(rs.getString("documento"));
        return q;
    };

    @Override
    public List<Cotizacion> listar() {
        return Db.query(SELECT + "ORDER BY q.id_cotizacion DESC LIMIT 5000", MAPPER);
    }

    @Override
    public Cotizacion buscar(int id) {
        return Db.queryOne(SELECT + "WHERE q.id_cotizacion = ?", MAPPER, id);
    }

    @Override
    public List<Cotizacion> listarPorCliente(int idCliente) {
        return Db.query(SELECT + "WHERE q.id_cliente = ? ORDER BY q.id_cotizacion DESC", MAPPER, idCliente);
    }

    @Override
    public Integer crear(Cotizacion q) {
        return Db.insert("INSERT INTO cotizacion (id_cliente, tipo, monto, moneda, plazo, unidad_plazo, tasa, cuota, "
                        + "total_interes, total_pagar, id_tipo_cambio, tipo_cambio, monto_recibir, moneda_recibir, "
                        + "descripcion, valor_estimado, estado, fecha_emision, fecha_vencimiento, id_operacion, origen) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                q.getIdCliente(), q.getTipo(), q.getMonto(), q.getMoneda(), q.getPlazo(), q.getUnidadPlazo(),
                q.getTasa(), q.getCuota(), q.getTotalInteres(), q.getTotalPagar(), q.getIdTipoCambio(),
                q.getTipoCambio(), q.getMontoRecibir(), q.getMonedaRecibir(), q.getDescripcion(),
                q.getValorEstimado(), q.getEstado(), q.getFechaEmision(), q.getFechaVencimiento(),
                q.getIdOperacion(), q.getOrigen());
    }

    @Override
    public int actualizar(Cotizacion q) {
        return Db.update("UPDATE cotizacion SET estado = ?, id_operacion = ? WHERE id_cotizacion = ?",
                q.getEstado(), q.getIdOperacion(), q.getIdCotizacion());
    }
}
