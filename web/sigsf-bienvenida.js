/* Reproductor compartido del MP3 original. Las páginas internas solo lo
   reproducen por petición explícita. La portada administra su presentación.
   No guarda una marca de sesión que pueda dejar el saludo en silencio. */
(function () {
  'use strict';
  if (window.self !== window.top) return;
  function iniciar() {
    var audio = document.getElementById('sigsfAudioBienvenida');
    if (audio && audio._sigsfBound) return;
    if (!audio) {
      var panel = document.createElement('aside');
      panel.id = 'sigsfBienvenida'; panel.className = 'sg-welcome-audio';
      panel.setAttribute('aria-label', 'Audio de bienvenida');
      audio = document.createElement('audio');
      audio.id = 'sigsfAudioBienvenida'; audio.src = 'audio/bienvenida.mp3'; audio.preload = 'metadata';
      var boton = document.createElement('button'); boton.type = 'button';
      boton.setAttribute('data-bienvenida-play', ''); boton.setAttribute('aria-controls', audio.id);
      var icono = document.createElement('i'); icono.className = 'fa-solid fa-volume-high'; icono.setAttribute('aria-hidden', 'true');
      var label = document.createElement('span'); label.setAttribute('data-audio-label', ''); label.textContent = 'Escuchar bienvenida';
      boton.appendChild(icono); boton.appendChild(label);
      var aviso = document.createElement('small'); aviso.id = 'sigsfAudioEstado'; aviso.setAttribute('role', 'status'); aviso.textContent = 'Activa el sonido';
      var cerrar = document.createElement('button'); cerrar.type = 'button'; cerrar.textContent = '×'; cerrar.setAttribute('aria-label', 'Cerrar audio de bienvenida');
      cerrar.addEventListener('click', function () { audio.pause(); panel.remove(); });
      panel.appendChild(audio); panel.appendChild(boton); panel.appendChild(aviso); panel.appendChild(cerrar); document.body.appendChild(panel);
    }
    audio._sigsfBound = true;
    var botones = Array.prototype.slice.call(document.querySelectorAll('[data-bienvenida-play]'));
    var estados = Array.prototype.slice.call(document.querySelectorAll('#bankAudioStatus, #introStatus, #sigsfAudioEstado'));
    botones.forEach(function (boton) { boton._sigsfIdle = boton.querySelector('[data-audio-label]').textContent; });
    function estado(texto) { estados.forEach(function (e) { e.textContent = texto; }); }
    function actualizar() {
      botones.forEach(function (boton) {
        boton.querySelector('[data-audio-label]').textContent = !audio.paused ? 'Pausar saludo' : audio.ended ? 'Repetir saludo' : audio.currentTime > 0 ? 'Continuar saludo' : boton._sigsfIdle;
        var ic = boton.querySelector('i'); if (ic) ic.className = 'fa-solid ' + (audio.paused ? 'fa-volume-high' : 'fa-pause');
      });
    }
    function fallo(error) {
      actualizar();
      estado(error && error.name === 'NotAllowedError' ? 'Pulsa el botón de sonido para escuchar el saludo.' : 'No se pudo reproducir. Puedes usar el reproductor o el enlace Abrir MP3.');
    }
    botones.forEach(function (boton) {
      boton.addEventListener('click', function () {
        if (!audio.paused) { audio.pause(); return; }
        // play() permanece dentro del gesto de clic. Sin fetch, retrasos ni navegación.
        audio.muted = false; audio.volume = 1;
        if (audio.ended) audio.currentTime = 0;
        estado('Preparando el saludo…');
        try { var promesa = audio.play(); if (promesa && promesa.catch) promesa.catch(fallo); }
        catch (error) { fallo(error); }
      });
    });
    audio.addEventListener('playing', function () { actualizar(); estado('Reproduciendo la bienvenida de SIGSF.'); });
    audio.addEventListener('pause', function () { actualizar(); if (!audio.ended) estado('Saludo en pausa. Puedes continuar cuando quieras.'); });
    audio.addEventListener('ended', function () { actualizar(); estado('Bienvenido a SIGSF. Puedes repetir el saludo.'); });
    audio.addEventListener('error', function () { fallo(audio.error); });
    window.addEventListener('pagehide', function () { audio.pause(); });
  }
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', iniciar); else iniciar();
})();
