package com.utp.sistemafinanciero.dao.memoria;

import com.utp.sistemafinanciero.model.operaciones.Operacion;
import com.utp.sistemafinanciero.security.Pbkdf2PasswordHasher;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Datos de demostración (RF-GEN: cada módulo muestra 100 registros).
 * Se carga la semilla normal (pocas filas) y luego los datos masivos.
 */
public class DatosMasivosTest {

    private MemoriaDaoFactory bd;
    private Pbkdf2PasswordHasher hasher;

    @Before
    public void preparar() {
        hasher = new Pbkdf2PasswordHasher(1000);
        bd = new MemoriaDaoFactory();
        DatosSemilla.cargar(bd, hasher);
        DatosMasivos.cargar(bd, hasher);
    }

    private long operaciones(String tipo) {
        return bd.operaciones().listar().stream().filter(o -> tipo.equals(o.getTipoOperacion())).count();
    }

    @Test
    public void cadaModuloAlcanzaCienRegistros() {
        assertEquals(100, bd.roles().listar().size());
        assertEquals(100, bd.usuarios().listar().size());
        assertEquals(100, bd.servicios().listar().size());
        assertEquals(100, bd.clientes().listar().size());
        assertEquals(100, bd.tiposCambio().listar().size());
        assertEquals(100, bd.prestamos().listar().size());
        assertEquals(100, bd.empenos().listar().size());
        assertEquals(100, bd.articulos().listar().size());
        assertEquals(100, bd.ventas().listar().size());
        assertEquals(100, bd.productos().listar().size());
        assertEquals(100, bd.proveedores().listar().size());
        assertEquals(100, bd.prospectos().listar().size());
        assertEquals(100, bd.campanias().listar().size());
        assertEquals(100, bd.lineasProduccion().listar().size());
        assertEquals(100, bd.ordenesProduccion().listar().size());
        assertEquals(100, bd.expedientes().listar().size());
    }

    @Test
    public void operacionesSonCienPorTipo() {
        assertEquals(100, operaciones("CAMBIO"));
        assertEquals(100, operaciones("PRESTAMO"));
        assertEquals(100, operaciones("EMPENO"));
        assertEquals(300, bd.operaciones().listar().size());
    }

    @Test
    public void cargarDosVecesNoDuplicaRegistros() {
        DatosMasivos.cargar(bd, hasher);
        assertEquals(100, bd.clientes().listar().size());
        assertEquals(300, bd.operaciones().listar().size());
        assertEquals(100, bd.ventas().listar().size());
    }

    @Test
    public void prestamosYEmpenosApuntanAOperacionesDeSuTipo() {
        List<Operacion> ops = bd.operaciones().listar();
        Set<Integer> idPrestamo = ops.stream().filter(o -> "PRESTAMO".equals(o.getTipoOperacion()))
                .map(Operacion::getIdOperacion).collect(Collectors.toSet());
        Set<Integer> idEmpeno = ops.stream().filter(o -> "EMPENO".equals(o.getTipoOperacion()))
                .map(Operacion::getIdOperacion).collect(Collectors.toSet());
        bd.prestamos().listar().forEach(p -> assertTrue(idPrestamo.contains(p.getIdOperacion())));
        bd.empenos().listar().forEach(e -> assertTrue(idEmpeno.contains(e.getIdOperacion())));
    }

    @Test
    public void lasVentasYArticulosApuntanAClientesExistentes() {
        Set<Integer> clientes = bd.clientes().listar().stream().map(c -> c.getIdCliente()).collect(Collectors.toSet());
        bd.ventas().listar().forEach(v -> assertTrue(clientes.contains(v.getIdCliente())));
        bd.articulos().listar().forEach(a -> assertTrue(clientes.contains(a.getIdCliente())));
    }

    @Test
    public void ordenTerminadaTieneAvanceCien() {
        bd.ordenesProduccion().listar().stream().filter(o -> "TERMINADA".equals(o.getEstado()))
                .forEach(o -> assertEquals(0, o.getAvance().compareTo(new java.math.BigDecimal("100"))));
    }

    @Test
    public void hayProductosConStockBajoParaLaAlerta() {
        long bajos = bd.productos().listar().stream().filter(p -> p.getStockActual() <= p.getStockMinimo()).count();
        assertTrue("debe haber productos en alerta de stock", bajos > 0);
    }

    @Test
    public void elClienteGeneradoPuedeEntrarAlPortalConSuDocumento() {
        int id = bd.clientes().listar().stream().filter(c -> "71000001".equals(c.getDocumento()))
                .findFirst().orElseThrow().getIdCliente();
        assertTrue(hasher.verificar("71000001", bd.accesosCliente().passwordDe(id)));
    }

    @Test
    public void interpretaFechasRelativasDelArchivo() {
        LocalDate hoy = LocalDate.of(2026, 10, 25);
        assertEquals(LocalDate.of(2026, 10, 13), DatosMasivos.fecha("D-12", hoy));
        assertEquals(LocalDate.of(2026, 10, 30), DatosMasivos.fecha("D+5", hoy));
        assertNull(DatosMasivos.fecha(null, hoy));
        LocalDateTime ahora = LocalDateTime.of(2026, 10, 25, 10, 0);
        assertEquals(LocalDateTime.of(2026, 10, 22, 4, 40), DatosMasivos.instante("T-3-5-20", ahora));
    }
}
