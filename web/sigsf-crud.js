/* ============================================================
   SIGSF · Motor de módulos (v5.2 · Bootstrap 5)
   A partir de una configuración sencilla construye un módulo
   completo: formulario con validación, tabla con búsqueda,
   orden por columna y paginación, edición, eliminación con
   confirmación, exportación (Excel, CSV, JSON) e importación
   (CSV o Excel) con vista previa. Requiere sigsf-ui.js.

   La configuración es la misma de versiones anteriores, así que
   las páginas de los módulos no cambian su lógica:
     SIGSF.modulo({ kpis(cont, secciones), secciones: [{
       titulo, tab, icono, descripcion, endpoint, idCampo, sinEditar,
       excel, archivos:{importar, recurso}, campos:[...], columnas:[...],
       acciones:[{icono, titulo, clase, visible, ejecutar, confirmar, exito}],
       antesDeGuardar(datos, sec), alCargar(datos, sec) }] })
   ============================================================ */
(function (S) {
  'use strict';
  var esc = S.escapar;
  function el(id) { return document.getElementById(id); }
  var TAMANOS = [10, 25, 50, 100];
  var MAX_BYTES_IMPORT = 700 * 1024; // el servidor acepta hasta 1 MB por petición (Base64 ≈ +33 %)

  function Seccion(cfg, indice) {
    this.cfg = cfg;
    this.pref = 'sec' + indice + '_';
    this.datos = [];
    this.lookups = {};
    this.editandoId = null;
    this.pagina = 1;
    this.porPagina = 25;
    this.orden = null;      // { k, dir: 1 | -1 }
    this.filtro = '';
  }

  // ---------------------------------------------------------------- HTML
  Seccion.prototype.html = function () {
    var c = this.cfg, p = this.pref, h = '';
    var puedeCrear = c.sinFormulario !== true;

    if (puedeCrear) {
      h += '<div class="card mb-3" id="' + p + 'formCard">';
      h += '<div class="card-header d-flex align-items-start justify-content-between gap-2">';
      h += '<div><h2><i class="fa-solid ' + (c.icono || 'fa-pen-to-square') + ' text-primary me-2"></i><span id="' + p + 'formTitulo">' + esc(c.titulo) + '</span></h2>';
      h += '<p class="card-sub">' + esc(c.descripcion || 'Completa los datos y presiona Guardar.') + '</p></div>';
      h += '<button class="btn btn-sm btn-light" type="button" data-bs-toggle="collapse" data-bs-target="#' + p + 'formBody" aria-expanded="true" title="Mostrar u ocultar el formulario"><i class="fa-solid fa-chevron-up"></i></button>';
      h += '</div>';
      h += '<div class="collapse show" id="' + p + 'formBody"><div class="card-body">';
      h += '<form id="' + p + 'form" novalidate><div class="row g-3">';
      c.campos.forEach(function (f) {
        var col = f.tipo === 'textarea' ? 'col-12 col-lg-6' : 'col-12 col-sm-6 col-lg-3';
        var id = p + f.k;
        h += '<div class="' + col + '">';
        h += '<label class="form-label" for="' + id + '">' + esc(f.l) + (f.req ? ' <span class="req" aria-hidden="true">*</span>' : '') + '</label>';
        if (f.tipo === 'select') {
          h += '<select class="form-select" id="' + id + '"' + (f.req ? ' required' : '') + '></select>';
        } else if (f.tipo === 'textarea') {
          h += '<textarea class="form-control" id="' + id + '" rows="2" placeholder="' + esc(f.ph || '') + '"></textarea>';
        } else {
          var t = (f.tipo === 'money' || f.tipo === 'entero') ? 'number' : (f.tipo || 'text');
          var extra = f.tipo === 'money' ? ' step="0.01" min="0" inputmode="decimal"' : (f.tipo === 'entero' ? ' step="1" inputmode="numeric"' : '');
          var pre = f.tipo === 'money' ? '<span class="input-group-text">S/</span>' : '';
          if (pre) h += '<div class="input-group">' + pre;
          h += '<input class="form-control" type="' + t + '" id="' + id + '"' + extra + ' placeholder="' + esc(f.ph || '') + '"' + (f.req ? ' required' : '') + (t === 'password' ? ' autocomplete="new-password"' : '') + '>';
          if (pre) h += '</div>';
        }
        h += '<div class="invalid-feedback">Este dato es obligatorio.</div>';
        h += '</div>';
      });
      h += '</div>';
      h += '<div class="d-flex flex-wrap gap-2 mt-3">';
      h += '<button class="btn btn-primary" type="submit" id="' + p + 'guardar"><i class="fa-solid fa-floppy-disk me-1"></i><span id="' + p + 'txtGuardar">Guardar</span></button>';
      h += '<button class="btn btn-light" type="button" id="' + p + 'limpiar"><i class="fa-solid fa-eraser me-1"></i><span id="' + p + 'txtLimpiar">Limpiar</span></button>';
      h += '</div>';
      h += '<div class="mt-3" id="' + p + 'msg" role="status"></div>';
      h += '</form></div></div></div>';
    }

    // Tabla
    h += '<div class="card mb-3">';
    h += '<div class="sg-toolbar">';
    h += '<div class="d-flex align-items-center gap-2"><h2 class="h6 mb-0"><i class="fa-solid fa-table-list text-primary me-2"></i>'
      + esc(c.tituloTabla || 'Registros') + '</h2><span class="badge text-bg-light border" id="' + p + 'total">—</span></div>';
    h += '<div class="d-flex flex-wrap gap-2 align-items-center">';
    h += '<div class="input-group input-group-sm search"><span class="input-group-text"><i class="fa-solid fa-magnifying-glass"></i></span>'
      + '<input class="form-control" id="' + p + 'buscar" type="search" placeholder="Buscar…" aria-label="Buscar en la tabla"></div>';
    if (c.archivos || c.excel) {
      h += '<div class="dropdown"><button class="btn btn-sm btn-outline-success dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false"><i class="fa-solid fa-file-arrow-down me-1"></i>Exportar</button>';
      h += '<ul class="dropdown-menu dropdown-menu-end">';
      if (c.archivos) {
        h += '<li><button class="dropdown-item" type="button" data-exp="xlsx"><i class="fa-solid fa-file-excel text-success me-2"></i>Excel (.xlsx)</button></li>';
        h += '<li><button class="dropdown-item" type="button" data-exp="csv"><i class="fa-solid fa-file-csv me-2"></i>CSV</button></li>';
        h += '<li><button class="dropdown-item" type="button" data-exp="json"><i class="fa-solid fa-file-code me-2"></i>JSON</button></li>';
      } else {
        h += '<li><button class="dropdown-item" type="button" id="' + p + 'excel"><i class="fa-solid fa-file-excel text-success me-2"></i>Excel (.xlsx)</button></li>';
      }
      h += '</ul></div>';
    }
    if (c.archivos && c.archivos.importar) {
      h += '<div class="dropdown"><button class="btn btn-sm btn-outline-primary dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false"><i class="fa-solid fa-file-arrow-up me-1"></i>Importar</button>';
      h += '<ul class="dropdown-menu dropdown-menu-end">';
      h += '<li><button class="dropdown-item" type="button" id="' + p + 'importar"><i class="fa-solid fa-upload me-2"></i>Subir CSV o Excel…</button></li>';
      h += '<li><button class="dropdown-item" type="button" id="' + p + 'plantilla"><i class="fa-solid fa-table-columns me-2"></i>Descargar plantilla</button></li>';
      h += '</ul></div>';
      h += '<input type="file" id="' + p + 'archivo" accept=".csv,.xlsx,.txt" hidden>';
    }
    h += '<button class="btn btn-sm btn-light btn-icon" type="button" id="' + p + 'recargar" title="Actualizar"><i class="fa-solid fa-rotate"></i></button>';
    h += '</div></div>';
    h += '<div id="' + p + 'importRes"></div>';
    h += '<div class="table-wrap"><table class="table table-hover align-middle"><thead><tr>';
    c.columnas.forEach(function (col, i) {
      var num = col.money || col.num;
      h += '<th scope="col" class="sortable' + (num ? ' num' : '') + '" data-col="' + i + '">' + esc(col.l) + '<i class="fa-solid fa-sort"></i></th>';
    });
    h += '<th scope="col" class="text-end">Acciones</th>';
    h += '</tr></thead><tbody id="' + p + 'tabla">' + this.filasCargando() + '</tbody></table></div>';
    h += '<div class="sg-foot"><div id="' + p + 'rango">—</div><div class="d-flex align-items-center gap-2">';
    h += '<label class="d-flex align-items-center gap-1 mb-0" for="' + p + 'tam">Filas'
      + '<select class="form-select form-select-sm" style="width:auto" id="' + p + 'tam">'
      + TAMANOS.map(function (n) { return '<option' + (n === 25 ? ' selected' : '') + '>' + n + '</option>'; }).join('') + '</select></label>';
    h += '<nav aria-label="Páginas"><ul class="pagination pagination-sm" id="' + p + 'pag"></ul></nav>';
    h += '</div></div></div>';
    return h;
  };

  Seccion.prototype.filasCargando = function () {
    var n = this.cfg.columnas.length + 1, h = '';
    for (var i = 0; i < 4; i++) {
      h += '<tr class="placeholder-glow">';
      for (var j = 0; j < n; j++) h += '<td><span class="placeholder col-' + (6 + (i + j) % 5) + '"></span></td>';
      h += '</tr>';
    }
    return h;
  };

  // ---------------------------------------------------------- Formulario
  Seccion.prototype.valorCampo = function (f) {
    var v = el(this.pref + f.k).value;
    if (v === null || v === undefined || String(v).trim() === '') return null;
    if (f.tipo === 'money') { var n = parseFloat(v); return isNaN(n) ? null : n; }
    if (f.tipo === 'entero' || f.tipo === 'selectId') { var i = parseInt(v, 10); return isNaN(i) ? null : i; }
    if (f.tipo === 'select' && f.lookup) { var i2 = parseInt(v, 10); return isNaN(i2) ? null : i2; }
    return String(v).trim();
  };

  Seccion.prototype.leerFormulario = function () {
    var datos = {}, self = this, faltan = [];
    this.cfg.campos.forEach(function (f) {
      var v = self.valorCampo(f);
      var input = el(self.pref + f.k);
      var falta = f.req && (v === null || v === '');
      input.classList.toggle('is-invalid', falta);
      if (falta) faltan.push(f.l);
      if (v !== null) datos[f.k] = v;
      else if (!f.req) datos[f.k] = null;
    });
    if (faltan.length) {
      var primero = this.cfg.campos.filter(function (f) { return faltan.indexOf(f.l) !== -1; })[0];
      if (primero) el(this.pref + primero.k).focus();
      throw new Error('Completa: ' + faltan.join(', ') + '.');
    }
    if (this.cfg.antesDeGuardar) this.cfg.antesDeGuardar(datos, this);
    return datos;
  };

  Seccion.prototype.limpiar = function () {
    var self = this;
    if (!el(this.pref + 'form')) return;
    this.cfg.campos.forEach(function (f) {
      var e = el(self.pref + f.k);
      e.classList.remove('is-invalid');
      if (e.tagName === 'SELECT') e.selectedIndex = 0; else e.value = '';
    });
    this.editandoId = null;
    el(this.pref + 'txtGuardar').textContent = 'Guardar';
    el(this.pref + 'txtLimpiar').textContent = 'Limpiar';
    el(this.pref + 'formTitulo').textContent = this.cfg.titulo;
    el(this.pref + 'formCard').classList.remove('border-primary');
    this.mensaje('', '');
  };

  /** Mensaje junto al formulario y aviso flotante. tipo: ok | err | '' */
  Seccion.prototype.mensaje = function (texto, tipo, sinToast) {
    var m = el(this.pref + 'msg');
    if (m) {
      if (!texto) { m.innerHTML = ''; }
      else {
        var clase = tipo === 'ok' ? 'alert-success' : tipo === 'err' ? 'alert-danger' : 'alert-info';
        m.innerHTML = '<div class="alert ' + clase + ' py-2 mb-0">' + esc(texto) + '</div>';
      }
    }
    if (texto && tipo && !sinToast) S.toast(texto, tipo);
  };

  Seccion.prototype.cargarLookups = async function () {
    var self = this;
    var pendientes = this.cfg.campos.filter(function (f) { return f.lookup; });
    await Promise.all(pendientes.map(async function (f) {
      try {
        var lista = await API.get(f.lookup.endpoint);
        self.lookups[f.k] = lista;
        var s = el(self.pref + f.k);
        if (!s) return;
        s.innerHTML = '';
        s.appendChild(new Option(f.req ? 'Selecciona…' : '— Sin asignar —', ''));
        lista.forEach(function (item) { s.appendChild(new Option(f.lookup.texto(item), item[f.lookup.id])); });
      } catch (e) {
        self.lookups[f.k] = [];
      }
    }));
    this.cfg.campos.filter(function (f) { return f.opciones; }).forEach(function (f) {
      var s = el(self.pref + f.k);
      if (!s) return;
      s.innerHTML = '';
      f.opciones.forEach(function (o) { s.appendChild(new Option(/^[A-Z_]{4,}$/.test(o) ? S.etiqueta(o) : o, o)); });
    });
  };

  Seccion.prototype.textoLookup = function (campoK, valor) {
    var f = this.cfg.campos.filter(function (x) { return x.k === campoK; })[0];
    if (valor === null || valor === undefined) return '—';
    if (!f || !f.lookup) return valor;
    var lista = this.lookups[campoK] || [];
    for (var i = 0; i < lista.length; i++) {
      if (String(lista[i][f.lookup.id]) === String(valor)) return f.lookup.texto(lista[i]);
    }
    return '#' + valor;
  };

  Seccion.prototype.celda = function (col, fila) {
    var v = fila[col.k];
    if (col.render) return col.render(fila, this);
    if (col.badge) return S.badge(v);
    if (col.money) return S.money(v);
    if (col.fecha) return esc(S.fechaHora(v));
    if (col.lookup) return esc(this.textoLookup(col.lookup, v));
    if (col.num && v !== null && v !== undefined && v !== '') return esc(Number(v).toLocaleString('es-PE', { maximumFractionDigits: 4 }));
    return esc(v === null || v === undefined || v === '' ? '—' : v);
  };

  /** Valor con el que se ordena una columna. */
  Seccion.prototype.valorOrden = function (col, fila) {
    var v = fila[col.k];
    if (col.lookup) return String(this.textoLookup(col.lookup, v)).toLowerCase();
    if (col.money || col.num) return Number(v) || 0;
    if (typeof v === 'number') return v;
    return String(v === null || v === undefined ? '' : v).toLowerCase();
  };

  // -------------------------------------------------------------- Tabla
  Seccion.prototype.filtrados = function () {
    var self = this, lista = this.datos, q = this.filtro;
    if (q) {
      lista = lista.filter(function (f) {
        if (JSON.stringify(f).toLowerCase().indexOf(q) !== -1) return true;
        // también busca en los textos de los campos relacionados (nombre del cliente, etc.)
        return self.cfg.columnas.some(function (col) {
          return col.lookup && String(self.textoLookup(col.lookup, f[col.k])).toLowerCase().indexOf(q) !== -1;
        });
      });
    }
    if (this.orden) {
      var col = this.cfg.columnas[this.orden.k], dir = this.orden.dir;
      lista = lista.slice().sort(function (a, b) {
        var x = self.valorOrden(col, a), y = self.valorOrden(col, b);
        return x < y ? -dir : x > y ? dir : 0;
      });
    }
    return lista;
  };

  Seccion.prototype.pintar = function () {
    var c = this.cfg, p = this.pref, self = this;
    var tb = el(p + 'tabla');
    var lista = this.filtrados();
    var total = lista.length, paginas = Math.max(1, Math.ceil(total / this.porPagina));
    if (this.pagina > paginas) this.pagina = paginas;
    var ini = (this.pagina - 1) * this.porPagina, fin = Math.min(total, ini + this.porPagina);

    el(p + 'total').textContent = S.num(this.datos.length);
    el(p + 'rango').textContent = total ? ('Mostrando ' + S.num(ini + 1) + '–' + S.num(fin) + ' de ' + S.num(total)
      + (this.filtro ? ' (filtrados)' : '')) : 'Sin resultados';

    // encabezados con dirección del orden
    var ths = tb.closest('table').querySelectorAll('th[data-col]');
    ths.forEach(function (th) {
      var i = parseInt(th.getAttribute('data-col'), 10), ic = th.querySelector('i');
      var activo = self.orden && self.orden.k === i;
      ic.className = 'fa-solid ' + (activo ? (self.orden.dir === 1 ? 'fa-sort-up' : 'fa-sort-down') : 'fa-sort');
      th.setAttribute('aria-sort', activo ? (self.orden.dir === 1 ? 'ascending' : 'descending') : 'none');
    });

    if (!total) {
      tb.innerHTML = '<tr class="empty-row"><td colspan="' + (c.columnas.length + 1) + '">'
        + (this.filtro ? 'Ningún registro coincide con «' + esc(this.filtro) + '».'
          : (c.sinFormulario ? 'Todavía no hay registros.' : 'Todavía no hay registros. Usa el formulario de arriba para crear el primero.')) + '</td></tr>';
      this.paginacion(paginas);
      return;
    }
    var h = '';
    lista.slice(ini, fin).forEach(function (fila, idx) {
      h += '<tr data-i="' + (ini + idx) + '">';
      c.columnas.forEach(function (col) {
        h += '<td' + (col.money || col.num ? ' class="num"' : '') + '>' + self.celda(col, fila) + '</td>';
      });
      h += '<td class="text-end text-nowrap">';
      (c.acciones || []).forEach(function (a, i) {
        if (a.visible && !a.visible(fila)) return;
        var cl = a.clase === 'ok' ? 'btn-outline-success' : a.clase === 'danger' ? 'btn-outline-danger' : 'btn-light';
        h += '<button class="btn btn-sm btn-icon ' + cl + ' ms-1" title="' + esc(a.titulo) + '" aria-label="' + esc(a.titulo) + '" data-accion="' + i + '"><i class="fa-solid ' + a.icono + '"></i></button>';
      });
      if (!c.sinEditar) {
        h += '<button class="btn btn-sm btn-icon btn-light ms-1" title="Editar" aria-label="Editar" data-editar="1"><i class="fa-solid fa-pen"></i></button>';
      }
      if (!c.sinEliminar) {
        h += '<button class="btn btn-sm btn-icon btn-light ms-1 text-danger" title="Eliminar" aria-label="Eliminar" data-eliminar="1"><i class="fa-solid fa-trash"></i></button>';
      }
      h += '</td></tr>';
    });
    tb.innerHTML = h;
    this.paginacion(paginas);
  };

  Seccion.prototype.paginacion = function (paginas) {
    var ul = el(this.pref + 'pag'), self = this, a = this.pagina, h = '';
    function item(n, txt, dis, act) {
      return '<li class="page-item' + (dis ? ' disabled' : '') + (act ? ' active' : '') + '"><button class="page-link" type="button" data-pg="' + n + '"'
        + (act ? ' aria-current="page"' : '') + '>' + txt + '</button></li>';
    }
    h += item(a - 1, '<i class="fa-solid fa-chevron-left"></i><span class="visually-hidden">Anterior</span>', a <= 1);
    var desde = Math.max(1, a - 2), hasta = Math.min(paginas, desde + 4);
    desde = Math.max(1, hasta - 4);
    if (desde > 1) h += item(1, '1', false, false) + (desde > 2 ? '<li class="page-item disabled"><span class="page-link">…</span></li>' : '');
    for (var i = desde; i <= hasta; i++) h += item(i, String(i), false, i === a);
    if (hasta < paginas) h += (hasta < paginas - 1 ? '<li class="page-item disabled"><span class="page-link">…</span></li>' : '') + item(paginas, String(paginas), false, false);
    h += item(a + 1, '<i class="fa-solid fa-chevron-right"></i><span class="visually-hidden">Siguiente</span>', a >= paginas);
    ul.innerHTML = h;
    ul.querySelectorAll('[data-pg]').forEach(function (b) {
      b.addEventListener('click', function () {
        var n = parseInt(b.getAttribute('data-pg'), 10);
        if (n >= 1 && n <= paginas) { self.pagina = n; self.pintar(); }
      });
    });
  };

  Seccion.prototype.cargar = async function () {
    var c = this.cfg;
    try {
      this.datos = await API.get(c.endpoint) || [];
      this.pintar();
      if (c.alCargar) c.alCargar(this.datos, this);
    } catch (e) {
      el(this.pref + 'tabla').innerHTML = '<tr class="empty-row"><td colspan="' + (c.columnas.length + 1) + '" class="text-danger">'
        + '<i class="fa-solid fa-triangle-exclamation me-1"></i>No se pudo cargar: ' + esc(e.message) + '</td></tr>';
      el(this.pref + 'rango').textContent = '—';
    }
  };

  Seccion.prototype.guardar = async function () {
    var c = this.cfg, btn = el(this.pref + 'guardar'), datos;
    try { datos = this.leerFormulario(); }
    catch (e) { this.mensaje(e.message, 'err', true); return; }
    btn.disabled = true;
    try {
      var editando = this.editandoId !== null && !c.sinEditar;
      if (editando) await API.put(c.endpoint + '/' + this.editandoId, datos);
      else await API.post(c.endpoint, datos);
      this.limpiar();
      this.mensaje(editando ? 'Registro actualizado.' : 'Registro guardado.', 'ok');
      await this.cargar();
      if (this.alGuardar) this.alGuardar();
    } catch (e) {
      this.mensaje('No se pudo guardar: ' + e.message, 'err');
    }
    btn.disabled = false;
  };

  Seccion.prototype.editar = function (fila) {
    var self = this;
    this.cfg.campos.forEach(function (f) {
      var e = el(self.pref + f.k), v = fila[f.k];
      e.classList.remove('is-invalid');
      if (f.tipo === 'password') { e.value = ''; return; }
      if (v === null || v === undefined) { if (e.tagName === 'SELECT') e.selectedIndex = 0; else e.value = ''; return; }
      if (f.tipo === 'date' && typeof v === 'string') v = v.substring(0, 10);
      e.value = v;
    });
    this.editandoId = fila[this.cfg.idCampo];
    el(this.pref + 'txtGuardar').textContent = 'Actualizar';
    el(this.pref + 'txtLimpiar').textContent = 'Cancelar edición';
    el(this.pref + 'formTitulo').textContent = 'Editar registro N° ' + this.editandoId;
    el(this.pref + 'formCard').classList.add('border-primary');
    var body = el(this.pref + 'formBody');
    if (body && !body.classList.contains('show') && window.bootstrap) bootstrap.Collapse.getOrCreateInstance(body).show();
    this.mensaje('', '');
    el(this.pref + 'formCard').scrollIntoView({ behavior: 'smooth', block: 'start' });
  };

  Seccion.prototype.eliminar = async function (fila) {
    var id = fila[this.cfg.idCampo];
    var ok = await S.confirmar('Se eliminará el registro N° ' + id + '. Esta acción no se puede deshacer.',
      { titulo: 'Eliminar registro', ok: 'Eliminar', variante: 'danger' });
    if (!ok) return;
    try {
      await API.delete(this.cfg.endpoint + '/' + id);
      S.toast('Registro N° ' + id + ' eliminado.', 'ok');
      await this.cargar();
    } catch (e) {
      S.toast('No se pudo eliminar: ' + e.message, 'err');
    }
  };

  Seccion.prototype.ejecutarAccion = async function (a, fila) {
    if (a.confirmar) {
      var ok = await S.confirmar(a.confirmar, { titulo: a.titulo, ok: a.titulo, variante: a.clase === 'danger' ? 'danger' : 'primary' });
      if (!ok) return;
    }
    try {
      await a.ejecutar(fila, this);
      S.toast(a.exito || 'Acción realizada.', 'ok');
      await this.cargar();
    } catch (e) {
      S.toast('No se pudo completar: ' + e.message, 'err');
    }
  };

  // ------------------------------------------------- Ficheros (exportar e importar)
  Seccion.prototype.base = function () {
    return '/' + (this.cfg.archivos.recurso || this.cfg.endpoint.replace(/^\//, ''));
  };

  Seccion.prototype.leerArchivo = function (archivo) {
    return new Promise(function (ok, mal) {
      var r = new FileReader();
      r.onload = function () { ok(String(r.result).split(',')[1] || ''); };
      r.onerror = function () { mal(new Error('No se pudo leer el archivo.')); };
      r.readAsDataURL(archivo);
    });
  };

  Seccion.prototype.pintarImportacion = function (res, puedeConfirmar) {
    var p = this.pref, self = this;
    var h = '<div class="alert ' + (res.filasRechazadas ? 'alert-warning' : 'alert-info') + ' m-3 mb-0">';
    h += '<div class="fw-600 mb-1"><i class="fa-solid ' + (res.soloValidacion ? 'fa-magnifying-glass' : 'fa-circle-check') + ' me-1"></i>'
      + (res.soloValidacion ? 'Vista previa de ' : 'Importación de ') + esc(res.archivo) + '</div>';
    h += '<div class="d-flex flex-wrap gap-3 small">';
    h += '<span>Filas leídas: <b>' + S.num(res.filasLeidas) + '</b></span>';
    h += '<span class="text-success">' + (res.soloValidacion ? 'Válidas' : 'Importadas') + ': <b>' + S.num(res.soloValidacion ? res.filasValidas : res.filasImportadas) + '</b></span>';
    h += '<span class="text-danger">Con error: <b>' + S.num(res.filasRechazadas) + '</b></span></div>';
    if (res.columnasIgnoradas && res.columnasIgnoradas.length) {
      h += '<div class="small mt-1 text-muted">Columnas ignoradas (no se importan o las calcula el sistema): ' + esc(res.columnasIgnoradas.join(', ')) + '</div>';
    }
    if (res.errores && res.errores.length) {
      h += '<div class="table-responsive mt-2" style="max-height:200px"><table class="table table-sm"><thead><tr><th>Fila</th><th>Motivo</th></tr></thead><tbody>';
      res.errores.forEach(function (e) { h += '<tr><td>' + e.fila + '</td><td>' + esc(e.mensaje) + '</td></tr>'; });
      h += '</tbody></table></div>';
    }
    if (res.soloValidacion && puedeConfirmar && res.filasValidas > 0) {
      h += '<div class="d-flex gap-2 mt-2"><button class="btn btn-sm btn-success" type="button" id="' + p + 'confirmar"><i class="fa-solid fa-check me-1"></i>Importar ' + S.num(res.filasValidas) + ' fila(s)</button>'
        + '<button class="btn btn-sm btn-light" type="button" id="' + p + 'cancelarImp">Cancelar</button></div>';
    }
    h += '</div>';
    el(p + 'importRes').innerHTML = h;
    var bc = el(p + 'confirmar');
    if (bc) bc.addEventListener('click', function () { self.importar(false); });
    var bx = el(p + 'cancelarImp');
    if (bx) bx.addEventListener('click', function () { el(p + 'importRes').innerHTML = ''; self.archivoPendiente = null; });
  };

  Seccion.prototype.importar = async function (soloValidar) {
    var a = this.archivoPendiente;
    if (!a) return;
    try {
      var res = await API.post(this.base() + '/importar', { nombre: a.nombre, contenido: a.contenido, soloValidar: soloValidar });
      this.pintarImportacion(res, true);
      if (!soloValidar) { this.archivoPendiente = null; S.toast('Importación terminada.', 'ok'); await this.cargar(); }
    } catch (e) {
      el(this.pref + 'importRes').innerHTML = '<div class="alert alert-danger m-3 mb-0">No se pudo importar: ' + esc(e.message) + '</div>';
    }
  };

  Seccion.prototype.activarArchivos = function () {
    var p = this.pref, self = this;
    var cont = el(p + 'tabla').closest('.card');
    cont.querySelectorAll('[data-exp]').forEach(function (b) {
      b.addEventListener('click', function () {
        API.descargar(self.base() + '/exportar?formato=' + b.getAttribute('data-exp'))
          .then(function () { S.toast('Descarga lista.', 'ok'); })
          .catch(function (e) { S.toast('No se pudo exportar: ' + e.message, 'err'); });
      });
    });
    if (!this.cfg.archivos.importar) return;
    el(p + 'plantilla').addEventListener('click', function () {
      API.descargar(self.base() + '/plantilla').catch(function (e) { S.toast('No se pudo descargar: ' + e.message, 'err'); });
    });
    el(p + 'importar').addEventListener('click', function () { el(p + 'archivo').click(); });
    el(p + 'archivo').addEventListener('change', async function () {
      var f = this.files[0];
      this.value = '';
      if (!f) return;
      if (!/\.(csv|xlsx|txt)$/i.test(f.name)) { S.toast('Solo se aceptan archivos .csv o .xlsx.', 'err'); return; }
      if (f.size > MAX_BYTES_IMPORT) { S.toast('El archivo supera 700 KB. Divídelo en partes.', 'err'); return; }
      try {
        self.archivoPendiente = { nombre: f.name, contenido: await self.leerArchivo(f) };
        await self.importar(true); // primero la vista previa
      } catch (e) { S.toast(e.message, 'err'); }
    });
  };

  Seccion.prototype.activar = async function () {
    var p = this.pref, self = this, c = this.cfg;
    var form = el(p + 'form');
    if (form) {
      form.addEventListener('submit', function (ev) { ev.preventDefault(); self.guardar(); });
      el(p + 'limpiar').addEventListener('click', function () { self.limpiar(); });
      c.campos.forEach(function (f) {
        el(p + f.k).addEventListener('input', function () { this.classList.remove('is-invalid'); });
      });
    }
    el(p + 'buscar').addEventListener('input', S.debounce(function () {
      self.filtro = (el(p + 'buscar').value || '').trim().toLowerCase();
      self.pagina = 1; self.pintar();
    }, 180));
    el(p + 'tam').addEventListener('change', function () { self.porPagina = parseInt(this.value, 10) || 25; self.pagina = 1; self.pintar(); });
    el(p + 'recargar').addEventListener('click', function () { self.cargar().then(function () { S.toast('Datos actualizados.', 'info'); }); });
    el(p + 'tabla').closest('table').querySelectorAll('th[data-col]').forEach(function (th) {
      th.tabIndex = 0;
      function ordenar() {
        var i = parseInt(th.getAttribute('data-col'), 10);
        self.orden = (self.orden && self.orden.k === i) ? (self.orden.dir === 1 ? { k: i, dir: -1 } : null) : { k: i, dir: 1 };
        self.pintar();
      }
      th.addEventListener('click', ordenar);
      th.addEventListener('keydown', function (e) { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); ordenar(); } });
    });
    // Acciones de fila (delegación de eventos: una sola escucha para toda la tabla)
    el(p + 'tabla').addEventListener('click', function (ev) {
      var b = ev.target.closest('button');
      if (!b) return;
      var tr = b.closest('tr[data-i]');
      if (!tr) return;
      var fila = self.filtrados()[parseInt(tr.getAttribute('data-i'), 10)];
      if (!fila) return;
      if (b.hasAttribute('data-accion')) self.ejecutarAccion(c.acciones[parseInt(b.getAttribute('data-accion'), 10)], fila);
      else if (b.hasAttribute('data-editar')) self.editar(fila);
      else if (b.hasAttribute('data-eliminar')) self.eliminar(fila);
    });
    if (c.excel && !c.archivos) {
      el(p + 'excel').addEventListener('click', function () {
        API.descargar(c.excel).catch(function (e) { S.toast('No se pudo descargar: ' + e.message, 'err'); });
      });
    }
    if (c.archivos) this.activarArchivos();
    await this.cargarLookups();
    await this.cargar();
  };

  // -------------------------------------------------- Módulo completo
  async function modulo(cfg) {
    S.embebido();
    var cont = el('moduloContenido');
    var secciones = cfg.secciones.map(function (s, i) { var x = new Seccion(s, i); x.indice = i; return x; });
    var h = '';
    if (cfg.kpis) h += '<div class="sg-kpis" id="moduloKpis">' + S.kpi('Cargando…', '<span class="placeholder col-6"></span>') + '</div>';
    if (secciones.length > 1) {
      h += '<ul class="nav nav-underline" role="tablist">';
      cfg.secciones.forEach(function (s, i) {
        h += '<li class="nav-item" role="presentation"><button class="nav-link' + (i === 0 ? ' active' : '') + '" type="button" role="tab" data-tab="' + i + '">'
          + '<i class="fa-solid ' + (s.icono || 'fa-folder') + ' me-1"></i>' + esc(s.tab || s.titulo) + '</button></li>';
      });
      h += '</ul>';
    }
    secciones.forEach(function (s, i) {
      h += '<div data-pane="' + i + '"' + (i === 0 ? '' : ' hidden') + '>' + s.html() + '</div>';
    });
    cont.innerHTML = h;

    if (secciones.length > 1) {
      cont.querySelectorAll('[data-tab]').forEach(function (b) {
        b.addEventListener('click', function () {
          var i = b.getAttribute('data-tab');
          cont.querySelectorAll('[data-tab]').forEach(function (x) { x.classList.toggle('active', x === b); });
          cont.querySelectorAll('[data-pane]').forEach(function (x) { x.hidden = x.getAttribute('data-pane') !== i; });
        });
      });
    }

    async function refrescarKpis() {
      if (cfg.kpis) { try { await cfg.kpis(el('moduloKpis'), secciones); } catch (e) { /* los indicadores no bloquean el módulo */ } }
    }
    secciones.forEach(function (s) { s.alGuardar = refrescarKpis; });
    await Promise.all(secciones.map(function (s) { return s.activar(); }));
    await refrescarKpis();

    // Asistente por voz del panel: "nuevo usuario" → enfoca el primer campo
    window.addEventListener('message', function (ev) {
      if (ev.origin !== location.origin) return;
      if (ev.data === 'sigsf:new-user' && secciones[0] && secciones[0].cfg.campos[0]) {
        var primero = el(secciones[0].pref + secciones[0].cfg.campos[0].k);
        if (primero) { primero.scrollIntoView({ block: 'center' }); primero.focus(); }
      }
    });
    return secciones;
  }

  S.modulo = modulo;
})(window.SIGSF);
