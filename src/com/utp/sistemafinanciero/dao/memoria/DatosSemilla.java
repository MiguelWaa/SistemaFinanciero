package com.utp.sistemafinanciero.dao.memoria;

import com.utp.sistemafinanciero.dao.DaoFactory;
import com.utp.sistemafinanciero.model.almacen.Producto;
import com.utp.sistemafinanciero.model.clientes.Cliente;
import com.utp.sistemafinanciero.model.clientes.ServicioFinanciero;
import com.utp.sistemafinanciero.model.direccion.ExpedienteAprobacion;
import com.utp.sistemafinanciero.model.logistica.Proveedor;
import com.utp.sistemafinanciero.model.marketing.Campania;
import com.utp.sistemafinanciero.model.marketing.Prospecto;
import com.utp.sistemafinanciero.model.operaciones.Articulo;
import com.utp.sistemafinanciero.model.operaciones.Empeno;
import com.utp.sistemafinanciero.model.operaciones.Operacion;
import com.utp.sistemafinanciero.model.operaciones.Prestamo;
import com.utp.sistemafinanciero.model.operaciones.TipoCambio;
import com.utp.sistemafinanciero.model.produccion.LineaProduccion;
import com.utp.sistemafinanciero.model.produccion.OrdenProduccion;
import com.utp.sistemafinanciero.model.seguridad.Rol;
import com.utp.sistemafinanciero.model.seguridad.Usuario;
import com.utp.sistemafinanciero.model.ventas.Venta;
import com.utp.sistemafinanciero.security.PasswordHasher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Carga en memoria los mismos datos de {@code db/datos_ejemplo.sql},
 * para el modo demostración. Usuarios: admin@sigsf.com/admin123,
 * lucia.ramos@sigsf.com/lucia123 (CAJERO), jorge.torres@sigsf.com/jorge123
 * (ANALISTA). Portal: documento 70000001 / clave 70000001.
 */
public final class DatosSemilla {

    private DatosSemilla() {
    }

    private static BigDecimal d(String v) { return new BigDecimal(v); }

