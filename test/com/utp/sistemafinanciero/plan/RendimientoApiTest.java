package com.utp.sistemafinanciero.plan;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * v5.5 · Plan de pruebas · Prueba NO FUNCIONAL de RENDIMIENTO y CONCURRENCIA.
 *
 * <p>Simula 20 usuarios simultáneos haciendo 25 consultas cada uno (500
 * peticiones) contra endpoints con sesión. Criterio de aceptación
 * (RNF-GEN-01): 0 % de errores y percentil 95 menor a 1 segundo en el
 * entorno de pruebas. Los umbrales son holgados a propósito para que la
 * prueba sea estable en la integración continua.</p>
 */
public class RendimientoApiTest {

    private static final int USUARIOS = 20;
    private static final int PETICIONES_POR_USUARIO = 25;
    private static final long P95_MAXIMO_MS = 1000;

    private static ServidorDePrueba s;
    private static String token;

    @BeforeClass
    public static void levantar() throws Exception {
        s = ServidorDePrueba.iniciar();
        token = s.loginPersonal("admin@sigsf.com", "admin123");
    }

    @AfterClass
    public static void detener() {
        s.close();
    }

    /** CP-REN-01 · 500 consultas concurrentes a clientes, operaciones y panel. */
    @Test
    public void cpRen01_veinteUsuariosConcurrentesSinErroresYP95MenorAUnSegundo() throws Exception {
        String[] rutas = {"/api/clientes", "/api/operaciones", "/api/dashboard", "/api/prestamos", "/api/pagos"};
        List<Long> tiempos = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger errores = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(USUARIOS);
        CountDownLatch salida = new CountDownLatch(1);
        List<Future<?>> tareas = new ArrayList<>();
        long inicio = System.nanoTime();
        for (int u = 0; u < USUARIOS; u++) {
            final int usuario = u;
            tareas.add(pool.submit(() -> {
                salida.await();
                for (int i = 0; i < PETICIONES_POR_USUARIO; i++) {
                    String ruta = rutas[(usuario + i) % rutas.length];
                    long t0 = System.nanoTime();
                    HttpResponse<String> r = s.get(ruta, token);
                    tiempos.add((System.nanoTime() - t0) / 1_000_000);
                    if (r.statusCode() != 200) errores.incrementAndGet();
                }
                return null;
            }));
        }
        salida.countDown();
        for (Future<?> f : tareas) f.get(2, TimeUnit.MINUTES);
        pool.shutdown();
        long totalMs = (System.nanoTime() - inicio) / 1_000_000;

        List<Long> orden = new ArrayList<>(tiempos);
        Collections.sort(orden);
        long p50 = orden.get(orden.size() / 2);
        long p95 = orden.get((int) Math.ceil(orden.size() * 0.95) - 1);
        long max = orden.get(orden.size() - 1);
        double rps = orden.size() * 1000.0 / Math.max(1, totalMs);
        System.out.printf("%n[RENDIMIENTO] peticiones=%d errores=%d p50=%dms p95=%dms max=%dms total=%dms rps=%.1f%n",
                orden.size(), errores.get(), p50, p95, max, totalMs, rps);

        assertEquals(USUARIOS * PETICIONES_POR_USUARIO, orden.size());
        assertEquals("No debe haber errores bajo carga", 0, errores.get());
        assertTrue("p95=" + p95 + " ms supera " + P95_MAXIMO_MS + " ms", p95 < P95_MAXIMO_MS);
    }

    /** CP-REN-02 · Inicio de sesión (PBKDF2) bajo concurrencia: 10 logins simultáneos correctos. */
    @Test
    public void cpRen02_loginsSimultaneosCorrectos() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(10);
        List<Future<String>> res = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            res.add(pool.submit(() -> s.loginPersonal("lucia.ramos@sigsf.com", "lucia123")));
        }
        java.util.Set<String> tokens = new java.util.HashSet<>();
        for (Future<String> f : res) tokens.add(f.get(1, TimeUnit.MINUTES));
        pool.shutdown();
        assertEquals("Cada sesión debe tener un token distinto", 10, tokens.size());
    }
}
