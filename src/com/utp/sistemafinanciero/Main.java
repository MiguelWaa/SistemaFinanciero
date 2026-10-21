package com.utp.sistemafinanciero;

import com.utp.sistemafinanciero.model.seguridad.Rol;
import com.utp.sistemafinanciero.model.seguridad.Usuario;
import com.utp.sistemafinanciero.model.clientes.Cliente;
import com.utp.sistemafinanciero.model.operaciones.Operacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Clase principal de demostracion del Sistema Integral de Gestion de
 * Servicios Financieros.
 *
 * Crea algunos objetos de ejemplo usando las entidades del paquete
 * com.utp.sistemafinanciero.model y los muestra por consola, para
 * verificar que el proyecto compila y ejecuta correctamente en NetBeans.
 *
 * Proyecto: Curso Integrador I Sistemas - Software (UTP)
 */
public class Main {

    public static void main(String[] args) {

        // --- Rol y Usuario (paquete seguridad) ---
        Rol rolAdmin = new Rol(1, "ADMIN", "Administrador del sistema");

        Usuario usuario = new Usuario(
                1, "Miguel", "Machaca", "miguel@financiero.com", "hash123",
                rolAdmin.getIdRol(), "ACTIVO", LocalDateTime.now()
        );

        // --- Cliente (paquete clientes) ---
        Cliente cliente = new Cliente(
                1, "DNI", "70000001", "Juan", "Perez",
                LocalDate.of(1995, 5, 20), "987654321", "juan@gmail.com",
                "Arequipa", "ACTIVO", LocalDateTime.now()
        );

        // --- Operacion (paquete operaciones) ---
        Operacion operacion = new Operacion(
                1, cliente.getIdCliente(), 1, "CAMBIO", "USD", "PEN",
                new BigDecimal("100.00"), new BigDecimal("3.78"),
                new BigDecimal("378.00"), null, "PENDIENTE", LocalDateTime.now()
        );

        System.out.println("=== Sistema Integral de Gestion de Servicios Financieros ===");
        System.out.println();
        System.out.println("Rol registrado:");
        System.out.println("  " + rolAdmin);
        System.out.println();
        System.out.println("Usuario registrado:");
        System.out.println("  " + usuario);
        System.out.println();
        System.out.println("Cliente registrado:");
        System.out.println("  " + cliente);
        System.out.println();
        System.out.println("Operacion registrada:");
        System.out.println("  " + operacion);
    }
}
