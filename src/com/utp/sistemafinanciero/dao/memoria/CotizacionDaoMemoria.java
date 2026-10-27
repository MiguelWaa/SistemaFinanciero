package com.utp.sistemafinanciero.dao.memoria;

import com.utp.sistemafinanciero.dao.CotizacionDao;
import com.utp.sistemafinanciero.model.clientes.Cliente;
import com.utp.sistemafinanciero.model.pagos.Cotizacion;

import java.util.List;
import java.util.Objects;

/** Implementación en memoria del {@link CotizacionDao}. */
public class CotizacionDaoMemoria extends RepositorioMemoria<Cotizacion> implements CotizacionDao {

    private final MemoriaDaoFactory bd;

    public CotizacionDaoMemoria(MemoriaDaoFactory bd) {
        super(Cotizacion::getIdCotizacion, Cotizacion::setIdCotizacion, true);
        this.bd = bd;
    }

    @Override
    public List<Cotizacion> listar() {
        return completar(super.listar());
    }

    @Override
    public Cotizacion buscar(int id) {
        Cotizacion c = super.buscar(id);
        if (c != null) completar(List.of(c));
        return c;
    }

    @Override
    public List<Cotizacion> listarPorCliente(int idCliente) {
        return completar(filtrar(c -> Objects.equals(c.getIdCliente(), idCliente)));
    }

    private List<Cotizacion> completar(List<Cotizacion> lista) {
        for (Cotizacion q : lista) {
            Cliente c = q.getIdCliente() == null ? null : bd.clientes().buscar(q.getIdCliente());
            if (c != null) {
                q.setCliente((c.getNombres() + " " + (c.getApellidos() == null ? "" : c.getApellidos())).trim());
                q.setDocumento(c.getDocumento());
            }
        }
        return lista;
    }
}
