package com.utp.sistemafinanciero.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.utp.sistemafinanciero.dao.DaoFactory;
import com.utp.sistemafinanciero.dto.IndicadoresDTO;

import java.math.BigDecimal;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Calcula los indicadores (KPI) del panel de inicio.
 *
 * <p>Cada indicador se calcula de forma independiente: si una consulta
 * falla, el resto se sigue mostrando (RNF-GEN-07).</p>
 */
public class DashboardService {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardService.class);

    private final DaoFactory bd;

    public DashboardService(DaoFactory bd) {
        this.bd = bd;
    }

    public IndicadoresDTO indicadores() {
        IndicadoresDTO d = new IndicadoresDTO();
        calcular(bd.ventas()::ingresosDelMes, d::setIngresosDelMes, BigDecimal.ZERO);
        calcular(bd.clientes()::contar, d::setClientesActivos, 0);
        calcular(() -> bd.clientes().listar().size(), d::setClientesTotales, 0);
        calcular(bd.operaciones()::contarHoy, d::setOperacionesHoy, 0);
        calcular(bd.operaciones()::totalMonto, d::setMontoOperacionesMes, BigDecimal.ZERO);
        calcular(bd.prestamos()::totalColocado, d::setPrestamosColocados, BigDecimal.ZERO);
        calcular(bd.productos()::contarStockBajo, d::setProductosStockBajo, 0);
        calcular(bd.expedientes()::contarPendientes, d::setExpedientesPendientes, 0);
        calcular(bd.usuarios()::contar, d::setUsuariosTotales, 0);
        calcular(() -> bd.operaciones().solicitudes("PENDIENTE").size(), d::setSolicitudesPendientes, 0);
        calcular(() -> bd.ventas().listar().size(), d::setVentasTotales, 0);
        return d;
    }

    /** Ejecuta un cálculo y, si falla, deja el valor por defecto. */
    private <T> void calcular(Supplier<T> calculo, Consumer<T> destino, T porDefecto) {
        try {
            T v = calculo.get();
            destino.accept(v == null ? porDefecto : v);
        } catch (Exception e) {
            LOG.warn("Indicador no disponible: {}", e.getMessage());
            destino.accept(porDefecto);
        }
    }
}
