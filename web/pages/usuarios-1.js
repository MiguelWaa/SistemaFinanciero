SIGSF.modulo({
  kpis: async function(cont, secs){
    var u = secs[0].datos || [];
    var act = u.filter(function(x){ return (x.estado||'')==='ACTIVO'; }).length;
    cont.innerHTML = SIGSF.kpi('Usuarios', SIGSF.num(u.length))
                   + SIGSF.kpi('Activos', SIGSF.num(act), 'verde')
                   + SIGSF.kpi('Roles definidos', SIGSF.num((secs[1].datos||[]).length), 'ambar');
  },
  secciones: [
    {
      titulo: 'Registrar usuario', tab: 'Usuarios', icono: 'fa-user-plus',
      descripcion: 'Si dejas la contraseña vacía, se asigna "cambiar123" por defecto.',
      endpoint: '/usuarios', idCampo: 'idUsuario',
      campos: [
        {k:'nombre', l:'Nombres', req:true, ph:'Lucía'},
        {k:'apellido', l:'Apellidos', ph:'Ramos'},
        {k:'email', l:'Correo (usuario)', req:true, tipo:'email', ph:'lucia.ramos@sigsf.com'},
        {k:'password', l:'Contraseña', tipo:'password', ph:'Dejar vacío = cambiar123'},
        {k:'idRol', l:'Rol', req:true, tipo:'select',
         lookup:{endpoint:'/roles', id:'idRol', texto:function(r){ return r.nombre; }}},
        {k:'estado', l:'Estado', tipo:'select', opciones:['ACTIVO','INACTIVO','PENDIENTE']}
      ],
      columnas: [
        {k:'idUsuario', l:'N°'},
        {k:'nombre', l:'Nombre', render:function(f){ return SIGSF.escapar(((f.nombre||'')+' '+(f.apellido||'')).trim()); }},
        {k:'email', l:'Correo'},
        {k:'idRol', l:'Rol', lookup:'idRol'},
        {k:'estado', l:'Estado', badge:true},
        {k:'fechaRegistro', l:'Registrado', fecha:true}
      ]
    },
    {
      titulo: 'Registrar rol', tab: 'Roles', icono: 'fa-shield-halved',
      descripcion: 'Perfiles de acceso del sistema.',
      endpoint: '/roles', idCampo: 'idRol',
      campos: [
        {k:'nombre', l:'Nombre del rol', req:true, ph:'SUPERVISOR'},
        {k:'descripcion', l:'Descripción', tipo:'textarea', ph:'Qué puede hacer este rol'}
      ],
      columnas: [
        {k:'idRol', l:'N°'},
        {k:'nombre', l:'Rol', badge:true},
        {k:'descripcion', l:'Descripción'}
      ]
    }
  ]
});
