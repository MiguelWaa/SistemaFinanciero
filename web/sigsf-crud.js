/* ============================================================
   SIGSF · Motor de módulos
   Construye, a partir de una configuración sencilla, un módulo
   completo y funcional: formulario de registro, tabla en vivo,
   edición, eliminación, búsqueda, exportación (Excel, CSV, JSON)
   e importación de ficheros (CSV o Excel) con vista previa.
   Todas las páginas de gestión del sistema lo utilizan.
   ============================================================ */
window.SIGSF = (function () {

  // ---------- Utilidades ----------
  function el(id) { return document.getElementById(id); }

  function money(n) {
    return 'S/ ' + Number(n || 0).toLocaleString('es-PE', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }
  function num(n) { return Number(n || 0).toLocaleString('es-PE'); }

  function fechaCorta(v) {
    if (!v) return '';
    return String(v).replace('T', ' ').substring(0, 16);
  }

  function escapar(s) {
    return String(s === null || s === undefined ? '' : s)
      .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  var COLORES = {
    ACTIVO: 'b-verde', ACTIVA: 'b-verde', COMPLETADA: 'b-verde', APROBADO: 'b-verde',
    TERMINADA: 'b-verde', REGISTRADA: 'b-verde', VIGENTE: 'b-verde',
    PENDIENTE: 'b-ambar', EVALUACION: 'b-ambar', CONTACTADO: 'b-ambar', EN_PROCESO: 'b-ambar',
    INACTIVO: 'b-gris', CERRADA: 'b-gris', FINALIZADA: 'b-gris',
    RECHAZADO: 'b-rojo', ANULADA: 'b-rojo', VENCIDO: 'b-rojo',
    NUEVO: 'b-azul'
  };
  function badge(v) {
    var t = String(v || '').toUpperCase();
    return '<span class="badge ' + (COLORES[t] || 'b-azul') + '">' + escapar(v || '—') + '</span>';
  }

  // ---------- Construcción de una sección ----------
  function Seccion(cfg, indice) {
    var self = this;
    this.cfg = cfg;
    this.pref = 'sec' + indice + '_';
    this.datos = [];
    this.lookups = {};       // endpoint -> lista
    this.editandoId = null;
  }

  Seccion.prototype.html = function () {
    var c = this.cfg, p = this.pref, h = '';

    h += '<div class="card">';
    h += '<h2><i class="fa-solid ' + (c.icono || 'fa-pen-to-square') + '" style="color:#2563eb;"></i> ' + escapar(c.titulo) + '</h2>';
    h += '<div class="sub" id="' + p + 'sub">' + escapar(c.descripcion || 'Completa los datos y presiona Guardar.') + '</div>';
    h += '<div class="form-grid">';
    c.campos.forEach(function (f) {
      h += '<div class="f">';
      h += '<label>' + escapar(f.l) + (f.req ? ' <span class="req">*</span>' : '') + '</label>';
      if (f.tipo === 'select') {
        h += '<select id="' + p + f.k + '"></select>';
      } else if (f.tipo === 'textarea') {
        h += '<textarea id="' + p + f.k + '" rows="2" placeholder="' + escapar(f.ph || '') + '"></textarea>';
      } else {
        var t = (f.tipo === 'money' || f.tipo === 'entero') ? 'number' : (f.tipo || 'text');
        var paso = f.tipo === 'money' ? ' step="0.01"' : (f.tipo === 'entero' ? ' step="1"' : '');
        h += '<input type="' + t + '" id="' + p + f.k + '"' + paso + ' placeholder="' + escapar(f.ph || '') + '">';
      }
      h += '</div>';
    });
    h += '</div>';

    h += '<div class="acciones">';
    h += '<button class="btn" id="' + p + 'guardar"><i class="fa-solid fa-floppy-disk"></i> <span id="' + p + 'txtGuardar">Guardar</span></button>';
    h += '<button class="btn gris" id="' + p + 'limpiar" type="button"><i class="fa-solid fa-eraser"></i> Limpiar</button>';
    if (c.excel && !c.archivos) {
      h += '<button class="btn verde" id="' + p + 'excel" type="button"><i class="fa-solid fa-file-excel"></i> Descargar Excel</button>';
    }
    h += '</div>';
    h += '<div class="msg" id="' + p + 'msg"></div>';
    h += '</div>';

    // Tabla
    h += '<div class="card">';
    h += '<div class="tabla-top">';
    h += '<h2 style="margin:0;"><i class="fa-solid fa-table-list" style="color:#2563eb;"></i> ' + escapar(c.tituloTabla || ('Registros de ' + c.titulo.toLowerCase())) + '</h2>';
    h += '<div class="buscador"><i class="fa-solid fa-magnifying-glass"></i><input id="' + p + 'buscar" placeholder="Buscar…"></div>';
    h += '</div>';
    if (c.archivos) {
      h += '<div class="archivos-bar">';
      h += '<span class="t"><i class="fa-solid fa-file-arrow-down"></i> Exportar</span>';
      h += '<button class="btn verde" type="button" data-exp="xlsx"><i class="fa-solid fa-file-excel"></i> Excel</button>';
      h += '<button class="btn linea" type="button" data-exp="csv"><i class="fa-solid fa-file-csv"></i> CSV</button>';
      h += '<button class="btn linea" type="button" data-exp="json"><i class="fa-solid fa-file-code"></i> JSON</button>';
      if (c.archivos.importar) {
        h += '<span class="sep"></span><span class="t"><i class="fa-solid fa-file-arrow-up"></i> Importar</span>';
        h += '<button class="btn linea" type="button" id="' + p + 'plantilla"><i class="fa-solid fa-table-columns"></i> Plantilla</button>';
        h += '<button class="btn ambar" type="button" id="' + p + 'importar"><i class="fa-solid fa-upload"></i> Subir CSV / Excel</button>';
        h += '<input type="file" id="' + p + 'archivo" accept=".csv,.xlsx,.txt" style="display:none">';
      }
      h += '</div>';
      h += '<div id="' + p + 'importRes"></div>';
    }
    h += '<div class="tabla-scroll"><table><thead><tr>';
    c.columnas.forEach(function (col) { h += '<th>' + escapar(col.l) + '</th>'; });
    h += '<th style="text-align:right;">Acciones</th>';
    h += '</tr></thead><tbody id="' + p + 'tabla"><tr><td class="vacio" colspan="' + (c.columnas.length + 1) + '">Cargando…</td></tr></tbody></table></div>';
    h += '</div>';
    return h;
  };

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
      if (f.req && (v === null || v === '')) faltan.push(f.l);
      if (v !== null) datos[f.k] = v;
      else if (!f.req) datos[f.k] = null;
    });
    if (faltan.length) throw new Error('Completa: ' + faltan.join(', ') + '.');
    if (this.cfg.antesDeGuardar) this.cfg.antesDeGuardar(datos, this);
    return datos;
  };

  Seccion.prototype.limpiar = function () {
    var self = this;
    this.cfg.campos.forEach(function (f) {
      var e = el(self.pref + f.k);
      if (e.tagName === 'SELECT') e.selectedIndex = 0; else e.value = '';
    });
    this.editandoId = null;
    el(this.pref + 'txtGuardar').textContent = 'Guardar';
    this.mensaje('', '');
  };

  Seccion.prototype.mensaje = function (texto, tipo) {
    var m = el(this.pref + 'msg');
    m.className = 'msg' + (tipo ? ' ' + tipo : '');
    m.innerHTML = texto ? ((tipo === 'ok' ? '<i class="fa-solid fa-circle-check"></i> ' : '') + escapar(texto)) : '';
  };

  // Carga las listas que alimentan los <select> que apuntan a otras tablas
  Seccion.prototype.cargarLookups = async function () {
    var self = this;
    var pendientes = this.cfg.campos.filter(function (f) { return f.lookup; });
    for (var i = 0; i < pendientes.length; i++) {
      var f = pendientes[i];
      try {
        var lista = await API.get(f.lookup.endpoint);
        self.lookups[f.k] = lista;
        var s = el(self.pref + f.k);
        s.innerHTML = '';
        if (!f.req) s.appendChild(new Option('— Sin asignar —', ''));
        lista.forEach(function (item) {
          s.appendChild(new Option(f.lookup.texto(item), item[f.lookup.id]));
        });
      } catch (e) {
        self.lookups[f.k] = [];
      }
    }
    // Opciones fijas (listas de texto)
    this.cfg.campos.filter(function (f) { return f.opciones; }).forEach(function (f) {
      var s = el(self.pref + f.k);
      s.innerHTML = '';
      f.opciones.forEach(function (o) { s.appendChild(new Option(o, o)); });
    });
  };

  /** Texto a mostrar para un valor que referencia otra tabla. */
  Seccion.prototype.textoLookup = function (campoK, valor) {
    var f = this.cfg.campos.filter(function (x) { return x.k === campoK; })[0];
    if (!f || !f.lookup || valor === null || valor === undefined) return valor === null || valor === undefined ? '—' : valor;
    var lista = this.lookups[campoK] || [];
    for (var i = 0; i < lista.length; i++) {
      if (String(lista[i][f.lookup.id]) === String(valor)) return f.lookup.texto(lista[i]);
    }
    return '#' + valor;
  };

  Seccion.prototype.celda = function (col, fila) {
    var v = fila[col.k];
    if (col.render) return col.render(fila, this);
    if (col.badge) return badge(v);
    if (col.money) return money(v);
    if (col.fecha) return escapar(fechaCorta(v));
    if (col.lookup) return escapar(this.textoLookup(col.lookup, v));
    return escapar(v === null || v === undefined || v === '' ? '—' : v);
  };

  Seccion.prototype.pintar = function () {
    var c = this.cfg, p = this.pref, self = this;
    var tb = el(p + 'tabla');
    var filtro = (el(p + 'buscar').value || '').toLowerCase();
    tb.innerHTML = '';

    var lista = this.datos;
    if (filtro) {
      lista = lista.filter(function (f) {
        return JSON.stringify(f).toLowerCase().indexOf(filtro) !== -1;
      });
    }
    if (!lista.length) {
      tb.innerHTML = '<tr><td class="vacio" colspan="' + (c.columnas.length + 1) + '">' +
        (filtro ? 'Ningún registro coincide con la búsqueda.' : 'Todavía no hay registros. Usa el formulario de arriba.') + '</td></tr>';
      return;
    }

    lista.forEach(function (fila) {
      var tr = document.createElement('tr');
      var h = '';
      c.columnas.forEach(function (col) {
        h += '<td' + (col.money || col.num ? ' class="num"' : '') + '>' + self.celda(col, fila) + '</td>';
      });
      h += '<td><div class="row-btns">';
      (c.acciones || []).forEach(function (a, i) {
        if (a.visible && !a.visible(fila)) return;
        h += '<button class="ibtn ' + (a.clase || '') + '" title="' + escapar(a.titulo) + '" data-accion="' + i + '"><i class="fa-solid ' + a.icono + '"></i></button>';
      });
      if (!c.sinEditar) {
        h += '<button class="ibtn" title="Editar" data-editar="1"><i class="fa-solid fa-pen"></i></button>';
      }
      h += '<button class="ibtn danger" title="Eliminar" data-eliminar="1"><i class="fa-solid fa-trash"></i></button>';
      h += '</div></td>';
      tr.innerHTML = h;

      tr.querySelectorAll('[data-accion]').forEach(function (b) {
        b.addEventListener('click', function () {
          var a = c.acciones[parseInt(b.getAttribute('data-accion'), 10)];
          self.ejecutarAccion(a, fila);
        });
      });
      var be = tr.querySelector('[data-editar]');
      if (be) be.addEventListener('click', function () { self.editar(fila); });
      tr.querySelector('[data-eliminar]').addEventListener('click', function () { self.eliminar(fila); });

      tb.appendChild(tr);
    });
  };

  Seccion.prototype.cargar = async function () {
    var c = this.cfg, self = this;
    var tb = el(this.pref + 'tabla');
    try {
      this.datos = await API.get(c.endpoint);
      this.pintar();
      if (c.alCargar) c.alCargar(this.datos, this);
    } catch (e) {
      tb.innerHTML = '<tr><td class="error-fila" colspan="' + (c.columnas.length + 1) + '">No se pudo cargar: ' + escapar(e.message) + '</td></tr>';
    }
  };

  Seccion.prototype.guardar = async function () {
    var c = this.cfg, self = this;
    var btn = el(this.pref + 'guardar');
    var datos;
    try { datos = this.leerFormulario(); }
    catch (e) { this.mensaje(e.message, 'err'); return; }

    btn.disabled = true;
    try {
      if (this.editandoId !== null && !c.sinEditar) {
        await API.put(c.endpoint + '/' + this.editandoId, datos);
        this.mensaje('Registro actualizado correctamente.', 'ok');
      } else {
        await API.post(c.endpoint, datos);
        this.mensaje('Registro guardado correctamente.', 'ok');
      }
      var msgActual = el(this.pref + 'msg').innerHTML;
      this.limpiar();
      el(this.pref + 'msg').className = 'msg ok';
      el(this.pref + 'msg').innerHTML = msgActual;
      await this.cargar();
    } catch (e) {
      this.mensaje('No se pudo guardar: ' + e.message, 'err');
    }
    btn.disabled = false;
  };

  Seccion.prototype.editar = function (fila) {
    var self = this;
    this.cfg.campos.forEach(function (f) {
      var e = el(self.pref + f.k);
      var v = fila[f.k];
      if (v === null || v === undefined) { if (e.tagName === 'SELECT') e.selectedIndex = 0; else e.value = ''; return; }
      if (f.tipo === 'date' && typeof v === 'string') v = v.substring(0, 10);
      e.value = v;
    });
    this.editandoId = fila[this.cfg.idCampo];
    el(this.pref + 'txtGuardar').textContent = 'Actualizar';
    this.mensaje('Editando el registro N° ' + this.editandoId + '. Presiona Actualizar para guardar los cambios.', '');
    el(this.pref + 'guardar').scrollIntoView({ behavior: 'smooth', block: 'center' });
  };

  Seccion.prototype.eliminar = async function (fila) {
    var id = fila[this.cfg.idCampo];
    if (!confirm('¿Eliminar el registro N° ' + id + '? Esta acción no se puede deshacer.')) return;
    try {
      await API.delete(this.cfg.endpoint + '/' + id);
      this.mensaje('Registro eliminado.', 'ok');
      await this.cargar();
    } catch (e) {
      this.mensaje('No se pudo eliminar: ' + e.message, 'err');
    }
  };

  Seccion.prototype.ejecutarAccion = async function (a, fila) {
    if (a.confirmar && !confirm(a.confirmar)) return;
    try {
      await a.ejecutar(fila, this);
      this.mensaje(a.exito || 'Acción realizada.', 'ok');
      await this.cargar();
    } catch (e) {
      this.mensaje('No se pudo completar: ' + e.message, 'err');
    }
  };

  // ---------- Ficheros: exportar e importar ----------
  var MAX_BYTES_IMPORT = 700 * 1024; // el servidor acepta peticiones de hasta 1 MB (Base64 ≈ +33 %)

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
    var h = '<div class="import-res">';
    h += '<strong><i class="fa-solid ' + (res.soloValidacion ? 'fa-magnifying-glass' : 'fa-circle-check') + '"></i> '
       + (res.soloValidacion ? 'Vista previa de ' : 'Resultado de la importación de ') + escapar(res.archivo) + '</strong>';
    h += '<div class="cifras">';
    h += '<span>Filas leídas: <b>' + num(res.filasLeidas) + '</b></span>';
    h += '<span style="color:#059669">' + (res.soloValidacion ? 'Válidas' : 'Importadas') + ': <b>'
       + num(res.soloValidacion ? res.filasValidas : res.filasImportadas) + '</b></span>';
    h += '<span style="color:#dc2626">Con error: <b>' + num(res.filasRechazadas) + '</b></span>';
    h += '</div>';
    if (res.columnasIgnoradas && res.columnasIgnoradas.length) {
      h += '<div class="nota">Columnas ignoradas (no se importan o las calcula el sistema): ' + escapar(res.columnasIgnoradas.join(', ')) + '</div>';
    }
    if (res.errores && res.errores.length) {
      h += '<div class="tabla-scroll"><table><thead><tr><th>Fila</th><th>Motivo</th></tr></thead><tbody>';
      res.errores.forEach(function (e) { h += '<tr><td>' + e.fila + '</td><td>' + escapar(e.mensaje) + '</td></tr>'; });
      h += '</tbody></table></div>';
    }
    if (res.soloValidacion) {
      h += '<div class="nota">La vista previa revisa columnas, formatos y campos obligatorios. Los duplicados se detectan al importar.</div>';
      if (puedeConfirmar && res.filasValidas > 0) {
        h += '<div class="acciones"><button class="btn verde" type="button" id="' + p + 'confirmar"><i class="fa-solid fa-check"></i> Importar ' + num(res.filasValidas) + ' fila(s)</button>'
           + '<button class="btn gris" type="button" id="' + p + 'cancelarImp">Cancelar</button></div>';
      }
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
      if (!soloValidar) { this.archivoPendiente = null; await this.cargar(); }
    } catch (e) {
      el(this.pref + 'importRes').innerHTML = '<div class="import-res msg err">No se pudo importar: ' + escapar(e.message) + '</div>';
    }
  };

  Seccion.prototype.activarArchivos = function () {
    var p = this.pref, self = this;
    var barra = el(p + 'importRes').previousElementSibling;
    barra.querySelectorAll('[data-exp]').forEach(function (b) {
      b.addEventListener('click', function () {
        API.descargar(self.base() + '/exportar?formato=' + b.getAttribute('data-exp'))
          .catch(function (e) { self.mensaje('No se pudo exportar: ' + e.message, 'err'); });
      });
    });
    if (!this.cfg.archivos.importar) return;
    el(p + 'plantilla').addEventListener('click', function () {
      API.descargar(self.base() + '/plantilla').catch(function (e) { self.mensaje('No se pudo descargar: ' + e.message, 'err'); });
    });
    el(p + 'importar').addEventListener('click', function () { el(p + 'archivo').click(); });
    el(p + 'archivo').addEventListener('change', async function () {
      var f = this.files[0];
      this.value = '';
      if (!f) return;
      if (!/\.(csv|xlsx|txt)$/i.test(f.name)) {
        el(p + 'importRes').innerHTML = '<div class="import-res msg err">Solo se aceptan archivos .csv o .xlsx.</div>'; return;
      }
      if (f.size > MAX_BYTES_IMPORT) {
        el(p + 'importRes').innerHTML = '<div class="import-res msg err">El archivo supera 700 KB. Divídalo en partes.</div>'; return;
      }
      try {
        self.archivoPendiente = { nombre: f.name, contenido: await self.leerArchivo(f) };
        await self.importar(true); // primero la vista previa
      } catch (e) {
        el(p + 'importRes').innerHTML = '<div class="import-res msg err">' + escapar(e.message) + '</div>';
      }
    });
  };

  Seccion.prototype.activar = async function () {
    var p = this.pref, self = this;
    el(p + 'guardar').addEventListener('click', function () { self.guardar(); });
    el(p + 'limpiar').addEventListener('click', function () { self.limpiar(); });
    el(p + 'buscar').addEventListener('input', function () { self.pintar(); });
    if (this.cfg.excel && !this.cfg.archivos) {
      el(p + 'excel').addEventListener('click', function () {
        API.descargar(self.cfg.excel).catch(function (e) { self.mensaje('No se pudo descargar: ' + e.message, 'err'); });
      });
    }
    if (this.cfg.archivos) this.activarArchivos();
    await this.cargarLookups();
    await this.cargar();
  };

  // ---------- Módulo completo ----------
  async function modulo(cfg) {
    // Oculta el encabezado cuando la página se abre dentro del panel
    if (window.top !== window.self) document.body.classList.add('sigsf-embed');

    var cont = el('moduloContenido');
    var secciones = cfg.secciones.map(function (s, i) { return new Seccion(s, i); });
    var h = '';

    if (cfg.kpis) h += '<div class="kpis" id="moduloKpis"></div>';

    if (secciones.length > 1) {
      h += '<div class="mod-tabs">';
      cfg.secciones.forEach(function (s, i) {
        h += '<button data-tab="' + i + '" class="' + (i === 0 ? 'on' : '') + '"><i class="fa-solid ' + (s.icono || 'fa-folder') + '"></i> ' + escapar(s.tab || s.titulo) + '</button>';
      });
      h += '</div>';
    }
    secciones.forEach(function (s, i) {
      h += '<div data-pane="' + i + '"' + (i === 0 ? '' : ' style="display:none;"') + '>' + s.html() + '</div>';
    });
    cont.innerHTML = h;

    if (secciones.length > 1) {
      cont.querySelectorAll('[data-tab]').forEach(function (b) {
        b.addEventListener('click', function () {
          var i = b.getAttribute('data-tab');
          cont.querySelectorAll('[data-tab]').forEach(function (x) { x.classList.remove('on'); });
          b.classList.add('on');
          cont.querySelectorAll('[data-pane]').forEach(function (x) {
            x.style.display = (x.getAttribute('data-pane') === i) ? 'block' : 'none';
          });
        });
      });
    }

    for (var i = 0; i < secciones.length; i++) { await secciones[i].activar(); }
    if (cfg.kpis) { try { await cfg.kpis(el('moduloKpis'), secciones); } catch (e) { } }
    return secciones;
  }

  function kpi(label, valor, clase) {
    return '<div class="kpi ' + (clase || '') + '"><div class="l">' + escapar(label) + '</div><div class="v">' + valor + '</div></div>';
  }

  return { modulo: modulo, money: money, num: num, badge: badge, kpi: kpi, fechaCorta: fechaCorta, escapar: escapar };
})();
