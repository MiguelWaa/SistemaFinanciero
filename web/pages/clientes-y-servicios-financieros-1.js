SIGSF.modulo({
  kpis: async function(cont, secs){
    var cli = secs[0].datos || [];
    var activos = cli.filter(function(c){ return (c.estado||'')==='ACTIVO'; }).length;
    cont.innerHTML = SIGSF.kpi('Clientes registrados', SIGSF.num(cli.length))
                   + SIGSF.kpi('Clientes activos', SIGSF.num(activos), 'verde')
                   + SIGSF.kpi('Servicios disponibles', SIGSF.num((secs[1].datos||[]).length), 'ambar');
  },
  secciones: [
    {
      titulo: 'Registrar cliente', tab: 'Clientes', icono: 'fa-user-plus',
      descripcion: 'Los campos marcados con * son obligatorios.',
      endpoint: '/clientes', idCampo: 'idCliente', excel: '/clientes/excel', archivos: {importar: true},
      campos: [
        {k:'tipoDocumento', l:'Tipo de documento', tipo:'select', opciones:['DNI','CE','RUC','PAS']},
        {k:'documento', l:'N° de documento', req:true, ph:'70000000'},
        {k:'nombres', l:'Nombres', req:true, ph:'Juan'},
        {k:'apellidos', l:'Apellidos', ph:'Pérez Salas'},
        {k:'fechaNacimiento', l:'Fecha de nacimiento', tipo:'date'},
        {k:'telefono', l:'Teléfono', ph:'987654321'},
        {k:'correo', l:'Correo', tipo:'email', ph:'correo@ejemplo.com'},
        {k:'direccion', l:'Dirección', ph:'Av. Siempre Viva 123'},
        {k:'estado', l:'Estado', tipo:'select', opciones:['ACTIVO','INACTIVO']}
      ],
      columnas: [
        {k:'idCliente', l:'N°'},
        {k:'documento', l:'Documento', render:function(f){ return SIGSF.escapar((f.tipoDocumento||'')+' '+(f.documento||'')); }},
        {k:'nombres', l:'Nombre completo', render:function(f){ return SIGSF.escapar(((f.nombres||'')+' '+(f.apellidos||'')).trim()); }},
        {k:'telefono', l:'Teléfono'},
        {k:'correo', l:'Correo'},
        {k:'estado', l:'Estado', badge:true}
      ]
    },
    {
      titulo: 'Registrar servicio financiero', tab: 'Servicios', icono: 'fa-hand-holding-dollar',
      descripcion: 'Catálogo de servicios que la entidad ofrece a sus clientes.',
      endpoint: '/servicios', idCampo: 'idServicio', archivos: {importar: false},
      campos: [
        {k:'nombre', l:'Nombre del servicio', req:true, ph:'Cambio de moneda'},
        {k:'tipoServicio', l:'Tipo', tipo:'select', opciones:['CAMBIO','CREDITO','EMPENO','GIRO','OTRO']},
        {k:'descripcion', l:'Descripción', tipo:'textarea', ph:'Breve descripción'},
        {k:'estado', l:'Estado', tipo:'select', opciones:['ACTIVO','INACTIVO']}
      ],
      columnas: [
        {k:'idServicio', l:'N°'},
        {k:'nombre', l:'Servicio'},
        {k:'tipoServicio', l:'Tipo', badge:true},
        {k:'descripcion', l:'Descripción'},
        {k:'estado', l:'Estado', badge:true}
      ]
    }
  ]
});
