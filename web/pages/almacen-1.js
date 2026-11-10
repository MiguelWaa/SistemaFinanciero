SIGSF.modulo({
  kpis: async function(cont, secs){
    var p = secs[0].datos || [];
    var bajos = p.filter(function(x){ return Number(x.stockActual||0) <= Number(x.stockMinimo||0); }).length;
    var unidades = p.reduce(function(a,x){ return a + Number(x.stockActual||0); }, 0);
    cont.innerHTML = SIGSF.kpi('Productos', SIGSF.num(p.length))
                   + SIGSF.kpi('Unidades en stock', SIGSF.num(unidades), 'verde')
                   + SIGSF.kpi('Stock bajo', SIGSF.num(bajos), bajos ? 'rojo' : 'verde');
  },
  secciones: [
    {
      titulo: 'Registrar producto', icono: 'fa-box',
      descripcion: 'Si el stock actual llega al mínimo, el producto se resalta en rojo.',
      endpoint: '/productos', idCampo: 'idProducto', excel: '/productos/excel', archivos: {importar: true},
      campos: [
        {k:'codigo', l:'Código', ph:'PRD-001'},
        {k:'nombre', l:'Nombre del producto', req:true, ph:'Formularios de operación'},
        {k:'descripcion', l:'Descripción', tipo:'textarea'},
        {k:'categoria', l:'Categoría', tipo:'select', opciones:['Papelería','Insumos','Equipos','Mobiliario','Otro']},
        {k:'unidadMedida', l:'Unidad', tipo:'select', opciones:['UND','CAJA','PAQ','KG','LT']},
        {k:'stockActual', l:'Stock actual', req:true, tipo:'entero', ph:'100'},
        {k:'stockMinimo', l:'Stock mínimo', req:true, tipo:'entero', ph:'20'},
        {k:'estado', l:'Estado', tipo:'select', opciones:['ACTIVO','INACTIVO']}
      ],
      columnas: [
        {k:'codigo', l:'Código'},
        {k:'nombre', l:'Producto'},
        {k:'categoria', l:'Categoría'},
        {k:'unidadMedida', l:'Unidad'},
        {k:'stockActual', l:'Stock', num:true, render:function(f){
            var bajo = Number(f.stockActual||0) <= Number(f.stockMinimo||0);
            return '<span class="'+(bajo?'stock-bajo':'')+'">'+SIGSF.num(f.stockActual)+(bajo?' <i class="fa-solid fa-triangle-exclamation"></i>':'')+'</span>';
        }},
        {k:'stockMinimo', l:'Mínimo', num:true},
        {k:'estado', l:'Estado', badge:true}
      ]
    }
  ]
});
