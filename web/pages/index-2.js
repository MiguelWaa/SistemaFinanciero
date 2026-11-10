/* ================= Asistente por voz: escucha y abre módulos ================= */
(function () {
  var $ = function (i) { return document.getElementById(i); };
  var launcher = $('vaLauncher'), panel = $('vaPanel'), micBtn = $('vaMic'), statusEl = $('vaStatus'),
      heardEl = $('vaHeard'), logEl = $('vaLog'), noteEl = $('vaNote'), speakChk = $('vaSpeak'), contChk = $('vaContinuous');
  var EJEMPLO = 'Ej.: “abre pagos”, “ir a reportes”, “nuevo usuario”';
  function openPanel() { panel.classList.add('open'); launcher.hidden = true; }
  function closePanel() { panel.classList.remove('open'); launcher.hidden = false; stopListening(); }
  launcher.addEventListener('click', openPanel);
  $('vaClose').addEventListener('click', closePanel);

  var synth = window.speechSynthesis || null, esVoice = null;
  function pickVoice() { if (synth && window.sigsfPickVoice) esVoice = window.sigsfPickVoice(synth); }
  if (synth) { pickVoice(); if (typeof synth.onvoiceschanged !== 'undefined') synth.onvoiceschanged = pickVoice; }
  function say(text) {
    if (!synth || !speakChk.checked) return;
    try { synth.cancel(); } catch (e) { }
    var u = new SpeechSynthesisUtterance(text);
    if (window.sigsfApplyVoice) window.sigsfApplyVoice(u, esVoice);
    synth.speak(u);
  }
  function logAction(icon, texto) {
    var t = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    var d = document.createElement('div');
    d.className = 'list-group-item small';
    d.innerHTML = '<i class="fa-solid ' + icon + ' text-primary me-2"></i>' + SIGSF.escapar(texto) + ' <span class="text-muted">· ' + t + '</span>';
    logEl.prepend(d);
  }
  var pendingMsg = null;
  function ir(target, titulo, frase) { window.sigsfAbrir(target, titulo); say(frase); logAction('fa-arrow-right-to-bracket', frase); }
  function norm(s) { return (s || '').toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '').replace(/[^a-z0-9 ]/g, ' ').replace(/\s+/g, ' ').trim(); }
  function hasAny(w, arr) { for (var i = 0; i < arr.length; i++) { if (w.indexOf(' ' + norm(arr[i]) + ' ') !== -1) return true; } return false; }

  var REGLAS = [
    [['pagos', 'pago', 'cobranza', 'cobranzas', 'yape', 'plin', 'tunki', 'qr', 'cotizacion', 'cotizaciones', 'billetera'], 'pagos.html', 'Pagos y cobranzas'],
    [['solicitudes', 'solicitud', 'bandeja'], 'solicitudes.html', 'Solicitudes de clientes'],
    [['reportes', 'reporte', 'informe', 'informes', 'estadisticas'], 'reportes.html', 'Reportes'],
    [['configuracion', 'ajustes', 'preferencias'], 'configuracion.html', 'Configuración'],
    [['seguridad', 'permisos', 'accesos'], 'seguridad.html', 'Seguridad'],
    [['respaldos', 'respaldo', 'auditoria', 'backup'], 'respaldos.html', 'Respaldos y auditoría'],
    [['versiones', 'version', 'git'], 'versiones.html', 'Control de versiones'],
    [['centro de ayuda', 'soporte', 'manual', 'preguntas frecuentes'], 'ayuda.html', 'Centro de ayuda'],
    [['marketing', 'campana', 'campanas', 'publicidad', 'promocion'], 'Marketing.html', 'Marketing'],
    [['ventas', 'venta', 'vender', 'comercial'], 'Ventas.html', 'Ventas'],
    [['logistica', 'proveedores', 'mercaderia'], 'Logistica.html', 'Logística'],
    [['almacen', 'stock', 'deposito', 'inventario'], 'almacen.html', 'Almacén'],
    [['produccion', 'lotes', 'fabricacion'], 'produccion.html', 'Producción'],
    [['operaciones', 'operacion', 'moneda', 'prestamo', 'prestamos', 'empeno', 'empenos', 'cambio de moneda'], 'operaciones financieras.html', 'Operaciones financieras'],
    [['direccion', 'expedientes', 'estrategica'], 'direccion gerencia.html', 'Dirección'],
    [['gerencia', 'gerencial', 'resultados'], 'gerencia general y toma de  decisiones.html', 'Gerencia general'],
    [['clientes', 'cliente', 'servicios financieros'], 'clientes y servicios financieros.html', 'Clientes y servicios']
  ];
  function process(text) {
    var w = ' ' + norm(text) + ' ';
    if (hasAny(w, ['que puedes hacer', 'comandos', 'que puedo decir', 'ayuda de voz'])) {
      say('Puedes decir: abre pagos, ir a ventas, muéstrame reportes, nuevo usuario, volver a inicio o cerrar sesión.');
      logAction('fa-circle-info', 'Comandos de voz'); return;
    }
    if (hasAny(w, ['cerrar sesion', 'cerrar la sesion', 'logout'])) {
      say('Cerrando sesión'); logAction('fa-right-from-bracket', 'Cerrando sesión');
      setTimeout(function () { API.logout(); }, 900); return;
    }
    if (hasAny(w, ['usuarios', 'usuario'])) {
      if (hasAny(w, ['nuevo', 'nueva', 'crear', 'agregar', 'registrar', 'alta'])) {
        pendingMsg = 'sigsf:new-user';
        ir('usuarios.html', 'Usuarios', 'Abriendo el registro de un nuevo usuario'); return;
      }
      ir('usuarios.html', 'Usuarios', 'Abriendo Usuarios'); return;
    }
    for (var i = 0; i < REGLAS.length; i++) {
      if (hasAny(w, REGLAS[i][0])) { ir(REGLAS[i][1], REGLAS[i][2], 'Abriendo ' + REGLAS[i][2]); return; }
    }
    if (hasAny(w, ['inicio', 'menu', 'principal', 'volver', 'regresar', 'atras'])) {
      window.sigsfAbrir('home'); say('Volviendo al inicio'); logAction('fa-house', 'Inicio'); return;
    }
    say('No te entendí. Puedes decir: abre pagos, ir a reportes o nuevo usuario.');
    logAction('fa-circle-question', 'No reconocido: “' + text + '”');
  }
  var frame = document.getElementById('moduleFrame');
  frame.addEventListener('load', function () {
    if (pendingMsg && frame.src.indexOf('about:blank') === -1) {
      var m = pendingMsg; pendingMsg = null;
      setTimeout(function () { try { frame.contentWindow.postMessage(m, location.origin); } catch (e) { } }, 400);
    }
  });

  var SR = window.SpeechRecognition || window.webkitSpeechRecognition || null, recog = null, active = false;
  if (!SR) {
    noteEl.textContent = 'Tu navegador no permite usar el micrófono aquí. Usa Google Chrome o Microsoft Edge.';
    micBtn.disabled = true;
  } else {
    recog = new SR(); recog.lang = 'es-ES'; recog.interimResults = true; recog.continuous = true;
    recog.onresult = function (e) {
      var interim = '', finalTxt = '';
      for (var i = e.resultIndex; i < e.results.length; i++) {
        var r = e.results[i];
        if (r.isFinal) finalTxt += r[0].transcript; else interim += r[0].transcript;
      }
      if (interim) heardEl.textContent = '“' + interim + '”';
      if (finalTxt.trim()) { heardEl.textContent = '“' + finalTxt.trim() + '”'; process(finalTxt.trim()); }
    };
    recog.onerror = function (ev) {
      if (ev.error === 'not-allowed' || ev.error === 'service-not-allowed') {
        noteEl.textContent = 'Permiso de micrófono denegado. Actívalo en el navegador para usar la voz.';
        stopListening();
      }
    };
    recog.onend = function () { if (active && contChk.checked) { try { recog.start(); } catch (e) { } } else { active = false; reflect(); } };
  }
  function reflect() {
    micBtn.classList.toggle('listening', active);
    statusEl.textContent = active ? 'Escuchando… habla ahora' : 'Toca el micrófono y habla';
    statusEl.classList.toggle('text-danger', active);
    if (!active) heardEl.textContent = EJEMPLO;
  }
  function startListening() { if (!recog) return; try { recog.start(); active = true; reflect(); say('Te escucho'); } catch (e) { } }
  function stopListening() { active = false; if (recog) { try { recog.stop(); } catch (e) { } } reflect(); }
  micBtn.addEventListener('click', function () { active ? stopListening() : startListening(); });
})();
