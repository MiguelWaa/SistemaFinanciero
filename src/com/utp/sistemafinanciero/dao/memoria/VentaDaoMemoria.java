package com.utp.sistemafinanciero.dao.memoria;

import com.utp.sistemafinanciero.dao.VentaDao;
import com.utp.sistemafinanciero.dto.VentaReporteDTO;
import com.utp.sistemafinanciero.model.clientes.Cliente;
import com.utp.sistemafinanciero.model.clientes.ServicioFinanciero;
import com.utp.sistemafinanciero.model.ventas.Venta;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/** Implementación en memoria del {@link VentaDao}. */
public class VentaDaoMemoria extends RepositorioMemoria<Venta> implements VentaDao {

    private final MemoriaDaoFactory bd;

    public VentaDaoMemoria(MemoriaDaoFactory bd) {
        super(Venta::getIdVenta, Venta::setIdVenta, true);
        this.bd = bd;
    }

    @Override
    public BigDecimal ingresosDelMes() {
        YearMonth mes = YearMonth.now();
        BigDecimal total = BigDecimal.ZERO;
        for (Venta v : valores()) {
            if (v.getFechaVenta() != null && YearMonth.from(v.getFechaVenta()).equals(mes)
                    && v.getImporte() != null) {
                total = total.add(v.getImporte());
            }
        }
        return total;
    }

    @Override
    public List<VentaReporteDTO> reporte() {
        List<Venta> ventas = listar();
        java.util.Collections.reverse(ventas); // ORDER BY id_venta (ascendente)
        List<VentaReporteDTO> filas = new ArrayList<>();
        for (Venta v : ventas) {
            Cliente c = v.getIdCliente() == null ? null : bd.clientes().buscar(v.getIdCliente());
            ServicioFinanciero s = v.getIdServicio() == null ? null : bd.servicios().buscar(v.getIdServicio());
            filas.add(new VentaReporteDTO(v.getIdVenta(),
                    c == null ? "" : (c.getNombres() + " " + (c.getApellidos() == null ? "" : c.getApellidos())).trim(),
                    s == null ? "" : s.getNombre(),
                    v.getFechaVenta(), v.getImporte() == null ? BigDecimal.ZERO : v.getImporte(),
                    v.getEstado() == null ? "" : v.getEstado()));
        }
        return filas;
    }
}
