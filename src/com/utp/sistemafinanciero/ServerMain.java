package com.utp.sistemafinanciero;

import com.utp.sistemafinanciero.config.Configuracion;
import com.utp.sistemafinanciero.config.Contenedor;
import com.utp.sistemafinanciero.config.VerificadorEsquema;
import com.utp.sistemafinanciero.util.Db;
import com.utp.sistemafinanciero.web.ApiServer;
import com.utp.sistemafinanciero.web.Rutas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.sql.Connection;

/**
 * Punto de entrada del SIGSF (Run / F6 en NetBeans).
 *
 * <p>Arquitectura en capas:</p>
 * <pre>
 *  Vista (web/*.html + api.js)  →  ApiServer + FiltroSeguridad  →  Controladores
 *        →  Servicios (reglas de negocio)  →  DAO (interfaces)  →  JDBC / PostgreSQL
 * </pre>
 *
 * <p>Los mensajes de arranque se registran con SLF4J/Logback
 * ({@code src/logback.xml}): salen por consola y quedan en
 * {@code logs/sigsf.log}. Nunca se registra la contraseña de la base.</p>
 */
public class ServerMain {

    private static final Logger LOG = LoggerFactory.getLogger(ServerMain.class);

    public static void main(String[] args) {
        LOG.info("Iniciando SIGSF...");
        boolean memoria = "memoria".equals(Configuracion.persistencia());

        if (memoria) {
            LOG.warn("[MODO DEMOSTRACIÓN] Persistencia en memoria: los datos se pierden al cerrar.");
        } else {
            LOG.info("Conectando a {} (usuario: {})", Configuracion.jdbcUrl(), Configuracion.dbUser());
            try (Connection cn = Db.getConnection()) {
                LOG.info("Conexión a PostgreSQL correcta.");
                VerificadorEsquema.verificar(cn);
            } catch (Exception e) {
                LOG.error("No se pudo conectar a la base de datos: {}", e.getMessage());
                LOG.error("Revise db.properties y que db/schema.sql se haya ejecutado. "
                        + "El servidor se iniciará igualmente para servir el frontend.");
            }
        }

        try {
            Contenedor c = Contenedor.desdeConfiguracion();
            ApiServer app = new ApiServer(Paths.get(Configuracion.webDir()), Configuracion.corsOrigen(),
                    Configuracion.maxCuerpoKb() * 1024);
            Rutas.registrar(app, c);
            int puerto = app.iniciar(Configuracion.serverPort());
            c.respaldos.programar(Configuracion.backupCadaHoras());

            LOG.info("========================================================");
            LOG.info("  SIGSF - Servidor iniciado");
            LOG.info("  Frontend:      http://localhost:{}/", puerto);
            LOG.info("  API:           http://localhost:{}/api/health", puerto);
            LOG.info("  Persistencia:  {}", c.bd.nombre());
            LOG.info("  Seguridad:     PBKDF2 + sesiones con token ({} min) + roles", Configuracion.sesionMinutos());
            LOG.info("  Respaldos:     {}{}", c.respaldos.carpeta(), Configuracion.backupCadaHoras() > 0
                    ? " (automático cada " + Configuracion.backupCadaHoras() + " h)" : " (automático desactivado)");
            LOG.info("  Logs:          carpeta logs/ (sigsf.log, sigsf-acceso.log, sigsf-seguridad.log, sigsf-errores.log)");
            LOG.info("  (Ctrl+C para detener)");
            LOG.info("========================================================");
        } catch (Exception e) {
            LOG.error("No se pudo iniciar el servidor: {}", e.getMessage(), e);
        }
    }
}
