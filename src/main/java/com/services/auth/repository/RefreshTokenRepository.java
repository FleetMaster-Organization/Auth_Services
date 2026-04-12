package com.services.auth.repository;

import com.services.auth.model.RefreshToken;
import com.services.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    // 1. Buscar por token (para validar al refrescar)
    Optional<RefreshToken> findByToken(String token);

    // 2. Buscar token activo (no revocado)
    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);

    // 3. Revocar todos los tokens de un usuario (logout global)
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user = :user AND rt.revoked = false")
    void revokeAllUserTokens(@Param("user") User user);

    // 4. Actualizar última actividad (para control de inactividad)
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.lastActivity = :lastActivity WHERE rt.token = :token")
    void updateLastActivity(@Param("token") String token, @Param("lastActivity") Instant lastActivity);
}