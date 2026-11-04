package com.utp.sistemafinanciero.integracion;

import com.utp.sistemafinanciero.config.Configuracion;
import com.utp.sistemafinanciero.util.Db;
import org.junit.Assume;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Ayuda de las pruebas de integración con PostgreSQL real (Plan de
 * Pruebas, entorno PE-01).
 *
 * <p>Regla de seguridad del plan: estas pruebas <b>solo</b> se ejecutan
 * contra una base cuyo nombre termina en {@code _test}
 * (SistemaFinanciero_test, creada con scripts/pruebas/preparar_bd_pruebas).
 * Si no está configurada o PostgreSQL no responde, las pruebas se
 * <i>omiten</i> (no fallan), para que el resto de la suite siga
 * funcionando sin base de datos.</p>
 *
 * <p>Configuración: {@code -Dsigsf.db.name=SistemaFinanciero_test} (scripts y
 * NetBeans vía test-sys-prop) o la variable SIGSF_DB_NAME.</p>
 */
public final class BdPruebas {

    private static Boolean disponible;
    private static String motivo = "";

    private BdPruebas() {
    }

    public static synchronized boolean disponible() {
        if (disponible == null) {
            if (!Configuracion.dbName().endsWith("_test")) {
                motivo = "la base configurada (" + Configuracion.dbName() + ") no es de pruebas (_test)";
                disponible = false;
            } else {
                DriverManager.setLoginTimeout(3);
                try (Connection cn = Db.getConnection()) {
                    disponible = cn.isValid(3);
                } catch (Exception e) {
                    motivo = "PostgreSQL no responde: " + e.getMessage();
                    disponible = false;
                }
            }
        }
        return disponible;
    }

    /** Omite la prueba (JUnit "skipped") si no hay base de pruebas. */
    public static void requerir() {
        Assume.assumeTrue("Prueba de integración omitida: " + motivo, disponible());
    }

    /** Sufijo único para no chocar con datos de otras ejecuciones. */
    public static String unico() {
        return String.valueOf(System.nanoTime() % 100_000_000L);
    }
}
