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

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args){
        if(userRepository.count()>0) {
            return;
        }

        Role adminRole = getOrCreateRole("ROLE_ADMINISTRADOR");
        Role coordinadorRole = getOrCreateRole("ROLE_COORDINADOR");
        Role despachadorRole = getOrCreateRole("ROLE_DESPACHADOR");
        Role mecanicoRole = getOrCreateRole("ROLE_MECANICO");

        User admin = User.builder()
                .email("admin@fleetmaster.com")
                .passwordHash(passwordEncoder.encode("AdminFleetMaster123"))
                .roles(Set.of(adminRole))
                .build();
        userRepository.save(admin);

        User coordinador = User.builder()
                .email("coordinador@fleetmaster.com")
                .passwordHash(passwordEncoder.encode("CoordFleetMaster123"))
                .roles(Set.of(coordinadorRole))
                .build();
        userRepository.save(coordinador);

        User despachador = User.builder()
                .email("despachador@fleetmaster.com")
                .passwordHash(passwordEncoder.encode("DespFleetMaster123"))
                .roles(Set.of(despachadorRole))
                .build();
        userRepository.save(despachador);

        User mecanico = User.builder()
                .email("mecanico@fleetmaster.com")
                .passwordHash(passwordEncoder.encode("MecaFleetMaster123"))
                .roles(Set.of(mecanicoRole))
                .build();
        userRepository.save(mecanico);
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
