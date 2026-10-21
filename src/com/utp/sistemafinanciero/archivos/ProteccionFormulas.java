package com.utp.sistemafinanciero.archivos;

/**
 * Protección contra <b>inyección de fórmulas</b> (CSV/Formula Injection,
 * OWASP). Si un dato que escribió un usuario empieza por {@code =}, {@code +},
 * {@code -}, {@code @}, tabulador o retorno de carro, una hoja de cálculo
 * podría interpretarlo como fórmula al abrir el CSV (por ejemplo
 * {@code =HYPERLINK(...)} o {@code =cmd|...}). Se antepone un apóstrofo para
 * que se muestre como texto.
 *
 * <p>Los números legítimos ({@code -15.50}) no se alteran.</p>
 */
public final class ProteccionFormulas {

    private ProteccionFormulas() {
    }

    public static String neutralizar(String valor) {
        if (valor == null || valor.isEmpty()) return valor == null ? "" : valor;
        char c = valor.charAt(0);
        boolean peligroso = c == '=' || c == '+' || c == '-' || c == '@' || c == '\t' || c == '\r';
        if (!peligroso) return valor;
        if ((c == '-' || c == '+') && valor.matches("[-+]?\\d+(\\.\\d+)?")) {
            return valor; // número con signo: no es fórmula
        }
        return "'" + valor;
    }
}
