package com.utp.sistemafinanciero.dao;

import com.utp.sistemafinanciero.dto.VentaReporteDTO;
import com.utp.sistemafinanciero.model.ventas.Venta;
import java.math.BigDecimal;
import java.util.List;

/**
 * Contrato DAO (Data Access Object) para la tabla {@code venta}.
 *
 * <p>La capa de servicios depende de esta interfaz y no de una
 * implementación concreta (principio de inversión de dependencias, DIP).
 * Implementaciones: {@code dao.jdbc.VentaDaoJdbc} (PostgreSQL) y
 * {@code dao.memoria.VentaDaoMemoria} (pruebas / demostración).</p>
 */
public interface VentaDao extends CrudDao<Venta> {
    /** Ingresos (importe) del mes actual. */
    BigDecimal ingresosDelMes();

    /**
     * Filas del reporte de ventas: incluye el nombre del cliente y del
     * servicio (JOIN) en lugar de solo sus identificadores.
     */
    List<VentaReporteDTO> reporte();
}
