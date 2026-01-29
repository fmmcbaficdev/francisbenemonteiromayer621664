package br.gov.mt.seplag.backend.controller;

import br.gov.mt.seplag.backend.dto.ArtistaDTO;
import br.gov.mt.seplag.backend.model.Artista;
import br.gov.mt.seplag.backend.repository.ArtistaRepository;
import br.gov.mt.seplag.backend.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para ArtistaController
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("ArtistaController - Testes de Integração")
class ArtistaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ArtistaRepository artistaRepository;

    @Autowired
    private JwtService jwtService;

    private String accessToken;

    @BeforeEach
    void setUp() {
        // Limpar dados de teste
        artistaRepository.deleteAll();

        // Gerar token JWT
        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities("ROLE_USER")
                .build();
        accessToken = jwtService.generateToken(userDetails);
    }

    @Test
    @DisplayName("Deve listar artistas com autenticação")
    void deveListarArtistasComAutenticacao() throws Exception {
        // Arrange
        Artista artista = Artista.builder()
                .nome("Artista Teste")
                .biografia("Biografia teste")
                .build();
        artistaRepository.save(artista);

        // Act & Assert
        mockMvc.perform(get("/v1/artistas")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].nome").value("Artista Teste"));
    }

    @Test
    @DisplayName("Deve criar artista com sucesso")
    void deveCriarArtistaComSucesso() throws Exception {
        // Arrange
        ArtistaDTO dto = ArtistaDTO.builder()
                .nome("Novo Artista")
                .biografia("Biografia")
                .build();

        // Act & Assert
        mockMvc.perform(post("/v1/artistas")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Novo Artista"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("Deve buscar artista por ID")
    void deveBuscarArtistaPorId() throws Exception {
        // Arrange
        Artista artista = Artista.builder()
                .nome("Artista Busca")
                .biografia("Biografia")
                .build();
        artista = artistaRepository.save(artista);

        // Act & Assert
        mockMvc.perform(get("/v1/artistas/" + artista.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(artista.getId()))
                .andExpect(jsonPath("$.nome").value("Artista Busca"));
    }

    @Test
    @DisplayName("Deve buscar artista por nome")
    void deveBuscarArtistaPorNome() throws Exception {
        // Arrange
        Artista artista = Artista.builder()
                .nome("Serj Tankian")
                .biografia("Vocalista")
                .build();
        artistaRepository.save(artista);

        // Act & Assert
        mockMvc.perform(get("/v1/artistas/buscar")
                        .param("nome", "Serj")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].nome").value("Serj Tankian"));
    }

    @Test
    @DisplayName("Deve atualizar artista com sucesso")
    void deveAtualizarArtistaComSucesso() throws Exception {
        // Arrange
        Artista artista = Artista.builder()
                .nome("Artista Original")
                .biografia("Biografia original")
                .build();
        artista = artistaRepository.save(artista);

        ArtistaDTO dtoAtualizado = ArtistaDTO.builder()
                .nome("Artista Atualizado")
                .biografia("Nova biografia")
                .build();

        // Act & Assert
        mockMvc.perform(put("/v1/artistas/" + artista.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoAtualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Artista Atualizado"))
                .andExpect(jsonPath("$.biografia").value("Nova biografia"));
    }

    @Test
    @DisplayName("Deve deletar artista com sucesso")
    void deveDeletarArtistaComSucesso() throws Exception {
        // Arrange
        Artista artista = Artista.builder()
                .nome("Artista para Deletar")
                .biografia("Biografia")
                .build();
        artista = artistaRepository.save(artista);

        // Act & Assert
        mockMvc.perform(delete("/v1/artistas/" + artista.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        // Verificar que foi deletado
        assertFalse(artistaRepository.existsById(artista.getId()));
    }

    @Test
    @DisplayName("Deve retornar 404 para artista inexistente")
    void deveRetornar404ParaArtistaInexistente() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/v1/artistas/999")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }
}
