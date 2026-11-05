package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.memoria.ExpedienteAprobacionDaoMemoria;
import com.utp.sistemafinanciero.model.direccion.ExpedienteAprobacion;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * v5.5 · Plan de pruebas · Nivel UNITARIO · Dirección / Gerencia.
 * CP-UNI-DIR-01…04 · RF-DIR-01/03: expediente nace PENDIENTE, código obligatorio,
 * solo estados válidos y fecha de resolución al resolver.
 */
public class ExpedienteServiceTest {

    private ExpedienteService servicio;

    @Before
    public void preparar() {
        servicio = new ExpedienteService(new ExpedienteAprobacionDaoMemoria());
    }

    private static ExpedienteAprobacion expediente(String codigo) {
        ExpedienteAprobacion e = new ExpedienteAprobacion();
        e.setCodigo(codigo);
        e.setArea("LOGISTICA");
        e.setSolicitante("Silvia Huamani");
        return e;
    }

    @Test
    public void elExpedienteNacePendienteConFechaDeSolicitud() {
        ExpedienteAprobacion e = servicio.crear(expediente("EXP-T01"));
        assertNotNull(e.getIdExpediente());
        assertEquals("PENDIENTE", e.getEstado());
        assertNotNull(e.getFechaSolicitud());
        assertNull(e.getFechaResolucion());
    }

    @Test
    public void elCodigoEsObligatorio() {
        assertThrows(ReglaNegocioException.class, () -> servicio.crear(expediente("")));
    }

    @Test
    public void resolverRegistraEstadoEnMayusculasYFecha() {
        ExpedienteAprobacion e = servicio.crear(expediente("EXP-T02"));
        ExpedienteAprobacion r = servicio.resolver(e.getIdExpediente(), "aprobado");
        assertEquals("APROBADO", r.getEstado());
        assertNotNull(r.getFechaResolucion());
    }

    @Test
    public void rechazaEstadosNoPermitidos() {
        ExpedienteAprobacion e = servicio.crear(expediente("EXP-T03"));
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> servicio.resolver(e.getIdExpediente(), "BORRADO"));
        assertTrue(ex.getMessage().contains("Estado no válido"));
        assertThrows(ReglaNegocioException.class, () -> servicio.resolver(e.getIdExpediente(), null));
    }
}
