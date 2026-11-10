SIGSF.modulo({
  kpis: async function(cont, secs){
    var estado = 'Sin conexión', bd = '—';
    try { var h = await API.get('/health'); estado = h.status === 'ok' ? 'Operativo' : 'Revisar'; bd = h.persistencia || bd; } catch(e) {}
    cont.innerHTML = SIGSF.kpi('Estado del servidor', estado, estado === 'Operativo' ? 'verde' : 'rojo')
                   + SIGSF.kpi('Base de datos', SIGSF.escapar(bd), 'ambar')
                   + SIGSF.kpi('Tipos de cambio', SIGSF.num((secs[0].datos||[]).length));
  },
  secciones: [
    {
      titulo: 'Tipos de cambio del sistema', icono: 'fa-coins',
      descripcion: 'Estas cotizaciones son las que usan Operaciones y el Portal del Cliente.',
      endpoint: '/tipos-cambio', idCampo: 'idTipoCambio',
      campos: [
        {k:'monedaOrigen', l:'Moneda origen', req:true, tipo:'select', opciones:['USD','EUR','PEN']},
        {k:'monedaDestino', l:'Moneda destino', req:true, tipo:'select', opciones:['PEN','USD','EUR']},
        {k:'compra', l:'Compra', req:true, tipo:'money', ph:'3.7200'},
        {k:'venta', l:'Venta', req:true, tipo:'money', ph:'3.7800'},
        {k:'fechaInicio', l:'Vigente desde', tipo:'date'},
        {k:'estado', l:'Estado', tipo:'select', opciones:['ACTIVO','INACTIVO']}
      ],
      columnas: [
        {k:'idTipoCambio', l:'N°'},
        {k:'monedaOrigen', l:'Par', render:function(f){ return SIGSF.escapar(f.monedaOrigen+' → '+f.monedaDestino); }},
        {k:'compra', l:'Compra', num:true},
        {k:'venta', l:'Venta', num:true},
        {k:'fechaInicio', l:'Desde'},
        {k:'estado', l:'Estado', badge:true}
      ]
    }
  ]
});
