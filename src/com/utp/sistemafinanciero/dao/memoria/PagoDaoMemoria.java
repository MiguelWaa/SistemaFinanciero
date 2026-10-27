package com.utp.sistemafinanciero.dao.memoria;

import com.utp.sistemafinanciero.dao.PagoDao;
import com.utp.sistemafinanciero.model.clientes.Cliente;
import com.utp.sistemafinanciero.model.pagos.Pago;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Implementación en memoria del {@link PagoDao}. Igual que el DAO JDBC,
 * los listados agregan el nombre del cliente (JOIN) y no incluyen la
 * imagen de la constancia.
 */
public class PagoDaoMemoria extends RepositorioMemoria<Pago> implements PagoDao {

    private final MemoriaDaoFactory bd;

    public PagoDaoMemoria(MemoriaDaoFactory bd) {
        super(Pago::getIdPago, Pago::setIdPago, true);
        this.bd = bd;
    }

    @Override
    public List<Pago> listar() {
        return completar(super.listar());
    }

    @Override
    public Pago buscar(int id) {
        Pago p = super.buscar(id);
        if (p == null) return null;
        completar(List.of(p));
        return p;
    }

    @Override
    public List<Pago> listarPorCliente(int idCliente) {
        return completar(filtrar(p -> Objects.equals(p.getIdCliente(), idCliente)));
    }

    @Override
    public List<Pago> listarPorEstado(String estado) {
        return completar(filtrar(p -> Objects.equals(p.getEstado(), estado)));
    }

    @Override
    public boolean existeNumeroOperacion(String metodo, String numeroOperacion) {
        if (numeroOperacion == null) return false;
        for (Pago p : valores()) {
            if (numeroOperacion.equalsIgnoreCase(p.getNumeroOperacion()) && Objects.equals(metodo, p.getMetodo())) {
                return true;
            }
        }
        return false;
    }

    /** Igual que el UPDATE del DAO JDBC: solo cambia estado y datos de verificación. */
    @Override
    public int actualizar(Pago cambios) {
        Pago actual = cambios.getIdPago() == null ? null : super.buscar(cambios.getIdPago());
        if (actual == null) return 0;
        actual.setEstado(cambios.getEstado());
        actual.setFechaVerificacion(cambios.getFechaVerificacion());
        actual.setVerificadoPor(cambios.getVerificadoPor());
        actual.setObservacion(cambios.getObservacion());
        return super.actualizar(actual);
    }

    @Override
    public String constancia(int idPago) {
        Pago p = super.buscar(idPago);
        return p == null ? null : p.getConstancia();
    }

    /** Solo para los datos de demostración: reparte los pagos en los últimos días. */
    void fijarFecha(int idPago, java.time.LocalDateTime fecha) {
        modificar(p -> Objects.equals(p.getIdPago(), idPago), p -> {
            p.setFechaPago(fecha);
            if (p.getFechaVerificacion() != null) p.setFechaVerificacion(fecha.plusMinutes(12));
        });
    }

    /** Agrega nombre/documento del cliente y quita la imagen (como el SELECT del DAO JDBC). */
    private List<Pago> completar(List<Pago> lista) {
        List<Pago> salida = new ArrayList<>(lista.size());
        for (Pago p : lista) {
            Cliente c = p.getIdCliente() == null ? null : bd.clientes().buscar(p.getIdCliente());
            if (c != null) {
                p.setCliente((c.getNombres() + " " + (c.getApellidos() == null ? "" : c.getApellidos())).trim());
                p.setDocumento(c.getDocumento());
            }
            p.setConConstancia(p.isTieneConstancia());
            p.setConstancia(null); // la imagen se pide aparte con constancia(id)
            salida.add(p);
        }
        return salida;
    }
}
