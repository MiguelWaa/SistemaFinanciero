package com.utp.sistemafinanciero.model.marketing;

import java.time.LocalDate;

/**
 * Entidad que representa la tabla campania_prospecto del
 * Sistema Integral de Gestion de Servicios Financieros.
 *
 * Proyecto: Curso Integrador I Sistemas - Software (UTP)
 */
public class CampaniaProspecto {

    // Identificador único del registro (PK) -> columna: id_campania_prospecto
    private Integer idCampaniaProspecto;

    // Campaña asociada (FK) -> columna: id_campania
    private Integer idCampania;

    // Prospecto asociado (FK) -> columna: id_prospecto
    private Integer idProspecto;

    // Fecha de contacto -> columna: fecha_contacto
    private LocalDate fechaContacto;

    // Resultado del contacto -> columna: resultado
    private String resultado;

    // Estado del contacto -> columna: estado
    private String estado;

    public CampaniaProspecto() {
    }

    public CampaniaProspecto(Integer idCampaniaProspecto, Integer idCampania, Integer idProspecto, LocalDate fechaContacto, String resultado, String estado) {
        this.idCampaniaProspecto = idCampaniaProspecto;
        this.idCampania = idCampania;
        this.idProspecto = idProspecto;
        this.fechaContacto = fechaContacto;
        this.resultado = resultado;
        this.estado = estado;
    }

    public Integer getIdCampaniaProspecto() {
        return idCampaniaProspecto;
    }

    public void setIdCampaniaProspecto(Integer idCampaniaProspecto) {
        this.idCampaniaProspecto = idCampaniaProspecto;
    }

    public Integer getIdCampania() {
        return idCampania;
    }

    public void setIdCampania(Integer idCampania) {
        this.idCampania = idCampania;
    }

    public Integer getIdProspecto() {
        return idProspecto;
    }

    public void setIdProspecto(Integer idProspecto) {
        this.idProspecto = idProspecto;
    }

    public LocalDate getFechaContacto() {
        return fechaContacto;
    }

    public void setFechaContacto(LocalDate fechaContacto) {
        this.fechaContacto = fechaContacto;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "CampaniaProspecto{" + "idCampaniaProspecto=" + idCampaniaProspecto + ", " + "idCampania=" + idCampania + ", " + "idProspecto=" + idProspecto + ", " + "fechaContacto=" + fechaContacto + ", " + "resultado=" + resultado + ", " + "estado=" + estado + "}";
    }
}
