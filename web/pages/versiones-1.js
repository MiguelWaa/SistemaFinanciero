(function(){
  SIGSF.embebido();
  var $ = function(id){ return document.getElementById(id); };
  var esc = SIGSF.escapar;
  var commits = [];

  var TIPOS = { feat:['b-verde','Funcionalidad'], fix:['b-rojo','Corrección'], docs:['b-azul','Documentación'],
                test:['b-cian','Pruebas'], build:['b-gris','Build'], ci:['b-gris','CI'], chore:['b-gris','Mantenimiento'],
                refactor:['b-morado','Refactor'], perf:['b-ambar','Rendimiento'], merge:['b-morado','Merge'],
                revert:['b-rojo','Rollback'], style:['b-gris','Estilo'], otro:['b-gris','Otro'] };

  document.querySelectorAll('[data-tab]').forEach(function(b){
    b.addEventListener('click', function(){
      document.querySelectorAll('[data-tab]').forEach(function(x){ x.classList.remove('on'); });
      b.classList.add('on');
      document.querySelectorAll('[data-pane]').forEach(function(p){
        p.style.display = p.getAttribute('data-pane') === b.getAttribute('data-tab') ? 'block' : 'none';
      });
    });
  });

  function fecha(v){ return v ? SIGSF.fechaCorta(String(v).substring(0,19)) : '—'; }
  function tipoBadge(t){ var x = TIPOS[t] || TIPOS.otro; return '<span class="badge ' + x[0] + '">' + esc(x[1]) + '</span>'; }

  function refs(texto){
    if (!texto) return '';
    return texto.split(',').map(function(r){
      r = r.trim(); if (!r) return '';
      var cls = 'rama';
      if (r.indexOf('tag: ') === 0){ cls = 'tag'; r = r.substring(5); }
      else if (r.indexOf('HEAD') === 0){ cls = 'head'; }
      else if (r.indexOf('origin/') === 0){ cls = 'remota'; }
      return '<span class="ref ' + cls + '">' + esc(r) + '</span>';
    }).join('');
  }

  function item(l, v, cls){ return '<div class="it"><div class="l">' + esc(l) + '</div><div class="v ' + (cls||'') + '">' + v + '</div></div>'; }

  function pintarActual(v){
    $('kpis').innerHTML = SIGSF.kpi('Versión del sistema', 'v' + (v.version || '—'))
      + SIGSF.kpi('Rama', v.rama || '—', 'verde')
      + SIGSF.kpi('Commit', v.commitCorto || '—', 'ambar')
      + SIGSF.kpi('Esquema BD', v.versionEsquema ? 'v' + v.versionEsquema : 'sin registrar', v.esquemaAlDia ? 'verde' : 'rojo');
    $('info').innerHTML =
        item('Versión (SemVer)', '<b>' + esc(v.version || '—') + '</b>')
      + item('Etiqueta más cercana (git describe)', esc(v.etiqueta || '—'), 'mono')
      + item('Rama', esc(v.rama || '—'), 'mono')
      + item('Commit', v.commit ? '<span class="hash">' + esc(v.commit) + '</span>' : '—')
      + item('Último mensaje', esc(v.mensaje || '—'))
      + item('Autor del commit', esc(v.autor || '—'))
      + item('Fecha del commit', esc(fecha(v.fechaCommit)))
      + item('Fecha de compilación', esc(fecha(v.fechaCompilacion)))
      + item('Origen del dato', SIGSF.badge(v.origen || '—'))
      + item('Esquema de la base', esc(v.versionEsquema || '—') + ' (esperado ' + esc(v.versionEsquemaEsperada || '—') + ')');
    var av = '';
    if (v.cambiosSinCommit) av += '<div class="aviso warn"><i class="fa-solid fa-triangle-exclamation"></i> Hay <b>cambios sin confirmar</b> en el proyecto: esta ejecución no corresponde exactamente al commit ' + esc(v.commitCorto) + '. Use <code>git status</code> y <code>git commit</code>.</div>';
    if (!v.esquemaAlDia) av += '<div class="aviso warn"><i class="fa-solid fa-database"></i> La base de datos está en la versión <b>' + esc(v.versionEsquema || 'sin registrar') + '</b> y el código espera la <b>' + esc(v.versionEsquemaEsperada) + '</b>. Ejecute en pgAdmin los scripts pendientes de la carpeta <code>db/</code> (al menos <code>db/version_esquema.sql</code>).</div>';
    else av += '<div class="aviso ok"><i class="fa-solid fa-circle-check"></i> Código y base de datos en versiones compatibles: aplicación <b>v' + esc(v.version) + '</b> · esquema <b>v' + esc(v.versionEsquema) + '</b>.</div>';
    $('avisos').innerHTML = av;
  }

  function pintarLanzamientos(lista){
    if (!lista || !lista.length){ $('lanzamientos').innerHTML = '<div class="vacio">No se encontró CHANGELOG.md.</div>'; return; }
    $('lanzamientos').innerHTML = lista.map(function(l){
      var pend = /publicar/i.test(l.version);
      var cambios = (l.cambios || []).slice(0, 6).map(function(c){ return '<li>' + esc(c.replace(/`/g,'')) + '</li>'; }).join('');
      return '<div class="tl' + (pend ? ' pend' : '') + '"><h3>' + esc(pend ? l.version : 'v' + l.version) + '<small>' + esc(l.titulo || '') + '</small></h3>'
        + (cambios ? '<ul>' + cambios + '</ul>' : '<ul><li style="color:#94a3b8;list-style:none;margin-left:-18px;">Sin cambios pendientes.</li></ul>') + '</div>';
    }).join('');
  }

  function pintarCommits(){
    var q = ($('buscarC').value || '').toLowerCase();
    var tb = $('tablaC'); tb.innerHTML = '';
    var lista = commits.filter(function(c){ return !q || (c.hash + ' ' + c.autor + ' ' + c.mensaje + ' ' + c.referencias).toLowerCase().indexOf(q) >= 0; });
    if (!lista.length){ tb.innerHTML = '<tr><td class="vacio" colspan="5">Sin resultados.</td></tr>'; return; }
    lista.forEach(function(c){
      var tr = document.createElement('tr');
      tr.innerHTML = '<td><span class="hash">' + esc(c.hash) + '</span></td><td>' + tipoBadge(c.tipo) + '</td>'
        + '<td>' + refs(c.referencias) + ' ' + esc(c.mensaje) + '</td><td>' + esc(c.autor) + '</td><td>' + esc(fecha(c.fecha)) + '</td>';
      tb.appendChild(tr);
    });
  }

  function pintarGrafo(lineas){
    if (!lineas || !lineas.length){ $('grafo').textContent = 'Sin repositorio Git en la carpeta del proyecto.'; return; }
    $('grafo').innerHTML = lineas.map(function(l){
      var m = l.match(/^([\s|\\\/*_.-]*)([0-9a-f]{7,})?(.*)$/);
      if (!m) return esc(l);
      var g = '', col = 0;
      for (var i = 0; i < m[1].length; i++){
        var ch = m[1][i];
        if (ch === ' ') { g += ' '; continue; }
        col = Math.floor(i / 2) % 6 + 1;
        g += '<span class="g' + col + '">' + esc(ch) + '</span>';
      }
      var partes = (m[3] || '').split('\u001f');
      var resto = esc(partes[0]) + (partes[1] ? ' <span class="d">(' + esc(partes[1]) + ')</span>' : '');
      return g + (m[2] ? '<span class="h">' + m[2] + '</span>' : '') + resto;
    }).join('\n');
  }

  function pintarRamas(ramas, etiquetas){
    $('tablaR').innerHTML = ramas.length ? ramas.map(function(r){
      var tipo = r.remota ? 'remota' : (/^(main|master)$/.test(r.nombre) ? 'producción' : r.nombre === 'develop' ? 'integración'
               : r.nombre.split('/')[0]);
      return '<tr><td><span class="ref ' + (r.remota ? 'remota' : 'rama') + '">' + esc(r.nombre) + '</span></td><td>' + SIGSF.badge(tipo)
        + '</td><td><span class="hash">' + esc(r.commit) + '</span></td><td>' + esc(r.descripcion) + '</td><td>' + esc(fecha(r.fecha)) + '</td></tr>';
    }).join('') : '<tr><td class="vacio" colspan="5">Sin ramas.</td></tr>';
    $('tablaT').innerHTML = etiquetas.length ? etiquetas.map(function(t){
      return '<tr><td><span class="ref tag">' + esc(t.nombre) + '</span></td><td><span class="hash">' + esc(t.commit) + '</span></td><td>'
        + esc(t.descripcion) + '</td><td>' + esc(fecha(t.fecha)) + '</td></tr>';
    }).join('') : '<tr><td class="vacio" colspan="4">Sin etiquetas.</td></tr>';
  }

  function pintarEsquema(lista){
    $('tablaE').innerHTML = lista && lista.length ? lista.map(function(v){
      return '<tr><td><b>v' + esc(v.version) + '</b></td><td>' + esc(v.descripcion) + '</td><td class="mono">' + esc(v.script)
        + '</td><td>' + esc(fecha(v.aplicadoEn)) + '</td><td>' + esc(v.aplicadoPor || '') + '</td></tr>';
    }).join('') : '<tr><td class="vacio" colspan="5">La tabla version_esquema está vacía.</td></tr>';
  }

  async function cargar(){
    try {
      var d = await API.get('/versiones');
      pintarActual(d.actual);
      pintarLanzamientos(d.lanzamientos);
      commits = d.commits || [];
      var usados = {}; commits.forEach(function(c){ usados[c.tipo] = 1; });
      $('leyenda').innerHTML = 'Tipos (Conventional Commits): ' + Object.keys(usados).map(tipoBadge).join(' ');
      pintarCommits();
      pintarGrafo(d.grafo);
      pintarRamas(d.ramas || [], d.etiquetas || []);
      pintarEsquema(d.esquema);
      if (!d.repositorio) $('avisos').innerHTML += '<div class="aviso info"><i class="fa-solid fa-circle-info"></i> El sistema no se está ejecutando dentro de un repositorio Git: se muestran los datos de compilación.</div>';
    } catch (e) {
      // Usuarios que no son ADMIN: solo la versión en ejecución.
      try {
        pintarActual(await API.get('/version'));
        $('lanzamientos').innerHTML = '<div class="vacio">El historial del repositorio es visible solo para el Administrador.</div>';
        document.querySelectorAll('[data-admin]').forEach(function(b){ b.style.display = 'none'; });
      } catch (e2) {
        $('avisos').innerHTML = '<div class="aviso warn">No se pudo consultar la versión: ' + esc(e2.message) + '</div>';
      }
    }
  }
  $('buscarC').addEventListener('input', pintarCommits);
  cargar();
})();
