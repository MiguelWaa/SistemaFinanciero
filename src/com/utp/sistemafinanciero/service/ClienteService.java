package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.ClienteDao;
import com.utp.sistemafinanciero.model.clientes.Cliente;

import java.time.LocalDateTime;
import java.util.List;

/** Reglas de negocio para la gestion de clientes. */
public class ClienteService implements ServicioCrud<Cliente, Cliente> {

    private final ClienteDao dao;

    /** El DAO se recibe por constructor (inyección de dependencias, DIP). */
    public ClienteService(ClienteDao dao) {
        this.dao = dao;
    }

    public List<Cliente> listar() {
        return dao.listar();
    }

    public Cliente buscar(int id) {
        Cliente c = dao.buscar(id);
        if (c == null) {
            throw new ReglaNegocioException("No existe el cliente con id " + id + ".");
        }
        return c;
    }

    public Cliente crear(Cliente c) {
        validar(c);
        if (c.getDocumento() != null && dao.buscarPorDocumento(c.getDocumento()) != null) {
            throw new ReglaNegocioException("Ya existe un cliente con el documento " + c.getDocumento() + ".");
        }
        if (c.getEstado() == null || c.getEstado().isBlank()) {
            c.setEstado("ACTIVO");
        }
        c.setFechaRegistro(LocalDateTime.now());
        c.setIdCliente(dao.crear(c));
        return c;
    }

    public Cliente actualizar(int id, Cliente c) {
        if (dao.buscar(id) == null) {
            throw new ReglaNegocioException("No existe el cliente con id " + id + ".");
        }
        validar(c);
        Cliente otro = dao.buscarPorDocumento(c.getDocumento());
        if (otro != null && otro.getIdCliente() != id) {
            throw new ReglaNegocioException("Ya existe otro cliente con el documento " + c.getDocumento() + ".");
        }
        c.setIdCliente(id);
        dao.actualizar(c);
        return c;
    }

    public void eliminar(int id) {
        if (dao.eliminar(id) == 0) {
            throw new ReglaNegocioException("No existe el cliente con id " + id + ".");
        }
    }

    private void validar(Cliente c) {
        if (c.getNombres() == null || c.getNombres().isBlank()) {
            throw new ReglaNegocioException("Los nombres son obligatorios.");
        }
        if (c.getDocumento() == null || c.getDocumento().isBlank()) {
            throw new ReglaNegocioException("El documento es obligatorio.");
        }
    }

    /** Activa o bloquea la cuenta de un cliente. */
    public Cliente cambiarEstado(int id, String estado) {
        if (estado == null || !java.util.Set.of("ACTIVO", "BLOQUEADO", "INACTIVO").contains(estado.toUpperCase())) {
            throw new ReglaNegocioException("Estado de cliente no válido. Use ACTIVO, BLOQUEADO o INACTIVO.");
        }
        estado = estado.toUpperCase();
        Cliente c = dao.buscar(id);
        if (c == null) {
            throw new ReglaNegocioException("No existe el cliente con id " + id + ".");
        }
        c.setEstado(estado);
        dao.actualizar(c);
        return c;
    }
}
