package com.utp.sistemafinanciero.version;

/**
 * DTO con la versión en ejecución del SIGSF (lo que muestra la pantalla
 * «Control de versiones» y el pie del panel). Inmutable.
 */
public class InfoVersion {

    private final String version;
    private final String rama;
    private final String commit;
    private final String commitCorto;
    private final String etiqueta;
    private final String fechaCommit;
    private final String autor;
    private final String mensaje;
    private final boolean cambiosSinCommit;
    private final String fechaCompilacion;
    private final String origen;
    private final String versionEsquema;
    private final String versionEsquemaEsperada;

    public InfoVersion(String version, String rama, String commit, String etiqueta, String fechaCommit,
                       String autor, String mensaje, boolean cambiosSinCommit, String fechaCompilacion,
                       String origen, String versionEsquema, String versionEsquemaEsperada) {
        this.version = version;
        this.rama = rama;
        this.commit = commit;
        this.commitCorto = commit == null ? null : commit.substring(0, Math.min(7, commit.length()));
        this.etiqueta = etiqueta;
        this.fechaCommit = fechaCommit;
        this.autor = autor;
        this.mensaje = mensaje;
        this.cambiosSinCommit = cambiosSinCommit;
        this.fechaCompilacion = fechaCompilacion;
        this.origen = origen;
        this.versionEsquema = versionEsquema;
        this.versionEsquemaEsperada = versionEsquemaEsperada;
    }

    /** Versión SemVer de la aplicación (archivo VERSION). */
    public String getVersion() { return version; }
    /** Rama de Git (main, develop, feature/...). */
    public String getRama() { return rama; }
    public String getCommit() { return commit; }
    public String getCommitCorto() { return commitCorto; }
    /** Resultado de {@code git describe --tags} (ej. v5.0.0-3-gabc1234). */
    public String getEtiqueta() { return etiqueta; }
    public String getFechaCommit() { return fechaCommit; }
    public String getAutor() { return autor; }
    public String getMensaje() { return mensaje; }
    /** true si el árbol de trabajo tiene cambios sin confirmar (commit). */
    public boolean isCambiosSinCommit() { return cambiosSinCommit; }
    public String getFechaCompilacion() { return fechaCompilacion; }
    /** De dónde se obtuvo: "git", "version.properties" o "VERSION". */
    public String getOrigen() { return origen; }
    /** Última versión registrada en la tabla version_esquema. */
    public String getVersionEsquema() { return versionEsquema; }
    public String getVersionEsquemaEsperada() { return versionEsquemaEsperada; }
    public boolean isEsquemaAlDia() {
        return versionEsquemaEsperada != null && versionEsquemaEsperada.equals(versionEsquema);
    }
}
