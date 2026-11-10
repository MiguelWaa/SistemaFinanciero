(function () {
  var $ = function (id) { return document.getElementById(id); }, msg = $('msg');
  function mostrar(texto, tipo) { msg.className = 'alert ' + (tipo === 'ok' ? 'alert-success' : 'alert-danger'); msg.textContent = texto; }
  function limpiar() { msg.className = 'alert d-none'; msg.textContent = ''; }
  function tab(login) {
    $('tabLogin').classList.toggle('active', login); $('tabReg').classList.toggle('active', !login);
    $('paneLogin').hidden = !login; $('paneReg').hidden = login; limpiar();
    (login ? $('liDoc') : $('rgDoc')).focus();
  }
  $('tabLogin').addEventListener('click', function () { tab(true); });
  $('tabReg').addEventListener('click', function () { tab(false); });
  try {
    var motivo = new URLSearchParams(location.search).get('motivo');
    if (motivo === 'expirada') mostrar('Tu sesión expiró. Ingresa nuevamente.', 'err');
    else if (motivo === 'sesion') mostrar('Inicia sesión para continuar.', 'err');
  } catch (e) { }

  function entrar(cliente) {
    try { sessionStorage.setItem('sigsf_cliente', JSON.stringify(cliente)); } catch (e) { }
    location.href = 'portal-inicio.html';
  }
  $('paneLogin').addEventListener('submit', async function (ev) {
    ev.preventDefault();
    var doc = $('liDoc').value.trim(), pass = $('liPass').value;
    if (!doc || !pass) { mostrar('Ingresa tu documento y tu contraseña.', 'err'); return; }
    var b = $('btnLogin'); b.disabled = true; limpiar();
    try { entrar(await API.post('/portal/login', { documento: doc, password: pass })); }
    catch (e) { mostrar(e.message, 'err'); b.disabled = false; }
  });
  $('paneReg').addEventListener('submit', async function (ev) {
    ev.preventDefault();
    var datos = {
      tipoDocumento: $('rgTipo').value, documento: $('rgDoc').value.trim(), nombres: $('rgNombres').value.trim(),
      apellidos: $('rgApellidos').value.trim(), telefono: $('rgTel').value.trim(), correo: $('rgCorreo').value.trim(),
      direccion: $('rgDir').value.trim(), password: $('rgPass').value
    };
    if (!datos.documento || !datos.nombres || !datos.password) { mostrar('Completa documento, nombres y contraseña.', 'err'); return; }
    var b = $('btnReg'); b.disabled = true; limpiar();
    try { var c = await API.post('/portal/registro', datos); mostrar('Cuenta creada. Ingresando…', 'ok'); setTimeout(function () { entrar(c); }, 600); }
    catch (e) { mostrar(e.message, 'err'); b.disabled = false; }
  });
})();
