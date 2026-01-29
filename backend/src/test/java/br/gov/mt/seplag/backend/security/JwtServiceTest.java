package br.gov.mt.seplag.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para JwtService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService - Testes Unitários")
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private UserDetails userDetails;
    private String secret;
    private Long jwtExpiration;
    private Long refreshExpiration;

    @BeforeEach
    void setUp() {
        // Configurar valores de teste
        secret = Base64.getEncoder().encodeToString("test-secret-key-for-jwt-service-unit-tests-12345678901234567890".getBytes());
        jwtExpiration = 300000L; // 5 minutos em ms
        refreshExpiration = 86400000L; // 24 horas em ms

        ReflectionTestUtils.setField(jwtService, "secret", secret);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", jwtExpiration);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", refreshExpiration);

        userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities("ROLE_USER")
                .build();
    }

    @Test
    @DisplayName("Deve gerar access token com sucesso")
    void deveGerarAccessTokenComSucesso() {
        // Act
        String token = jwtService.generateToken(userDetails);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals("testuser", jwtService.extractUsername(token));
        assertTrue(jwtService.isAccessToken(token));
    }

    @Test
    @DisplayName("Deve gerar refresh token com sucesso")
    void deveGerarRefreshTokenComSucesso() {
        // Act
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Assert
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
        assertEquals("testuser", jwtService.extractUsername(refreshToken));
        assertTrue(jwtService.isRefreshToken(refreshToken));
    }

    @Test
    @DisplayName("Deve validar token válido")
    void deveValidarTokenValido() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Deve invalidar token com username diferente")
    void deveInvalidarTokenComUsernameDiferente() {
        // Arrange
        String token = jwtService.generateToken(userDetails);
        UserDetails outroUsuario = User.builder()
                .username("outrouser")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        // Act
        boolean isValid = jwtService.isTokenValid(token, outroUsuario);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Deve invalidar refresh token usado como access token")
    void deveInvalidarRefreshTokenUsadoComoAccessToken() {
        // Arrange
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Act
        boolean isValid = jwtService.isTokenValid(refreshToken, userDetails);

        // Assert
        assertFalse(isValid); // Refresh token não pode ser usado como access token
    }

    @Test
    @DisplayName("Deve validar refresh token corretamente")
    void deveValidarRefreshTokenCorretamente() {
        // Arrange
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Act
        boolean isValid = jwtService.isRefreshTokenValid(refreshToken, userDetails);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Deve extrair username do token")
    void deveExtrairUsernameDoToken() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act
        String username = jwtService.extractUsername(token);

        // Assert
        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("Deve extrair expiração do token")
    void deveExtrairExpiracaoDoToken() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act
        Date expiration = jwtService.extractExpiration(token);

        // Assert
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    @DisplayName("Deve identificar token expirado")
    void deveIdentificarTokenExpirado() {
        // Arrange
        // Criar token com expiração muito curta
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 100L); // 100ms
        String token = jwtService.generateToken(userDetails);

        // Aguardar expiração
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act
        boolean isExpired = jwtService.isTokenExpired(token);

        // Assert
        assertTrue(isExpired);
    }

    @Test
    @DisplayName("Deve identificar token não expirado")
    void deveIdentificarTokenNaoExpirado() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act
        boolean isExpired = jwtService.isTokenExpired(token);

        // Assert
        assertFalse(isExpired);
    }

    @Test
    @DisplayName("Deve retornar tempo de expiração em segundos")
    void deveRetornarTempoDeExpiracaoEmSegundos() {
        // Act
        Long expirationSeconds = jwtService.getExpirationTime();

        // Assert
        assertNotNull(expirationSeconds);
        assertEquals(300L, expirationSeconds); // 5 minutos = 300 segundos
    }

    @Test
    @DisplayName("Deve lançar exceção ao extrair claims de token inválido")
    void deveLancarExcecaoAoExtrairClaimsDeTokenInvalido() {
        // Arrange
        String tokenInvalido = "token.invalido.aqui";

        // Act & Assert
        assertThrows(Exception.class, () -> jwtService.extractUsername(tokenInvalido));
    }

    @Test
    @DisplayName("Deve diferenciar access token de refresh token")
    void deveDiferenciarAccessTokenDeRefreshToken() {
        // Arrange
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Act & Assert
        assertTrue(jwtService.isAccessToken(accessToken));
        assertFalse(jwtService.isRefreshToken(accessToken));
        assertTrue(jwtService.isRefreshToken(refreshToken));
        assertFalse(jwtService.isAccessToken(refreshToken));
    }
}
