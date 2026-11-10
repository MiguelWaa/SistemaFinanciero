SIGSF.modulo({
  kpis: async function(cont, secs){
    var pr = secs[0].datos || [], ca = secs[1].datos || [];
    var nuevos = pr.filter(function(p){ return (p.estado||'')==='NUEVO'; }).length;
    var presu = ca.reduce(function(a,c){ return a + Number(c.presupuesto||0); }, 0);
    cont.innerHTML = SIGSF.kpi('Prospectos', SIGSF.num(pr.length))
                   + SIGSF.kpi('Prospectos nuevos', SIGSF.num(nuevos), 'ambar')
                   + SIGSF.kpi('Campañas', SIGSF.num(ca.length), 'verde')
                   + SIGSF.kpi('Presupuesto total', SIGSF.money(presu), 'verde');
  },
  secciones: [
    {
      titulo: 'Registrar prospecto', tab: 'Prospectos', icono: 'fa-user-tag',
      descripcion: 'Personas interesadas que aún no son clientes.',
      endpoint: '/prospectos', idCampo: 'idProspecto', archivos: {importar: true},
      campos: [
        {k:'nombres', l:'Nombres', req:true, ph:'Luis'},
        {k:'apellidos', l:'Apellidos', ph:'Gonzales'},
        {k:'documento', l:'Documento', ph:'70011122'},
        {k:'telefono', l:'Teléfono', ph:'987000111'},
        {k:'correo', l:'Correo', tipo:'email', ph:'correo@ejemplo.com'},
        {k:'origen', l:'¿Cómo nos conoció?', tipo:'select', opciones:['WEB','FERIA','REFERIDO','REDES','VOLANTES','OTRO']},
        {k:'estado', l:'Estado', tipo:'select', opciones:['NUEVO','CONTACTADO','INTERESADO','CONVERTIDO','DESCARTADO']}
      ],
      columnas: [
        {k:'idProspecto', l:'N°'},
        {k:'nombres', l:'Nombre', render:function(f){ return SIGSF.escapar(((f.nombres||'')+' '+(f.apellidos||'')).trim()); }},
        {k:'telefono', l:'Teléfono'},
        {k:'correo', l:'Correo'},
        {k:'origen', l:'Origen', badge:true},
        {k:'estado', l:'Estado', badge:true},
        {k:'fechaRegistro', l:'Registrado', fecha:true}
      ]
    },
    {
      titulo: 'Registrar campaña', tab: 'Campañas', icono: 'fa-bullhorn',
      descripcion: 'Campañas de difusión con su presupuesto y canal.',
      endpoint: '/campanias', idCampo: 'idCampania', archivos: {importar: true},
      campos: [
        {k:'nombre', l:'Nombre de la campaña', req:true, ph:'Cambio sin comisión'},
        {k:'descripcion', l:'Descripción', tipo:'textarea', ph:'Objetivo de la campaña'},
        {k:'canal', l:'Canal', tipo:'select', opciones:['REDES','VOLANTES','RADIO','TV','EMAIL','OTRO']},
        {k:'presupuesto', l:'Presupuesto (S/)', tipo:'money', ph:'1500.00'},
        {k:'fechaInicio', l:'Fecha de inicio', tipo:'date'},
        {k:'fechaFin', l:'Fecha de fin', tipo:'date'},
        {k:'estado', l:'Estado', tipo:'select', opciones:['ACTIVA','PLANIFICADA','FINALIZADA']}
      ],
      columnas: [
        {k:'idCampania', l:'N°'},
        {k:'nombre', l:'Campaña'},
        {k:'canal', l:'Canal', badge:true},
        {k:'presupuesto', l:'Presupuesto', money:true},
        {k:'fechaInicio', l:'Inicio'},
        {k:'fechaFin', l:'Fin'},
        {k:'estado', l:'Estado', badge:true}
      ]
    }
  ]
});
