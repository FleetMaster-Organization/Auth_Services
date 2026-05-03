package com.services.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro parseador de JWT para microservicios.
 *
 * NO valida firma ni expiración: esa responsabilidad ya fue cumplida por el API Gateway.
 * Solo extrae el subject (UUID del usuario) y los roles del token para poblar
 * el SecurityContext y permitir que @PreAuthorize funcione correctamente.
 *
 * Opción A: los roles en el token ya vienen con prefijo ROLE_ (ej: ROLE_ADMINISTRADOR),
 * por lo que se mapean directamente a SimpleGrantedAuthority sin añadir prefijo.
 */
@Component
@RequiredArgsConstructor
public class JwtParsingFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                String userId = tokenProvider.getSubjectFromToken(token);
                List<String> roles = tokenProvider.getRolesFromToken(token);

                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                // Token corrupto o ilegible: no se autentica, continúa sin contexto
            }
        }

        filterChain.doFilter(request, response);
    }
}