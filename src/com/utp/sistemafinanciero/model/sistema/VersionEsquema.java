package com.utp.sistemafinanciero.model.sistema;

import java.time.LocalDateTime;

/**
 * Entidad que representa la tabla {@code version_esquema}: registro de
 * los scripts SQL aplicados a la base de datos (versionamiento del
 * esquema). Permite saber en qué versión está la base de cada equipo y
 * qué script falta ejecutar.
 */
public class VersionEsquema {

    // Identificador (PK) -> columna: id_version
    private Integer idVersion;
    // Versión SemVer del esquema (ej. 5.0.0) -> columna: version (UNIQUE)
    private String version;
    // Qué cambió en el esquema -> columna: descripcion
    private String descripcion;
    // Script SQL que se ejecutó (ej. db/version_esquema.sql) -> columna: script
    private String script;
    // Fecha y hora en que se aplicó -> columna: aplicado_en
    private LocalDateTime aplicadoEn;
    // Usuario de base de datos que lo aplicó -> columna: aplicado_por
    private String aplicadoPor;

    public VersionEsquema() {
    }

    public VersionEsquema(String version, String descripcion, String script) {
        this.version = version;
        this.descripcion = descripcion;
        this.script = script;
        this.aplicadoEn = LocalDateTime.now();
        this.aplicadoPor = "sistema";
    }

    public Integer getIdVersion() { return idVersion; }
    public void setIdVersion(Integer idVersion) { this.idVersion = idVersion; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getScript() { return script; }
    public void setScript(String script) { this.script = script; }

    public LocalDateTime getAplicadoEn() { return aplicadoEn; }
    public void setAplicadoEn(LocalDateTime aplicadoEn) { this.aplicadoEn = aplicadoEn; }

    public String getAplicadoPor() { return aplicadoPor; }
    public void setAplicadoPor(String aplicadoPor) { this.aplicadoPor = aplicadoPor; }
}
