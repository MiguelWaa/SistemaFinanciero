SIGSF.modulo({
  kpis: async function(cont, secs){
    var e = secs[0].datos || [];
    var pend = e.filter(function(x){ return (x.estado||'')==='PENDIENTE'; }).length;
    var apr = e.filter(function(x){ return (x.estado||'')==='APROBADO'; }).length;
    var monto = e.reduce(function(a,x){ return a + Number(x.monto||0); }, 0);
    cont.innerHTML = SIGSF.kpi('Expedientes', SIGSF.num(e.length))
                   + SIGSF.kpi('Pendientes', SIGSF.num(pend), pend ? 'ambar' : 'verde')
                   + SIGSF.kpi('Aprobados', SIGSF.num(apr), 'verde')
                   + SIGSF.kpi('Monto solicitado', SIGSF.money(monto));
  },
  secciones: [
    {
      titulo: 'Registrar expediente', icono: 'fa-folder-open',
      descripcion: 'Usa los botones de la tabla para aprobar o rechazar cada expediente.',
      endpoint: '/expedientes', idCampo: 'idExpediente', sinEditar: true, archivos: {importar: true},
      campos: [
        {k:'codigo', l:'Código', req:true, ph:'EXP-004'},
        {k:'area', l:'Área solicitante', tipo:'select', opciones:['OPERACIONES','MARKETING','ALMACEN','LOGISTICA','PRODUCCION','TI','RRHH']},
        {k:'solicitante', l:'Solicitante', ph:'Nombre del responsable'},
        {k:'monto', l:'Monto solicitado (S/)', tipo:'money', ph:'5000.00'},
        {k:'impacto', l:'Impacto', tipo:'select', opciones:['ALTO','MEDIO','BAJO']},
        {k:'descripcion', l:'Descripción / justificación', tipo:'textarea'}
      ],
      columnas: [
        {k:'codigo', l:'Código'},
        {k:'area', l:'Área', badge:true},
        {k:'solicitante', l:'Solicitante'},
        {k:'monto', l:'Monto', money:true},
        {k:'impacto', l:'Impacto', badge:true},
        {k:'estado', l:'Estado', badge:true},
        {k:'fechaSolicitud', l:'Solicitado', fecha:true}
      ],
      acciones: [
        {icono:'fa-check', titulo:'Aprobar', clase:'ok', exito:'Expediente aprobado.',
         visible:function(f){ return (f.estado||'')==='PENDIENTE'; },
         ejecutar:function(f){ return API.put('/expedientes/'+f.idExpediente,{estado:'APROBADO'}); }},
        {icono:'fa-xmark', titulo:'Rechazar', clase:'danger', exito:'Expediente rechazado.',
         confirmar:'¿Rechazar este expediente?',
         visible:function(f){ return (f.estado||'')==='PENDIENTE'; },
         ejecutar:function(f){ return API.put('/expedientes/'+f.idExpediente,{estado:'RECHAZADO'}); }}
      ]
    }
  ]
});
