/* El video original precede a la portada en el mismo documento.
   Si el navegador exige un gesto para el sonido, se ofrece un control explícito. */
(function () {
  'use strict';
  var intro = document.getElementById('bankIntro');
  var landing = document.getElementById('bankLanding');
  var video = document.getElementById('sigsfVideoIntro');
  var audio = document.getElementById('sigsfAudioBienvenida');
  if (!intro || !landing || !video) return;
  var status = document.getElementById('introStatus');
  var start = document.getElementById('introStart');
  var timer = null, salida = null, intentoId = 0;
  var media = window.matchMedia ? window.matchMedia('(prefers-reduced-motion: reduce)') : { matches: false };
  var reducido = media.matches, pausaAnimacion = reducido;
  var controles = document.querySelectorAll('[data-bank-motion]');
  function activo() { return !intro.hidden && salida === null; }
  function cancelarTiempo() { if (timer !== null) { clearTimeout(timer); timer = null; } }
  function actualizarAnimacion() {
    document.body.classList.toggle('bank-paused', pausaAnimacion);
    controles.forEach(function (b) {
      b.setAttribute('aria-pressed', String(pausaAnimacion));
      b.querySelector('span').textContent = reducido ? 'Movimiento reducido' : (pausaAnimacion ? 'Reanudar animación' : 'Pausar animación');
      b.querySelector('i').className = 'fa-solid ' + (pausaAnimacion ? 'fa-play' : 'fa-pause');
      b.disabled = reducido;
    });
  }
  controles.forEach(function (b) { b.addEventListener('click', function () { pausaAnimacion = !pausaAnimacion; actualizarAnimacion(); }); });
  if (media.addEventListener) media.addEventListener('change', function (e) {
    reducido = e.matches; pausaAnimacion = reducido; actualizarAnimacion();
    if (reducido && activo()) { intentoId++; video.pause(); cancelarTiempo(); status.textContent = 'Movimiento reducido. Puedes reproducir el video o entrar a la portada.'; }
  });
  actualizarAnimacion();
  function actualizarBoton() {
    var sinSonido = video.muted || video.volume === 0;
    start.querySelector('span').textContent = sinSonido ? 'Activar sonido desde el inicio' : (video.paused ? 'Reproducir con sonido' : 'Pausar presentación');
    start.querySelector('i').className = 'fa-solid ' + (sinSonido ? 'fa-volume-high' : (video.paused ? 'fa-play' : 'fa-pause'));
  }
  function mostrarPortada() {
    cancelarTiempo();
    if (!activo()) return;
    intentoId++;
    intro.classList.add('is-leaving');
    salida = setTimeout(function () {
      intro.hidden = true; intro.classList.remove('is-leaving'); landing.inert = false; salida = null;
      document.getElementById('landingTitle').focus({ preventScroll: true });
    }, reducido ? 0 : 350);
    video.pause();
  }
  function esperarCarga() { cancelarTiempo(); timer = setTimeout(mostrarPortada, 15000); }
  function reproducir(permitirSilencio) {
    var id = ++intentoId;
    esperarCarga();
    function fallo(error) {
      if (id !== intentoId || !activo()) return;
      cancelarTiempo();
      if (permitirSilencio && error && error.name === 'NotAllowedError') {
        video.muted = true; actualizarBoton(); reproducir(false);
      } else {
        actualizarBoton();
        status.textContent = 'Pulsa «Reproducir con sonido» o usa los controles del video. También puedes entrar directamente.';
      }
    }
    try {
      var intento = video.play();
      if (intento && intento.then) intento.then(function () { if (!activo()) video.pause(); }, fallo);
    } catch (error) { fallo(error); }
  }
  function presentar() {
    if (salida !== null) { clearTimeout(salida); salida = null; }
    cancelarTiempo(); intentoId++; video.pause();
    if (audio) audio.pause();
    video.currentTime = 0; video.muted = false; video.volume = 1;
    intro.classList.remove('is-leaving'); intro.hidden = false; landing.inert = true;
    actualizarBoton(); start.focus({ preventScroll: true });
    if (reducido) status.textContent = 'Movimiento reducido. Pulsa «Reproducir con sonido» para ver la presentación.';
    else { status.textContent = 'Cargando presentación. Al terminar se abrirá la portada.'; reproducir(true); }
  }
  start.addEventListener('click', function () {
    cancelarTiempo();
    if (!video.paused && !video.muted && video.volume > 0) { intentoId++; video.pause(); return; }
    if (video.muted || video.volume === 0 || video.ended) video.currentTime = 0;
    video.muted = false; video.volume = 1;
    if (audio) audio.pause();
    reproducir(false);
  });
  document.getElementById('introSkip').addEventListener('click', mostrarPortada);
  document.getElementById('replayIntro').addEventListener('click', presentar);
  intro.addEventListener('keydown', function (e) { if (e.key === 'Escape') { e.preventDefault(); mostrarPortada(); } });
  video.addEventListener('playing', function () {
    if (!activo()) { video.pause(); return; }
    if (audio) audio.pause();
    cancelarTiempo(); actualizarBoton();
    status.textContent = video.muted || video.volume === 0 ? 'Reproduciendo sin sonido. Actívalo para escuchar el video desde el inicio.' : 'Reproduciendo presentación. Al terminar se abrirá la portada.';
  });
  video.addEventListener('pause', function () {
    cancelarTiempo(); actualizarBoton();
    if (activo() && !video.ended) status.textContent = 'Presentación pausada. Puedes continuar o entrar a la portada.';
  });
  video.addEventListener('volumechange', actualizarBoton);
  video.addEventListener('ended', mostrarPortada);
  ['waiting', 'stalled'].forEach(function (evento) { video.addEventListener(evento, function () {
    if (activo() && !video.paused) { status.textContent = 'Cargando video… Puedes entrar a la portada en cualquier momento.'; esperarCarga(); }
  }); });
  video.addEventListener('error', function () {
    if (!activo()) return;
    intentoId++; video.pause(); cancelarTiempo();
    status.textContent = 'No se pudo cargar el video. Abriendo la portada…';
    timer = setTimeout(mostrarPortada, 2500);
  });
  if (audio) audio.addEventListener('playing', function () { if (activo()) audio.pause(); });
  window.addEventListener('pagehide', function () {
    intentoId++; cancelarTiempo(); if (salida !== null) clearTimeout(salida);
    intro.hidden = true; landing.inert = false; salida = null; video.pause(); if (audio) audio.pause();
  });
  presentar();
})();
