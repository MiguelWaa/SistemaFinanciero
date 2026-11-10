SIGSF.modulo({
  kpis: async function(cont, secs){
    var v = secs[0].datos || [];
    var total = v.reduce(function(a,x){ return a + Number(x.importe||0); }, 0);
    var hoy = new Date().toISOString().substring(0,10);
    var delDia = v.filter(function(x){ return String(x.fechaVenta||'').substring(0,10)===hoy; }).length;
    cont.innerHTML = SIGSF.kpi('Ventas registradas', SIGSF.num(v.length))
                   + SIGSF.kpi('Importe acumulado', SIGSF.money(total), 'verde')
                   + SIGSF.kpi('Ventas de hoy', SIGSF.num(delDia), 'ambar');
  },
  secciones: [
    {
      titulo: 'Registrar venta', icono: 'fa-cart-plus',
      descripcion: 'Al guardar, la venta se agrega al archivo Excel de reportes automáticamente.',
      endpoint: '/ventas', idCampo: 'idVenta', sinEditar: true, excel: '/ventas/excel', archivos: {importar: false},
      campos: [
        {k:'idCliente', l:'Cliente', req:true, tipo:'select',
         lookup:{endpoint:'/clientes', id:'idCliente', texto:function(c){ return ((c.nombres||'')+' '+(c.apellidos||'')).trim()+' ('+(c.documento||'')+')'; }}},
        {k:'idServicio', l:'Servicio vendido', req:true, tipo:'select',
         lookup:{endpoint:'/servicios', id:'idServicio', texto:function(s){ return s.nombre; }}},
        {k:'importe', l:'Importe cobrado (S/)', req:true, tipo:'money', ph:'350.00'},
        {k:'estado', l:'Estado', tipo:'select', opciones:['REGISTRADA','ANULADA']}
      ],
      columnas: [
        {k:'idVenta', l:'N°'},
        {k:'idCliente', l:'Cliente', lookup:'idCliente'},
        {k:'idServicio', l:'Servicio', lookup:'idServicio'},
        {k:'importe', l:'Importe', money:true},
        {k:'estado', l:'Estado', badge:true},
        {k:'fechaVenta', l:'Fecha', fecha:true}
      ]
    }
  ]
});
