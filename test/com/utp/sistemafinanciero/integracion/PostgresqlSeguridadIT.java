package com.utp.sistemafinanciero.integracion;

import com.utp.sistemafinanciero.config.Configuracion;
import com.utp.sistemafinanciero.dao.jdbc.ClienteDaoJdbc;
import com.utp.sistemafinanciero.dao.jdbc.PagoDaoJdbc;
import com.utp.sistemafinanciero.model.clientes.Cliente;
import com.utp.sistemafinanciero.util.Db;
import org.junit.BeforeClass;
import org.junit.Test;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.Assert.*;

/** Se ejecuta explícitamente con scripts/probar_postgresql.py, jamás en memoria. */
public class PostgresqlSeguridadIT {
    @BeforeClass public static void exigirBaseDePrueba() {
        assertTrue("Solo BD desechable creada por el ejecutor", Configuracion.dbName().startsWith("sigsf_test_"));
        assertEquals("postgresql", Configuracion.persistencia());
    }
    @Test public void esquemaCompletoInstalado() throws Exception {
        try(Connection c=Db.getConnection();var st=c.createStatement();var r=st.executeQuery(
                "SELECT count(*) FROM information_schema.tables WHERE table_schema='public' AND table_type='BASE TABLE'")) {
            assertTrue(r.next());assertEquals(37,r.getInt(1));
        }
    }
    @Test public void daoClienteCrudConConsultaParametrizada() {
        ClienteDaoJdbc dao=new ClienteDaoJdbc();Cliente c=new Cliente();
        c.setDocumento("IT"+UUID.randomUUID().toString().replace("-","").substring(0,12));
        c.setTipoDocumento("DNI");c.setNombres("O'Connor");c.setEstado("ACTIVO");c.setFechaRegistro(LocalDateTime.now());
        Integer id=dao.crear(c);
        try {
            assertEquals("O'Connor",dao.buscar(id).getNombres());
            assertNull(dao.buscarPorDocumento("' OR '1'='1"));
            c.setIdCliente(id);c.setNombres("Actualizado");assertEquals(1,dao.actualizar(c));
            assertEquals("Actualizado",dao.buscar(id).getNombres());
            assertNotNull(new PagoDaoJdbc().listar());
        } finally {dao.eliminar(id);}
        assertNull(dao.buscar(id));
    }
    private void rechaza(String sql,String estado) throws Exception {
        try(Connection c=Db.getConnection()) {
            c.setAutoCommit(false);
            try(var st=c.createStatement()) {
                SQLException e=assertThrows(SQLException.class,()->st.executeUpdate(sql));
                assertEquals(estado,e.getSQLState());
            } finally {c.rollback();}
        }
    }
    @Test public void pagoConMontoNegativoRechazadoPorBd() throws Exception {
        rechaza("INSERT INTO pago(id_cliente,concepto,metodo,monto) VALUES(1,'OTRO','YAPE',-1)","23514");
    }
    @Test public void pagoConClienteInexistenteRechazadoPorBd() throws Exception {
        rechaza("INSERT INTO pago(id_cliente,concepto,metodo,monto) VALUES(-999,'OTRO','YAPE',1)","23503");
    }
    @Test public void numeroOperacionUnicoSinDistinguirMayusculas() throws Exception {
        try(Connection c=Db.getConnection()) {
            c.setAutoCommit(false);
            try(var st=c.createStatement()) {
                st.executeUpdate("INSERT INTO pago(id_cliente,concepto,metodo,monto,numero_operacion) VALUES(1,'OTRO','YAPE',1,'IT-unique')");
                SQLException e=assertThrows(SQLException.class,()->st.executeUpdate(
                        "INSERT INTO pago(id_cliente,concepto,metodo,monto,numero_operacion) VALUES(1,'OTRO','YAPE',1,'it-UNIQUE')"));
                assertEquals("23505",e.getSQLState());
            } finally {c.rollback();}
        }
    }
    @Test public void rollbackJdbcRevierteCambio() throws Exception {
        try(Connection c=Db.getConnection()) {
            c.setAutoCommit(false);
            try(var st=c.createStatement()) {
                String previo;try(var r=st.executeQuery("SELECT nombres FROM cliente WHERE id_cliente=1")){assertTrue(r.next());previo=r.getString(1);}
                st.executeUpdate("UPDATE cliente SET nombres='IT-ROLLBACK' WHERE id_cliente=1");c.rollback();
                try(var r=st.executeQuery("SELECT nombres FROM cliente WHERE id_cliente=1")){assertTrue(r.next());assertEquals(previo,r.getString(1));}
            } finally {c.rollback();}
        }
    }
    @Test public void rolAplicacionNoModificaNiBorraAuditoria() throws Exception {
        try(Connection c=Db.getConnection();var st=c.createStatement();var r=st.executeQuery(
                "SELECT has_table_privilege('sigsf_app','auditoria','UPDATE'),has_table_privilege('sigsf_app','auditoria','DELETE'),has_table_privilege('sigsf_app','auditoria','INSERT')")) {
            assertTrue(r.next());assertFalse(r.getBoolean(1));assertFalse(r.getBoolean(2));assertTrue(r.getBoolean(3));
        }
    }
}
