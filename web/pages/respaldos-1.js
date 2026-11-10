(function(){
  SIGSF.embebido();
  var $ = function(id){ return document.getElementById(id); };
  var esc = SIGSF.escapar;
  var respaldos = [], eventos = [];

  // Pestañas
  document.querySelectorAll('[data-tab]').forEach(function(b){
    b.addEventListener('click', function(){
      document.querySelectorAll('[data-tab]').forEach(function(x){ x.classList.remove('on'); });
      b.classList.add('on');
      document.querySelectorAll('[data-pane]').forEach(function(p){
        p.style.display = p.getAttribute('data-pane') === b.getAttribute('data-tab') ? 'block' : 'none';
      });
    });
  });

  function msg(texto, tipo){
    var m = $('msgR'); m.className = 'msg' + (tipo ? ' ' + tipo : '');
    m.innerHTML = texto ? ((tipo === 'ok' ? '<i class="fa-solid fa-circle-check"></i> ' : '') + esc(texto)) : '';
  }

  var COLOR_ACCION = { LOGIN_OK:'b-verde', LOGIN_FALLIDO:'b-rojo', ACCESO_DENEGADO:'b-rojo', ELIMINAR:'b-rojo',
                       CREAR:'b-azul', ACTUALIZAR:'b-ambar', RESPALDO:'b-verde', EXPORTAR:'b-gris', LOGOUT:'b-gris' };

  function kpis(){
    var ult = respaldos[0];
    var fallidos = eventos.filter(function(e){ return e.accion === 'LOGIN_FALLIDO'; }).length;
    var denegados = eventos.filter(function(e){ return e.accion === 'ACCESO_DENEGADO'; }).length;
    $('kpis').innerHTML = SIGSF.kpi('Respaldos guardados', SIGSF.num(respaldos.length))
      + SIGSF.kpi('Último respaldo', ult ? SIGSF.fechaCorta(ult.fecha) : '—', 'verde')
      + SIGSF.kpi('Logins fallidos', SIGSF.num(fallidos), 'ambar')
      + SIGSF.kpi('Accesos denegados', SIGSF.num(denegados), 'ambar');
  }

  function pintarR(){
    var tb = $('tablaR'); tb.innerHTML = '';
    if (!respaldos.length){ tb.innerHTML = '<tr><td class="vacio" colspan="5">Aún no hay respaldos. Presiona «Generar respaldo ahora».</td></tr>'; return; }
    respaldos.forEach(function(r){
      var tr = document.createElement('tr');
      tr.innerHTML = '<td><i class="fa-solid fa-file-code" style="color:#64748b;"></i> ' + esc(r.nombre) + '</td>'
        + '<td>' + SIGSF.badge(r.metodo.toUpperCase()) + '</td>'
        + '<td>' + esc(SIGSF.fechaCorta(r.fecha)) + '</td>'
        + '<td class="num">' + esc(r.tamanio) + '</td>'
        + '<td><div class="row-btns">'
        + '<button class="ibtn ok" title="Descargar" data-d="1"><i class="fa-solid fa-download"></i></button>'
        + '<button class="ibtn danger" title="Eliminar" data-e="1"><i class="fa-solid fa-trash"></i></button></div></td>';
      tr.querySelector('[data-d]').addEventListener('click', function(){
        API.descargar('/respaldos/' + encodeURIComponent(r.nombre) + '/descargar', r.nombre)
           .catch(function(e){ msg('No se pudo descargar: ' + e.message, 'err'); });
      });
      tr.querySelector('[data-e]').addEventListener('click', async function(){
        if (!confirm('¿Eliminar el respaldo ' + r.nombre + '?')) return;
        try { await API.delete('/respaldos/' + encodeURIComponent(r.nombre)); msg('Respaldo eliminado.', 'ok'); await cargarR(); }
        catch(e){ msg('No se pudo eliminar: ' + e.message, 'err'); }
      });
      tb.appendChild(tr);
    });
  }

  function pintarA(){
    var tb = $('tablaA'); tb.innerHTML = '';
    var f = ($('buscarA').value || '').toLowerCase();
    var lista = eventos.filter(function(e){ return !f || JSON.stringify(e).toLowerCase().indexOf(f) !== -1; });
    if (!lista.length){ tb.innerHTML = '<tr><td class="vacio" colspan="8">Sin eventos.</td></tr>'; return; }
    lista.forEach(function(e){
      var tr = document.createElement('tr');
      tr.innerHTML = '<td>' + esc(e.idAuditoria) + '</td><td>' + esc(SIGSF.fechaCorta(e.fecha)) + '</td>'
        + '<td>' + esc(e.usuario) + '</td><td>' + esc(e.rol || '—') + '</td>'
        + '<td><span class="badge ' + (COLOR_ACCION[e.accion] || 'b-azul') + '">' + esc(e.accion) + '</span></td>'
        + '<td style="font-family:monospace;font-size:.78rem;">' + esc(e.recurso) + '</td>'
        + '<td>' + esc(e.estadoHttp) + '</td><td>' + esc(e.ip) + '</td>';
      tb.appendChild(tr);
    });
  }

  async function cargarR(){
    try { respaldos = await API.get('/respaldos'); pintarR(); }
    catch(e){ $('tablaR').innerHTML = '<tr><td class="error-fila" colspan="5">' + esc(e.message) + '</td></tr>'; }
    kpis();
  }
  async function cargarA(){
    try { eventos = await API.get('/auditoria'); pintarA(); }
    catch(e){ $('tablaA').innerHTML = '<tr><td class="error-fila" colspan="8">' + esc(e.message) + '</td></tr>'; }
    kpis();
  }

  $('btnRespaldar').addEventListener('click', async function(){
    var b = this; b.disabled = true; $('txtRespaldar').textContent = 'Generando…'; msg('', '');
    try {
      var r = await API.post('/respaldos', {});
      msg('Respaldo generado: ' + r.nombre + ' (' + r.tamanio + ', método ' + r.metodo + ').', 'ok');
      await cargarR(); await cargarA();
    } catch(e){ msg('No se pudo generar el respaldo: ' + e.message, 'err'); }
    b.disabled = false; $('txtRespaldar').textContent = 'Generar respaldo ahora';
  });
  $('btnRefrescar').addEventListener('click', function(){ cargarR(); cargarA(); });
  $('buscarA').addEventListener('input', pintarA);
  document.querySelectorAll('[data-expa]').forEach(function(btn){
    btn.addEventListener('click', function(){
      API.descargar('/auditoria/exportar?formato=' + btn.getAttribute('data-expa'))
         .catch(function(e){ msg('No se pudo exportar: ' + e.message, 'err'); });
    });
  });

  cargarR(); cargarA();
})();
