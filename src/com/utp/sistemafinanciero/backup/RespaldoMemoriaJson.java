package com.utp.sistemafinanciero.backup;

import com.utp.sistemafinanciero.dao.DaoFactory;
import com.utp.sistemafinanciero.util.Json;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Respaldo para el modo demostración (persistencia en memoria): exporta
 * todas las colecciones a un archivo JSON. Las contraseñas no se exportan.
 */
public class RespaldoMemoriaJson implements EstrategiaRespaldo {

    private final DaoFactory bd;

    public RespaldoMemoriaJson(DaoFactory bd) {
        this.bd = bd;
    }

    @Override public String nombre() { return "memoria"; }
    @Override public String extension() { return "json"; }
    @Override public boolean disponible() { return true; }

    @Override
    public void generar(Path destino) throws Exception {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("generado", LocalDateTime.now());
        datos.put("persistencia", bd.nombre());
        datos.put("rol", bd.roles().listar());
        datos.put("usuario", bd.usuarios().listar().stream()
                .map(com.utp.sistemafinanciero.dto.UsuarioDTO::de).toList());
        datos.put("cliente", bd.clientes().listar());
        datos.put("servicio_financiero", bd.servicios().listar());
        datos.put("tipo_cambio", bd.tiposCambio().listar());
        datos.put("operacion", bd.operaciones().listar());
        datos.put("prestamo", bd.prestamos().listar());
        datos.put("empeno", bd.empenos().listar());
        datos.put("articulo", bd.articulos().listar());
        datos.put("producto", bd.productos().listar());
        datos.put("proveedor", bd.proveedores().listar());
        datos.put("campania", bd.campanias().listar());
        datos.put("prospecto", bd.prospectos().listar());
        datos.put("venta", bd.ventas().listar());
        datos.put("linea_produccion", bd.lineasProduccion().listar());
        datos.put("orden_produccion", bd.ordenesProduccion().listar());
        datos.put("expediente_aprobacion", bd.expedientes().listar());
        Files.writeString(destino, Json.stringify(datos), StandardCharsets.UTF_8);
    }
}
