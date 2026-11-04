package com.utp.sistemafinanciero.integracion;

import com.utp.sistemafinanciero.dao.ClienteDao;
import com.utp.sistemafinanciero.model.clientes.Cliente;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.Assert.*;

/**
 * Plan de Pruebas · PI-DAO · <b>Prueba de contrato del patrón DAO</b>.
 *
 * <p>Las mismas pruebas se ejecutan contra las dos implementaciones de
 * {@link ClienteDao} (memoria y JDBC/PostgreSQL). Si ambas pasan, son
 * intercambiables para los servicios: es la comprobación práctica del
 * principio de sustitución de Liskov (SOLID) y de la Abstract Factory.</p>
 */
public abstract class ContratoClienteDao {

    protected ClienteDao dao;
    private Integer creado;

    /** Cada subclase entrega su implementación. */
    protected abstract ClienteDao crearDao();

    @Before
    public void preparar() {
        dao = crearDao();
    }

    @After
    public void limpiar() {
        if (creado != null) {
            dao.eliminar(creado);
        }
    }

    private Cliente nuevo() {
        Cliente c = new Cliente();
        c.setTipoDocumento("DNI");
        c.setDocumento("9" + BdPruebas.unico());
        c.setNombres("Prueba Contrato");
        c.setApellidos("DAO");
        c.setCorreo("contrato@sigsf.test");
        c.setEstado("ACTIVO");
        c.setFechaRegistro(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        return c;
    }

    @Test
    public void crearDevuelveIdYBuscarRecuperaLosMismosDatos() {
        Cliente c = nuevo();
        creado = dao.crear(c);
        assertNotNull(creado);
        Cliente leido = dao.buscar(creado);
        assertEquals(c.getDocumento(), leido.getDocumento());
        assertEquals("Prueba Contrato", leido.getNombres());
        assertEquals("ACTIVO", leido.getEstado());
    }

    @Test
    public void buscarPorDocumentoEncuentraAlCliente() {
        Cliente c = nuevo();
        creado = dao.crear(c);
        assertEquals(creado, dao.buscarPorDocumento(c.getDocumento()).getIdCliente());
        assertNull(dao.buscarPorDocumento("documento-que-no-existe"));
    }

    @Test
    public void actualizarPersisteLosCambios() {
        Cliente c = nuevo();
        creado = dao.crear(c);
        c.setIdCliente(creado);
        c.setTelefono("987654321");
        c.setEstado("BLOQUEADO");
        assertEquals(1, dao.actualizar(c));
        Cliente leido = dao.buscar(creado);
        assertEquals("987654321", leido.getTelefono());
        assertEquals("BLOQUEADO", leido.getEstado());
    }

    @Test
    public void eliminarDevuelveFilasAfectadas() {
        creado = dao.crear(nuevo());
        assertEquals(1, dao.eliminar(creado));
        assertNull(dao.buscar(creado));
        assertEquals(0, dao.eliminar(creado));
        creado = null;
    }

    @Test
    public void contarYListarReflejanElAlta() {
        int antes = dao.contar();
        creado = dao.crear(nuevo());
        assertEquals(antes + 1, dao.contar());
        assertTrue(dao.listar().stream().anyMatch(x -> creado.equals(x.getIdCliente())));
    }
}
