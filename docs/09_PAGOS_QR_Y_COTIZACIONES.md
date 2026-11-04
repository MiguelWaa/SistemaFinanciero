# 09 · Pagos con QR (Yape, Plin, Tunki), cotizaciones e interfaz Bootstrap 5 — v5.2.0

## 1. Qué se agregó

| Tema | Qué hace | Dónde |
|---|---|---|
| **Pagar con QR (portal)** | El cliente elige qué paga (cuota de préstamo, rescate de empeño, cambio de moneda u otro), el monto y la billetera; ve el QR de cobro, el número, el titular, el monto exacto y un código de referencia; paga en su app y registra el **número de operación** (y opcionalmente la captura de la constancia). | `web/portal-inicio.html` › *Pagar con QR* |
| **Bandeja de verificación** | El pago aparece al instante en el sistema interno como **Por verificar** (contador en la barra lateral, campana y aviso emergente). Caja lo compara con su billetera y lo **confirma** o lo **rechaza** con motivo. | `web/pagos.html` › *Por verificar* |
| **Efecto en el negocio** | Al confirmar, se descuenta el saldo del préstamo o empeño; si llega a cero, el préstamo pasa a `CANCELADO` y el empeño a `RECUPERADO`. Se emite el comprobante `RC-000123`. | `PagoService.aplicarEfectos` |
| **Cobro en caja** | El cajero registra pagos en ventanilla (efectivo, transferencia o la billetera del cliente); quedan confirmados. | `pagos.html` › *Cobrar en caja* |
| **Billeteras y QR** | El ADMIN registra el número, el titular, las indicaciones y **sube la imagen del QR de cobro** de Yape, Plin y Tunki; puede activarlas o desactivarlas. | `pagos.html` › *Billeteras y QR* |
| **Cotizaciones** | Préstamo (cuota y cronograma), empeño (interés y total para recuperar) y cambio (monto a recibir). Mismas fórmulas y tasas del sistema, número `COT-000001`, vigencia de 7 días, se imprimen y se convierten en solicitud con un clic. | portal › *Cotizar* y `pagos.html` › *Cotizaciones* |
| **Documentos imprimibles** | Comprobante de pago, cotización con cronograma, estado de cuenta del cliente y reporte de pagos con filtros. Se imprimen o se guardan en PDF desde el navegador (A4). | `documento.html`, `portal-documento.html`, `sigsf-documentos.js` |
| **Reportes y exportación** | Pagos y cotizaciones se exportan a Excel, CSV y JSON con el mismo motor de ficheros de la v4.0. Nuevos indicadores en el panel. | `CatalogoArchivos`, `reportes.html`, `GET /api/dashboard` |
| **Interfaz Bootstrap 5** | Todo el sistema migrado a Bootstrap 5.3.3 servido localmente (sin CDN), tema propio, tablas con orden y paginación, avisos y diálogos de confirmación. | `web/vendor/`, `sigsf-ui.css`, `sigsf-ui.js`, `sigsf-crud.js` |

## 2. Flujo de un pago

```
Cliente (portal)                          Sistema interno (caja)
────────────────                          ──────────────────────
1. Elige trámite, monto y billetera
2. Ve el QR + número + referencia C2-OP45
3. Paga en Yape / Plin / Tunki
4. Registra N° de operación  ───────────▶ Pago POR_VERIFICAR (aviso en el panel)
                                          5. Busca el abono en la app de la empresa
                                          6a. Confirmar ─▶ CONFIRMADO, baja el saldo, comprobante RC-…
                                          6b. Rechazar  ─▶ RECHAZADO con motivo (lo ve el cliente)
7. Ve el estado y descarga su comprobante ◀────────────
```

### ¿Por qué hay que verificar?

Yape, Plin y Tunki no ofrecen a un sistema propio una API pública para comprobar automáticamente
que un abono llegó. Por eso el sistema registra el **número de operación** y la caja lo verifica
contra los movimientos de la billetera de la empresa, igual que hacen los comercios que cobran con QR.
Para conciliar automáticamente se necesitaría un convenio de cobros con la entidad emisora o una
pasarela de pagos (por ejemplo, Niubiz o Izipay), que se podría conectar después en `PagoService`.

Para **demostraciones** se puede activar la confirmación automática en `db.properties`:

```properties
pagos.confirmacion.automatica=true   # el pago del portal queda CONFIRMADO al registrarlo
pagos.monto.maximo=10000             # tope por pago en soles
```

### El QR que ve el cliente

- Si el ADMIN subió la **imagen del QR de cobro** que genera la app de la empresa, el cliente ve esa
  imagen y puede escanearla con su app.
- Si no la subió, el portal muestra un **QR de referencia** con los datos del pago (número, titular,
  monto y referencia). Las apps de billetera usan su propio formato de QR, así que en ese caso el
  cliente paga al número indicado. Lo recomendable es subir el QR real de cada billetera.

## 3. Reglas de negocio (`PagoService`)

