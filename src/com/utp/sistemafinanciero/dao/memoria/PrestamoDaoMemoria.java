package com.utp.sistemafinanciero.dao.memoria;

import com.utp.sistemafinanciero.dao.PrestamoDao;
import com.utp.sistemafinanciero.model.operaciones.Prestamo;

import java.math.BigDecimal;
import java.util.Objects;

/** Implementación en memoria del {@link PrestamoDao}. */
public class PrestamoDaoMemoria extends RepositorioMemoria<Prestamo> implements PrestamoDao {

    public PrestamoDaoMemoria() {
        super(Prestamo::getIdPrestamo, Prestamo::setIdPrestamo, true);
    }

    @Override
    public BigDecimal totalColocado() {
        BigDecimal total = BigDecimal.ZERO;
        for (Prestamo p : valores()) {
            if ("ACTIVO".equals(p.getEstado()) && p.getMontoPrestamo() != null) {
                total = total.add(p.getMontoPrestamo());
            }
        }
        return total;
    }

    @Override
    public int actualizarEstadoPorOperacion(int idOperacion, String estado) {
        return modificar(p -> Objects.equals(p.getIdOperacion(), idOperacion), p -> p.setEstado(estado));
    }

    /** Préstamo asociado a una operación (apoyo para la bandeja de solicitudes). */
    Prestamo porOperacion(int idOperacion) {
        return filtrar(p -> Objects.equals(p.getIdOperacion(), idOperacion)).stream().findFirst().orElse(null);
    }
}
