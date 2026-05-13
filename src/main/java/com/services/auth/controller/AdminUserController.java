package com.services.auth.controller;

import com.services.auth.dto.request.CreateUserRequest;
import com.services.auth.dto.request.ResetPasswordRequest;
import com.services.auth.dto.request.UpdateUserRequest;
import com.services.auth.dto.response.ErrorResponse;
import com.services.auth.dto.response.UserResponse;
import com.services.auth.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin - Usuarios", description = "Gestión de usuarios por el administrador")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "Listar todos los usuarios")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente")
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminUserService.getAllUsers());
    }

    @Operation(summary = "Obtener un usuario por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "401", description = "Usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{idUser}")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID idUser) {
        return ResponseEntity.ok(adminUserService.getUserById(idUser));
    }

    @Operation(summary = "Crear un nuevo usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Email ya registrado u otro error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminUserService.createUser(request));
    }

    @Operation(summary = "Editar nombre, email o rol de un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Error en la operación",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{idUser}")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID idUser,
                                                   @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(adminUserService.updateUser(idUser, request));
    }

    @Operation(summary = "Activar cuenta de usuario")
    @ApiResponse(responseCode = "200", description = "Usuario activado exitosamente")
    @PatchMapping("/{idUser}/enable")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public ResponseEntity<UserResponse> enableUser(@PathVariable UUID idUser) {
        return ResponseEntity.ok(adminUserService.toggleUserStatus(idUser, true));
    }

    @Operation(summary = "Desactivar cuenta de usuario")
    @ApiResponse(responseCode = "200", description = "Usuario desactivado exitosamente")
    @PatchMapping("/{idUser}/disable")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public ResponseEntity<UserResponse> disableUser(@PathVariable UUID idUser) {
        return ResponseEntity.ok(adminUserService.toggleUserStatus(idUser, false));
    }

    @Operation(
            summary = "Restablecer contraseña de usuario",
            description = "El administrador asigna una nueva contraseña manualmente. " +
                    "Se revocan todas las sesiones activas del usuario para forzar re-login."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Contraseña restablecida y sesiones revocadas"),
            @ApiResponse(responseCode = "400", description = "Contraseña inválida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{idUser}/reset-password")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public ResponseEntity<Void> resetPassword(@PathVariable UUID idUser,
                                              @Valid @RequestBody ResetPasswordRequest request) {
        adminUserService.resetPassword(idUser, request);
        return ResponseEntity.noContent().build();
    }
}