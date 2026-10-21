package com.utp.sistemafinanciero.controller;

import com.utp.sistemafinanciero.util.ExcelExporter;
import com.utp.sistemafinanciero.web.Descarga;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Apoyo para las exportaciones a Excel de los controladores. */
final class Exportar {

    static final String TIPO_EXCEL = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private Exportar() {
    }

    static <T> Descarga excel(String archivo, String hoja, String[] encabezados, List<T> lista,
                              Function<T, String[]> fila) {
        List<String[]> filas = new ArrayList<>();
        for (T t : lista) {
            filas.add(fila.apply(t));
        }
        return new Descarga(archivo, TIPO_EXCEL, ExcelExporter.generar(hoja, encabezados, filas));
    }

    static String texto(Object s) {
        return s == null ? "" : String.valueOf(s);
    }

    static String decimal(BigDecimal n) {
        return n == null ? "0" : n.toPlainString();
    }
}
