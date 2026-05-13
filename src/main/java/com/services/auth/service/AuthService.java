package com.services.auth.service;

import com.services.auth.dto.request.LoginRequest;
import com.services.auth.dto.response.LoginResponse;
import com.services.auth.model.RefreshToken;
import com.services.auth.model.Role;
import com.services.auth.model.User;
import com.services.auth.repository.RefreshTokenRepository;
import com.services.auth.repository.UserRepository;
import com.services.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    @Value("${app.jwt.access-expiration}")
    private long accessExpiration;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpiration;

    private static final long INACTIVITY_LIMIT_MS = 1_800_000L;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmailWithRoles(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<String> roles = extractRoles(user);

        String accessToken  = tokenProvider.generateAccessToken(user.getIdUser(), user.getEmail(), roles, accessExpiration);
        String refreshToken = tokenProvider.generateRefreshToken(user.getIdUser(), refreshExpiration);

        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expirationDate(Instant.now().plusMillis(refreshExpiration))
                .lastActivity(Instant.now())
                .revoked(false)
                .build());

        return buildLoginResponse(accessToken, refreshToken, user, roles);
    }

    @Transactional
    public LoginResponse refreshToken(String refreshToken) {
        RefreshToken storedToken = refreshTokenRepository.findByTokenAndRevokedFalse(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token inválido"));

        if (storedToken.getExpirationDate().isBefore(Instant.now())) {
            revokeToken(storedToken);
            throw new RuntimeException("Refresh token expirado");
        }

        if (storedToken.getLastActivity().isBefore(Instant.now().minusMillis(INACTIVITY_LIMIT_MS))) {
            revokeToken(storedToken);
            throw new RuntimeException("Sesión expirada por inactividad de 30 minutos");
        }

        storedToken.setLastActivity(Instant.now());
        refreshTokenRepository.save(storedToken);

        User user = storedToken.getUser();
        List<String> roles = extractRoles(user);

        String newAccessToken = tokenProvider.generateAccessToken(user.getIdUser(), user.getEmail(), roles, accessExpiration);

        return buildLoginResponse(newAccessToken, refreshToken, user, roles);
    }

    /**
     * Logout: revoca únicamente el refresh token del dispositivo actual.
     * El userId viene del SecurityContext (subject del JWT parseado por JwtParsingFilter).
     */
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(this::revokeToken);
    }

    private void revokeToken(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    private List<String> extractRoles(User user) {
        return user.getRoles().stream()
                .map(Role::getNameRole)
                .collect(Collectors.toList());
    }

    private LoginResponse buildLoginResponse(String accessToken, String refreshToken,
                                             User user, List<String> roles) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .type("Bearer")
                .idUser(user.getIdUser())
                .email(user.getEmail())
                .roles(roles)
                .expiresIn(accessExpiration / 1000)
                .build();
    }
}