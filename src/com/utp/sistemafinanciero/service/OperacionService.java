package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.OperacionDao;
import com.utp.sistemafinanciero.dao.TipoCambioDao;
import com.utp.sistemafinanciero.model.operaciones.TipoCambio;
import com.utp.sistemafinanciero.model.operaciones.Operacion;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Reglas de negocio para las operaciones financieras (cambio de moneda,
 * registro de prestamos/empenos como operacion base, etc.).
 *
 * <p>Para una operacion de cambio calcula automaticamente el total a
 * entregar y a recibir a partir del monto y el tipo de cambio.</p>
 */
public class OperacionService {

    private static final java.util.Set<String> ESTADOS = java.util.Set.of("COMPLETADA", "ANULADA", "PENDIENTE");

    private final OperacionDao dao;
    private final TipoCambioDao tipoCambioDao;

    /** Los DAO se reciben por constructor (inyección de dependencias, DIP). */
    public OperacionService(OperacionDao dao, TipoCambioDao tipoCambioDao) {
        this.dao = dao;
        this.tipoCambioDao = tipoCambioDao;
    }

    public List<Operacion> listar() {
        return dao.listar();
    }

    public List<Operacion> listarPorCliente(int idCliente) {
        return dao.listarPorCliente(idCliente);
    }

    public Operacion buscar(int id) {
        Operacion o = dao.buscar(id);
        if (o == null) {
            throw new ReglaNegocioException("No existe la operacion con id " + id + ".");
        }
        return o;
    }

    public Operacion crear(Operacion o) {
        if (o.getIdCliente() == null) {
            throw new ReglaNegocioException("La operacion debe estar asociada a un cliente.");
        }
        if (o.getMonto() == null || o.getMonto().signum() <= 0) {
            throw new ReglaNegocioException("El monto debe ser mayor que cero.");
        }
        if (o.getTipoOperacion() == null || o.getTipoOperacion().isBlank()) {
            o.setTipoOperacion("CAMBIO");
        }

        // Si se eligió un tipo de cambio registrado, la cotización y las
        // monedas se toman del servidor (no se confía en el navegador).
        if (o.getIdTipoCambio() != null) {
            TipoCambio tc = tipoCambioDao.buscar(o.getIdTipoCambio());
            if (tc == null) {
                throw new ReglaNegocioException("El tipo de cambio seleccionado no existe.");
            }
            o.setTipoCambio(tc.getVenta());
            o.setMonedaOrigen(tc.getMonedaOrigen());
            o.setMonedaDestino(tc.getMonedaDestino());
        }
        // Cálculo del cambio de moneda (RF-OPE-02): total a entregar y a recibir.
        if ("CAMBIO".equalsIgnoreCase(o.getTipoOperacion())) {
            if (o.getTipoCambio() == null || o.getTipoCambio().signum() <= 0) {
                throw new ReglaNegocioException("Indique un tipo de cambio válido.");
            }
            o.setTotalEntregar(o.getMonto());
            o.setTotalRecibir(o.getMonto().multiply(o.getTipoCambio()).setScale(2, RoundingMode.HALF_UP));
        }

        if (o.getEstado() == null || o.getEstado().isBlank()) {
            o.setEstado("COMPLETADA");
        }
        o.setFechaOperacion(LocalDateTime.now());
        o.setIdOperacion(dao.crear(o));
        return o;
    }

    public Operacion cambiarEstado(int id, String estado) {
        if (estado == null || !ESTADOS.contains(estado.toUpperCase())) {
            throw new ReglaNegocioException("Estado no válido. Use COMPLETADA, ANULADA o PENDIENTE.");
        }
        estado = estado.toUpperCase();
        if (dao.buscar(id) == null) {
            throw new ReglaNegocioException("No existe la operacion con id " + id + ".");
        }
        dao.actualizarEstado(id, estado);
        return dao.buscar(id);
    }

    public void eliminar(int id) {
        if (dao.eliminar(id) == 0) {
            throw new ReglaNegocioException("No existe la operacion con id " + id + ".");
        }
    }
}
