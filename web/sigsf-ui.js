/* ============================================================
   SIGSF · Utilidades de interfaz (v5.2)
   Formato de montos y fechas, insignias de estado, avisos (toasts),
   diálogos de confirmación, gráficos SVG livianos, compresión de
   imágenes y códigos QR. Requiere Bootstrap 5 (bundle) cargado antes.
   Lo usan el panel, todos los módulos y el Portal del Cliente.
   ============================================================ */
window.SIGSF = window.SIGSF || {};
(function (S) {
  'use strict';

  // ---------------- Formato ----------------
  function escapar(s) {
    return String(s === null || s === undefined ? '' : s)
      .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
  }
  function money(n, moneda) {
    var pref = moneda && moneda !== 'PEN' ? (moneda === 'USD' ? 'US$ ' : moneda === 'EUR' ? '€ ' : moneda + ' ') : 'S/ ';
    return pref + Number(n || 0).toLocaleString('es-PE', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }
  function num(n) { return Number(n || 0).toLocaleString('es-PE'); }
  function fechaCorta(v) { return v ? String(v).replace('T', ' ').substring(0, 16) : ''; }
  function fecha(v) {
    if (!v) return '';
    var s = String(v).substring(0, 10).split('-');
    return s.length === 3 ? s[2] + '/' + s[1] + '/' + s[0] : String(v);
  }
  function fechaHora(v) {
    if (!v) return '';
    var t = String(v).replace('T', ' ');
    return fecha(t) + (t.length > 11 ? ' ' + t.substring(11, 16) : '');
  }
  /** "POR_VERIFICAR" → "Por verificar" (sin mayúsculas sostenidas). */
  function etiqueta(v) {
    var t = String(v || '').replace(/_/g, ' ').toLowerCase()
      .replace(/\bempeno(s?)\b/g, 'empeño$1').replace(/\bprestamo(s?)\b/g, 'préstamo$1')
      .replace(/\bevaluacion\b/g, 'evaluación').replace(/\boperacion\b/g, 'operación').replace(/\bcredito\b/g, 'crédito');
    return t.charAt(0).toUpperCase() + t.slice(1);
  }

  // ---------------- Estados ----------------
  var TONO = {
    ok: ['ACTIVO', 'ACTIVA', 'COMPLETADA', 'APROBADO', 'APROBADA', 'TERMINADA', 'REGISTRADA', 'VIGENTE', 'CONFIRMADO',
         'RECUPERADO', 'CANCELADO', 'OK', 'LE', 'PAGADO'],
    warn: ['PENDIENTE', 'EVALUACION', 'CONTACTADO', 'EN_PROCESO', 'POR_VERIFICAR', 'PLANIFICADA', 'OBSERVADO', 'OBSERVADA', 'L'],
    bad: ['RECHAZADO', 'RECHAZADA', 'ANULADA', 'ANULADO', 'VENCIDO', 'VENCIDA', 'BLOQUEADO', 'ERROR'],
    info: ['NUEVO', 'NUEVA', 'CONVERTIDA', 'CAJA', 'PORTAL'],
    mute: ['INACTIVO', 'INACTIVA', 'CERRADA', 'FINALIZADA', 'SUSPENDIDA']
  };
  function tono(v) {
    var t = String(v || '').toUpperCase();
    for (var k in TONO) { if (TONO[k].indexOf(t) !== -1) return k; }
    return 'info';
  }
  function badge(v) {
    if (v === null || v === undefined || v === '') return '<span class="text-muted">—</span>';
    var t = String(v);
    var texto = /^[A-Z_]+$/.test(t) && t.length > 3 ? etiqueta(t) : t;
    return '<span class="st st-' + tono(t) + '">' + escapar(texto) + '</span>';
  }
  var WALLET_INICIAL = { YAPE: 'Y', PLIN: 'P', TUNKI: 'T', EFECTIVO: 'S/', TRANSFERENCIA: 'TR' };
  function wallet(m) {
    if (!m) return '—';
    var k = String(m).toUpperCase();
    return '<span class="wallet wallet-' + escapar(k) + '"><i>' + (WALLET_INICIAL[k] || '·') + '</i>' + escapar(etiqueta(k)) + '</span>';
  }
  var CONCEPTOS = { CUOTA_PRESTAMO: 'Cuota de préstamo', RESCATE_EMPENO: 'Rescate de empeño', CAMBIO_MONEDA: 'Cambio de moneda', OTRO: 'Otro pago' };
  function concepto(c) { return CONCEPTOS[c] || etiqueta(c); }

  /** Indicador para la franja .sg-kpis. clase: ok | warn | bad | gold (o las antiguas verde/ambar/rojo). */
  function kpi(label, valor, clase, detalle) {
    var map = { verde: 'ok', ambar: 'warn', rojo: 'bad' };
    var c = map[clase] || clase || '';
    return '<div class="sg-kpi ' + c + '"><div class="l">' + escapar(label) + '</div><div class="v">' + valor + '</div>'
      + (detalle ? '<div class="d">' + detalle + '</div>' : '') + '</div>';
  }

  // ---------------- Avisos y diálogos ----------------
  function contenedorToasts() {
    var c = document.getElementById('sgToasts');
    if (!c) {
      c = document.createElement('div');
      c.id = 'sgToasts';
      c.className = 'toast-container position-fixed top-0 end-0 p-3';
      c.setAttribute('aria-live', 'polite');
      document.body.appendChild(c);
    }
    return c;
  }
  /** tipo: ok | err | info */
  function toast(mensaje, tipo) {
    var colores = { ok: 'text-bg-success', err: 'text-bg-danger', info: 'text-bg-dark' };
    var iconos = { ok: 'fa-circle-check', err: 'fa-circle-exclamation', info: 'fa-circle-info' };
    tipo = tipo || 'ok';
    var d = document.createElement('div');
    d.className = 'toast align-items-center ' + (colores[tipo] || colores.info);
    d.setAttribute('role', tipo === 'err' ? 'alert' : 'status');
    d.innerHTML = '<div class="d-flex"><div class="toast-body"><i class="fa-solid ' + iconos[tipo] + ' me-2"></i>'
      + escapar(mensaje) + '</div><button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Cerrar"></button></div>';
    contenedorToasts().appendChild(d);
    if (window.bootstrap) {
      var t = new bootstrap.Toast(d, { delay: tipo === 'err' ? 7000 : 3800 });
      d.addEventListener('hidden.bs.toast', function () { d.remove(); });
      t.show();
    }
  }

  function modalBase(titulo, cuerpo, botonOk, variante) {
    var d = document.createElement('div');
    d.className = 'modal fade';
    d.tabIndex = -1;
    d.innerHTML = '<div class="modal-dialog modal-dialog-centered"><div class="modal-content">'
      + '<div class="modal-header"><h5 class="modal-title fs-6">' + escapar(titulo) + '</h5>'
      + '<button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button></div>'
      + '<div class="modal-body">' + cuerpo + '</div>'
      + '<div class="modal-footer"><button type="button" class="btn btn-light" data-bs-dismiss="modal">Cancelar</button>'
      + '<button type="button" class="btn btn-' + (variante || 'primary') + '" data-ok>' + escapar(botonOk || 'Aceptar') + '</button></div>'
      + '</div></div>';
    document.body.appendChild(d);
    return d;
  }

  /** Confirmación con modal de Bootstrap. Devuelve una promesa con true/false. */
  function confirmar(texto, opciones) {
    opciones = opciones || {};
    return new Promise(function (resolver) {
      if (!window.bootstrap) { resolver(window.confirm(texto)); return; }
      var d = modalBase(opciones.titulo || 'Confirmar', '<p class="mb-0">' + escapar(texto) + '</p>',
        opciones.ok || 'Confirmar', opciones.variante || 'primary');
      var m = new bootstrap.Modal(d);
      var r = false;
      d.querySelector('[data-ok]').addEventListener('click', function () { r = true; m.hide(); });
      d.addEventListener('hidden.bs.modal', function () { d.remove(); resolver(r); });
      m.show();
    });
  }

  /** Pide un texto (por ejemplo, el motivo de un rechazo). Promesa con el texto o null. */
  function pedirTexto(opciones) {
    opciones = opciones || {};
    return new Promise(function (resolver) {
      var cuerpo = '<label class="form-label" for="sgPedir">' + escapar(opciones.etiqueta || 'Detalle') + '</label>'
        + '<textarea class="form-control" id="sgPedir" rows="3" maxlength="200" placeholder="' + escapar(opciones.placeholder || '') + '"></textarea>'
        + (opciones.ayuda ? '<div class="form-text">' + escapar(opciones.ayuda) + '</div>' : '');
      var d = modalBase(opciones.titulo || 'Ingresar dato', cuerpo, opciones.ok || 'Aceptar', opciones.variante || 'primary');
      var m = new bootstrap.Modal(d);
      var r = null;
      var ta = d.querySelector('textarea');
      d.querySelector('[data-ok]').addEventListener('click', function () {
        if (!ta.value.trim()) { ta.classList.add('is-invalid'); ta.focus(); return; }
        r = ta.value.trim(); m.hide();
      });
      d.addEventListener('shown.bs.modal', function () { ta.focus(); });
      d.addEventListener('hidden.bs.modal', function () { d.remove(); resolver(r); });
      m.show();
    });
  }

  // ---------------- Gráficos SVG livianos (sin librerías) ----------------
  /** Barras verticales. datos: [{label, valor}] */
  function graficoBarras(el, datos, opciones) {
    opciones = opciones || {};
    var W = 640, H = opciones.alto || 200, pl = 8, pb = 22, pt = 18;
    var max = Math.max.apply(null, datos.map(function (d) { return Number(d.valor) || 0; }).concat([1]));
    var n = datos.length || 1, paso = (W - pl * 2) / n, ancho = Math.max(6, paso * 0.62);
    var color = opciones.color || 'var(--sg-primary)';
    var s = '<svg class="sg-chart" viewBox="0 0 ' + W + ' ' + H + '" role="img" aria-label="' + escapar(opciones.titulo || 'Gráfico') + '">';
    s += '<line x1="' + pl + '" y1="' + (H - pb) + '" x2="' + (W - pl) + '" y2="' + (H - pb) + '" stroke="#dde3ea"/>';
    datos.forEach(function (d, i) {
      var v = Number(d.valor) || 0, h = Math.round((H - pb - pt) * v / max);
      var x = pl + i * paso + (paso - ancho) / 2, y = H - pb - h;
      s += '<g><title>' + escapar(d.label + ': ' + (opciones.formato ? opciones.formato(v) : v)) + '</title>';
      s += '<rect x="' + x.toFixed(1) + '" y="' + y + '" width="' + ancho.toFixed(1) + '" height="' + Math.max(h, v > 0 ? 2 : 0) + '" rx="3" fill="' + (d.color || color) + '"/></g>';
      if (n <= 16 || i % 2 === 0) {
        s += '<text x="' + (x + ancho / 2).toFixed(1) + '" y="' + (H - 6) + '" text-anchor="middle">' + escapar(d.label) + '</text>';
      }
    });
    s += '</svg>';
    el.innerHTML = s;
  }

  /** Dona. datos: [{label, valor, color}] */
  function graficoDona(el, datos, opciones) {
    opciones = opciones || {};
    var total = datos.reduce(function (a, d) { return a + (Number(d.valor) || 0); }, 0);
    var R = 60, r = 40, cx = 70, cy = 70, ang = -Math.PI / 2;
    var s = '<svg class="sg-chart" viewBox="0 0 140 140" style="max-width:160px" role="img" aria-label="' + escapar(opciones.titulo || 'Distribución') + '">';
    if (!total) {
      s += '<circle cx="70" cy="70" r="50" fill="none" stroke="#edf1f5" stroke-width="20"/>';
    } else {
      datos.forEach(function (d) {
        var v = Number(d.valor) || 0; if (!v) return;
        var a2 = ang + 2 * Math.PI * v / total;
        if (v === total) a2 -= 0.0001;
        var g = a2 - ang > Math.PI ? 1 : 0;
        var p = [cx + R * Math.cos(ang), cy + R * Math.sin(ang), cx + R * Math.cos(a2), cy + R * Math.sin(a2),
                 cx + r * Math.cos(a2), cy + r * Math.sin(a2), cx + r * Math.cos(ang), cy + r * Math.sin(ang)]
          .map(function (x) { return x.toFixed(2); });
        s += '<path d="M' + p[0] + ' ' + p[1] + ' A' + R + ' ' + R + ' 0 ' + g + ' 1 ' + p[2] + ' ' + p[3] + ' L' + p[4] + ' ' + p[5]
          + ' A' + r + ' ' + r + ' 0 ' + g + ' 0 ' + p[6] + ' ' + p[7] + 'Z" fill="' + d.color + '"><title>'
          + escapar(d.label) + '</title></path>';
        ang = a2;
      });
    }
    if (opciones.centro) {
      s += '<text x="70" y="74" text-anchor="middle" style="font-size:13px;font-weight:600;fill:#0f2340">' + escapar(opciones.centro) + '</text>';
    }
    s += '</svg>';
    el.innerHTML = s;
  }
  var COLOR_METODO = { YAPE: '#742384', PLIN: '#00a19a', TUNKI: '#e4572e', EFECTIVO: '#5b6779', TRANSFERENCIA: '#1d6fa5' };

  // ---------------- Imágenes y QR ----------------
  /** Reduce una foto a JPEG (máx. ladoMax px) para enviarla como data URL liviana. */
  function imagenADataUrl(archivo, ladoMax, calidad, formato) {
    return new Promise(function (ok, mal) {
      if (!archivo || !/^image\//.test(archivo.type)) { mal(new Error('Selecciona una imagen (PNG o JPG).')); return; }
      var lector = new FileReader();
      lector.onerror = function () { mal(new Error('No se pudo leer la imagen.')); };
      lector.onload = function () {
        var img = new Image();
        img.onerror = function () { mal(new Error('La imagen no es válida.')); };
        img.onload = function () {
          var k = Math.min(1, (ladoMax || 1200) / Math.max(img.width, img.height));
          var c = document.createElement('canvas');
          c.width = Math.round(img.width * k); c.height = Math.round(img.height * k);
          var ctx = c.getContext('2d');
          ctx.fillStyle = '#fff'; ctx.fillRect(0, 0, c.width, c.height);
          ctx.drawImage(img, 0, 0, c.width, c.height);
          ok(c.toDataURL(formato || 'image/jpeg', calidad || 0.78));
        };
        img.src = lector.result;
      };
      lector.readAsDataURL(archivo);
    });
  }

  /** Dibuja un QR (SVG) con el texto dado. Usa vendor/qrcode/qrcode.js. */
  function qr(el, texto) {
    if (typeof window.qrcode !== 'function') { el.textContent = texto; return; }
    var q = window.qrcode(0, 'M');
    q.addData(unescape(encodeURIComponent(texto)));
    q.make();
    el.innerHTML = q.createSvgTag({ cellSize: 6, margin: 2, scalable: true });
  }

  // ---------------- Varios ----------------
  function debounce(fn, ms) {
    var t;
    return function () { var a = arguments, self = this; clearTimeout(t); t = setTimeout(function () { fn.apply(self, a); }, ms || 220); };
  }
  /** Oculta el encabezado propio cuando la página se abre dentro del panel. */
  function embebido() {
    if (window.top !== window.self) document.body.classList.add('sigsf-embed');
  }
  /** Abre un documento imprimible (cotización, comprobante, reporte) en otra pestaña. */
  function abrirDocumento(url) {
    var w = window.open(url, '_blank');
    if (!w) location.href = url;
  }

  S.escapar = escapar; S.money = money; S.num = num; S.fechaCorta = fechaCorta; S.fecha = fecha; S.fechaHora = fechaHora;
  S.etiqueta = etiqueta; S.badge = badge; S.tono = tono; S.wallet = wallet; S.concepto = concepto; S.kpi = kpi;
  S.toast = toast; S.confirmar = confirmar; S.pedirTexto = pedirTexto;
  S.graficoBarras = graficoBarras; S.graficoDona = graficoDona; S.COLOR_METODO = COLOR_METODO;
  S.imagenADataUrl = imagenADataUrl; S.qr = qr; S.debounce = debounce; S.embebido = embebido; S.abrirDocumento = abrirDocumento;
})(window.SIGSF);
