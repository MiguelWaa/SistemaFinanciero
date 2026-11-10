(function () {
  'use strict';
  var S = SIGSF, esc = S.escapar, $ = function (i) { return document.getElementById(i); };
  var cliente = null, tipos = [], billeteras = [], pagables = [], operaciones = [], misPagos = [], cotizaciones = [];
  var pago = { idx: null, metodo: null };

  try { cliente = JSON.parse(sessionStorage.getItem('sigsf_cliente') || 'null'); } catch (e) { }
  if (!cliente || !cliente.idCliente) { location.href = 'portal.html'; return; }
  var BASE = '/portal/' + cliente.idCliente;

  function pintarCabecera() {
    var nombre = ((cliente.nombres || '') + ' ' + (cliente.apellidos || '')).trim();
    $('hNombre').textContent = nombre || 'Cliente';
    $('hDoc').textContent = (cliente.tipoDocumento || '') + ' ' + (cliente.documento || '');
    $('saludo').textContent = cliente.nombres || 'cliente';
    $('mdTel').value = cliente.telefono || ''; $('mdCorreo').value = cliente.correo || ''; $('mdDir').value = cliente.direccion || '';
  }
  pintarCabecera();
  $('btnSalir').addEventListener('click', function () { API.logout(); });

  // ---------------- Pestañas ----------------
  function mostrar(tab) {
    document.querySelectorAll('#tabs [data-tab]').forEach(function (b) { b.classList.toggle('active', b.dataset.tab === tab); });
    document.querySelectorAll('[data-pane]').forEach(function (p) { p.hidden = p.dataset.pane !== tab; });
    history.replaceState(null, '', '#' + tab);
    window.scrollTo({ top: 0 });
  }
  document.querySelectorAll('#tabs [data-tab]').forEach(function (b) { b.addEventListener('click', function () { mostrar(b.dataset.tab); }); });
  document.addEventListener('click', function (e) { var b = e.target.closest('[data-ir]'); if (b) { e.preventDefault(); mostrar(b.dataset.ir); } });

  // ---------------- Datos ----------------
  async function cargarTodo() {
    var r = await Promise.allSettled([API.get(BASE + '/pagables'), API.get(BASE + '/operaciones'), API.get(BASE + '/pagos'), API.get(BASE + '/estado-cuenta')]);
    pagables = r[0].status === 'fulfilled' ? r[0].value : [];
    operaciones = r[1].status === 'fulfilled' ? r[1].value : [];
    misPagos = r[2].status === 'fulfilled' ? r[2].value : [];
    if (r[3].status === 'fulfilled') {
      var ec = r[3].value;
      $('rDeuda').textContent = S.money(ec.deudaTotal);
      $('rVerif').textContent = S.money(ec.porVerificar);
      $('rPagado').textContent = S.money(ec.totalPagado);
    }
    var pend = operaciones.filter(function (o) { return o.estado === 'PENDIENTE'; }).length;
    $('rPend').textContent = S.num(pend); $('rTram').textContent = S.num(operaciones.length) + ' trámites en total';
    pintarInicio(); pintarPagos(); pintarTramites();
    // No se repinta la lista mientras el cliente está en medio de un pago (no se pierde lo que escribió).
    if (!primeraCarga || document.querySelector('[data-pane="pagar"]').hidden) pintarPagables();
    primeraCarga = true;
  }
  var primeraCarga = false;

  function pintarInicio() {
    $('inicioPagables').innerHTML = pagables.length ? pagables.map(function (p, i) {
      return '<li><div class="who"><b>' + esc(p.titulo) + '</b><span>' + esc(p.detalle) + '</span></div>'
        + '<span class="amt">' + S.money(p.montoSugerido) + '</span><button class="btn btn-sm btn-primary" data-pagar="' + i + '">Pagar</button></li>';
    }).join('') : '<li class="sg-empty"><i class="fa-solid fa-circle-check"></i>No tienes pagos pendientes. Si quieres pagar otro concepto, usa “Pagar con QR”.</li>';
    var mov = misPagos.slice(0, 4).map(function (p) {
      return '<li>' + S.wallet(p.metodo) + '<div class="who"><b>' + esc(S.concepto(p.concepto)) + '</b><span>' + esc(S.fechaHora(p.fechaPago)) + '</span></div>'
        + '<div class="text-end"><div class="amt">' + S.money(p.monto) + '</div>' + S.badge(p.estado) + '</div></li>';
    });
    operaciones.slice(0, 6 - mov.length).forEach(function (o) {
      mov.push('<li><span class="badge text-bg-light border">' + esc(S.etiqueta(o.tipoOperacion)) + '</span><div class="who"><b>Solicitud N° ' + o.idOperacion
        + '</b><span>' + esc(S.fechaHora(o.fechaOperacion)) + '</span></div><div class="text-end"><div class="amt">' + S.money(o.monto) + '</div>' + S.badge(o.estado) + '</div></li>');
    });
    $('inicioMov').innerHTML = mov.join('') || '<li class="sg-empty">Aún no tienes movimientos.</li>';
  }
  $('inicioPagables').addEventListener('click', function (e) {
    var b = e.target.closest('[data-pagar]'); if (!b) return;
    pago.idx = parseInt(b.dataset.pagar, 10); pintarPagables(); paso(1); mostrar('pagar');
  });

  // ================= PAGAR CON QR =================
  function paso(n) {
    document.querySelectorAll('[data-step]').forEach(function (x) { x.hidden = String(n) !== x.dataset.step; });
    document.querySelectorAll('[data-paso]').forEach(function (x) {
      var k = parseInt(x.dataset.paso, 10); x.classList.toggle('on', k === n); x.classList.toggle('done', k < n);
    });
  }
  document.querySelectorAll('[data-volver]').forEach(function (b) { b.addEventListener('click', function () { paso(parseInt(b.dataset.volver, 10)); }); });

  function pintarPagables() {
    var h = pagables.map(function (p, i) {
      return '<label class="pagable' + (pago.idx === i ? ' sel' : '') + '"><input class="form-check-input" type="radio" name="pag" value="' + i + '"' + (pago.idx === i ? ' checked' : '') + '>'
        + '<div><div class="t">' + esc(p.titulo) + '</div><div class="d">' + esc(p.detalle) + '</div></div><div class="m">' + S.money(p.montoSugerido) + '</div></label>';
    }).join('');
    h += '<label class="pagable' + (pago.idx === 'OTRO' ? ' sel' : '') + '"><input class="form-check-input" type="radio" name="pag" value="OTRO"' + (pago.idx === 'OTRO' ? ' checked' : '') + '>'
      + '<div><div class="t">Otro pago</div><div class="d">Adelantos, comisiones u otros conceptos acordados con la entidad.</div></div></label>';
    $('listaPagables').innerHTML = h;
    if (pago.idx === null && pagables.length) elegir(0); else elegir(pago.idx);
  }
  function elegir(idx) {
    pago.idx = idx;
    document.querySelectorAll('.pagable').forEach(function (l) { var i = l.querySelector('input'); l.classList.toggle('sel', i.value === String(idx)); i.checked = i.value === String(idx); });
    var p = pagables[idx];
    $('otroWrap').classList.toggle('d-none', idx !== 'OTRO');
    if (p) { $('pMonto').value = Number(p.montoSugerido).toFixed(2); $('pMonto').max = p.montoMaximo; $('pMax').textContent = 'Puedes pagar hasta ' + S.money(p.montoMaximo) + '.'; }
    else { $('pMax').textContent = ''; $('pMonto').removeAttribute('max'); }
  }
  $('listaPagables').addEventListener('change', function (e) { if (e.target.name === 'pag') elegir(e.target.value === 'OTRO' ? 'OTRO' : parseInt(e.target.value, 10)); });

  var INICIAL = { YAPE: 'Y', PLIN: 'P', TUNKI: 'T' };
  async function cargarBilleteras() {
    try {
      billeteras = await API.get(BASE + '/billeteras');
      var orden = ['YAPE', 'PLIN', 'TUNKI'];
      $('billeteras').innerHTML = orden.map(function (c) {
        var b = billeteras.filter(function (x) { return x.codigo === c; })[0];
        return '<div><input type="radio" name="wal" id="w' + c + '" value="' + c + '"' + (b ? '' : ' disabled') + '><label for="w' + c + '">'
          + '<span class="dot" style="background:var(--sg-' + c.toLowerCase() + ')">' + INICIAL[c] + '</span>' + S.etiqueta(c)
          + (b ? '' : '<div class="small text-muted fw-normal">No disponible</div>') + '</label></div>';
      }).join('');
      var primera = document.querySelector('input[name="wal"]:not(:disabled)');
      if (primera) { primera.checked = true; pago.metodo = primera.value; }
      document.querySelectorAll('input[name="wal"]').forEach(function (r) { r.addEventListener('change', function () { pago.metodo = r.value; }); });
    } catch (e) { $('billeteras').innerHTML = '<div class="text-danger small">' + esc(e.message) + '</div>'; }
  }

  function errorPaso(id, msg) { var e = $(id); e.textContent = msg || ''; e.classList.toggle('d-none', !msg); }

  $('btnPaso2').addEventListener('click', function () {
    var monto = parseFloat($('pMonto').value), p = pagables[pago.idx];
    if (pago.idx === null || pago.idx === undefined) { errorPaso('p1Err', 'Elige qué vas a pagar.'); return; }
    if (!(monto > 0)) { errorPaso('p1Err', 'Ingresa el monto a pagar.'); $('pMonto').focus(); return; }
    if (p && monto > Number(p.montoMaximo) + 0.001) { errorPaso('p1Err', 'El monto supera lo pendiente (' + S.money(p.montoMaximo) + ').'); return; }
    if (pago.idx === 'OTRO' && !$('pDesc').value.trim()) { errorPaso('p1Err', 'Cuéntanos qué estás pagando.'); $('pDesc').focus(); return; }
    if (!pago.metodo) { errorPaso('p1Err', 'Elige con qué billetera pagarás.'); return; }
    errorPaso('p1Err', '');
    pago.monto = Math.round(monto * 100) / 100;
    pago.sel = p || null;
    pago.descripcion = $('pDesc').value.trim();
    pago.referencia = 'C' + cliente.idCliente + (p ? '-OP' + p.idOperacion : '');
    pintarTicket(); paso(2);
  });

  function pintarTicket() {
    var b = billeteras.filter(function (x) { return x.codigo === pago.metodo; })[0] || {};
    var t = $('ticket');
    t.className = 'pay-ticket ' + pago.metodo;
    t.innerHTML = '<div class="top"><span class="brand">' + esc(b.nombre || S.etiqueta(pago.metodo)) + '</span><span class="small">Pago a ' + esc(b.titular || 'SIGSF') + '</span></div>'
      + '<div class="amount"><span class="cur">S/</span><span class="val">' + Number(pago.monto).toLocaleString('es-PE', { minimumFractionDigits: 2 }) + '</span>'
      + '<div class="small text-muted mt-1">Paga el monto exacto</div></div>'
      + '<div class="qr" id="qrBox"></div>'
      + (b.tieneQr ? '' : '<div class="demo">QR de referencia con los datos del pago. Si tu app no lo reconoce, paga al número indicado.</div>')
      + '<div class="perf"></div>'
      + '<dl><dt>Número</dt><dd><span class="copiable">' + esc(b.numeroCelular || '—') + '<button type="button" data-copiar="' + esc(b.numeroCelular || '') + '" title="Copiar número" aria-label="Copiar número"><i class="fa-regular fa-copy"></i></button></span></dd>'
      + '<dt>Titular</dt><dd>' + esc(b.titular || '—') + '</dd>'
      + '<dt>Mensaje / referencia</dt><dd><span class="copiable"><span class="ref">' + esc(pago.referencia) + '</span><button type="button" data-copiar="' + esc(pago.referencia) + '" title="Copiar referencia" aria-label="Copiar referencia"><i class="fa-regular fa-copy"></i></button></span></dd>'
      + '<dt>Concepto</dt><dd>' + esc(pago.sel ? pago.sel.titulo : pago.descripcion) + '</dd></dl>';
    var box = $('qrBox');
    if (b.tieneQr) { var img = new Image(); img.alt = 'QR de cobro de ' + (b.nombre || ''); img.src = b.qrImagen; box.appendChild(img); }
    else S.qr(box, (b.nombre || pago.metodo) + ' ' + (b.numeroCelular || '') + ' | ' + (b.titular || 'SIGSF') + ' | S/ ' + pago.monto.toFixed(2) + ' | Ref ' + pago.referencia);
    var pasos = [
      'Abre <b>' + esc(b.nombre || pago.metodo) + '</b> en tu celular y elige pagar o escanear QR.',
      'Escanea el código o paga al número <b>' + esc(b.numeroCelular || '') + '</b> a nombre de <b>' + esc(b.titular || '') + '</b>.',
      'Ingresa exactamente <b>' + S.money(pago.monto) + '</b> y escribe <b>' + esc(pago.referencia) + '</b> en el mensaje.',
      'Al terminar, anota el <b>número de operación</b> que te muestra la app y regístralo en el siguiente paso.'
    ];
    $('instrucciones').innerHTML = pasos.map(function (x) { return '<li class="mb-1">' + x + '</li>'; }).join('');
    $('instrBil').textContent = b.instrucciones || '';
    $('instrBil').hidden = !b.instrucciones;
  }
  $('ticket').addEventListener('click', function (e) {
    var b = e.target.closest('[data-copiar]'); if (!b) return;
    var txt = b.dataset.copiar;
    (navigator.clipboard ? navigator.clipboard.writeText(txt) : Promise.reject()).then(function () { S.toast('Copiado: ' + txt, 'info'); })
      .catch(function () { S.toast('No se pudo copiar. Escríbelo manualmente: ' + txt, 'info'); });
  });

  $('btnPaso3').addEventListener('click', function () {
    $('resumenPago').innerHTML = '<span>' + S.wallet(pago.metodo) + '</span><span class="fw-600">' + S.money(pago.monto) + '</span>';
    if (!$('pCelular').value && cliente.telefono) $('pCelular').value = cliente.telefono;
    paso(3); $('pNumero').focus();
  });

  var constancia = null;
  $('pFoto').addEventListener('change', async function () {
    constancia = null; $('pFotoPrev').innerHTML = '';
    var f = this.files[0]; if (!f) return;
    try {
      constancia = await S.imagenADataUrl(f, 1100, 0.72);
      $('pFotoPrev').innerHTML = '<img alt="Vista previa de la constancia" src="' + constancia + '">';
    } catch (e) { S.toast(e.message, 'err'); this.value = ''; }
  });

  $('formPago').addEventListener('submit', async function (ev) {
    ev.preventDefault();
    var num = $('pNumero').value.trim().replace(/\s/g, '');
    var valido = /^[A-Za-z0-9-]{4,30}$/.test(num);
    $('pNumero').classList.toggle('is-invalid', !valido);
    if (!valido) { $('pNumero').focus(); return; }
    var p = pago.sel;
    var datos = { metodo: pago.metodo, monto: pago.monto, numeroOperacion: num, celularOrigen: $('pCelular').value.trim(), constancia: constancia };
    if (p) { datos.idOperacion = p.idOperacion; datos.concepto = p.concepto; }
    else { datos.concepto = 'OTRO'; datos.descripcion = pago.descripcion; }
    var btn = $('btnRegistrar'); btn.disabled = true; errorPaso('p3Err', '');
    try {
      var r = await API.post(BASE + '/pagos', datos);
      var ok = r.estado === 'CONFIRMADO';
      $('pagoListo').innerHTML = (ok && S.checkAnimado ? S.checkAnimado() : '<div class="mb-2"><i class="fa-solid fa-hourglass-half sg-reloj"></i></div>')
        + '<h2 class="h4">' + (ok ? 'Pago confirmado' : 'Pago registrado') + '</h2>'
        + '<p class="text-muted mb-3">' + (ok ? 'Tu pago ya se aplicó. Puedes descargar tu comprobante.'
          : 'Caja verificará tu pago con ' + esc(S.etiqueta(r.metodo)) + ' y lo verás confirmado en “Mis pagos”. Te recomendamos guardar tu constancia.') + '</p>'
        + '<div class="d-inline-grid text-start small mb-3" style="grid-template-columns:auto auto;gap:.25rem 1.25rem;display:inline-grid">'
        + '<span class="text-muted">Código</span><b>' + esc(r.codigo) + '</b><span class="text-muted">Monto</span><b>' + S.money(r.monto) + '</b>'
        + '<span class="text-muted">N° de operación</span><b>' + esc(r.numeroOperacion) + '</b><span class="text-muted">Estado</span><span>' + S.badge(r.estado) + '</span></div>'
        + '<div class="d-flex flex-wrap gap-2 justify-content-center"><button class="btn btn-outline-primary" id="verConst"><i class="fa-solid fa-receipt me-1"></i>' + (ok ? 'Ver comprobante' : 'Ver constancia de registro') + '</button>'
        + '<button class="btn btn-primary" id="otroPago">Hacer otro pago</button></div>';
      $('verConst').addEventListener('click', function () { S.abrirDocumento('portal-documento.html?tipo=pago&id=' + r.idPago); });
      $('otroPago').addEventListener('click', function () { reiniciarPago(); });
      paso(4);
      S.toast(ok ? 'Pago confirmado.' : 'Pago registrado. Queda por verificar.', 'ok');
      if (S.celebrar) S.celebrar();
      cargarTodo();
    } catch (e) { errorPaso('p3Err', e.message); }
    btn.disabled = false;
  });
  function reiniciarPago() {
    pago.idx = null; constancia = null;
    $('formPago').reset(); $('pFotoPrev').innerHTML = ''; $('pDesc').value = '';
    pintarPagables(); paso(1);
  }

  // ================= COTIZAR =================
  // Mismas tasas que PortalService (15 % mensual préstamo, 10 % mensual empeño); el servidor hace el cálculo oficial.
  var TASA_PRESTAMO = 15, TASA_EMPENO = 10;
  function tipoCot() { return document.querySelector('input[name="qTipo"]:checked').value; }
  function estimar() {
    var t = tipoCot(), m = parseFloat($('qMonto').value), out = $('qEstimado');
    document.querySelectorAll('[data-q]').forEach(function (x) { x.hidden = x.dataset.q !== t; });
    $('qMontoLbl').textContent = t === 'CAMBIO' ? 'Monto a cambiar' : 'Monto que necesitas';
    var tcSel = tipos.filter(function (x) { return String(x.idTipoCambio) === $('qTc').value; })[0];
    $('qMon').textContent = t === 'CAMBIO' && tcSel ? tcSel.monedaOrigen : 'S/';
    if (!(m > 0)) { out.innerHTML = '<span class="text-muted small">Ingresa un monto para ver el cálculo.</span>'; return; }
    if (t === 'PRESTAMO') {
      var n = parseInt($('qPlazo').value, 10), total = m * (1 + TASA_PRESTAMO / 100 * n);
      out.innerHTML = '<div class="small text-muted">Cuota mensual estimada</div><div class="big">' + S.money(total / n) + '</div>'
        + '<div class="small">Total ' + S.money(total) + ' en ' + n + ' cuotas · tasa ' + TASA_PRESTAMO + ' % mensual</div>';
    } else if (t === 'EMPENO') {
      var d = parseInt($('qDias').value, 10), interes = m * TASA_EMPENO / 100 * d / 30;
      out.innerHTML = '<div class="small text-muted">Para recuperar tu artículo pagarías aprox.</div><div class="big">' + S.money(m + interes) + '</div>'
        + '<div class="small">Interés ' + S.money(interes) + ' por ' + d + ' días</div>';
    } else if (tcSel) {
      out.innerHTML = '<div class="small text-muted">Recibirías aprox.</div><div class="big">' + S.money(m * Number(tcSel.venta), tcSel.monedaDestino) + '</div>'
        + '<div class="small">Tipo de cambio ' + esc(tcSel.venta) + '</div>';
    }
  }
  ['qMonto', 'qPlazo', 'qDias', 'qTc'].forEach(function (id) { $(id).addEventListener('input', estimar); $(id).addEventListener('change', estimar); });
  document.querySelectorAll('input[name="qTipo"]').forEach(function (r) { r.addEventListener('change', estimar); });

  $('formCot').addEventListener('submit', async function (ev) {
    ev.preventDefault();
    var t = tipoCot(), datos = { tipo: t, monto: parseFloat($('qMonto').value) };
    if (t === 'PRESTAMO') datos.plazo = parseInt($('qPlazo').value, 10);
    if (t === 'EMPENO') { datos.plazo = parseInt($('qDias').value, 10); datos.descripcion = $('qArt').value.trim(); datos.valorEstimado = parseFloat($('qValor').value) || null; }
    if (t === 'CAMBIO') datos.idTipoCambio = parseInt($('qTc').value, 10);
    try {
      var q = await API.post(BASE + '/cotizaciones', datos);
      S.toast('Cotización ' + q.numero + ' generada.', 'ok');
      S.abrirDocumento('portal-documento.html?tipo=cotizacion&id=' + q.idCotizacion);
      cargarCotizaciones();
    } catch (e) { S.toast(e.message, 'err'); }
  });

  async function cargarCotizaciones() {
    try {
      cotizaciones = await API.get(BASE + '/cotizaciones');
      $('tablaCot').innerHTML = cotizaciones.length ? cotizaciones.map(function (q) {
        var res = q.tipo === 'PRESTAMO' ? S.money(q.cuota) + '<span class="sub">cuota · ' + q.plazo + ' meses</span>'
          : q.tipo === 'CAMBIO' ? S.money(q.montoRecibir, q.monedaRecibir) + '<span class="sub">recibes</span>' : S.money(q.totalPagar) + '<span class="sub">para recuperar</span>';
        return '<tr><td class="fw-600 text-nowrap">' + esc(q.numero) + '</td><td>' + esc(S.etiqueta(q.tipo)) + '</td><td class="num">' + S.money(q.monto, q.moneda) + '</td><td class="num">' + res + '</td>'
          + '<td>' + S.badge(q.estado) + '<span class="sub">hasta ' + esc(S.fecha(q.fechaVencimiento)) + '</span></td><td class="text-end text-nowrap">'
          + '<button class="btn btn-sm btn-light btn-icon" data-ver="' + q.idCotizacion + '" title="Ver e imprimir" aria-label="Ver e imprimir"><i class="fa-solid fa-print"></i></button>'
          + (q.estado === 'VIGENTE' ? ' <button class="btn btn-sm btn-outline-primary" data-sol="' + q.idCotizacion + '">Solicitar</button>' : '') + '</td></tr>';
      }).join('') : '<tr class="empty-row"><td colspan="6">Aún no tienes cotizaciones. Genera la primera con el formulario.</td></tr>';
    } catch (e) { $('tablaCot').innerHTML = '<tr class="empty-row"><td colspan="6" class="text-danger">' + esc(e.message) + '</td></tr>'; }
  }
  $('tablaCot').addEventListener('click', async function (e) {
    var b = e.target.closest('button'); if (!b) return;
    if (b.dataset.ver) S.abrirDocumento('portal-documento.html?tipo=cotizacion&id=' + b.dataset.ver);
    if (b.dataset.sol) {
      var ok = await S.confirmar('Enviaremos una solicitud con los datos de esta cotización para que la entidad la evalúe.', { titulo: 'Enviar solicitud', ok: 'Enviar solicitud' });
      if (!ok) return;
      try { var r = await API.post(BASE + '/cotizaciones/' + b.dataset.sol + '/solicitar', {}); S.toast(r.mensaje, 'ok'); cargarCotizaciones(); cargarTodo(); }
      catch (err) { S.toast(err.message, 'err'); }
    }
  });

  // ================= TRÁMITES =================
  async function cargarTipos() {
    try {
      tipos = await API.get('/tipos-cambio');
      var opts = tipos.filter(function (t) { return t.estado !== 'INACTIVO'; }).map(function (t) {
        return '<option value="' + t.idTipoCambio + '">' + esc(t.monedaOrigen + ' → ' + t.monedaDestino + ' (venta ' + t.venta + ')') + '</option>';
      }).join('');
      $('cbTipo').innerHTML = opts; $('qTc').innerHTML = opts;
    } catch (e) { }
  }
  function msg(id, ok, t) { var e = $(id); e.className = 'mt-2 small ' + (ok ? 'text-success' : 'text-danger'); e.textContent = t; }
  function enviarTramite(form, msgId, fn) {
    $(form).addEventListener('submit', async function (ev) {
      ev.preventDefault();
      var b = this.querySelector('button[type="submit"]'); b.disabled = true;
      try { var t = await fn(); msg(msgId, true, t); this.reset(); cargarTodo(); }
      catch (e) { msg(msgId, false, e.message); }
      b.disabled = false;
    });
  }
  enviarTramite('fCambio', 'cbMsg', async function () {
    var m = parseFloat($('cbMonto').value); if (!(m > 0)) throw new Error('Ingresa un monto válido.');
    var o = await API.post(BASE + '/cambio', { idTipoCambio: parseInt($('cbTipo').value, 10), monto: m });
    return 'Solicitud enviada. Recibirás ' + S.money(o.totalRecibir, o.monedaDestino) + '.';
  });
  enviarTramite('fPrestamo', 'prMsg', async function () {
    var m = parseFloat($('prMonto').value), n = parseInt($('prPlazo').value, 10); if (!(m > 0)) throw new Error('Ingresa el monto que necesitas.');
    var r = await API.post(BASE + '/prestamo', { montoPrestamo: m, plazoMeses: n });
    return 'Solicitud enviada. Cuota estimada: ' + S.money(r.prestamo ? r.prestamo.cuotaMensual : 0) + ' por ' + n + ' meses.';
  });
  enviarTramite('fEmpeno', 'emMsg', async function () {
    var d = $('emDesc').value.trim(), v = parseFloat($('emValor').value), m = parseFloat($('emMonto').value);
    if (!d) throw new Error('Describe el artículo.'); if (!(m > 0)) throw new Error('Ingresa el monto que solicitas.');
    var r = await API.post(BASE + '/empeno', { descripcionArticulo: d, valorEstimado: isNaN(v) ? null : v, montoPrestamo: m, plazoDias: 30 });
    return r.mensaje || 'Solicitud de empeño enviada.';
  });

  // ================= HISTORIAL =================
  function pintarPagos() {
    $('tablaPagos').innerHTML = misPagos.length ? misPagos.map(function (p) {
      return '<tr><td class="text-nowrap">' + esc(S.fechaHora(p.fechaPago)) + '</td><td>' + esc(S.concepto(p.concepto)) + '<span class="sub">' + esc(p.descripcion || '') + '</span></td>'
        + '<td>' + S.wallet(p.metodo) + '</td><td><code>' + esc(p.numeroOperacion || '—') + '</code></td><td class="num fw-600">' + S.money(p.monto) + '</td>'
        + '<td>' + S.badge(p.estado) + (p.observacion ? '<span class="sub">' + esc(p.observacion) + '</span>' : '') + '</td>'
        + '<td class="text-end"><button class="btn btn-sm btn-light" data-doc="' + p.idPago + '"><i class="fa-solid fa-receipt me-1"></i>' + esc(p.numeroComprobante || 'Constancia') + '</button></td></tr>';
    }).join('') : '<tr class="empty-row"><td colspan="7">Aún no registras pagos.</td></tr>';
  }
  $('tablaPagos').addEventListener('click', function (e) { var b = e.target.closest('[data-doc]'); if (b) S.abrirDocumento('portal-documento.html?tipo=pago&id=' + b.dataset.doc); });
  $('btnEstado').addEventListener('click', function () { S.abrirDocumento('portal-documento.html?tipo=estado-cuenta'); });
  function pintarTramites() {
    $('histTabla').innerHTML = operaciones.length ? operaciones.map(function (o) {
      return '<tr><td>' + o.idOperacion + '</td><td>' + esc(S.etiqueta(o.tipoOperacion)) + '</td><td class="num">' + S.money(o.monto) + '</td><td>' + S.badge(o.estado) + '</td><td>' + esc(S.fechaHora(o.fechaOperacion)) + '</td></tr>';
    }).join('') : '<tr class="empty-row"><td colspan="5">Aún no tienes trámites.</td></tr>';
  }

  // ================= MIS DATOS =================
  $('fDatos').addEventListener('submit', async function (ev) {
    ev.preventDefault();
    try {
      cliente = await API.put(BASE + '/datos', { telefono: $('mdTel').value.trim(), correo: $('mdCorreo').value.trim(), direccion: $('mdDir').value.trim() });
      try { sessionStorage.setItem('sigsf_cliente', JSON.stringify(cliente)); } catch (e) { }
      pintarCabecera(); S.toast('Datos actualizados.', 'ok');
    } catch (e) { S.toast(e.message, 'err'); }
  });

  // ---------------- Inicio ----------------
  cargarBilleteras(); cargarTipos().then(estimar); cargarCotizaciones(); cargarTodo();
  var t0 = (location.hash || '').slice(1);
  if (t0 && document.querySelector('[data-tab="' + t0 + '"]')) mostrar(t0);
  setInterval(function () { if (!document.hidden) cargarTodo(); }, 30000);
})();
