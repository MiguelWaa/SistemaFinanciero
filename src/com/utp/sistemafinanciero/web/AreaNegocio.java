package com.utp.sistemafinanciero.web;

import java.util.Locale;

/**
 * Áreas del negocio de SIGSF y los módulos de la API que pertenecen a cada
 * una. Se usa para etiquetar cada línea del log ({@code %X{area}} en
 * {@code logback.xml}) y para agrupar la importación y exportación de
 * ficheros por área.
 *
 * <p>El módulo es el primer segmento de la ruta: {@code /api/prospectos/5}
 * → {@code prospectos} → {@link #MARKETING}.</p>
 */
public enum AreaNegocio {

    MARKETING("Marketing", "campanias", "prospectos"),
    VENTAS("Ventas", "ventas"),
    LOGISTICA("Logística", "proveedores"),
    ALMACEN("Almacén", "productos"),
    PRODUCCION("Producción", "lineas", "ordenes"),
    OPERACIONES("Operaciones", "operaciones", "tipos-cambio", "prestamos", "empenos", "solicitudes",
            "pagos", "billeteras", "cotizaciones"),
    DIRECCION_GERENCIA("Dirección y Gerencia", "expedientes", "dashboard"),
    CLIENTES("Clientes y servicios", "clientes", "servicios", "portal"),
    SEGURIDAD("Seguridad y usuarios", "auth", "usuarios", "roles", "auditoria", "respaldos"),
    SISTEMA("Sistema", "health");

    private final String nombre;
    private final String[] modulos;

    AreaNegocio(String nombre, String... modulos) {
        this.nombre = nombre;
        this.modulos = modulos;
    }

    public String nombre() {
        return nombre;
    }

    public String[] modulos() {
        return modulos.clone();
    }

    /** Área a la que pertenece un módulo de la API; {@link #SISTEMA} si no se reconoce. */
    public static AreaNegocio deModulo(String modulo) {
        if (modulo != null) {
            String m = modulo.toLowerCase(Locale.ROOT);
            for (AreaNegocio a : values()) {
                for (String x : a.modulos) {
                    if (x.equals(m)) return a;
                }
            }
        }
        return SISTEMA;
    }

    /** Área de una ruta de la API ({@code /api/{modulo}/...}). */
    public static AreaNegocio deRuta(String ruta) {
        if (ruta == null) return SISTEMA;
        String[] p = ruta.split("/");
        return p.length >= 3 ? deModulo(p[2]) : SISTEMA;
    }
}
