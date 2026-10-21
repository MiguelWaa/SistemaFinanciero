package com.utp.sistemafinanciero.archivos;

import java.util.Objects;
import java.util.function.Function;

/**
 * Una columna de un fichero de exportación/importación.
 *
 * @param <T> entidad o DTO de la fila
 */
public final class Columna<T> {

    private final String clave;
    private final String titulo;
    private final TipoDato tipo;
    private final Function<T, Object> valor;
    private final boolean importable;
    private final boolean obligatoria;
    private final String ejemplo;

    private Columna(String clave, String titulo, TipoDato tipo, Function<T, Object> valor,
                    boolean importable, boolean obligatoria, String ejemplo) {
        this.clave = Objects.requireNonNull(clave);
        this.titulo = Objects.requireNonNull(titulo);
        this.tipo = tipo;
        this.valor = valor;
        this.importable = importable;
        this.obligatoria = obligatoria;
        this.ejemplo = ejemplo == null ? "" : ejemplo;
    }

    /** Columna solo de salida (id, fechas que asigna el servidor, totales calculados...). */
    public static <T> Columna<T> salida(String clave, String titulo, TipoDato tipo, Function<T, Object> valor) {
        return new Columna<>(clave, titulo, tipo, valor, false, false, null);
    }

    /** Columna que se exporta y que también se acepta al importar. */
    public static <T> Columna<T> editable(String clave, String titulo, TipoDato tipo, Function<T, Object> valor,
                                          String ejemplo) {
        return new Columna<>(clave, titulo, tipo, valor, true, false, ejemplo);
    }

    /** Columna obligatoria al importar (debe venir en el encabezado y tener valor). */
    public static <T> Columna<T> obligatoria(String clave, String titulo, TipoDato tipo, Function<T, Object> valor,
                                             String ejemplo) {
        return new Columna<>(clave, titulo, tipo, valor, true, true, ejemplo);
    }

    /** Clave del campo (la misma que usa el JSON de la API y el MapeadorEntrada). */
    public String clave() { return clave; }

    public String titulo() { return titulo; }

    public TipoDato tipo() { return tipo; }

    public boolean importable() { return importable; }

    public boolean obligatoria() { return obligatoria; }

    public String ejemplo() { return ejemplo; }

    /** Valor de la columna para una fila, ya convertido a texto ("" si es nulo). */
    public String texto(T fila) {
        Object v = valor == null ? null : valor.apply(fila);
        if (v == null) return "";
        if (v instanceof java.math.BigDecimal) return ((java.math.BigDecimal) v).toPlainString();
        if (v instanceof java.time.LocalDateTime) {
            String s = v.toString().replace('T', ' ');
            return s.length() > 16 ? s.substring(0, 16) : s;
        }
        return String.valueOf(v);
    }

    /** Valor "crudo" de la columna (para la exportación JSON). */
    public Object valor(T fila) {
        return valor == null ? null : valor.apply(fila);
    }
}
