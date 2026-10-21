package com.utp.sistemafinanciero.version;

import java.util.Optional;

/**
 * Abstracción del repositorio Git del proyecto (principio de inversión de
 * dependencias, DIP): {@link VersionService} depende de esta interfaz y no
 * del comando {@code git}. Así las pruebas usan un repositorio simulado y
 * el sistema funciona aunque Git no esté instalado.
 */
public interface FuenteGit {

    /** ¿Hay un repositorio Git y el comando {@code git} está disponible? */
    boolean disponible();

    /**
     * Ejecuta un subcomando de Git de solo lectura y devuelve su salida
     * estándar, o vacío si falló.
     */
    Optional<String> ejecutar(String... argumentos);
}
