package com.services.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateUserRequest {

    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String fullName;

    @Email(message = "Debe proporcionar un email válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;

    private UUID roleId;
}