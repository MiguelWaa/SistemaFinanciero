package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.memoria.ClienteDaoMemoria;
import com.utp.sistemafinanciero.model.clientes.Cliente;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/** RF-CLI-03/04/06 · RNF-CLI-01 · Mantenimiento de clientes. */
public class ClienteServiceTest {

    private ClienteService servicio;

    @Before
    public void preparar() {
        servicio = new ClienteService(new ClienteDaoMemoria());
    }

    private static Cliente cliente(String nombres, String documento) {
        Cliente c = new Cliente();
        c.setNombres(nombres);
        c.setDocumento(documento);
        return c;
    }

    @Test
    public void registraClienteActivoConFechaAutomatica() {
        Cliente c = servicio.crear(cliente("Juan", "70000001"));
        assertNotNull(c.getIdCliente());
        assertEquals("ACTIVO", c.getEstado());
        assertNotNull(c.getFechaRegistro());
    }

    @Test
    public void noPermiteDocumentoRepetido() {
        servicio.crear(cliente("Juan", "70000001"));
        ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
                () -> servicio.crear(cliente("Pedro", "70000001")));
        assertTrue(e.getMessage().contains("Ya existe un cliente con el documento"));
    }

    @Test
    public void nombresYDocumentoSonObligatorios() {
        assertThrows(ReglaNegocioException.class, () -> servicio.crear(cliente("", "123")));
        assertThrows(ReglaNegocioException.class, () -> servicio.crear(cliente("Ana", null)));
    }

    @Test
    public void actualizaYEliminaCliente() {
        Cliente c = servicio.crear(cliente("Juan", "70000001"));
        Cliente cambio = cliente("Juan Carlos", "70000001");
        servicio.actualizar(c.getIdCliente(), cambio);
        assertEquals("Juan Carlos", servicio.buscar(c.getIdCliente()).getNombres());
        servicio.eliminar(c.getIdCliente());
        assertThrows(ReglaNegocioException.class, () -> servicio.buscar(c.getIdCliente()));
    }

    @Test
    public void bloqueaYActivaLaCuentaDelPortal() {
        Cliente c = servicio.crear(cliente("Juan", "70000001"));
        assertEquals("BLOQUEADO", servicio.cambiarEstado(c.getIdCliente(), "bloqueado").getEstado());
        assertThrows(ReglaNegocioException.class, () -> servicio.cambiarEstado(c.getIdCliente(), "X"));
    }
}
