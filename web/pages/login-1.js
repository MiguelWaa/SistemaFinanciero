(function () {
  var $ = function (i) { return document.getElementById(i); };
  var aviso = $('aviso'), btn = $('btnEntrar');
  function mostrar(txt, tipo) { aviso.className = 'alert ' + (tipo === 'ok' ? 'alert-success' : 'alert-danger'); aviso.textContent = txt; }
  function ocultar() { aviso.className = 'alert d-none'; }

  $('verPass').addEventListener('click', function () {
    var p = $('pass'), i = this.querySelector('i');
    if (p.type === 'password') { p.type = 'text'; i.className = 'fa-solid fa-eye'; } else { p.type = 'password'; i.className = 'fa-solid fa-eye-slash'; }
  });
  $('olvide').addEventListener('click', function (e) { e.preventDefault(); mostrar('Pide al administrador del sistema que restablezca tu contraseña desde el módulo Usuarios.', 'ok'); });

  try {
    var motivo = new URLSearchParams(location.search).get('motivo');
    if (motivo === 'expirada') mostrar('Tu sesión expiró por inactividad. Ingresa nuevamente.', 'err');
    else if (motivo === 'sesion') mostrar('Inicia sesión para acceder al sistema.', 'err');
  } catch (e) { }
  try { var g = localStorage.getItem('sigsf_ultimo_usuario'); if (g) { $('user').value = g; $('recordar').checked = true; $('pass').focus(); } else { $('user').focus(); } } catch (e) { }

  $('loginForm').addEventListener('submit', async function (e) {
    e.preventDefault();
    var u = $('user').value.trim(), p = $('pass').value;
    if (!u || !p) { mostrar('Completa tu correo y contraseña.', 'err'); return; }
    ocultar(); btn.disabled = true; $('btnTxt').textContent = 'Verificando…';
    try {
      var usuario = await API.login(u, p);
      try {
        sessionStorage.setItem('sigsf_user', usuario.nombre || u);
        sessionStorage.setItem('sigsf_usuario', JSON.stringify(usuario));
        if ($('recordar').checked) localStorage.setItem('sigsf_ultimo_usuario', u); else localStorage.removeItem('sigsf_ultimo_usuario');
      } catch (_) { }
      $('btnTxt').textContent = 'Entrando…';
      location.href = 'index.html';
    } catch (ex) {
      mostrar(ex.message || 'No se pudo iniciar sesión.', 'err');
      btn.disabled = false; $('btnTxt').textContent = 'Ingresar';
    }
  });
})();
