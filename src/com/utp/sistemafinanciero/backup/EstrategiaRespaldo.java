package com.utp.sistemafinanciero.backup;

import java.nio.file.Path;

/**
 * Forma de generar un respaldo (patrón <b>Strategy</b>). El
 * {@link RespaldoService} prueba las estrategias en orden y usa la
 * primera disponible:
 * <ol>
 *   <li>{@link RespaldoPgDump}: herramienta oficial {@code pg_dump} (respaldo completo).</li>
 *   <li>{@link RespaldoJdbcSql}: exportación en Java puro (si pg_dump no está instalado).</li>
 *   <li>{@link RespaldoMemoriaJson}: en modo demostración (sin base de datos).</li>
 * </ol>
 */
public interface EstrategiaRespaldo {

    /** Nombre corto del método (aparece en el nombre del archivo). */
    String nombre();

    /** Extensión del archivo generado, sin punto (sql, json). */
    String extension();

    /** ¿Puede usarse en este equipo? */
    boolean disponible();

    /** Escribe el respaldo en el archivo indicado. */
    void generar(Path destino) throws Exception;
}