| Regla | Mensaje |
|---|---|
| Solo billeteras activas (Yape, Plin, Tunki) desde el portal | «Por ahora no recibimos pagos con …» |
| El número de operación es obligatorio (4–30 letras, números o guiones) | «Ingresa el número de operación…» |
| Un número de operación no se repite con la misma billetera (índice único en la BD) | «Ese número de operación de YAPE ya fue registrado.» |
| No se paga más de lo que se debe, descontando lo que ya está por verificar | «El monto supera lo pendiente de pago (S/ …).» |
| Solo se pagan trámites propios y activos | «El trámite seleccionado no pertenece al cliente.» |
| Un pago solo se confirma o rechaza una vez; el rechazo exige motivo | «El pago PG-… ya fue atendido…» |
| Los pagos no se eliminan (trazabilidad del dinero); el rol `sigsf_app` no tiene `DELETE` sobre `pago` | — |
| Imágenes (QR y constancia): solo PNG, JPG o WEBP en Base64, máximo 500 KB | «… debe ser una imagen PNG, JPG o WEBP.» |

## 4. API

**Portal del Cliente** (solo su propio `{id}`; lo controla `PoliticaAcceso`):

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/portal/{id}/billeteras` | Billeteras activas (número, titular, QR) |
| GET | `/api/portal/{id}/pagables` | Cuotas, rescates y cambios que puede pagar ahora |
| POST | `/api/portal/{id}/pagos` | Registrar un pago `{idOperacion, concepto, monto, metodo, numeroOperacion, celularOrigen, constancia}` |
| GET | `/api/portal/{id}/pagos` · `/pagos/{idPago}` | Mis pagos · comprobante |
| GET | `/api/portal/{id}/estado-cuenta` | Deudas, pagos y totales |
| GET/POST | `/api/portal/{id}/cotizaciones` | Mis cotizaciones · nueva cotización |
| GET | `/api/portal/{id}/cotizaciones/{idCot}` | Cotización con cronograma |
| POST | `/api/portal/{id}/cotizaciones/{idCot}/solicitar` | Convertir en solicitud |

**Personal**:

| Método | Ruta | ADMIN | SUPERVISOR | CAJERO | ANALISTA |
|---|---|---|---|---|---|
| GET | `/api/pagos[?estado=]`, `/api/pagos/{id}`, `/api/pagos/resumen` | ✔ | ✔ | ✔ | ✔ |
| POST | `/api/pagos` (cobro en caja) | ✔ | ✔ | ✔ | — |
| PUT | `/api/pagos/{id}/confirmar`, `/api/pagos/{id}/rechazar` | ✔ | ✔ | ✔ | — |
| GET | `/api/pagos/pagables/{idCliente}`, `/api/pagos/{id}/constancia` | ✔ | ✔ | ✔ | ✔ |
| GET / PUT | `/api/billeteras` / `/api/billeteras/{id}` | ✔ / ✔ | ✔ / — | ✔ / — | ✔ / — |
| GET / POST | `/api/cotizaciones` | ✔ / ✔ | ✔ / — | ✔ / ✔ | ✔ / — |
| GET | `/api/pagos/exportar?formato=xlsx\|csv\|json`, `/api/cotizaciones/exportar` | ✔ | ✔ | ✔ | ✔ |

## 5. Base de datos (esquema 5.2.0)

`db/pagos_qr.sql` crea `billetera`, `pago` y `cotizacion` (re-ejecutable, no borra datos) y registra
la versión 5.2.0 en `version_esquema`. Si las tablas no existen, el servidor también las crea solo al
arrancar (`VerificadorEsquema.crearPagosSiFaltan`).

## 6. Interfaz (Bootstrap 5.3.3)

- **Librerías locales** en `web/vendor/` (Bootstrap 5.3.3, Font Awesome 6.5.2, qrcode-generator 2.0.4):
  el sistema funciona sin internet y la política CSP ya no permite CDN de scripts ni estilos.
- **Tema** `sigsf-ui.css`: azul marino, petróleo y dorado; tipografía IBM Plex Sans con cifras tabulares;
  estados con texto y color (no solo color); foco visible; respeta «reducir movimiento».
- **Motor de módulos** `sigsf-crud.js`: la misma configuración de siempre ahora genera formularios con
  validación, tablas con orden por columna, paginación (10/25/50/100), búsqueda con espera, avisos y
  confirmaciones en ventana modal. Las 12 pantallas de gestión lo usan sin cambiar su lógica.
- **Panel** `index.html`: menú lateral agrupado (atención y caja, gestión, dirección, administración),
  menú desplegable en celulares, campana con pagos por verificar, indicadores, gráfico de cobros de 14 días
  y enlace directo a cada módulo (`index.html#pagos.html`).
- **Servidor**: caché de 7 días para `web/vendor`, `ETag` y compresión gzip para HTML/CSS/JS y más hilos
  de atención para soportar más usuarios a la vez.

## 6b. Capa animada (v5.3.0)

`sigsf-anim.css` y `sigsf-anim.js` se cargan después del tema en las 26 páginas. No requieren cambios en los módulos:
`sigsf-anim.js` observa la página y anima solo lo nuevo (indicadores que cuentan, tarjetas y filas que entran escalonadas,
ícono automático según el texto de cada indicador, onda al pulsar botones). Expone `SIGSF.celebrar()` (confeti) y
`SIGSF.checkAnimado()` (marca dibujada), que usan la confirmación de pagos y el portal. Con «reducir movimiento» activado
en Windows/macOS/Android/iOS las animaciones se desactivan.

## 7. Pruebas

`PagoServiceTest` (14), `CotizacionServiceTest` (7) y `PagosApiTest` (4): **151 pruebas en total**.
Además se corrigió `scripts/compilar_y_probar.*`, que no copiaba `semilla_100.tsv` al classpath y hacía
fallar 9 pruebas en GitHub Actions.
