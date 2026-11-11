/* Bienvenida con el MP3 original. No modifica autenticación ni navegación.
   Un intento automático por página hasta que se reproduce; una reproducción
   automática por pestaña/sesión. Si el navegador exige un gesto, el botón queda
   disponible. No se interceptan clics generales ni se cambia la política CSP. */
(function () {
  'use strict';
  if (window.self !== window.top) return;
  var KEY = 'sigsf_bienvenida_reproducida_v1';
  function recordar() {
    try { sessionStorage.setItem(KEY, '1'); } catch (_) { /* Almacenamiento opcional. */ }
  }
  function escuchada() {
    try { return sessionStorage.getItem(KEY) === '1'; } catch (_) { return false; }
  }
  function iniciar() {
    if (document.getElementById('sigsfBienvenida')) return;
    var panel = document.createElement('aside');
    panel.id = 'sigsfBienvenida';
    panel.className = 'sg-welcome-audio';
    panel.setAttribute('aria-label', 'Audio de bienvenida');
    var audio = document.createElement('audio');
    audio.id = 'sigsfAudioBienvenida';
    audio.src = 'audio/bienvenida.mp3';
    audio.preload = 'metadata';
    var boton = document.createElement('button');
    boton.type = 'button';
    boton.setAttribute('aria-controls', audio.id);
    var icono = document.createElement('i');
    icono.className = 'fa-solid fa-volume-high';
    icono.setAttribute('aria-hidden', 'true');
    var texto = document.createElement('span');
    texto.textContent = 'Escuchar bienvenida';
    boton.appendChild(icono);
    boton.appendChild(texto);
    var estado = document.createElement('small');
    estado.setAttribute('role', 'status');
    estado.textContent = 'SIGSF';
    var cerrar = document.createElement('button');
    cerrar.type = 'button';
    cerrar.textContent = '×';
    cerrar.setAttribute('aria-label', 'Cerrar audio de bienvenida');
    panel.appendChild(audio);
    panel.appendChild(boton);
    panel.appendChild(estado);
    panel.appendChild(cerrar);
    document.body.appendChild(panel);

    function mostrarPausa() {
      texto.textContent = audio.ended ? 'Repetir bienvenida' : 'Escuchar bienvenida';
      icono.className = 'fa-solid fa-volume-high';
    }
    function fallo(error) {
      mostrarPausa();
      if (error && error.name === 'NotAllowedError') {
        estado.textContent = 'Pulsa para escuchar';
      } else if (!audio.error && error && error.name === 'AbortError') {
        estado.textContent = 'En pausa';
      } else {
        estado.textContent = 'Audio no disponible';
      }
    }
    function reproducir() {
      try {
        if (audio.ended) audio.currentTime = 0;
        var intento = audio.play();
        if (intento && typeof intento.catch === 'function') intento.catch(fallo);
      } catch (error) { fallo(error); }
    }
    boton.addEventListener('click', function () {
      if (audio.paused) reproducir();
      else { recordar(); audio.pause(); }
    });
    cerrar.addEventListener('click', function () {
      recordar();
      audio.pause();
      panel.remove();
    });
    audio.addEventListener('playing', function () {
      recordar();
      texto.textContent = 'Pausar bienvenida';
      icono.className = 'fa-solid fa-pause';
      estado.textContent = 'Reproduciendo';
    });
    audio.addEventListener('pause', function () {
      mostrarPausa();
      estado.textContent = audio.ended ? 'Bienvenido a SIGSF' : 'En pausa';
    });
    audio.addEventListener('ended', function () {
      recordar();
      mostrarPausa();
      estado.textContent = 'Bienvenido a SIGSF';
    });
    audio.addEventListener('error', function () { fallo(audio.error); });
    window.addEventListener('pagehide', function () { audio.pause(); });
    if (!escuchada()) reproducir();
  }
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', iniciar);
  else iniciar();
})();
