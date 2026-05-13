package com.services.auth.config;

import com.services.auth.model.Role;
import com.services.auth.model.User;
import com.services.auth.repository.RoleRepository;
import com.services.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Carga inicial de datos: roles estáticos del sistema y usuario administrador por defecto.
 * Los roles no se crean desde la API; son fijos y se definen aquí.
 * El resto de usuarios los crea el administrador desde la interfaz.
 */
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        Role adminRole = getOrCreateRole("ROLE_ADMINISTRADOR");
        getOrCreateRole("ROLE_COORDINADOR");
        getOrCreateRole("ROLE_DESPACHADOR");
        getOrCreateRole("ROLE_MECANICO");

        if (!userRepository.existsByEmail("admin@fleetmaster.com")) {
            User admin = User.builder()
                    .fullName("Pompilio Roncancio")
                    .email("admin@fleetmaster.com")
                    .passwordHash(passwordEncoder.encode("AdminFleetMaster123"))
                    .roles(Set.of(adminRole))
                    .enabled(true)
                    .build();
            userRepository.save(admin);
        }
    }

    private Role getOrCreateRole(String roleName) {
        return roleRepository.findByNameRole(roleName)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setNameRole(roleName);
                    return roleRepository.save(role);
                });
    }
}