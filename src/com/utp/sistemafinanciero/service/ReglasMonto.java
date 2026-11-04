package com.utp.sistemafinanciero.service;

import java.math.BigDecimal;

/**
 * Validaciones numéricas comunes de las operaciones financieras.
 *
 * <p>Nacen del <b>Plan de Pruebas (Avance 4)</b>: el análisis de valores
 * límite mostró que montos, tasas y plazos extremos llegaban a PostgreSQL
 * y provocaban errores 500 (desborde de NUMERIC) o se aceptaban en modo
 * memoria y se rechazaban en PostgreSQL. Ahora la regla vive en el
 * servicio, igual para las dos persistencias, y la base conserva sus
 * CHECK como segunda barrera (defensa en profundidad).</p>
 */
final class ReglasMonto {

    /** Tasa de interés: 0 % a 100 % (la columna es NUMERIC(6,3)). */
    static final BigDecimal TASA_MAXIMA = new BigDecimal("100");

    private ReglasMonto() {
    }

    /** Exige min ≤ valor ≤ max y como máximo 2 decimales (columnas NUMERIC(14,2)). */
    static void exigirMonto(BigDecimal valor, BigDecimal minimo, BigDecimal maximo, String campo) {
        if (valor == null || valor.signum() <= 0) {
            throw new ReglaNegocioException(campo + " debe ser mayor que cero.");
        }
        if (valor.stripTrailingZeros().scale() > 2) {
            throw new ReglaNegocioException(campo + " admite como máximo 2 decimales.");
        }
        if (valor.compareTo(minimo) < 0) {
            throw new ReglaNegocioException(campo + " mínimo es S/ " + minimo.toPlainString() + ".");
        }
        if (valor.compareTo(maximo) > 0) {
            throw new ReglaNegocioException(campo + " máximo es S/ " + maximo.toPlainString() + ".");
        }
    }

    /** Tasa vacía = 0; si no, 0 ≤ tasa ≤ 100 con hasta 3 decimales. */
    static BigDecimal exigirTasa(BigDecimal tasa) {
        if (tasa == null) {
            return BigDecimal.ZERO;
        }
        if (tasa.signum() < 0 || tasa.compareTo(TASA_MAXIMA) > 0) {
            throw new ReglaNegocioException("La tasa de interés debe estar entre 0 % y 100 %.");
        }
        if (tasa.stripTrailingZeros().scale() > 3) {
            throw new ReglaNegocioException("La tasa de interés admite como máximo 3 decimales.");
        }
        return tasa;
    }

    /** Exige 1 ≤ plazo ≤ maximo. */
    static void exigirPlazo(Integer plazo, int maximo, String unidad) {
        if (plazo == null || plazo <= 0) {
            throw new ReglaNegocioException("El plazo en " + unidad + " debe ser mayor que cero.");
        }
        if (plazo > maximo) {
            throw new ReglaNegocioException("El plazo máximo es de " + maximo + " " + unidad + ".");
        }
    }
}
