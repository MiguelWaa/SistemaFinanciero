/* ============================================================
   SIGSF · Animaciones de la interfaz (v5.3)
   Se carga después de sigsf-ui.js en todas las páginas y no
   necesita configuración: observa la página y anima solo.
     - Números de los indicadores que cuentan hasta su valor.
     - Entrada escalonada de tarjetas, filas y listas nuevas
       (no se repite si un refresco trae los mismos datos).
     - Ícono automático en cada indicador según su texto.
     - Onda al pulsar botones, íconos flotantes decorativos.
     - SIGSF.celebrar(): confeti para un pago confirmado.
     - SIGSF.checkAnimado(): marca de verificación dibujada.
   Respeta «reducir movimiento» del sistema operativo.
   ============================================================ */
(function (S) {
  'use strict';
  S = window.SIGSF = window.SIGSF || {};
  var reducido = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  // ---------------- Íconos de los indicadores ----------------
  var ICONOS = [
    [/cobrad|cobro|recaud/i, 'fa-sack-dollar'], [/verific|espera/i, 'fa-hourglass-half'], [/deuda/i, 'fa-scale-balanced'],
    [/pagad|pago/i, 'fa-receipt'], [/cotiza/i, 'fa-file-invoice-dollar'], [/solicitud|atender|revisi/i, 'fa-inbox'],
    [/cliente|cuenta/i, 'fa-users'], [/operaci/i, 'fa-right-left'], [/préstamo|prestamo/i, 'fa-hand-holding-dollar'],
    [/empeñ|empen/i, 'fa-gem'], [/venta|ingreso|importe/i, 'fa-chart-line'], [/stock|producto|unidad/i, 'fa-boxes-stacked'],
    [/expediente|decidir|decisi/i, 'fa-folder-open'], [/usuario|activo/i, 'fa-user-check'], [/rol|permiso|contraseña|sesión|sesion/i, 'fa-shield-halved'],
    [/campañ|campan|presupuesto/i, 'fa-bullhorn'], [/prospect|nuevo/i, 'fa-user-plus'], [/proveedor/i, 'fa-truck'],
    [/línea|linea|orden/i, 'fa-industry'], [/respaldo/i, 'fa-database'], [/login|acceso|denegad/i, 'fa-lock'],
    [/servidor|base de datos/i, 'fa-server'], [/tipo/i, 'fa-coins'], [/monto|total|acumulado|comprometido|solicitado/i, 'fa-money-bill-trend-up'],
    [/impacto|alto/i, 'fa-triangle-exclamation'], [/aprobad/i, 'fa-circle-check'], [/pendiente/i, 'fa-clock'],
    [/versi|commit|rama|esquema/i, 'fa-code-branch'], [/registrad/i, 'fa-list-check']
  ];
  function ponerIcono(kpi) {
    if (kpi.querySelector('.sg-kpi-ic')) return;
    var l = kpi.querySelector('.l');
    var texto = l ? l.textContent : '';
    var ic = 'fa-chart-simple';
    for (var i = 0; i < ICONOS.length; i++) { if (ICONOS[i][0].test(texto)) { ic = ICONOS[i][1]; break; } }
    var e = document.createElement('i');
    e.className = 'sg-kpi-ic fa-solid ' + ic;
    e.setAttribute('aria-hidden', 'true');
    kpi.insertBefore(e, kpi.firstChild);
  }

  // ---------------- Números que cuentan ----------------
  var NUMERO = /^(\D{0,4}?)(\d{1,3}(?:,\d{3})*|\d+)(\.\d+)?(\D{0,3})$/;
  function contar(el) {
    var texto = (el.textContent || '').trim();
    if (el._sgFinal === texto || el._sgAnimando) return;
    var m = texto.match(NUMERO);
    if (!m || reducido) { el._sgFinal = texto; return; }
    var pref = m[1], dec = m[3] ? m[3].length - 1 : 0, suf = m[4];
    var fin = parseFloat((m[2] + (m[3] || '')).replace(/,/g, ''));
    var desde = el._sgValor || 0;
    if (!isFinite(fin) || fin === desde) { el._sgFinal = texto; el._sgValor = fin; return; }
    var dur = 900, t0 = null;
    el._sgAnimando = true;
    function paso(t) {
      if (!t0) t0 = t;
      var k = Math.min(1, (t - t0) / dur), e = 1 - Math.pow(1 - k, 3);
      var v = desde + (fin - desde) * e;
      el.textContent = pref + v.toLocaleString('es-PE', { minimumFractionDigits: dec, maximumFractionDigits: dec }) + suf;
      if (k < 1) requestAnimationFrame(paso);
      else { el.textContent = texto; el._sgFinal = texto; el._sgValor = fin; el._sgAnimando = false; }
    }
    requestAnimationFrame(paso);
  }

  // ---------------- Entradas escalonadas ----------------
  var ANIMABLES = '.card, .sg-kpi, .kpi, .sg-tile, .pv, .sol, .pagable, .sg-feed > li, tbody > tr, .wallet-choice > div, .import-res';
  var ultimoHtml = new WeakMap();
  function escalonar(lista) {
    if (reducido) return;
    var n = 0;
    lista.forEach(function (el) {
      if (el.dataset.sgIn || el.closest('.modal, .dropdown-menu, .doc-sheet, .toast')) return;
      if (el.tagName === 'TR' && el.parentElement && el.parentElement.querySelectorAll('tr').length > 60 && n > 25) return;
      el.dataset.sgIn = '1';
      el.style.animationDelay = Math.min(n, 14) * 45 + 'ms';
      el.classList.add('sg-in');
      n++;
      el.addEventListener('animationend', function fin(ev) {
        if (ev.target !== el) return;
        el.classList.remove('sg-in'); el.style.animationDelay = '';
        el.removeEventListener('animationend', fin);
      });
    });
  }

  function procesar(raiz) {
    if (!(raiz instanceof Element)) return;
    var kpis = [];
    if (raiz.matches('.sg-kpi, .kpi')) kpis.push(raiz);
    raiz.querySelectorAll('.sg-kpi, .kpi').forEach(function (k) { kpis.push(k); });
    kpis.forEach(function (k) { ponerIcono(k); var v = k.querySelector('.v'); if (v) contar(v); });
    raiz.querySelectorAll('[data-contar]').forEach(contar);
    var anim = [];
    if (raiz.matches(ANIMABLES)) anim.push(raiz);
    raiz.querySelectorAll(ANIMABLES).forEach(function (e) { anim.push(e); });
    escalonar(anim);
    // Gráficos: retraso escalonado en barras y porciones
    raiz.querySelectorAll('svg.sg-chart').forEach(function (svg) {
      svg.querySelectorAll('rect').forEach(function (r, i) { r.style.animationDelay = (i * 35) + 'ms'; });
      svg.querySelectorAll('path').forEach(function (p, i) { p.style.animationDelay = (i * 90) + 'ms'; });
    });
  }

  function observar() {
    new MutationObserver(function (cambios) {
      var vistos = new Set();
      cambios.forEach(function (c) {
        if (c.type === 'characterData') {
          var p = c.target.parentElement;
          if (p && p.matches('.sg-kpi .v, .kpi .v, [data-contar]')) contar(p);
          return;
        }
        var t = c.target;
        if (t.matches && t.matches('.sg-kpi .v, .kpi .v, [data-contar]')) { contar(t); return; }
        if (vistos.has(t)) return;
        vistos.add(t);
        // Si un refresco automático trae exactamente lo mismo, no se vuelve a animar.
        var html = t.innerHTML;
        if (ultimoHtml.get(t) === html) return;
        ultimoHtml.set(t, html);
        Array.prototype.forEach.call(t.children || [], procesar);
      });
    }).observe(document.body, { childList: true, subtree: true, characterData: true });
  }

  // ---------------- Onda al pulsar botones ----------------
  document.addEventListener('pointerdown', function (ev) {
    if (reducido) return;
    var b = ev.target.closest('.btn, .page-link, .nav-link');
    if (!b || b.disabled || b.id === 'btnCampana') return;
    var r = b.getBoundingClientRect(), d = Math.max(r.width, r.height);
    var o = document.createElement('span');
    o.className = 'sg-onda';
    o.style.width = o.style.height = d + 'px';
    o.style.left = (ev.clientX - r.left - d / 2) + 'px';
    o.style.top = (ev.clientY - r.top - d / 2) + 'px';
    if (getComputedStyle(b).position === 'static') b.style.position = 'relative';
    b.style.overflow = 'hidden';
    b.appendChild(o);
    setTimeout(function () { o.remove(); }, 650);
  }, { passive: true });

  // ---------------- Íconos flotantes ----------------
  var FLOTANTES = ['fa-coins', 'fa-qrcode', 'fa-sack-dollar', 'fa-gem', 'fa-chart-line', 'fa-credit-card', 'fa-receipt', 'fa-landmark', 'fa-money-bill-wave', 'fa-mobile-screen'];
  function flotantes(cont, cantidad) {
    if (reducido || !cont || cont.querySelector(':scope > .sg-floaters')) return;
    var f = document.createElement('div');
    f.className = 'sg-floaters';
    f.setAttribute('aria-hidden', 'true');
    for (var i = 0; i < cantidad; i++) {
      var e = document.createElement('i');
      e.className = 'fa-solid ' + FLOTANTES[i % FLOTANTES.length];
      e.style.left = (5 + Math.random() * 90) + '%';
      e.style.fontSize = (14 + Math.random() * 26) + 'px';
      e.style.animationDuration = (14 + Math.random() * 16) + 's';
      e.style.animationDelay = (-Math.random() * 20) + 's';
      f.appendChild(e);
    }
    if (getComputedStyle(cont).position === 'static') cont.style.position = 'relative';
    cont.insertBefore(f, cont.firstChild);
  }

  // ---------------- Confeti ----------------
  function celebrar() {
    if (reducido) return;
    var c = document.createElement('canvas');
    c.className = 'sg-confeti';
    c.width = innerWidth * devicePixelRatio; c.height = innerHeight * devicePixelRatio;
    document.body.appendChild(c);
    var ctx = c.getContext('2d'), colores = ['#0b6e79', '#12949f', '#c8961e', '#e7b84a', '#742384', '#00a19a', '#e4572e', '#157347'];
    var piezas = [], W = c.width, H = c.height;
    for (var i = 0; i < 160; i++) {
      var lado = i % 2 ? 1 : -1;
      piezas.push({
        x: lado > 0 ? W * .15 : W * .85, y: H * .65,
        vx: lado * (6 + Math.random() * 10) * devicePixelRatio * (Math.random() < .5 ? 1 : .6),
        vy: -(14 + Math.random() * 12) * devicePixelRatio,
        w: (6 + Math.random() * 6) * devicePixelRatio, h: (8 + Math.random() * 10) * devicePixelRatio,
        r: Math.random() * Math.PI, vr: (Math.random() - .5) * .35, c: colores[i % colores.length]
      });
    }
    var t0 = performance.now();
    (function cuadro(t) {
      var dt = Math.min(2, (t - (cuadro.u || t)) / 16.7); cuadro.u = t;
      ctx.clearRect(0, 0, W, H);
      piezas.forEach(function (p) {
        p.vy += .45 * devicePixelRatio * dt; p.vx *= .99; p.x += p.vx * dt; p.y += p.vy * dt; p.r += p.vr * dt;
        ctx.save(); ctx.translate(p.x, p.y); ctx.rotate(p.r); ctx.fillStyle = p.c;
        ctx.globalAlpha = Math.max(0, 1 - (t - t0) / 2600);
        ctx.fillRect(-p.w / 2, -p.h / 2, p.w, p.h); ctx.restore();
      });
      if (t - t0 < 2600) requestAnimationFrame(cuadro); else c.remove();
    })(t0);
  }

  /** Marca de verificación que se dibuja (para pantallas de éxito). */
  function checkAnimado(color) {
    color = color || '#157347';
    return '<svg class="sg-check" viewBox="0 0 84 84" aria-hidden="true"><circle cx="42" cy="42" r="38" fill="none" stroke="' + color
      + '" stroke-width="5" stroke-linecap="round"/><path d="M26 43 l11 11 l21 -23" fill="none" stroke="' + color
      + '" stroke-width="6" stroke-linecap="round" stroke-linejoin="round"/></svg>';
  }

  S.celebrar = celebrar;
  S.checkAnimado = checkAnimado;
  S.flotantes = flotantes;

  function iniciar() {
    procesar(document.body);
    observar();
    flotantes(document.querySelector('.auth-side'), 14);
    flotantes(document.querySelector('.sg-hero'), 10);
    if (document.querySelector('.w-main')) flotantes(document.body, 18);
  }
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', iniciar); else iniciar();
})(window.SIGSF);
