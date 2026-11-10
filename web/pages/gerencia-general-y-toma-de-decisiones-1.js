SIGSF.modulo({
  kpis: async function(cont, secs){
    var e = secs[0].datos || [];
    var pend = e.filter(function(x){ return (x.estado||'')==='PENDIENTE'; }).length;
    var alto = e.filter(function(x){ return (x.impacto||'')==='ALTO'; }).length;
    var apr = e.filter(function(x){ return (x.estado||'')==='APROBADO'; });
    var comprometido = apr.reduce(function(a,x){ return a + Number(x.monto||0); }, 0);
    cont.innerHTML = SIGSF.kpi('Por decidir', SIGSF.num(pend), pend ? 'ambar' : 'verde')
                   + SIGSF.kpi('Impacto alto', SIGSF.num(alto), alto ? 'rojo' : 'verde')
                   + SIGSF.kpi('Aprobados', SIGSF.num(apr.length), 'verde')
                   + SIGSF.kpi('Monto comprometido', SIGSF.money(comprometido));
  },
  secciones: [
    {
      titulo: 'Decisiones de gerencia', icono: 'fa-gavel',
      descripcion: 'Aprueba, observa o rechaza los expedientes elevados a la gerencia.',
      endpoint: '/expedientes', idCampo: 'idExpediente', sinEditar: true, archivos: {importar: false},
      tituloTabla: 'Expedientes para decisión',
      campos: [
        {k:'codigo', l:'Código', req:true, ph:'EXP-005'},
        {k:'area', l:'Área', tipo:'select', opciones:['DIRECCION','OPERACIONES','MARKETING','FINANZAS','TI']},
        {k:'solicitante', l:'Elevado por', ph:'Nombre'},
        {k:'monto', l:'Monto (S/)', tipo:'money'},
        {k:'impacto', l:'Impacto', tipo:'select', opciones:['ALTO','MEDIO','BAJO']},
        {k:'descripcion', l:'Asunto de la decisión', tipo:'textarea'}
      ],
      columnas: [
        {k:'codigo', l:'Código'},
        {k:'descripcion', l:'Asunto'},
        {k:'area', l:'Área', badge:true},
        {k:'monto', l:'Monto', money:true},
        {k:'impacto', l:'Impacto', badge:true},
        {k:'estado', l:'Decisión', badge:true}
      ],
      acciones: [
        {icono:'fa-thumbs-up', titulo:'Aprobar', clase:'ok', exito:'Decisión registrada: APROBADO.',
         ejecutar:function(f){ return API.put('/expedientes/'+f.idExpediente,{estado:'APROBADO'}); }},
        {icono:'fa-eye', titulo:'Observar', exito:'Expediente observado.',
         ejecutar:function(f){ return API.put('/expedientes/'+f.idExpediente,{estado:'OBSERVADO'}); }},
        {icono:'fa-thumbs-down', titulo:'Rechazar', clase:'danger', exito:'Decisión registrada: RECHAZADO.',
         confirmar:'¿Rechazar definitivamente este expediente?',
         ejecutar:function(f){ return API.put('/expedientes/'+f.idExpediente,{estado:'RECHAZADO'}); }}
      ]
    }
  ]
});
