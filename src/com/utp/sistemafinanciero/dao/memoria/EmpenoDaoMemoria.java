package com.utp.sistemafinanciero.dao.memoria;

import com.utp.sistemafinanciero.dao.EmpenoDao;
import com.utp.sistemafinanciero.model.operaciones.Empeno;

import java.util.Objects;

/** Implementación en memoria del {@link EmpenoDao}. */
public class EmpenoDaoMemoria extends RepositorioMemoria<Empeno> implements EmpenoDao {

    public EmpenoDaoMemoria() {
        super(Empeno::getIdEmpeno, Empeno::setIdEmpeno, true);
    }

    @Override
    public int actualizarEstadoPorOperacion(int idOperacion, String estado) {
        return modificar(e -> Objects.equals(e.getIdOperacion(), idOperacion), e -> e.setEstado(estado));
    }

    /** Empeño asociado a una operación (apoyo para la bandeja de solicitudes). */
    Empeno porOperacion(int idOperacion) {
        return filtrar(e -> Objects.equals(e.getIdOperacion(), idOperacion)).stream().findFirst().orElse(null);
    }
}
