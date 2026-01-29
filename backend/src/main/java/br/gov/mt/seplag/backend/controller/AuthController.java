package br.gov.mt.seplag.backend.controller;

import br.gov.mt.seplag.backend.dto.AuthRequestDTO;
import br.gov.mt.seplag.backend.dto.AuthResponseDTO;
import br.gov.mt.seplag.backend.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para autenticação (login)
 */
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticação", description = "Endpoints de autenticação")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    /**
     * POST /v1/auth/login
     * Login e geração de JWT
     */
    @PostMapping("/login")
    @Operation(summary = "Login", description = "Autentica usuário e retorna JWT token")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO request) {
        log.info("Tentativa de login: {}", request.getUsername());

        // Autenticar
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Carregar usuário
        UserDetails user = userDetailsService.loadUserByUsername(request.getUsername());

        // Gerar tokens
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        log.info("Login bem-sucedido: {}", request.getUsername());

        return ResponseEntity.ok(AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .build());
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh Token", description = "Renova access token usando refresh token")
    public ResponseEntity<AuthResponseDTO> refresh(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        log.info("Solicitação de refresh token");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.warn("Header Authorization ausente ou inválido");
            return ResponseEntity.badRequest().build();
        }

        String token = authorizationHeader.substring(7);

        try {
            String username = jwtService.extractUsername(token);
            UserDetails user = userDetailsService.loadUserByUsername(username);

            if (!jwtService.isRefreshTokenValid(token, user)) {
                log.warn("Refresh token inválido ou expirado");
                return ResponseEntity.status(401).build();
            }

            String newAccessToken = jwtService.generateToken(user);

            log.info("Access token renovado com sucesso para {}", username);

            return ResponseEntity.ok(AuthResponseDTO.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(token)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getExpirationTime())
                    .build());

        } catch (Exception e) {
            log.error("Erro ao renovar token", e);
            return ResponseEntity.status(401).build();
        }
    }


}