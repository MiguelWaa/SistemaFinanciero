package com.utp.sistemafinanciero.dao;

import com.utp.sistemafinanciero.dao.memoria.ClienteDaoMemoria;
import com.utp.sistemafinanciero.model.clientes.Cliente;
import org.junit.Test;

import static org.junit.Assert.*;

/** Contrato del patrón DAO: la implementación en memoria se comporta como una tabla. */
public class RepositorioMemoriaTest {

    @Test
    public void asignaIdsCorrelativosComoSerial() {
        ClienteDao dao = new ClienteDaoMemoria();
        Cliente c = new Cliente();
        c.setNombres("A");
        assertEquals(Integer.valueOf(1), dao.crear(c));
        assertEquals(Integer.valueOf(2), dao.crear(c));
    }

    @Test
    public void modificarUnObjetoLeidoNoAlteraLoGuardadoHastaActualizar() {
        ClienteDao dao = new ClienteDaoMemoria();
        Cliente c = new Cliente();
        c.setNombres("Original");
        int id = dao.crear(c);
        Cliente leido = dao.buscar(id);
        leido.setNombres("Cambiado");
        assertEquals("Original", dao.buscar(id).getNombres());
        assertEquals(1, dao.actualizar(leido));
        assertEquals("Cambiado", dao.buscar(id).getNombres());
    }

    @Test
    public void eliminarDevuelveCeroSiNoExiste() {
        assertEquals(0, new ClienteDaoMemoria().eliminar(99));
    }
}
