package com.utp.sistemafinanciero.archivos;

/** Tipo de dato de una columna: decide cómo se escribe en Excel y cómo se interpreta al importar. */
public enum TipoDato {
    /** Texto libre: en Excel se guarda como texto (conserva ceros a la izquierda: DNI, RUC, teléfonos). */
    TEXTO,
    /** Número (entero o decimal con punto): en Excel se guarda como número. */
    NUMERO,
    /** Fecha AAAA-MM-DD: al importar acepta también la fecha serial de Excel. */
    FECHA
}
