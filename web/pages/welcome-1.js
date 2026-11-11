/* Presentación bancaria: un mismo documento y un solo audio durante toda
   la transición. Si el navegador bloquea autoplay, no se fuerza su política. */
(function () {
  'use strict';
  var intro = document.getElementById('bankIntro');
  var landing = document.getElementById('bankLanding');
  var audio = document.getElementById('sigsfAudioBienvenida');
  if (!intro || !landing || !audio) return;
  var status = document.getElementById('introStatus');
  var timer = null, salida = null, primera = true;
  var media = window.matchMedia ? window.matchMedia('(prefers-reduced-motion: reduce)') : { matches: false };
  var reducido = media.matches;
  var pausaAnimacion = reducido;
  var controles = document.querySelectorAll('[data-bank-motion]');
  function cancelarTiempo() { if (timer !== null) { clearTimeout(timer); timer = null; } }
  function actualizarAnimacion() {
    document.body.classList.toggle('bank-paused', pausaAnimacion);
    controles.forEach(function (b) {
      b.setAttribute('aria-pressed', String(pausaAnimacion));
      b.querySelector('span').textContent = pausaAnimacion ? 'Reanudar animación' : 'Pausar animación';
      b.querySelector('i').className = 'fa-solid ' + (pausaAnimacion ? 'fa-play' : 'fa-pause');
      b.disabled = reducido;
      if (reducido) b.querySelector('span').textContent = 'Movimiento reducido';
    });
  }
  controles.forEach(function (b) { b.addEventListener('click', function () { pausaAnimacion = !pausaAnimacion; actualizarAnimacion(); }); });
  if (media.addEventListener) media.addEventListener('change', function (e) { reducido = e.matches; pausaAnimacion = reducido; actualizarAnimacion(); });
  actualizarAnimacion();
  function mostrarPortada(detener) {
    cancelarTiempo();
    if (intro.hidden || salida !== null) return;
    if (detener) audio.pause();
    intro.classList.add('is-leaving');
    salida = setTimeout(function () {
      intro.hidden = true; intro.classList.remove('is-leaving'); landing.inert = false; salida = null;
      document.getElementById('landingTitle').focus({ preventScroll: true });
    }, reducido ? 0 : 350);
  }
  function reproducir() {
    audio.muted = false; audio.volume = 1;
    try {
      var intento = audio.play();
      if (intento && intento.catch) intento.catch(function () {
        if (!intro.hidden) status.textContent = 'Pulsa «Iniciar con sonido» para escuchar. También puedes entrar directamente.';
      });
    } catch (_) { status.textContent = 'Pulsa «Iniciar con sonido» para escuchar el saludo.'; }
  }
  function presentar() {
    if (salida !== null) { clearTimeout(salida); salida = null; }
    cancelarTiempo(); audio.pause(); audio.currentTime = 0;
    intro.classList.remove('is-leaving'); intro.hidden = false; landing.inert = true;
    status.textContent = 'Presentación breve · Activa el sonido para escuchar el saludo.';
    document.getElementById('introStart').focus({ preventScroll: true });
    // Avance sin sonido; si empieza el audio, el evento playing cancela este
    // temporizador y se espera a ended. Un fallo de sonido nunca bloquea acceso.
    timer = setTimeout(function () { mostrarPortada(false); }, reducido ? 3500 : 9000);
    reproducir(); primera = false;
  }
  document.getElementById('introStart').addEventListener('click', function () {
    cancelarTiempo();
    // El controlador compartido inicia o pausa directamente en este clic.
    // Si falla, se mantiene un acceso independiente a la portada.
  });
  document.getElementById('introSkip').addEventListener('click', function () { mostrarPortada(true); });
  document.getElementById('replayIntro').addEventListener('click', presentar);
  intro.addEventListener('keydown', function (e) { if (e.key === 'Escape') { e.preventDefault(); mostrarPortada(true); } });
  audio.addEventListener('playing', function () {
    if (intro.hidden) return;
    cancelarTiempo();
    // Límite de espera ante una carga detenida: cambia de vista sin cortar audio.
    timer = setTimeout(function () { mostrarPortada(false); }, 20000);
  });
  audio.addEventListener('pause', function () { if (!intro.hidden && !primera) cancelarTiempo(); });
  audio.addEventListener('ended', function () { if (!intro.hidden) mostrarPortada(false); });
  audio.addEventListener('error', function () { if (!intro.hidden) { cancelarTiempo(); timer = setTimeout(function () { mostrarPortada(false); }, 3500); } });
  window.addEventListener('pagehide', function () { cancelarTiempo(); if (salida !== null) clearTimeout(salida); });
  presentar();
})();
