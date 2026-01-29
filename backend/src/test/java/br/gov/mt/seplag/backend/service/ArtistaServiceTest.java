package br.gov.mt.seplag.backend.service;

import br.gov.mt.seplag.backend.dto.ArtistaDTO;
import br.gov.mt.seplag.backend.exception.EntityNotFoundException;
import br.gov.mt.seplag.backend.model.Artista;
import br.gov.mt.seplag.backend.repository.ArtistaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para ArtistaService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ArtistaService - Testes Unitários")
class ArtistaServiceTest {

    @Mock
    private ArtistaRepository artistaRepository;

    @InjectMocks
    private ArtistaService artistaService;

    private Artista artista;

    @BeforeEach
    void setUp() {
        artista = Artista.builder()
                .id(1L)
                .nome("Serj Tankian")
                .biografia("Vocalista do System of a Down")
                .build();
    }

    @Test
    @DisplayName("Deve listar artistas com paginação")
    void deveListarArtistasComPaginacao() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Artista> page = new PageImpl<>(List.of(artista), pageable, 1);

        when(artistaRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<ArtistaDTO> resultado = artistaService.listar(pageable);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals("Serj Tankian", resultado.getContent().get(0).getNome());
        verify(artistaRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Deve buscar artistas por nome")
    void deveBuscarArtistasPorNome() {
        // Arrange
        String nome = "Serj";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Artista> page = new PageImpl<>(List.of(artista), pageable, 1);

        when(artistaRepository.findByNomeContainingIgnoreCase(nome, pageable)).thenReturn(page);

        // Act
        Page<ArtistaDTO> resultado = artistaService.buscarPorNome(nome, pageable);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(artistaRepository, times(1)).findByNomeContainingIgnoreCase(nome, pageable);
    }

    @Test
    @DisplayName("Deve buscar artista por ID com sucesso")
    void deveBuscarArtistaPorIdComSucesso() {
        // Arrange
        Long id = 1L;
        when(artistaRepository.findById(id)).thenReturn(Optional.of(artista));

        // Act
        ArtistaDTO resultado = artistaService.buscarPorId(id);

        // Assert
        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
        assertEquals("Serj Tankian", resultado.getNome());
        verify(artistaRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve lançar exceção quando artista não encontrado")
    void deveLancarExcecaoQuandoArtistaNaoEncontrado() {
        // Arrange
        Long id = 999L;
        when(artistaRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> artistaService.buscarPorId(id));
        verify(artistaRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve criar novo artista com sucesso")
    void deveCriarNovoArtistaComSucesso() {
        // Arrange
        ArtistaDTO novoDTO = ArtistaDTO.builder()
                .nome("Mike Shinoda")
                .biografia("Vocalista do Linkin Park")
                .build();

        Artista novoArtista = Artista.builder()
                .id(2L)
                .nome("Mike Shinoda")
                .biografia("Vocalista do Linkin Park")
                .build();

        when(artistaRepository.save(any(Artista.class))).thenReturn(novoArtista);

        // Act
        ArtistaDTO resultado = artistaService.criar(novoDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals("Mike Shinoda", resultado.getNome());
        verify(artistaRepository, times(1)).save(any(Artista.class));
    }

    @Test
    @DisplayName("Deve atualizar artista com sucesso")
    void deveAtualizarArtistaComSucesso() {
        // Arrange
        ArtistaDTO dtoAtualizado = ArtistaDTO.builder()
                .nome("Serj Tankian (Atualizado)")
                .biografia("Biografia atualizada")
                .build();

        Artista artistaAtualizado = Artista.builder()
                .id(1L)
                .nome("Serj Tankian (Atualizado)")
                .biografia("Biografia atualizada")
                .build();

        when(artistaRepository.findById(1L)).thenReturn(Optional.of(artista));
        when(artistaRepository.save(any(Artista.class))).thenReturn(artistaAtualizado);

        // Act
        ArtistaDTO resultado = artistaService.atualizar(1L, dtoAtualizado);

        // Assert
        assertNotNull(resultado);
        assertEquals("Serj Tankian (Atualizado)", resultado.getNome());
        verify(artistaRepository, times(1)).findById(1L);
        verify(artistaRepository, times(1)).save(any(Artista.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar artista inexistente")
    void deveLancarExcecaoAoAtualizarArtistaInexistente() {
        // Arrange
        Long id = 999L;
        ArtistaDTO dto = ArtistaDTO.builder()
                .nome("Inexistente")
                .build();

        when(artistaRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> artistaService.atualizar(id, dto));
        verify(artistaRepository, times(1)).findById(id);
        verify(artistaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve deletar artista com sucesso")
    void deveDeletarArtistaComSucesso() {
        // Arrange
        Long id = 1L;
        when(artistaRepository.existsById(id)).thenReturn(true);
        doNothing().when(artistaRepository).deleteById(id);

        // Act
        artistaService.deletar(id);

        // Assert
        verify(artistaRepository, times(1)).existsById(id);
        verify(artistaRepository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar artista inexistente")
    void deveLancarExcecaoAoDeletarArtistaInexistente() {
        // Arrange
        Long id = 999L;
        when(artistaRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> artistaService.deletar(id));
        verify(artistaRepository, times(1)).existsById(id);
        verify(artistaRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve converter Entity para DTO corretamente")
    void deveConverterEntityParaDTOCorretamente() {
        // Arrange
        Artista artistaComAlbuns = Artista.builder()
                .id(1L)
                .nome("Serj Tankian")
                .biografia("Biografia")
                .build();

        when(artistaRepository.findById(1L)).thenReturn(Optional.of(artistaComAlbuns));

        // Act
        ArtistaDTO resultado = artistaService.buscarPorId(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(artistaComAlbuns.getId(), resultado.getId());
        assertEquals(artistaComAlbuns.getNome(), resultado.getNome());
        assertEquals(artistaComAlbuns.getBiografia(), resultado.getBiografia());
    }
}
