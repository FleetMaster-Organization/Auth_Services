package com.services.auth.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class UserResponse {

    private UUID idUser;
    private String fullName;
    private String email;
    private boolean enabled;
    private List<String> roles;
}