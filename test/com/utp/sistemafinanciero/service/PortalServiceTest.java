package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.config.Contenedor;
import com.utp.sistemafinanciero.dto.ClientePublicoDTO;
import com.utp.sistemafinanciero.dto.LoginDTO;
import com.utp.sistemafinanciero.model.clientes.Cliente;
import com.utp.sistemafinanciero.model.operaciones.Operacion;
import com.utp.sistemafinanciero.model.operaciones.Prestamo;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Plan de Pruebas · PU-POR · Portal del Cliente (RF-POR-01…05).
 * Registro, inicio de sesión, trámites y — hallazgo del Avance 4 — que
 * una solicitud rechazada no deje operaciones "huérfanas".
 */
public class PortalServiceTest {

    private Contenedor c;
    private PortalService portal;

    @Before
    public void preparar() throws Exception {
        c = Contenedor.enMemoria(Files.createTempDirectory("sigsf-portal"));
        portal = c.portal;
    }

    private int registrar(String documento) {
        Cliente nuevo = new Cliente();
        nuevo.setDocumento(documento);
        nuevo.setNombres("Ana");
        nuevo.setApellidos("Portal");
        return portal.registrar(nuevo, "clave123").getIdCliente();
    }

    @Test
    public void PU_POR_01_registroCreaClienteActivoYDevuelveToken() {
        Cliente nuevo = new Cliente();
        nuevo.setDocumento("72000001");
        nuevo.setNombres("Ana");
        ClientePublicoDTO dto = portal.registrar(nuevo, "clave123");
        assertNotNull(dto.getToken());
        assertEquals("ACTIVO", c.bd.clientes().buscarPorDocumento("72000001").getEstado());
        assertEquals("DNI", c.bd.clientes().buscarPorDocumento("72000001").getTipoDocumento());
    }

    @Test
    public void PU_POR_02_registroValidaClaveYDocumentoRepetido() {
        Cliente corto = new Cliente();
        corto.setDocumento("72000002");
        corto.setNombres("Ana");
        assertThrows(ReglaNegocioException.class, () -> portal.registrar(corto, "123"));
        registrar("72000003");
        Cliente repetido = new Cliente();
        repetido.setDocumento("72000003");
        repetido.setNombres("Otra");
        assertThrows(ReglaNegocioException.class, () -> portal.registrar(repetido, "clave123"));
    }

    @Test
    public void PU_POR_03_loginCorrectoYMensajeGenericoSiFalla() {
        registrar("72000004");
        assertNotNull(portal.login(new LoginDTO("72000004", "clave123")).getToken());
        ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
                () -> portal.login(new LoginDTO("72000004", "otra-clave")));
        ReglaNegocioException e2 = assertThrows(ReglaNegocioException.class,
                () -> portal.login(new LoginDTO("00000000", "otra-clave")));
        assertEquals("No debe revelar si el documento existe", e.getMessage(), e2.getMessage());
    }

    @Test
    public void PU_POR_04_cambioDeMonedaQuedaPendienteConElTipoDelServidor() {
        int id = registrar("72000005");
        int tc = c.bd.tiposCambio().listar().get(0).getIdTipoCambio();
        Operacion o = portal.solicitarCambio(id, tc, new BigDecimal("100"));
        assertEquals("PENDIENTE", o.getEstado());
        assertEquals(new BigDecimal("100").multiply(o.getTipoCambio()).setScale(2, java.math.RoundingMode.HALF_UP),
                o.getTotalRecibir());
    }

    @Test
    public void PU_POR_05_prestamoValidoCreaOperacionYPrestamoPendientes() {
        int id = registrar("72000006");
        Map<String, Object> r = portal.solicitarPrestamo(id, new BigDecimal("2000"), 12, null);
        Prestamo p = (Prestamo) r.get("prestamo");
        assertEquals("PENDIENTE", p.getEstado());
        assertEquals(0, new BigDecimal("15").compareTo(p.getTasaInteres()));   // tasa por defecto
        assertEquals(1, portal.misOperaciones(id).size());
    }

    @Test
    public void PU_POR_06_prestamoRechazadoNoDejaOperacionHuerfana() {
        // Antes: se grababa la operación PENDIENTE y luego fallaba el préstamo,
        // quedando en la bandeja una solicitud sin préstamo (defecto DEF-007).
        int id = registrar("72000007");
        assertThrows(ReglaNegocioException.class,
                () -> portal.solicitarPrestamo(id, new BigDecimal("2000"), 500, null));   // plazo > 360
        assertThrows(ReglaNegocioException.class,
                () -> portal.solicitarPrestamo(id, new BigDecimal("50"), 12, null));      // monto < 100
        assertEquals(0, portal.misOperaciones(id).size());
    }

    @Test
    public void PU_POR_07_empenoRechazadoNoDejaArticuloNiOperacion() {
        int id = registrar("72000008");
        int articulosAntes = c.bd.articulos().listar().size();
        assertThrows(ReglaNegocioException.class,
                () -> portal.solicitarEmpeno(id, "Reloj", new BigDecimal("900"), new BigDecimal("2000000"), 30));
        assertEquals(0, portal.misOperaciones(id).size());
        assertEquals(articulosAntes, c.bd.articulos().listar().size());
    }

    @Test
    public void PU_POR_08_actualizarDatosValidaElCorreo() {
        int id = registrar("72000009");
        assertEquals("999888777", portal.actualizarDatos(id, Map.of("telefono", "999888777")).getTelefono());
        assertThrows(ReglaNegocioException.class, () -> portal.actualizarDatos(id, Map.of("correo", "no-es-correo")));
    }

    @Test
    public void PU_POR_09_cambioConMontoExcesivoSeRechaza() {
        int id = registrar("72000010");
        int tc = c.bd.tiposCambio().listar().get(0).getIdTipoCambio();
        assertThrows(ReglaNegocioException.class, () -> portal.solicitarCambio(id, tc, new BigDecimal("1e20")));
    }
}
