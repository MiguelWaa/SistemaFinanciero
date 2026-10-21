package com.utp.sistemafinanciero.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Carga la configuración de la aplicación desde {@code db.properties}
 * (RNF-GEN-05: cambiar la configuración no requiere recompilar).
 *
 * <p>Busca el archivo en la carpeta de trabajo y, si no existe, en el
 * classpath; si tampoco aparece, usa valores por defecto. Las claves
 * sensibles (contraseña de la base) pueden darse también por variables
 * de entorno ({@code SIGSF_DB_PASSWORD}), que tienen prioridad.</p>
 */
public final class Configuracion {

    private static final Properties PROPS = new Properties();

    static {
        cargar();
    }

    private Configuracion() {
    }

    /**
     * Orden de carga (cada archivo sobrescribe al anterior):
     * <ol>
     *   <li>{@code db.properties.ejemplo}: plantilla versionada en Git con
     *       valores de laboratorio (así el proyecto funciona recién clonado).</li>
     *   <li>{@code db.properties}: configuración local de cada integrante.
     *       <b>No se versiona</b> (está en .gitignore) porque guarda credenciales.</li>
     * </ol>
     * Si no hay ninguno en la carpeta de trabajo se busca en el classpath.
     */
    private static void cargar() {
        boolean encontrado = cargarArchivo("db.properties.ejemplo");
        encontrado |= cargarArchivo("db.properties");
        if (encontrado) {
            return;
        }
        try (InputStream in = Configuracion.class.getResourceAsStream("/db.properties")) {
            if (in != null) {
                PROPS.load(in);
            }
        } catch (IOException ignore) {
            // valores por defecto
        }
    }

    private static boolean cargarArchivo(String nombre) {
        try (InputStream in = new FileInputStream(nombre)) {
            PROPS.load(in);
            return true;
        } catch (IOException noExiste) {
            return false;
        }
    }

    public static String get(String clave, String porDefecto) {
        String env = System.getenv("SIGSF_" + clave.toUpperCase().replace('.', '_'));
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        return PROPS.getProperty(clave, porDefecto).trim();
    }

    public static int getInt(String clave, int porDefecto) {
        try {
            return Integer.parseInt(get(clave, String.valueOf(porDefecto)));
        } catch (NumberFormatException e) {
            return porDefecto;
        }
    }

    // --- Base de datos ---
    public static String dbHost()     { return get("db.host", "localhost"); }
    public static int    dbPort()     { return getInt("db.port", 5432); }
    public static String dbName()     { return get("db.name", "SistemaFinanciero"); }
    public static String dbUser()     { return get("db.user", "postgres"); }
    public static String dbPassword() { return get("db.password", "postgres"); }

    /** "postgresql" (producción) o "memoria" (demostración / pruebas sin base de datos). */
    public static String persistencia() { return get("persistencia", "postgresql").toLowerCase(); }

    // --- Servidor ---
    public static int    serverPort() { return getInt("server.port", 8091); }
    public static String webDir()     { return get("web.dir", "web"); }
    /** Origen permitido para CORS; vacío = solo el mismo origen (recomendado). */
    public static String corsOrigen() { return get("seguridad.cors.origen", ""); }
    public static int    maxCuerpoKb() { return getInt("server.max.cuerpo.kb", 1024); }

    // --- Seguridad ---
    public static int sesionMinutos()     { return getInt("seguridad.sesion.minutos", 30); }
    public static int intentosMaximos()   { return getInt("seguridad.intentos.max", 5); }
    public static int bloqueoMinutos()    { return getInt("seguridad.bloqueo.minutos", 5); }
    public static int pbkdf2Iteraciones() { return getInt("seguridad.pbkdf2.iteraciones", 120000); }

    // --- Importación de ficheros ---
    /** Máximo de filas por archivo importado (protección contra archivos enormes). */
    public static int importacionMaxFilas() { return getInt("importacion.max.filas", 2000); }

    // --- Respaldos ---
    public static String backupDir()      { return get("backup.dir", "respaldos"); }
    /** Ruta de pg_dump; "auto" = buscar en el PATH y en las rutas típicas de Windows. */
    public static String backupPgDump()   { return get("backup.pgdump", "auto"); }
    public static int    backupRetener()  { return getInt("backup.retener", 10); }
    public static int    backupCadaHoras() { return getInt("backup.automatico.horas", 24); }

    /** URL JDBC para conectarse a PostgreSQL. */
    public static String jdbcUrl() {
        return "jdbc:postgresql://" + dbHost() + ":" + dbPort() + "/" + dbName();
    }
}
