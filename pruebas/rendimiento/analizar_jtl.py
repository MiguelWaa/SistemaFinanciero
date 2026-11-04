"""Resume un resultados.jtl (CSV) de JMeter como el "Aggregate Report" y
verifica los criterios de aceptación de rendimiento del Plan de Pruebas.

Uso:  python analizar_jtl.py build/reportes/jmeter/resultados.jtl
Escribe resumen_rendimiento.csv junto al .jtl y termina con código 1 si
algún criterio no se cumple.
"""
import csv
import math
import pathlib
import sys

P90_MAX_MS, ERROR_MAX, RPS_MIN = 1000, 0.01, 20


def percentil(valores, p):
    v = sorted(valores)
    k = max(0, math.ceil(p / 100 * len(v)) - 1)
    return v[k]


def main(ruta):
    filas = list(csv.DictReader(open(ruta, encoding="utf-8")))
    por = {}
    for f in filas:
        por.setdefault(f["label"], []).append(f)
    por["TOTAL"] = filas
    t0 = min(int(f["timeStamp"]) for f in filas)
    t1 = max(int(f["timeStamp"]) + int(f["elapsed"]) for f in filas)
    seg = (t1 - t0) / 1000
    salida = []
    print(f"{'Petición':34s} {'n':>5s} {'prom':>6s} {'p90':>6s} {'p95':>6s} {'máx':>6s} {'error':>7s} {'req/s':>6s}")
    for etiqueta, fs in por.items():
        t = [int(f["elapsed"]) for f in fs]
        err = sum(1 for f in fs if f["success"] != "true") / len(fs)
        fila = [etiqueta, len(fs), round(sum(t) / len(t)), percentil(t, 90), percentil(t, 95), max(t),
                round(100 * err, 2), round(len(fs) / seg, 1)]
        salida.append(fila)
        print(f"{fila[0]:34s} {fila[1]:5d} {fila[2]:6d} {fila[3]:6d} {fila[4]:6d} {fila[5]:6d} {fila[6]:6.2f}% {fila[7]:6.1f}")
    total = salida[-1]
    criterios = [
        ("p90 global < %d ms" % P90_MAX_MS, total[3] < P90_MAX_MS, f"{total[3]} ms"),
        ("errores < %.0f %%" % (ERROR_MAX * 100), total[6] < ERROR_MAX * 100, f"{total[6]} %"),
        ("rendimiento >= %d req/s" % RPS_MIN, total[7] >= RPS_MIN, f"{total[7]} req/s"),
    ]
    print()
    for nombre, ok, valor in criterios:
        print(f"[{'CUMPLE' if ok else 'NO CUMPLE'}] {nombre}: {valor}")
    destino = pathlib.Path(ruta).with_name("resumen_rendimiento.csv")
    with open(destino, "w", newline="", encoding="utf-8") as f:
        w = csv.writer(f, delimiter=";")
        w.writerow(["peticion", "muestras", "promedio_ms", "p90_ms", "p95_ms", "max_ms", "error_pct", "req_s"])
        w.writerows(salida)
    return 0 if all(ok for _, ok, _ in criterios) else 1


if __name__ == "__main__":
    sys.exit(main(sys.argv[1] if len(sys.argv) > 1 else "build/reportes/jmeter/resultados.jtl"))
