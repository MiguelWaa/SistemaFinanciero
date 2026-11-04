/* ============================================================
   SIGSF · Documentos imprimibles (v5.2)
   Arma, a partir de la API, los documentos que se imprimen o se
   guardan como PDF desde el navegador:
     ?tipo=pago&id=N           Comprobante de pago
     ?tipo=cotizacion&id=N     Cotización con cronograma
     ?tipo=estado-cuenta       Estado de cuenta (solo Portal del Cliente)
     ?tipo=reporte-pagos&...   Reporte de pagos con filtros (solo personal)
   Lo usan documento.html (personal) y portal-documento.html (cliente).
   ============================================================ */
(function (S) {
  'use strict';
  var esc = S.escapar, $ = function (i) { return document.getElementById(i); };
  var q = new URLSearchParams(location.search);
  var tipo = q.get('tipo'), id = parseInt(q.get('id'), 10);
  var esPortal = (location.pathname.split('/').pop() || '').indexOf('portal') === 0;
  var cliente = null;
  if (esPortal) { try { cliente = JSON.parse(sessionStorage.getItem('sigsf_cliente') || 'null'); } catch (e) { } }
  var base = esPortal && cliente ? '/portal/' + cliente.idCliente : '';

  function cabecera(titulo, numero, extra) {
    return '<div class="doc-head"><div class="doc-brand"><span class="mark"><i class="fa-solid fa-landmark"></i></span>'
      + '<div><b>SIGSF</b><small>Servicios financieros · cambio, préstamos y empeños</small></div></div>'
      + '<div class="doc-id"><div class="tipo">' + esc(titulo) + '</div><div class="num">' + esc(numero || '') + '</div>'
      + (extra ? '<div class="small text-muted">' + extra + '</div>' : '') + '</div></div>';
  }
  function dato(l, v) { return '<div><div class="l">' + esc(l) + '</div><div class="v">' + (v === null || v === undefined || v === '' ? '—' : v) + '</div></div>'; }
  function pie(texto) {
    return '<div class="doc-foot">' + texto + '<br>Emitido el ' + esc(new Date().toLocaleString('es-PE')) + ' desde SIGSF.</div>';
  }

  // ---------------- Comprobante de pago ----------------
  async function comprobante() {
    var p = await API.get((esPortal ? base + '/pagos/' : '/pagos/') + id);
    document.title = (p.numeroComprobante || p.codigo) + ' · Comprobante de pago';
    var confirmado = p.estado === 'CONFIRMADO';
    var h = cabecera(confirmado ? 'Comprobante de pago' : 'Constancia de registro de pago', confirmado ? p.numeroComprobante : p.codigo,
      'Pago ' + esc(p.codigo) + ' · ' + esc(S.fechaHora(p.fechaPago)));
    if (!confirmado) {
      h += '<div class="alert ' + (p.estado === 'RECHAZADO' ? 'alert-danger' : 'alert-warning') + '">'
        + (p.estado === 'RECHAZADO' ? 'Este pago fue <b>rechazado</b>' + (p.observacion ? ': ' + esc(p.observacion) : '.')
          : 'Este pago está <b>por verificar</b>. El comprobante se emite cuando caja confirma el abono en su billetera.') + '</div>';
    }
    h += '<div class="doc-grid">' + dato('Cliente', esc(p.cliente)) + dato('Documento', esc(p.documento))
      + dato('Concepto', esc(S.concepto(p.concepto))) + dato('Trámite', p.idOperacion ? 'N° ' + p.idOperacion : '—') + '</div>';
    h += '<table class="table"><thead><tr><th>Descripción</th><th>Método</th><th>N° de operación</th><th class="num">Importe</th></tr></thead><tbody>'
      + '<tr><td>' + esc(p.descripcion || S.concepto(p.concepto)) + '<span class="sub">Referencia ' + esc(p.referencia || '—') + '</span></td>'
      + '<td>' + S.wallet(p.metodo) + '</td><td><code>' + esc(p.numeroOperacion || '—') + '</code></td><td class="num">' + S.money(p.monto) + '</td></tr></tbody></table>';
    h += '<div class="doc-total"><span>Total pagado</span><span class="v">' + S.money(p.monto) + '</span></div>';
    h += '<div class="doc-grid">' + dato('Estado', S.badge(p.estado)) + dato('Registrado desde', p.origen === 'CAJA' ? 'Caja (ventanilla)' : 'Portal del Cliente')
      + dato('Verificado por', esc(p.verificadoPor)) + dato('Fecha de verificación', esc(S.fechaHora(p.fechaVerificacion))) + '</div>';
    if (confirmado) h += '<div class="text-success mt-2"><span class="doc-sello">PAGADO</span></div>';
    h += pie('Comprobante interno de SIGSF. No reemplaza a la boleta o factura electrónica emitida ante SUNAT.');
    return h;
  }

  // ---------------- Cotización ----------------
  async function cotizacion() {
    var r = await API.get((esPortal ? base + '/cotizaciones/' : '/cotizaciones/') + id);
    var c = r.cotizacion;
    document.title = c.numero + ' · Cotización';
    var titulos = { PRESTAMO: 'Cotización de préstamo personal', EMPENO: 'Cotización de empeño', CAMBIO: 'Cotización de cambio de moneda' };
    var h = cabecera(titulos[c.tipo] || 'Cotización', c.numero, 'Emitida ' + esc(S.fechaHora(c.fechaEmision)) + ' · vigente hasta ' + esc(S.fecha(c.fechaVencimiento)));
    h += '<div class="doc-grid">' + dato('Cliente', esc(c.cliente)) + dato('Documento', esc(c.documento)) + dato('Estado', S.badge(c.estado))
      + dato('Origen', c.origen === 'CAJA' ? 'Ventanilla' : 'Portal del Cliente') + '</div>';
    if (c.tipo === 'PRESTAMO') {
      h += '<div class="doc-grid">' + dato('Monto solicitado', S.money(c.monto)) + dato('Plazo', c.plazo + ' meses')
        + dato('Tasa de interés', S.num(c.tasa) + ' % mensual (interés simple)') + dato('Interés total', S.money(c.totalInteres)) + '</div>';
      h += '<div class="doc-total"><span>Cuota mensual</span><span class="v">' + S.money(c.cuota) + '</span></div>';
      h += '<div class="d-flex justify-content-between small mb-2"><span>Total a pagar en ' + c.plazo + ' cuotas</span><b>' + S.money(c.totalPagar) + '</b></div>';
      h += '<h3>Cronograma de pagos referencial</h3><table class="table table-sm"><thead><tr><th>N°</th><th>Fecha</th><th class="num">Capital</th><th class="num">Interés</th><th class="num">Cuota</th><th class="num">Saldo</th></tr></thead><tbody>'
        + r.cronograma.map(function (f) {
          return '<tr><td>' + f.numero + '</td><td>' + esc(S.fecha(f.fecha)) + '</td><td class="num">' + S.money(f.capital) + '</td><td class="num">'
            + S.money(f.interes) + '</td><td class="num fw-600">' + S.money(f.cuota) + '</td><td class="num">' + S.money(f.saldo) + '</td></tr>';
        }).join('') + '</tbody></table>';
    } else if (c.tipo === 'EMPENO') {
      h += '<div class="doc-grid">' + dato('Artículo', esc(c.descripcion)) + dato('Valor estimado', c.valorEstimado ? S.money(c.valorEstimado) : 'Por tasar')
        + dato('Monto del préstamo', S.money(c.monto)) + dato('Plazo', c.plazo + ' días') + dato('Tasa', S.num(c.tasa) + ' % mensual')
        + dato('Interés estimado', S.money(c.totalInteres)) + '</div>';
      h += '<div class="doc-total"><span>Total estimado para recuperar el artículo</span><span class="v">' + S.money(c.totalPagar) + '</span></div>';
      h += '<p class="small text-muted">El monto final depende de la tasación del artículo en tienda.</p>';
    } else {
      h += '<div class="doc-grid">' + dato('Entregas', S.money(c.monto, c.moneda)) + dato('Tipo de cambio aplicado', esc(c.tipoCambio))
        + dato('Par', esc(c.moneda + ' → ' + c.monedaRecibir)) + '</div>';
      h += '<div class="doc-total"><span>Recibes</span><span class="v">' + S.money(c.montoRecibir, c.monedaRecibir) + '</span></div>';
      h += '<p class="small text-muted">El tipo de cambio puede variar durante el día; se respeta el de esta cotización hasta su vencimiento si hay disponibilidad.</p>';
    }
    h += pie('Cotización referencial sin valor contractual. Vigente ' + esc(r.vigenciaDias) + ' días; sujeta a evaluación crediticia y verificación de datos.');
    var acciones = '';
    if (esPortal && c.estado === 'VIGENTE') {
      acciones = '<button class="btn btn-sm btn-warning" id="btnSolicitar"><i class="fa-solid fa-paper-plane me-1"></i>Solicitar con esta cotización</button>';
    }
    return { html: h, acciones: acciones };
  }

  // ---------------- Estado de cuenta (cliente) ----------------
  async function estadoCuenta() {
    var r = await API.get(base + '/estado-cuenta');
    document.title = 'Estado de cuenta · ' + r.cliente.nombre;
    var h = cabecera('Estado de cuenta', r.cliente.documento, 'Al ' + esc(S.fechaHora(r.fecha)));
    h += '<div class="doc-grid">' + dato('Cliente', esc(r.cliente.nombre)) + dato('Teléfono', esc(r.cliente.telefono)) + dato('Correo', esc(r.cliente.correo))
      + dato('Dirección', esc(r.cliente.direccion)) + '</div>';
    h += '<div class="doc-grid">' + dato('Deuda vigente', '<span class="fs-5">' + S.money(r.deudaTotal) + '</span>')
      + dato('Total pagado', '<span class="fs-5 text-success">' + S.money(r.totalPagado) + '</span>')
      + dato('Pagos por verificar', '<span class="fs-5 text-warning">' + S.money(r.porVerificar) + '</span>') + '</div>';
    h += '<h3>Préstamos y empeños</h3><table class="table table-sm"><thead><tr><th>Trámite</th><th>Tipo</th><th class="num">Monto</th><th class="num">Cuota</th><th>Plazo</th><th>Vence</th><th class="num">Saldo</th><th>Estado</th></tr></thead><tbody>'
      + (r.deudas.length ? r.deudas.map(function (d) {
        return '<tr><td>N° ' + d.idOperacion + '</td><td>' + esc(d.tipo) + '</td><td class="num">' + S.money(d.monto) + '</td><td class="num">' + (d.cuota ? S.money(d.cuota) : '—')
          + '</td><td>' + esc(d.plazo) + '</td><td>' + esc(S.fecha(d.vencimiento)) + '</td><td class="num fw-600">' + S.money(d.saldo) + '</td><td>' + S.badge(d.estado) + '</td></tr>';
      }).join('') : '<tr class="empty-row"><td colspan="8">Sin préstamos ni empeños.</td></tr>') + '</tbody></table>';
    h += '<h3>Pagos realizados</h3><table class="table table-sm"><thead><tr><th>Fecha</th><th>Comprobante</th><th>Concepto</th><th>Método</th><th class="num">Monto</th><th>Estado</th></tr></thead><tbody>'
      + (r.pagos.length ? r.pagos.map(function (p) {
        return '<tr><td>' + esc(S.fechaHora(p.fechaPago)) + '</td><td>' + esc(p.numeroComprobante || p.codigo) + '</td><td>' + esc(S.concepto(p.concepto))
          + '</td><td>' + S.wallet(p.metodo) + '</td><td class="num">' + S.money(p.monto) + '</td><td>' + S.badge(p.estado) + '</td></tr>';
      }).join('') : '<tr class="empty-row"><td colspan="6">Aún no hay pagos.</td></tr>') + '</tbody></table>';
    h += pie('Documento informativo. Los saldos consideran solo los pagos confirmados por caja.');
    return h;
  }

  // ---------------- Reporte de pagos (personal) ----------------
  async function reportePagos() {
    var lista = await API.get('/pagos');
    var est = q.get('estado') || '', met = q.get('metodo') || '', d = q.get('desde') || '', hh = q.get('hasta') || '';
    lista = lista.filter(function (p) {
      var f = String(p.fechaPago || '').substring(0, 10);
      return (!est || p.estado === est) && (!met || p.metodo === met) && (!d || f >= d) && (!hh || f <= hh);
    });
    document.title = 'Reporte de pagos';
    var filtros = [est ? 'Estado: ' + S.etiqueta(est) : 'Todos los estados', met ? 'Método: ' + S.etiqueta(met) : 'Todos los métodos',
      d || hh ? 'Del ' + (d ? S.fecha(d) : 'inicio') + ' al ' + (hh ? S.fecha(hh) : 'hoy') : 'Todas las fechas'].join(' · ');
    var h = cabecera('Reporte de pagos y cobranzas', S.num(lista.length) + ' pagos', esc(filtros));
    var porMetodo = {}, porEstado = { POR_VERIFICAR: 0, CONFIRMADO: 0, RECHAZADO: 0 }, total = 0;
    lista.forEach(function (p) {
      porEstado[p.estado] = (porEstado[p.estado] || 0) + Number(p.monto);
      if (p.estado === 'CONFIRMADO') { porMetodo[p.metodo] = (porMetodo[p.metodo] || 0) + Number(p.monto); total += Number(p.monto); }
    });
    h += '<div class="doc-grid">' + dato('Confirmado', '<span class="fs-5 text-success">' + S.money(porEstado.CONFIRMADO) + '</span>')
      + dato('Por verificar', '<span class="fs-5 text-warning">' + S.money(porEstado.POR_VERIFICAR) + '</span>')
      + dato('Rechazado', '<span class="fs-5 text-danger">' + S.money(porEstado.RECHAZADO) + '</span>') + '</div>';
    h += '<h3>Cobrado por método</h3><table class="table table-sm"><thead><tr><th>Método</th><th class="num">Monto</th><th class="num">Participación</th></tr></thead><tbody>'
      + Object.keys(porMetodo).map(function (m) {
        return '<tr><td>' + S.wallet(m) + '</td><td class="num">' + S.money(porMetodo[m]) + '</td><td class="num">' + (total ? Math.round(100 * porMetodo[m] / total) : 0) + ' %</td></tr>';
      }).join('') + '<tr class="fw-600"><td>Total</td><td class="num">' + S.money(total) + '</td><td class="num">100 %</td></tr></tbody></table>';
    h += '<h3>Detalle</h3><table class="table table-sm"><thead><tr><th>Código</th><th>Fecha</th><th>Cliente</th><th>Concepto</th><th>Método</th><th>N° op.</th><th class="num">Monto</th><th>Estado</th></tr></thead><tbody>'
      + lista.map(function (p) {
        return '<tr><td>' + esc(p.codigo) + '</td><td class="text-nowrap">' + esc(S.fechaHora(p.fechaPago)) + '</td><td>' + esc(p.cliente) + '<span class="sub">' + esc(p.documento || '') + '</span></td><td>'
          + esc(S.concepto(p.concepto)) + '</td><td>' + esc(S.etiqueta(p.metodo)) + '</td><td>' + esc(p.numeroOperacion || '—') + '</td><td class="num">' + S.money(p.monto) + '</td><td>' + S.badge(p.estado) + '</td></tr>';
      }).join('') + '</tbody></table>';
    h += pie('Reporte generado por el módulo Pagos y cobranzas.');
    return h;
  }

  async function iniciar() {
    var hoja = $('hoja'), acciones = '';
    try {
      var r;
      if (tipo === 'pago' && id) r = await comprobante();
      else if (tipo === 'cotizacion' && id) { var x = await cotizacion(); r = x.html; acciones = x.acciones; }
      else if (tipo === 'estado-cuenta' && esPortal) r = await estadoCuenta();
      else if (tipo === 'reporte-pagos' && !esPortal) r = await reportePagos();
      else throw new Error('Documento no válido.');
      hoja.innerHTML = r;
      $('acciones').innerHTML = acciones;
      var bs = $('btnSolicitar');
      if (bs) bs.addEventListener('click', async function () {
        var ok = await S.confirmar('Se enviará una solicitud con los datos de esta cotización. La entidad la evaluará.', { titulo: 'Enviar solicitud', ok: 'Enviar solicitud' });
        if (!ok) return;
        try {
          var res = await API.post(base + '/cotizaciones/' + id + '/solicitar', {});
          S.toast(res.mensaje, 'ok');
          setTimeout(function () { location.reload(); }, 1200);
        } catch (e) { S.toast(e.message, 'err'); }
      });
      if (q.get('imprimir') === '1') setTimeout(function () { window.print(); }, 400);
    } catch (e) {
      hoja.innerHTML = '<div class="alert alert-danger mb-0"><i class="fa-solid fa-triangle-exclamation me-1"></i>No se pudo generar el documento: ' + esc(e.message) + '</div>';
    }
  }
  $('btnImprimir').addEventListener('click', function () { window.print(); });
  $('btnCerrar').addEventListener('click', function () { if (window.opener) window.close(); else history.back(); });
  iniciar();
})(window.SIGSF);
