SIGSF.modulo({
  kpis: async function(cont, secs){
    var l = secs[0].datos || [], o = secs[1].datos || [];
    var pend = o.filter(function(x){ return (x.estado||'')==='PENDIENTE'; }).length;
    cont.innerHTML = SIGSF.kpi('Líneas', SIGSF.num(l.length))
                   + SIGSF.kpi('Órdenes', SIGSF.num(o.length), 'verde')
                   + SIGSF.kpi('Órdenes pendientes', SIGSF.num(pend), pend ? 'ambar' : 'verde');
  },
  secciones: [
    {
      titulo: 'Registrar línea de producción', tab: 'Líneas', icono: 'fa-diagram-project',
      descripcion: 'Líneas o áreas donde se ejecutan las órdenes.',
      endpoint: '/lineas', idCampo: 'idLinea', archivos: {importar: true},
      campos: [
        {k:'codigo', l:'Código', ph:'LIN-01'},
        {k:'nombre', l:'Nombre de la línea', req:true, ph:'Línea de impresión'},
        {k:'descripcion', l:'Descripción', tipo:'textarea'},
        {k:'estado', l:'Estado', tipo:'select', opciones:['ACTIVA','INACTIVA']}
      ],
      columnas: [
        {k:'idLinea', l:'N°'},
        {k:'codigo', l:'Código'},
        {k:'nombre', l:'Línea'},
        {k:'descripcion', l:'Descripción'},
        {k:'estado', l:'Estado', badge:true}
      ]
    },
    {
      titulo: 'Registrar orden de producción', tab: 'Órdenes', icono: 'fa-clipboard-list',
      descripcion: 'Si el avance llega a 100 %, la orden se marca como terminada.',
      endpoint: '/ordenes', idCampo: 'idOrden', archivos: {importar: false},
      campos: [
        {k:'idProducto', l:'Producto', tipo:'select',
         lookup:{endpoint:'/productos', id:'idProducto', texto:function(p){ return p.nombre; }}},
        {k:'idLinea', l:'Línea', tipo:'select',
         lookup:{endpoint:'/lineas', id:'idLinea', texto:function(l){ return l.nombre; }}},
        {k:'cantidad', l:'Cantidad', req:true, tipo:'entero', ph:'100'},
        {k:'fechaInicio', l:'Inicio', tipo:'date'},
        {k:'fechaFinEstimada', l:'Fin estimado', tipo:'date'},
        {k:'avance', l:'Avance (%)', tipo:'money', ph:'0'},
        {k:'estado', l:'Estado', tipo:'select', opciones:['PENDIENTE','EN_PROCESO','TERMINADA']},
        {k:'observaciones', l:'Observaciones', tipo:'textarea'}
      ],
      columnas: [
        {k:'idOrden', l:'N°'},
        {k:'idProducto', l:'Producto', lookup:'idProducto'},
        {k:'idLinea', l:'Línea', lookup:'idLinea'},
        {k:'cantidad', l:'Cantidad', num:true},
        {k:'avance', l:'Avance', render:function(f){ return SIGSF.num(f.avance||0)+' %'; }},
        {k:'fechaFinEstimada', l:'Fin estimado'},
        {k:'estado', l:'Estado', badge:true}
      ]
    }
  ]
});
