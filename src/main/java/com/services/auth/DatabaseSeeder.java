package com.services.auth;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;

/**
 * HERRAMIENTA DE SOPORTE PARA DESARROLLO
 * Genera hashes de BCrypt compatibles con el microservicio y comandos SQL de inserción.
 */
public class DatabaseSeeder {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        String password = "FleetMaster123";
        String hash = encoder.encode(password);

        System.out.println("\n============================================================");
        System.out.println("CONFIGURACIÓN DE SEGURIDAD FLEETMASTER - SQL DINÁMICO");
        System.out.println("============================================================");
        System.out.println("Password RAW: " + password);
        System.out.println("BCrypt Hash:  " + hash);
        
        System.out.println("\n--- COPIA Y PEGA ESTOS COMANDOS EN TU DB ---");
        
        System.out.println("-- 1. Actualizar Admin");
        System.out.println("UPDATE auth_db.users SET password_hash = '" + hash + "' WHERE email = 'admin@fleetmaster.com';");

        System.out.println("\n-- 2. Insertar Coordinador");
        printUserSql("Carlos Coordinador", "coord@fleetmaster.com", hash, "ROLE_COORDINADOR");

        System.out.println("\n-- 3. Insertar Despachador");
        printUserSql("Diego Despachador", "despacho@fleetmaster.com", hash, "ROLE_DESPACHADOR");
        
        System.out.println("============================================================\n");
    }

    private static void printUserSql(String name, String email, String hash, String roleName) {
        System.out.println("INSERT INTO auth_db.users (id_user, full_name, email, password_hash, enabled) VALUES (gen_random_uuid(), '" + name + "', '" + email + "', '" + hash + "', true) ON CONFLICT (email) DO UPDATE SET password_hash = EXCLUDED.password_hash;");
        System.out.println("INSERT INTO auth_db.users_roles (id_user, id_role) SELECT id_user, (SELECT id_role FROM auth_db.roles WHERE name_role = '" + roleName + "') FROM auth_db.users WHERE email = '" + email + "' ON CONFLICT DO NOTHING;");
    }
}
