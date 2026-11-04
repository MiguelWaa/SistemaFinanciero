package com.utp.sistemafinanciero;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ejecutor del Plan de Pruebas (no es una prueba: no termina en "Test").
 *
 * <p>Ejecuta las clases JUnit indicadas y, además del resumen de JUnit,
 * deja en {@code build/reportes/} la evidencia por caso de prueba:</p>
 * <ul>
 *   <li>{@code resultado_pruebas.csv}: clase, caso, nivel, resultado, ms</li>
 *   <li>{@code resumen_por_nivel.txt}: totales por nivel del plan</li>
 * </ul>
 * <p>Distingue las pruebas <b>omitidas</b> (por ejemplo, las de PostgreSQL
 * cuando no hay base de pruebas), que JUnitCore cuenta como correctas.</p>
 *
 * <p>Uso: {@code java ... com.utp.sistemafinanciero.EjecutorPlanDePruebas clase1 clase2 ...}</p>
 */
public final class EjecutorPlanDePruebas {

    private EjecutorPlanDePruebas() {
    }

    /** Nivel del plan según el paquete/nombre de la clase de prueba. */
    static String nivel(String clase) {
        if (clase.contains(".integracion.") || clase.endsWith("ApiTest") || clase.endsWith("IntegracionTest")) {
            return "Integración";
        }
        return "Unitaria";
    }

    public static void main(String[] args) throws Exception {
        List<String[]> filas = new ArrayList<>();
        Map<String, int[]> porNivel = new LinkedHashMap<>();   // nivel -> {total, ok, fallidas, omitidas}
        int total = 0, fallidas = 0, omitidas = 0;
        long inicio = System.currentTimeMillis();

        for (String nombre : args) {
            Class<?> clase = Class.forName(nombre);
            String nivel = nivel(nombre);
            Map<String, String> estado = new LinkedHashMap<>();
            Map<String, Long> tiempos = new LinkedHashMap<>();
            Map<String, Long> desde = new LinkedHashMap<>();
            JUnitCore core = new JUnitCore();
            core.addListener(new RunListener() {
                @Override public void testStarted(Description d) {
                    desde.put(d.getDisplayName(), System.nanoTime());
                    estado.put(d.getDisplayName(), "CORRECTA");
                }
                @Override public void testFinished(Description d) {
                    tiempos.put(d.getDisplayName(), (System.nanoTime() - desde.get(d.getDisplayName())) / 1_000_000);
                }
                @Override public void testFailure(Failure f) {
                    estado.put(f.getDescription().getDisplayName(), "FALLIDA");
                    System.out.println("  ✘ " + f.getDescription().getDisplayName() + "\n      " + f.getMessage());
                }
                @Override public void testAssumptionFailure(Failure f) {
                    estado.put(f.getDescription().getDisplayName(), "OMITIDA");
                }
                @Override public void testIgnored(Description d) {
                    estado.put(d.getDisplayName(), "OMITIDA");
                }
            });
            Result r = core.run(Request.aClass(clase));
            int[] n = porNivel.computeIfAbsent(nivel, k -> new int[4]);
            int ok = 0, ko = 0, om = 0;
            for (Map.Entry<String, String> e : estado.entrySet()) {
                String caso = e.getKey().replaceAll("\\(" + java.util.regex.Pattern.quote(nombre) + "\\)$", "");
                filas.add(new String[]{clase.getSimpleName(), caso, nivel, e.getValue(),
                        String.valueOf(tiempos.getOrDefault(e.getKey(), 0L))});
                switch (e.getValue()) {
                    case "FALLIDA": ko++; break;
                    case "OMITIDA": om++; break;
                    default: ok++;
                }
            }
            n[0] += estado.size(); n[1] += ok; n[2] += ko; n[3] += om;
            total += estado.size(); fallidas += ko; omitidas += om;
            System.out.printf("%-38s %-12s %3d pruebas  %3d ✔  %2d ✘  %2d omitidas  (%d ms)%n",
                    clase.getSimpleName(), nivel, estado.size(), ok, ko, om, r.getRunTime());
        }

        Path dir = Paths.get("build", "reportes");
        Files.createDirectories(dir);
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(dir.resolve("resultado_pruebas.csv"),
                StandardCharsets.UTF_8))) {
            w.println("clase;caso;nivel;resultado;ms");
            for (String[] f : filas) w.println(String.join(";", f));
        }
        StringBuilder resumen = new StringBuilder("Nivel;Total;Correctas;Fallidas;Omitidas\n");
        porNivel.forEach((k, v) -> resumen.append(k).append(';').append(v[0]).append(';').append(v[1])
                .append(';').append(v[2]).append(';').append(v[3]).append('\n'));
        Files.writeString(dir.resolve("resumen_por_nivel.txt"), resumen, StandardCharsets.UTF_8);

        System.out.println("------------------------------------------------------------------------");
        System.out.print(resumen.toString().replace(';', '\t'));
        System.out.printf("TOTAL: %d pruebas | %d correctas | %d fallidas | %d omitidas | %d ms%n",
                total, total - fallidas - omitidas, fallidas, omitidas, System.currentTimeMillis() - inicio);
        System.exit(fallidas == 0 ? 0 : 1);
    }

    static void escribir(Path p, String s) throws IOException {
        Files.writeString(p, s, StandardCharsets.UTF_8);
    }
}
