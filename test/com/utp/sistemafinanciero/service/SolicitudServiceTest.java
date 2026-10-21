package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.memoria.MemoriaDaoFactory;
import com.utp.sistemafinanciero.model.operaciones.Operacion;
import com.utp.sistemafinanciero.model.operaciones.Prestamo;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.Assert.*;

/** RF-SOL-01/02 · Bandeja de solicitudes del Portal. */
public class SolicitudServiceTest {

    private MemoriaDaoFactory bd;
    private SolicitudService servicio;
    private int idOperacion;
    private int idPrestamo;

    @Before
    public void preparar() {
        bd = new MemoriaDaoFactory();
        servicio = new SolicitudService(bd.operaciones(), bd.prestamos(), bd.empenos());
        idOperacion = bd.operaciones().crear(new Operacion(null, 1, null, "PRESTAMO", "PEN", "PEN",
                new BigDecimal("2000"), null, null, null, "PENDIENTE", LocalDateTime.now()));
        Prestamo p = new Prestamo();
        p.setIdOperacion(idOperacion);
        p.setMontoPrestamo(new BigDecimal("2000"));
        p.setEstado("PENDIENTE");
        idPrestamo = bd.prestamos().crear(p);
    }

    @Test
    public void listaSoloPendientes() {
        assertEquals(1, servicio.listar("pendiente").size());
        assertEquals(new BigDecimal("2000"), servicio.listar(null).get(0).getMonto());
    }

    @Test
    public void aprobarCompletaLaOperacionYActivaElPrestamo() {
        servicio.aprobar(idOperacion);
        assertEquals("COMPLETADA", bd.operaciones().buscar(idOperacion).getEstado());
        assertEquals("ACTIVO", bd.prestamos().buscar(idPrestamo).getEstado());
    }

    @Test
    public void rechazarAnulaElPrestamo() {
        servicio.rechazar(idOperacion);
        assertEquals("ANULADO", bd.prestamos().buscar(idPrestamo).getEstado());
    }

    @Test
    public void noSePuedeResolverDosVeces() {
        servicio.aprobar(idOperacion);
        assertThrows(ReglaNegocioException.class, () -> servicio.rechazar(idOperacion));
    }
}
