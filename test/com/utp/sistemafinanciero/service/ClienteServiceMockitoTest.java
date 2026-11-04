package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.ClienteDao;
import com.utp.sistemafinanciero.model.clientes.Cliente;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Plan de Pruebas · PU-CLI-M · ClienteService con <b>dobles de prueba
 * (Mockito)</b>. A diferencia de las pruebas con DAO en memoria, aquí se
 * verifica la <i>interacción</i> del servicio con el DAO: qué métodos
 * llama, cuántas veces y con qué datos (y cuáles NO llama).
 */
public class ClienteServiceMockitoTest {

    private ClienteDao dao;
    private ClienteService servicio;

    @Before
    public void preparar() {
        dao = mock(ClienteDao.class);
        servicio = new ClienteService(dao);
    }

    private static Cliente cliente(String nombres, String documento) {
        Cliente c = new Cliente();
        c.setNombres(nombres);
        c.setDocumento(documento);
        return c;
    }

    @Test
    public void crearGuardaUnaSolaVezConEstadoActivoYFecha() {
        when(dao.buscarPorDocumento("71234567")).thenReturn(null);
        when(dao.crear(any(Cliente.class))).thenReturn(41);

        Cliente creado = servicio.crear(cliente("Rosa", "71234567"));

        ArgumentCaptor<Cliente> enviado = ArgumentCaptor.forClass(Cliente.class);
        verify(dao, times(1)).crear(enviado.capture());
        assertEquals("ACTIVO", enviado.getValue().getEstado());
        assertNotNull(enviado.getValue().getFechaRegistro());
        assertEquals(Integer.valueOf(41), creado.getIdCliente());
    }

    @Test
    public void documentoRepetidoNuncaLlegaAlDao() {
        when(dao.buscarPorDocumento("70000001")).thenReturn(cliente("Ya", "70000001"));

        assertThrows(ReglaNegocioException.class, () -> servicio.crear(cliente("Otro", "70000001")));
        verify(dao, never()).crear(any());
    }

    @Test
    public void datosIncompletosNiSiquieraConsultanLaBase() {
        assertThrows(ReglaNegocioException.class, () -> servicio.crear(cliente("  ", "71234567")));
        verifyNoMoreInteractions(dao);   // ninguna llamada al DAO
    }

    @Test
    public void estadoInicialInventadoSeRechaza() {
        Cliente c = cliente("Rosa", "71234567");
        c.setEstado("VIP_SIN_CONTROL");
        assertThrows(ReglaNegocioException.class, () -> servicio.crear(c));
        verify(dao, never()).crear(any());
    }

    @Test
    public void actualizarClienteInexistenteNoEscribe() {
        when(dao.buscar(99)).thenReturn(null);
        assertThrows(ReglaNegocioException.class, () -> servicio.actualizar(99, cliente("Rosa", "71234567")));
        verify(dao, never()).actualizar(any());
    }

    @Test
    public void cambiarEstadoNormalizaAMayusculas() {
        Cliente existente = cliente("Rosa", "71234567");
        existente.setIdCliente(5);
        existente.setEstado("ACTIVO");
        when(dao.buscar(5)).thenReturn(existente);

        servicio.cambiarEstado(5, "bloqueado");

        ArgumentCaptor<Cliente> enviado = ArgumentCaptor.forClass(Cliente.class);
        verify(dao).actualizar(enviado.capture());
        assertEquals("BLOQUEADO", enviado.getValue().getEstado());
    }

    @Test
    public void eliminarInexistenteInformaError() {
        when(dao.eliminar(7)).thenReturn(0);
        assertThrows(ReglaNegocioException.class, () -> servicio.eliminar(7));
        verify(dao).eliminar(7);
    }
}