    public static void cargar(DaoFactory bd, PasswordHasher hasher) {
        LocalDate hoy = LocalDate.now();
        LocalDateTime ahora = LocalDateTime.now();

        bd.roles().crear(new Rol(null, "ADMIN", "Administrador del sistema"));
        bd.roles().crear(new Rol(null, "SUPERVISOR", "Supervisa operaciones y reportes"));
        bd.roles().crear(new Rol(null, "CAJERO", "Registra operaciones de caja y cambio"));
        bd.roles().crear(new Rol(null, "ANALISTA", "Analiza prestamos, empenos y riesgos"));

        bd.usuarios().crear(new Usuario(null, "Miguel", "Machaca", "admin@sigsf.com", hasher.hash("admin123"), 1, "ACTIVO", ahora));
        bd.usuarios().crear(new Usuario(null, "Lucia", "Ramos", "lucia.ramos@sigsf.com", hasher.hash("lucia123"), 3, "ACTIVO", ahora));
        bd.usuarios().crear(new Usuario(null, "Jorge", "Torres", "jorge.torres@sigsf.com", hasher.hash("jorge123"), 4, "ACTIVO", ahora));

        bd.servicios().crear(new ServicioFinanciero(null, "Cambio de moneda", "CAMBIO", "Compra y venta de divisas", "ACTIVO"));
        bd.servicios().crear(new ServicioFinanciero(null, "Prestamo personal", "CREDITO", "Prestamos con interes", "ACTIVO"));
        bd.servicios().crear(new ServicioFinanciero(null, "Empeno de articulos", "EMPENO", "Prestamo con garantia", "ACTIVO"));
        bd.servicios().crear(new ServicioFinanciero(null, "Giros y envios", "GIRO", "Envio de dinero nacional", "ACTIVO"));

        String[][] cli = {
                {"DNI", "70000001", "Juan", "Perez Salas", "1995-05-20", "987654321", "juan.perez@gmail.com", "Av. Los Olivos 123"},
                {"DNI", "70000002", "Maria", "Lopez Diaz", "1990-11-02", "987111222", "maria.lopez@gmail.com", "Jr. Union 456"},
                {"DNI", "70000003", "Carlos", "Quispe Mamani", "1988-03-15", "987333444", "carlos.quispe@gmail.com", "Calle Real 789"},
                {"CE", "001234567", "Ana", "Torres Vega", "1998-07-30", "987555666", "ana.torres@gmail.com", "Av. Grau 1010"}};
        for (String[] c : cli) {
            int id = bd.clientes().crear(new Cliente(null, c[0], c[1], c[2], c[3], LocalDate.parse(c[4]),
                    c[5], c[6], c[7], "ACTIVO", ahora));
            bd.accesosCliente().guardar(id, hasher.hash(c[1]));
        }

        bd.tiposCambio().crear(new TipoCambio(null, "USD", "PEN", d("3.7200"), d("3.7800"), hoy, "ACTIVO"));
        bd.tiposCambio().crear(new TipoCambio(null, "EUR", "PEN", d("4.0500"), d("4.1500"), hoy, "ACTIVO"));

        bd.productos().crear(new Producto(null, "PRD-001", "Formularios de operacion", "Talonarios preimpresos", "Papeleria", "UND", 120, 50, "ACTIVO"));
        bd.productos().crear(new Producto(null, "PRD-002", "Sobres de seguridad", "Sobres para valores", "Papeleria", "UND", 15, 40, "ACTIVO"));
        bd.productos().crear(new Producto(null, "PRD-003", "Cinta para contadora", "Insumo de contadora", "Insumos", "UND", 8, 20, "ACTIVO"));
        bd.productos().crear(new Producto(null, "PRD-004", "Tinta para sellos", "Tinta indeleble", "Insumos", "UND", 60, 25, "ACTIVO"));
        bd.productos().crear(new Producto(null, "PRD-005", "Calculadora financiera", "Equipo de caja", "Equipos", "UND", 10, 10, "ACTIVO"));

        bd.proveedores().crear(new Proveedor(null, "Distribuidora Andina SAC", "20512345678", "Pedro Ramos", "014567890", "ventas@andina.com", "Av. Industrial 200", "ACTIVO"));
        bd.proveedores().crear(new Proveedor(null, "Importaciones del Sur EIRL", "20598765432", "Rosa Diaz", "015678901", "contacto@delsur.com", "Jr. Comercio 45", "ACTIVO"));

        bd.prospectos().crear(new Prospecto(null, "Luis", "Gonzales", "70011122", "987000111", "luis.g@gmail.com", "FERIA", "NUEVO", ahora));
        bd.prospectos().crear(new Prospecto(null, "Sofia", "Rojas", "70022233", "987000222", "sofia.r@gmail.com", "WEB", "CONTACTADO", ahora));
        bd.prospectos().crear(new Prospecto(null, "Diego", "Flores", "70033344", "987000333", "diego.f@gmail.com", "REFERIDO", "NUEVO", ahora));

        bd.campanias().crear(new Campania(null, "Cambio sin comision", "Promocion de cambio de dolares", hoy, hoy.plusDays(30), d("1500.00"), "REDES", "ACTIVA"));
        bd.campanias().crear(new Campania(null, "Prestamos rapidos", "Difusion de prestamos personales", hoy, hoy.plusDays(45), d("2500.00"), "VOLANTES", "ACTIVA"));

        bd.articulos().crear(new Articulo(null, 1, "Laptop 14 pulgadas", "Electronica", "HP", "245 G8", "SN-HP-0099", "BUENO", d("1800.00"), hoy, "ACTIVO"));
        bd.articulos().crear(new Articulo(null, 3, "Anillo de oro 18k", "Joyeria", "-", "-", "-", "MUY BUENO", d("1200.00"), hoy, "ACTIVO"));

        bd.operaciones().crear(new Operacion(null, 1, 1, "CAMBIO", "USD", "PEN", d("100.00"), d("3.7800"), d("100.00"), d("378.00"), "COMPLETADA", ahora));
        bd.operaciones().crear(new Operacion(null, 2, null, "PRESTAMO", "PEN", "PEN", d("5000.00"), null, null, null, "COMPLETADA", ahora));
        bd.operaciones().crear(new Operacion(null, 1, null, "EMPENO", "PEN", "PEN", d("800.00"), null, null, null, "COMPLETADA", ahora));
        // Una solicitud del portal pendiente de aprobación (para la bandeja)
        bd.operaciones().crear(new Operacion(null, 3, null, "PRESTAMO", "PEN", "PEN", d("2000.00"), null, null, null, "PENDIENTE", ahora));

        bd.prestamos().crear(new Prestamo(null, 2, d("5000.00"), d("15.000"), 12, d("479.17"), hoy, hoy.plusMonths(12), d("5750.00"), "ACTIVO"));
        bd.prestamos().crear(new Prestamo(null, 4, d("2000.00"), d("15.000"), 6, d("633.33"), hoy, hoy.plusMonths(6), d("3800.00"), "PENDIENTE"));
        bd.empenos().crear(new Empeno(null, 3, 1, d("800.00"), d("10.000"), 30, hoy, hoy.plusDays(30), d("800.00"), "ACTIVO"));

        bd.ventas().crear(new com.utp.sistemafinanciero.model.ventas.Venta(null, 1, 1, ahora, d("378.00"), d("378.00"), "REGISTRADA"));
        bd.ventas().crear(new Venta(null, 2, 2, ahora, d("5000.00"), d("5750.00"), "REGISTRADA"));
        bd.ventas().crear(new Venta(null, 3, 3, ahora, d("800.00"), d("800.00"), "REGISTRADA"));
        bd.ventas().crear(new Venta(null, 4, 4, ahora, d("300.00"), d("305.00"), "REGISTRADA"));

        bd.lineasProduccion().crear(new LineaProduccion(null, "LIN-01", "Emision de tarjetas", "Personalizacion de tarjetas de debito", "ACTIVA"));
        bd.lineasProduccion().crear(new LineaProduccion(null, "LIN-02", "Impresion de formularios", "Talonarios y vouchers", "ACTIVA"));
        bd.ordenesProduccion().crear(new OrdenProduccion(null, 1, 2, 500, hoy, hoy.plusDays(7), null, d("60"), "EN_PROCESO", "Lote mensual"));

        bd.expedientes().crear(new ExpedienteAprobacion(null, "EXP-001", "OPERACIONES", "Lucia Ramos", d("20000.00"), "ALTO", "Ampliacion de limite de caja para cambio", "PENDIENTE", ahora, null));
        bd.expedientes().crear(new ExpedienteAprobacion(null, "EXP-002", "MARKETING", "Jorge Torres", d("5000.00"), "MEDIO", "Presupuesto adicional para campania", "PENDIENTE", ahora, null));
        bd.expedientes().crear(new ExpedienteAprobacion(null, "EXP-003", "ALMACEN", "Miguel Machaca", d("3000.00"), "BAJO", "Compra de insumos de caja", "APROBADO", ahora, ahora));
    }
}
