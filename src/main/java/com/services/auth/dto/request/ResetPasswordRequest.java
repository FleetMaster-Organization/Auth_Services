package com.services.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, max = 255, message = "La contraseña debe tener entre 8 y 255 caracteres")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[0-9]).*$",
            message = "La contraseña debe contener al menos una letra mayúscula y un número"
    )
    private String newPassword;
}