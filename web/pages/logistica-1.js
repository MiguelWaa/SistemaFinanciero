SIGSF.modulo({
  kpis: async function(cont, secs){
    var p = secs[0].datos || [];
    var act = p.filter(function(x){ return (x.estado||'')==='ACTIVO'; }).length;
    cont.innerHTML = SIGSF.kpi('Proveedores', SIGSF.num(p.length))
                   + SIGSF.kpi('Proveedores activos', SIGSF.num(act), 'verde');
  },
  secciones: [
    {
      titulo: 'Registrar proveedor', icono: 'fa-truck-field',
      descripcion: 'Empresas que abastecen de insumos y servicios a la entidad.',
      endpoint: '/proveedores', idCampo: 'idProveedor', archivos: {importar: true},
      campos: [
        {k:'razonSocial', l:'Razón social', req:true, ph:'Distribuidora Andina SAC'},
        {k:'ruc', l:'RUC', ph:'20512345678'},
        {k:'contacto', l:'Persona de contacto', ph:'Pedro Ramos'},
        {k:'telefono', l:'Teléfono', ph:'014567890'},
        {k:'correo', l:'Correo', tipo:'email', ph:'ventas@empresa.com'},
        {k:'direccion', l:'Dirección', ph:'Av. Industrial 200'},
        {k:'estado', l:'Estado', tipo:'select', opciones:['ACTIVO','INACTIVO']}
      ],
      columnas: [
        {k:'idProveedor', l:'N°'},
        {k:'razonSocial', l:'Razón social'},
        {k:'ruc', l:'RUC'},
        {k:'contacto', l:'Contacto'},
        {k:'telefono', l:'Teléfono'},
        {k:'correo', l:'Correo'},
        {k:'estado', l:'Estado', badge:true}
      ]
    }
  ]
});
