(function () {
  'use strict';
  var S = SIGSF, esc = S.escapar, $ = function (i) { return document.getElementById(i); };
  S.embebido();

  var DESCARGAS = [
    ['fa-qrcode', 'Pagos y cobranzas', 'Pagos con Yape, Plin, Tunki y caja: cliente, N° de operación, estado y comprobante.', 'pagos'],
    ['fa-file-invoice-dollar', 'Cotizaciones', 'Cotizaciones emitidas desde el portal y la ventanilla.', 'cotizaciones'],
    ['fa-cart-shopping', 'Ventas', 'Ventas con cliente, servicio, fecha e importe.', 'ventas'],
    ['fa-users', 'Clientes', 'Padrón de clientes con datos de contacto.', 'clientes'],
    ['fa-right-left', 'Operaciones', 'Cambios, préstamos y empeños con montos y estados.', 'operaciones'],
    ['fa-hand-holding-dollar', 'Préstamos', 'Cuotas, saldos y vencimientos.', 'prestamos'],
    ['fa-gem', 'Empeños', 'Montos, plazos y saldos de los empeños.', 'empenos'],
    ['fa-boxes-stacked', 'Almacén', 'Productos con stock actual y mínimo.', 'productos']
  ];
  $('descargas').innerHTML = DESCARGAS.map(function (d) {
    return '<tr><td class="fw-600 text-nowrap"><i class="fa-solid ' + d[0] + ' text-primary me-2"></i>' + esc(d[1]) + '</td><td class="text-muted">' + esc(d[2]) + '</td>'
      + '<td class="text-end text-nowrap"><div class="btn-group btn-group-sm">'
      + ['xlsx', 'csv', 'json'].map(function (f) { return '<button class="btn btn-' + (f === 'xlsx' ? 'success' : 'outline-secondary') + '" data-rec="' + d[3] + '" data-f="' + f + '">' + (f === 'xlsx' ? 'Excel' : f.toUpperCase()) + '</button>'; }).join('')
      + '</div></td></tr>';
  }).join('');
  $('descargas').addEventListener('click', function (e) {
    var b = e.target.closest('[data-rec]'); if (!b) return;
    b.disabled = true;
    API.descargar('/' + b.dataset.rec + '/exportar?formato=' + b.dataset.f).then(function () { S.toast('Descarga lista.', 'ok'); })
      .catch(function (err) { S.toast('No se pudo descargar: ' + err.message, 'err'); }).finally(function () { b.disabled = false; });
  });

  function hoyISO() { var d = new Date(); return new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().substring(0, 10); }
  document.querySelectorAll('[data-rango]').forEach(function (b) {
    b.addEventListener('click', function () {
      var h = hoyISO(); $('rHasta').value = h; $('rDesde').value = b.dataset.rango === 'hoy' ? h : h.substring(0, 8) + '01';
    });
  });
  $('formRep').addEventListener('submit', function (e) {
    e.preventDefault();
    var q = new URLSearchParams({ tipo: 'reporte-pagos', estado: $('rEstado').value, metodo: $('rMetodo').value, desde: $('rDesde').value, hasta: $('rHasta').value });
    S.abrirDocumento('documento.html?' + q.toString());
  });

  async function cargarKPIs() {
    try {
      var d = await API.dashboard();
      $('kpis').innerHTML = S.kpi('Cobrado este mes', S.money(d.cobradoMes), 'gold', 'Yape, Plin, Tunki y caja')
        + S.kpi('Pagos por verificar', S.num(d.pagosPorVerificar), d.pagosPorVerificar ? 'warn' : 'ok', S.money(d.montoPorVerificar))
        + S.kpi('Ingresos por ventas del mes', S.money(d.ingresosDelMes))
        + S.kpi('Préstamos colocados', S.money(d.prestamosColocados))
        + S.kpi('Cotizaciones vigentes', S.num(d.cotizacionesVigentes))
        + S.kpi('Productos con stock bajo', S.num(d.productosStockBajo), d.productosStockBajo ? 'bad' : 'ok');
    } catch (e) { $('kpis').innerHTML = S.kpi('Indicadores', 'No disponibles', 'bad'); }
  }
  async function cargarConceptos() {
    try {
      var r = await API.get('/pagos/resumen'), total = r.porConcepto.reduce(function (a, c) { return a + Number(c.monto); }, 0) || 1;
      $('porConcepto').innerHTML = r.porConcepto.length ? r.porConcepto.map(function (c) {
        var pct = Math.round(100 * Number(c.monto) / total);
        return '<div class="d-flex justify-content-between small mb-1"><span class="fw-600">' + esc(S.concepto(c.concepto)) + ' <span class="text-muted fw-normal">· ' + S.num(c.cantidad) + ' pagos</span></span>'
          + '<span class="fw-600">' + S.money(c.monto) + '</span></div><div class="sg-bar mb-3"><span style="width:' + pct + '%;background:var(--sg-primary)"></span></div>';
      }).join('') + '<div class="d-flex justify-content-between border-top pt-2"><span>Total cobrado</span><b>' + S.money(r.cobradoTotal) + '</b></div>'
        : '<div class="small-muted">Aún no hay cobros confirmados.</div>';
    } catch (e) { $('porConcepto').innerHTML = '<div class="small text-danger">' + esc(e.message) + '</div>'; }
  }

  var ops = [];
  function pintar() {
    var q = $('buscar').value.trim().toLowerCase();
    var l = q ? ops.filter(function (o) { return JSON.stringify(o).toLowerCase().indexOf(q) !== -1; }) : ops;
    $('tablaOps').innerHTML = l.length ? l.slice(0, 50).map(function (o) {
      return '<tr><td>' + o.idOperacion + '</td><td>' + esc(S.etiqueta(o.tipoOperacion)) + '</td><td class="num">' + S.money(o.monto) + '</td><td>' + S.badge(o.estado) + '</td><td>' + esc(S.fechaHora(o.fechaOperacion)) + '</td></tr>';
    }).join('') : '<tr class="empty-row"><td colspan="5">Sin operaciones.</td></tr>';
  }
  $('buscar').addEventListener('input', S.debounce(pintar));
  API.get('/operaciones').then(function (l) { ops = l; pintar(); })
    .catch(function (e) { $('tablaOps').innerHTML = '<tr class="empty-row"><td colspan="5" class="text-danger">' + esc(e.message) + '</td></tr>'; });
  cargarKPIs(); cargarConceptos();
})();
