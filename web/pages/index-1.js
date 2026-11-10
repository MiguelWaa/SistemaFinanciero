(function () {
  'use strict';
  var S = SIGSF, $ = function (i) { return document.getElementById(i); };
  var homeView = $('homeView'), frameView = $('frameView'), frame = $('moduleFrame'), loader = $('frameLoader');

  // ---------- Usuario ----------
  var usuario = null;
  try { usuario = JSON.parse(sessionStorage.getItem('sigsf_usuario') || 'null'); } catch (e) { }
  var nombre = usuario ? ((usuario.nombre || '') + ' ' + (usuario.apellido || '')).trim() : (sessionStorage.getItem('sigsf_user') || 'Usuario');
  $('navUser').textContent = nombre || 'Usuario';
  $('navRol').textContent = usuario && usuario.rol ? S.etiqueta(usuario.rol) : '—';
  $('navIni').textContent = (nombre || 'U').split(/\s+/).map(function (w) { return w[0]; }).slice(0, 2).join('').toUpperCase();
  var h = new Date().getHours();
  $('saludo').textContent = (h < 12 ? 'Buenos días' : h < 19 ? 'Buenas tardes' : 'Buenas noches') + ', ' + (usuario && usuario.nombre ? usuario.nombre : 'bienvenido');
  $('hoy').textContent = new Date().toLocaleDateString('es-PE', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
  $('logoutBtn').addEventListener('click', function (e) { e.preventDefault(); API.logout(); });

  // ---------- Navegación ----------
  var titulos = {};
  document.querySelectorAll('#sideMenu a[data-nav]').forEach(function (a) { titulos[a.getAttribute('data-nav')] = a.getAttribute('data-title'); });

  function marcar(target) {
    document.querySelectorAll('#sideMenu a').forEach(function (a) { a.classList.toggle('active', a.getAttribute('data-nav') === target); });
  }
  function cerrarMenuMovil() {
    var off = bootstrap.Offcanvas.getInstance($('sgSide'));
    if (off) off.hide();
  }
  function abrir(target, titulo, sinHistorial) {
    titulo = titulo || titulos[target] || target.replace('.html', '');
    marcar(target);
    $('pageTitle').textContent = titulo;
    cerrarMenuMovil();
    if (target === 'home') {
      $('crumbs').textContent = 'Panel';
      frameView.hidden = true; homeView.hidden = false;
      $('btnVolver').classList.add('d-none');
      loader.classList.remove('show');
      frame.src = 'about:blank';
      document.title = 'SIGSF · Panel';
      if (!sinHistorial) history.replaceState(null, '', location.pathname);
      cargarTodo();
      return;
    }
    $('crumbs').textContent = 'Panel / ' + titulo;
    homeView.hidden = true; frameView.hidden = false;
    $('btnVolver').classList.remove('d-none');
    loader.classList.add('show');
    frame.src = encodeURI(target);
    document.title = 'SIGSF · ' + titulo;
    if (!sinHistorial) history.replaceState(null, '', '#' + encodeURIComponent(target));
  }
  frame.addEventListener('load', function () {
    if (frame.src.indexOf('about:blank') === -1) {
      loader.classList.remove('show');
      frameView.classList.remove('sg-entrando'); void frameView.offsetWidth; frameView.classList.add('sg-entrando');
    }
  });
  document.querySelectorAll('#sideMenu a[data-nav]').forEach(function (a) {
    a.addEventListener('click', function (e) { e.preventDefault(); abrir(a.getAttribute('data-nav'), a.getAttribute('data-title')); });
  });
  document.addEventListener('click', function (e) {
    var b = e.target.closest('[data-ir]');
    if (!b) return;
    e.preventDefault();
    abrir(b.getAttribute('data-ir'));
  });
  $('btnVolver').addEventListener('click', function () { abrir('home'); });

  // Compatibilidad con el asistente por voz y enlaces antiguos
  window.sigsfNavigate = function (target, title) { abrir(target, title); };
  window.openMod = function (target, title) { abrir(target, title); };
  window.sigsfAbrir = abrir;

  // ---------- Indicadores ----------
  function set(id, v) { var e = $(id); if (e) e.textContent = v; }
  async function cargarKPIs() {
    try {
      var d = await API.dashboard();
      set('kpiCobrado', S.money(d.cobradoMes));
      set('kpiPorVerificar', S.num(d.pagosPorVerificar));
      set('kpiPorVerificarMonto', S.money(d.montoPorVerificar) + ' en espera');
      set('kpiIngresos', S.money(d.ingresosDelMes));
      set('kpiVentas', S.num(d.ventasTotales) + ' ventas registradas');
      set('kpiClientes', S.num(d.clientesActivos));
      set('kpiClientesTot', S.num(d.clientesTotales) + ' en total');
      set('kpiOperaciones', S.num(d.operacionesHoy));
      set('kpiSolic', S.num(d.solicitudesPendientes) + ' solicitudes pendientes');
      set('kpiPrestamos', S.money(d.prestamosColocados));
      set('kpiExpedientes', S.num(d.expedientesPendientes) + ' expedientes pendientes');
    } catch (e) { console.warn('Indicadores no disponibles:', e.message); }
  }

  // ---------- Pagos por verificar (campana + bandeja del inicio) ----------
  var ultimoPendiente = null;
  function filaPago(p) {
    return '<li><span>' + S.wallet(p.metodo) + '</span><div class="who"><b>' + S.escapar(p.cliente || 'Cliente') + '</b>'
      + '<span>' + S.escapar(S.concepto(p.concepto)) + ' · N° op. ' + S.escapar(p.numeroOperacion || '—') + ' · ' + S.escapar(S.fechaHora(p.fechaPago)) + '</span></div>'
      + '<span class="amt">' + S.money(p.monto) + '</span></li>';
  }
  async function cargarPendientes() {
    try {
      var lista = await API.get('/pagos?estado=POR_VERIFICAR');
      var n = lista.length;
      ['navPagos', 'campanaN'].forEach(function (id) { var b = $(id); b.textContent = n; b.classList.toggle('d-none', !n); });
      var vacio = '<li class="sg-empty"><i class="fa-solid fa-circle-check"></i>No hay pagos por verificar.</li>';
      $('campanaLista').innerHTML = n ? lista.slice(0, 5).map(filaPago).join('') : vacio;
      $('feedPagos').innerHTML = n ? lista.slice(0, 6).map(filaPago).join('') : vacio;
      var masNuevo = n ? lista[0].idPago : 0;
      if (ultimoPendiente !== null && masNuevo > ultimoPendiente) {
        S.toast('Nuevo pago por verificar: ' + lista[0].cliente + ' · ' + S.money(lista[0].monto), 'info');
        var c = $('btnCampana'); c.classList.remove('timbre'); void c.offsetWidth; c.classList.add('timbre');
      }
      ultimoPendiente = masNuevo;
    } catch (e) {
      $('feedPagos').innerHTML = '<li class="sg-empty">' + S.escapar(e.message) + '</li>';
    }
  }

  async function cargarCobros() {
    try {
      var r = await API.get('/pagos/resumen');
      S.graficoBarras($('graficoDias'), r.porDia.map(function (d) {
        return { label: S.fecha(d.fecha).substring(0, 5), valor: Number(d.monto) };
      }), { alto: 170, formato: S.money, titulo: 'Cobros por día' });
      var total = r.porMetodo.reduce(function (a, m) { return a + Number(m.monto); }, 0) || 1;
      $('porMetodo').innerHTML = r.porMetodo.filter(function (m) { return Number(m.monto) > 0; }).map(function (m) {
        var pct = Math.round(100 * Number(m.monto) / total);
        return '<div class="d-flex justify-content-between small mb-1"><span>' + S.wallet(m.metodo) + '</span><span class="fw-600">'
          + S.money(m.monto) + ' <span class="text-muted fw-normal">· ' + pct + ' %</span></span></div>'
          + '<div class="sg-bar mb-2"><span style="width:' + pct + '%;background:' + (S.COLOR_METODO[m.metodo] || '#5b6779') + '"></span></div>';
      }).join('') || '<div class="small-muted">Aún no hay cobros confirmados.</div>';
    } catch (e) { $('graficoDias').innerHTML = '<div class="small-muted">' + S.escapar(e.message) + '</div>'; }
  }

  async function cargarSolicitudes() {
    try {
      var lista = await API.get('/solicitudes/pendientes');
      var b = $('navSol'); b.textContent = lista.length; b.classList.toggle('d-none', !lista.length);
      $('feedSol').innerHTML = lista.length ? lista.slice(0, 5).map(function (s) {
        return '<li><span class="badge text-bg-light border">' + S.escapar(S.etiqueta(s.tipoOperacion)) + '</span><div class="who"><b>'
          + S.escapar(s.cliente || '—') + '</b><span>Doc. ' + S.escapar(s.documento || '—') + ' · ' + S.escapar(S.fechaHora(s.fechaOperacion)) + '</span></div>'
          + '<span class="amt">' + S.money(s.monto) + '</span></li>';
      }).join('') : '<li class="sg-empty"><i class="fa-solid fa-inbox"></i>No hay solicitudes pendientes.</li>';
    } catch (e) { $('feedSol').innerHTML = '<li class="sg-empty">' + S.escapar(e.message) + '</li>'; }
  }

  async function cargarClientes() {
    var tb = $('tablaClientesPanel');
    try {
      var lista = await API.get('/clientes');
      $('totalClientesBadge').textContent = S.num(lista.length) + (lista.length === 1 ? ' cliente registrado' : ' clientes registrados');
      lista = lista.slice().sort(function (a, b) { return String(b.fechaRegistro || '').localeCompare(String(a.fechaRegistro || '')); });
      tb.innerHTML = lista.length ? lista.slice(0, 6).map(function (c) {
        var n = ((c.nombres || '') + ' ' + (c.apellidos || '')).trim();
        return '<tr><td><span class="fw-600">' + S.escapar(n) + '</span><span class="sub">' + S.escapar(c.correo || '') + '</span></td>'
          + '<td class="text-nowrap">' + S.escapar((c.tipoDocumento || '') + ' ' + (c.documento || '')) + '</td><td>' + S.escapar(c.telefono || '—') + '</td>'
          + '<td>' + S.badge(c.estado) + '</td><td class="text-muted text-nowrap">' + S.escapar(S.fecha(c.fechaRegistro)) + '</td></tr>';
      }).join('') : '<tr class="empty-row"><td colspan="5">Aún no hay clientes.</td></tr>';
    } catch (e) { tb.innerHTML = '<tr class="empty-row"><td colspan="5" class="text-danger">' + S.escapar(e.message) + '</td></tr>'; }
  }

  function cargarTodo() { cargarKPIs(); cargarPendientes(); cargarCobros(); cargarSolicitudes(); cargarClientes(); }

  API.get('/version').then(function (v) {
    $('pieVersion').textContent = 'SIGSF v' + v.version + (v.versionEsquema ? ' · esquema ' + v.versionEsquema : '') + (v.commitCorto ? ' · ' + v.commitCorto : '');
  }).catch(function () { });

  // Apertura directa de un módulo (panel/#pagos.html) o inicio
  var inicial = decodeURIComponent((location.hash || '').replace(/^#/, ''));
  if (inicial && titulos[inicial]) abrir(inicial, titulos[inicial], true); else cargarTodo();

  // Avisos en vivo: pagos y solicitudes nuevos cada 20 s; el resto del inicio cada 60 s
  setInterval(function () { cargarPendientes(); cargarSolicitudes(); }, 20000);
  setInterval(function () { if (!homeView.hidden) { cargarKPIs(); cargarCobros(); cargarClientes(); } }, 60000);
})();
