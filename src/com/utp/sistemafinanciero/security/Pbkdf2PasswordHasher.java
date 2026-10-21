package com.utp.sistemafinanciero.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Protección de contraseñas con <b>PBKDF2-HMAC-SHA256</b> (recomendado
 * por OWASP y NIST SP 800-132), usando una sal aleatoria de 16 bytes por
 * contraseña y miles de iteraciones para encarecer los ataques de fuerza
 * bruta y de tablas arcoíris.
 *
 * <p>Formato almacenado: {@code pbkdf2$<iteraciones>$<sal base64>$<hash base64>}</p>
 *
 * <p><b>Compatibilidad:</b> acepta los valores antiguos del sistema
 * (SHA-256 sin sal y, en el portal, contraseñas en texto plano) para que
 * nadie quede bloqueado; {@link #requiereActualizar} los marca y el
 * servicio de autenticación los reemplaza por PBKDF2 en el siguiente
 * inicio de sesión correcto.</p>
 */
public class Pbkdf2PasswordHasher implements PasswordHasher {

    private static final String PREFIJO = "pbkdf2";
    private static final int BYTES_SAL = 16;
    private static final int BITS_CLAVE = 256;

    private final int iteraciones;
    private final SecureRandom aleatorio = new SecureRandom();

    public Pbkdf2PasswordHasher(int iteraciones) {
        if (iteraciones < 1000) {
            throw new IllegalArgumentException("Se requieren al menos 1000 iteraciones.");
        }
        this.iteraciones = iteraciones;
    }

    @Override
    public String hash(String plano) {
        byte[] sal = new byte[BYTES_SAL];
        aleatorio.nextBytes(sal);
        byte[] h = derivar(plano == null ? "" : plano, sal, iteraciones);
        Base64.Encoder b64 = Base64.getEncoder();
        return PREFIJO + "$" + iteraciones + "$" + b64.encodeToString(sal) + "$" + b64.encodeToString(h);
    }

    @Override
    public boolean verificar(String plano, String almacenado) {
        if (plano == null || almacenado == null || almacenado.isEmpty()) {
            return false;
        }
        if (almacenado.startsWith(PREFIJO + "$")) {
            String[] p = almacenado.split("\\$");
            if (p.length != 4) return false;
            try {
                int iter = Integer.parseInt(p[1]);
                byte[] sal = Base64.getDecoder().decode(p[2]);
                byte[] esperado = Base64.getDecoder().decode(p[3]);
                byte[] calculado = derivar(plano, sal, iter);
                return MessageDigest.isEqual(esperado, calculado); // tiempo constante
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        if (esSha256Hex(almacenado)) {
            // Formato heredado (SHA-256 sin sal).
            return MessageDigest.isEqual(sha256Hex(plano).getBytes(StandardCharsets.US_ASCII),
                    almacenado.toLowerCase().getBytes(StandardCharsets.US_ASCII));
        }
        // Formato heredado en texto plano (accesos antiguos del portal).
        return MessageDigest.isEqual(plano.getBytes(StandardCharsets.UTF_8),
                almacenado.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean requiereActualizar(String almacenado) {
        if (almacenado == null || !almacenado.startsWith(PREFIJO + "$")) {
            return true;
        }
        String[] p = almacenado.split("\\$");
        try {
            return p.length != 4 || Integer.parseInt(p[1]) < iteraciones;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    private static byte[] derivar(String plano, byte[] sal, int iter) {
        try {
            PBEKeySpec spec = new PBEKeySpec(plano.toCharArray(), sal, iter, BITS_CLAVE);
            try {
                return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            } finally {
                spec.clearPassword();
            }
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2 no disponible en esta JVM", e);
        }
    }

    /** SHA-256 en hexadecimal (solo para validar el formato heredado). */
    public static String sha256Hex(String texto) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean esSha256Hex(String s) {
        return s.matches("(?i)[0-9a-f]{64}");
    }
}
