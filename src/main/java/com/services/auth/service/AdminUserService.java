package com.services.auth.service;

import com.services.auth.dto.request.CreateUserRequest;
import com.services.auth.dto.request.ResetPasswordRequest;
import com.services.auth.dto.request.UpdateUserRequest;
import com.services.auth.dto.response.UserResponse;
import com.services.auth.model.Role;
import com.services.auth.model.User;
import com.services.auth.model.UserAudit;
import com.services.auth.repository.RefreshTokenRepository;
import com.services.auth.repository.RoleRepository;
import com.services.auth.repository.UserAuditRepository;
import com.services.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserAuditRepository userAuditRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado: " + request.getEmail());
        }

        Role role = findRoleWithFallback(request.getRoleId());
        if (role == null) {
            throw new RuntimeException("Rol no encontrado");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(role))
                .enabled(true)
                .build();

        userRepository.save(user);
        saveAudit(user, "CREATE", null, null, null, getAdminIdentifier());
        return toResponse(user);
    }

    @Transactional
    public UserResponse updateUser(UUID idUser, UpdateUserRequest request) {
        User user = findUserOrThrow(idUser);
        String adminId = getAdminIdentifier();

        if (request.getFullName() != null && !request.getFullName().equals(user.getFullName())) {
            saveAudit(user, "UPDATE", "full_name", user.getFullName(), request.getFullName(), adminId);
            user.setFullName(request.getFullName());
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("El email ya está registrado: " + request.getEmail());
            }
            saveAudit(user, "UPDATE", "email", user.getEmail(), request.getEmail(), adminId);
            user.setEmail(request.getEmail());
        }

        if (request.getRoleId() != null) {
            Role newRole = findRoleWithFallback(request.getRoleId());
            if (newRole == null) {
                throw new RuntimeException("Rol no encontrado");
            }
            String oldRoles = user.getRoles().stream().map(Role::getNameRole).collect(Collectors.joining(","));
            saveAudit(user, "UPDATE", "roles", oldRoles, newRole.getNameRole(), adminId);
            user.getRoles().clear();
            user.getRoles().add(newRole);
        }

        userRepository.save(user);
        return toResponse(user);
    }

    @Transactional
    public UserResponse toggleUserStatus(UUID idUser, boolean enable) {
        User user = findUserOrThrow(idUser);

        if (user.isEnabled() == enable) {
            return toResponse(user);
        }

        String actionType = enable ? "ENABLE" : "DISABLE";
        saveAudit(user, actionType, "enabled", String.valueOf(!enable), String.valueOf(enable), getAdminIdentifier());

        user.setEnabled(enable);
        userRepository.save(user);
        return toResponse(user);
    }

    /**
     * Restablece la contraseña del usuario y revoca TODOS sus refresh tokens activos.
     *
     * Esto fuerza el re-login en todos los dispositivos donde el usuario tenga sesión abierta,
     * garantizando que la contraseña anterior no siga funcionando en ningún contexto activo.
     * El admin entrega la nueva contraseña al usuario de forma manual (presencial, Email, etc.).
     */
    @Transactional
    public void resetPassword(UUID idUser, ResetPasswordRequest request) {
        User user = findUserOrThrow(idUser);

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        refreshTokenRepository.revokeAllUserTokens(user);

        saveAudit(user, "UPDATE", "password_hash", null, null, getAdminIdentifier());
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(UUID idUser) {
        return toResponse(findUserOrThrow(idUser));
    }

    private User findUserOrThrow(UUID idUser) {
        return userRepository.findByIdWithRoles(idUser)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    private Role findRoleWithFallback(UUID roleId) {
        if (roleId == null) {
            return null;
        }
        return roleRepository.findById(roleId)
                .orElseGet(() -> {
                    String roleName = null;
                    String idStr = roleId.toString();
                    if (idStr.equals("ff4b650b-9765-4859-9545-fe28f168d60c")) {
                        roleName = "ROLE_ADMINISTRADOR";
                    } else if (idStr.equals("dd34042b-f399-48a9-a6ef-688df176dbe9")) {
                        roleName = "ROLE_COORDINADOR";
                    } else if (idStr.equals("9b56ce70-d44e-4006-92aa-fbd86bfd6087")) {
                        roleName = "ROLE_DESPACHADOR";
                    } else if (idStr.equals("345625ce-b6c0-4a06-96ec-b4fdbc6e14c8")) {
                        roleName = "ROLE_MECANICO";
                    }
                    if (roleName != null) {
                        return roleRepository.findByNameRole(roleName).orElse(null);
                    }
                    return null;
                });
    }

    private void saveAudit(User user, String actionType, String field,
                           String oldValue, String newValue, String modifiedBy) {
        userAuditRepository.save(UserAudit.builder()
                .user(user)
                .actionType(actionType)
                .modifiedField(field)
                .oldValue(oldValue)
                .newValue(newValue)
                .modifiedBy(modifiedBy)
                .build());
    }

    /**
     * El principal en el SecurityContext es el UUID del usuario (subject del JWT).
     * Se usa como identificador del admin que realiza la acción en la auditoría.
     */
    private String getAdminIdentifier() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .idUser(user.getIdUser())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .roles(user.getRoles().stream().map(Role::getNameRole).collect(Collectors.toList()))
                .build();
    }
}