package com.services.auth.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String type;
    private UUID idUser;
    private String email;
    private List<String> roles;
    private Long expiresIn;
}