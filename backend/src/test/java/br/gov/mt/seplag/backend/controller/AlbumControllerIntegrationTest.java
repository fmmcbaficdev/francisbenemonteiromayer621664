package br.gov.mt.seplag.backend.controller;

import br.gov.mt.seplag.backend.dto.AlbumDTO;
import br.gov.mt.seplag.backend.model.Album;
import br.gov.mt.seplag.backend.model.Artista;
import br.gov.mt.seplag.backend.repository.AlbumRepository;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para AlbumController
 * Testa a camada completa: Controller -> Service -> Repository
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("AlbumController - Testes de Integração")
class AlbumControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private ArtistaRepository artistaRepository;

    @Autowired
    private JwtService jwtService;

    private String accessToken;
    private Artista artista;

    @BeforeEach
    void setUp() {
        // Limpar dados de teste
        albumRepository.deleteAll();
        artistaRepository.deleteAll();

        // Criar artista de teste
        artista = Artista.builder()
                .nome("Artista Teste")
                .biografia("Biografia teste")
                .build();
        artista = artistaRepository.save(artista);

        // Gerar token JWT
        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities("ROLE_USER")
                .build();
        accessToken = jwtService.generateToken(userDetails);
    }

    @Test
    @DisplayName("Deve listar álbuns com autenticação")
    void deveListarAlbunsComAutenticacao() throws Exception {
        // Arrange
        Album album = Album.builder()
                .titulo("Álbum Teste")
                .anoLancamento(2024)
                .descricao("Descrição teste")
                .artistas(new HashSet<>(Set.of(artista)))
                .build();
        albumRepository.save(album);

        // Act & Assert
        mockMvc.perform(get("/v1/albuns")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].titulo").value("Álbum Teste"));
    }

    @Test
    @DisplayName("Deve retornar 401 sem autenticação")
    void deveRetornar401SemAutenticacao() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/v1/albuns")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve criar álbum com sucesso")
    void deveCriarAlbumComSucesso() throws Exception {
        // Arrange
        AlbumDTO dto = AlbumDTO.builder()
                .titulo("Novo Álbum")
                .anoLancamento(2024)
                .descricao("Descrição")
                .artistasIds(List.of(artista.getId()))
                .build();

        // Act & Assert
        mockMvc.perform(post("/v1/albuns")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Novo Álbum"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("Deve buscar álbum por ID")
    void deveBuscarAlbumPorId() throws Exception {
        // Arrange
        Album album = Album.builder()
                .titulo("Álbum Busca")
                .anoLancamento(2024)
                .artistas(new HashSet<>(Set.of(artista)))
                .build();
        album = albumRepository.save(album);

        // Act & Assert
        mockMvc.perform(get("/v1/albuns/" + album.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(album.getId()))
                .andExpect(jsonPath("$.titulo").value("Álbum Busca"));
    }

    @Test
    @DisplayName("Deve retornar 404 para álbum inexistente")
    void deveRetornar404ParaAlbumInexistente() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/v1/albuns/999")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve atualizar álbum com sucesso")
    void deveAtualizarAlbumComSucesso() throws Exception {
        // Arrange
        Album album = Album.builder()
                .titulo("Álbum Original")
                .anoLancamento(2020)
                .artistas(new HashSet<>(Set.of(artista)))
                .build();
        album = albumRepository.save(album);

        AlbumDTO dtoAtualizado = AlbumDTO.builder()
                .titulo("Álbum Atualizado")
                .anoLancamento(2024)
                .descricao("Nova descrição")
                .artistasIds(List.of(artista.getId()))
                .build();

        // Act & Assert
        mockMvc.perform(put("/v1/albuns/" + album.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoAtualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Álbum Atualizado"))
                .andExpect(jsonPath("$.anoLancamento").value(2024));
    }

    @Test
    @DisplayName("Deve deletar álbum com sucesso")
    void deveDeletarAlbumComSucesso() throws Exception {
        // Arrange
        Album album = Album.builder()
                .titulo("Álbum para Deletar")
                .anoLancamento(2024)
                .artistas(new HashSet<>(Set.of(artista)))
                .build();
        album = albumRepository.save(album);

        // Act & Assert
        mockMvc.perform(delete("/v1/albuns/" + album.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        // Verificar que foi deletado
        assertFalse(albumRepository.existsById(album.getId()));
    }

    @Test
    @DisplayName("Deve buscar álbuns por artista")
    void deveBuscarAlbunsPorArtista() throws Exception {
        // Arrange
        Album album1 = Album.builder()
                .titulo("Álbum 1")
                .anoLancamento(2024)
                .artistas(new HashSet<>(Set.of(artista)))
                .build();
        albumRepository.save(album1);

        // Act & Assert
        mockMvc.perform(get("/v1/albuns/artista/" + artista.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].titulo").value("Álbum 1"));
    }
}
