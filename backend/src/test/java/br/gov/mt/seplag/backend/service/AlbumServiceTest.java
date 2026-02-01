package br.gov.mt.seplag.backend.service;

import br.gov.mt.seplag.backend.dto.AlbumDTO;
import br.gov.mt.seplag.backend.exception.EntityNotFoundException;
import br.gov.mt.seplag.backend.model.Album;
import br.gov.mt.seplag.backend.model.Artista;
import br.gov.mt.seplag.backend.model.ImagemCapa;
import br.gov.mt.seplag.backend.repository.AlbumRepository;
import br.gov.mt.seplag.backend.repository.ArtistaRepository;
import br.gov.mt.seplag.backend.repository.ImagemCapaRepository;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para AlbumService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AlbumService - Testes Unitários")
class AlbumServiceTest {

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private ArtistaRepository artistaRepository;

    @Mock
    private ImagemCapaRepository imagemCapaRepository;

    @Mock
    private WebSocketNotificationService webSocketService;

    @Mock
    private MinioService minIOService;

    @InjectMocks
    private AlbumService albumService;

    private Album album;
    private Artista artista;
    private AlbumDTO albumDTO;

    @BeforeEach
    void setUp() {
        artista = Artista.builder()
                .id(1L)
                .nome("Serj Tankian")
                .biografia("Vocalista do System of a Down")
                .build();

        album = Album.builder()
                .id(1L)
                .titulo("Harakiri")
                .anoLancamento(2012)
                .descricao("Terceiro álbum solo")
                .artistas(new HashSet<>(Set.of(artista)))
                .imagensCapa(new HashSet<>())
                .build();

        albumDTO = AlbumDTO.builder()
                .id(1L)
                .titulo("Harakiri")
                .anoLancamento(2012)
                .descricao("Terceiro álbum solo")
                .artistasIds(List.of(1L))
                .build();
    }

    @Test
    @DisplayName("Deve listar álbuns com paginação")
    void deveListarAlbunsComPaginacao() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Album> page = new PageImpl<>(List.of(album), pageable, 1);

        when(albumRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<AlbumDTO> resultado = albumService.listar(pageable);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals("Harakiri", resultado.getContent().get(0).getTitulo());
        verify(albumRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Deve buscar álbuns por título")
    void deveBuscarAlbunsPorTitulo() {
        // Arrange
        String titulo = "Harakiri";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Album> page = new PageImpl<>(List.of(album), pageable, 1);

        when(albumRepository.findByTituloContainingIgnoreCase(titulo, pageable)).thenReturn(page);

        // Act
        Page<AlbumDTO> resultado = albumService.buscarPorTitulo(titulo, pageable);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(albumRepository, times(1)).findByTituloContainingIgnoreCase(titulo, pageable);
    }

    @Test
    @DisplayName("Deve buscar álbuns por artista")
    void deveBuscarAlbunsPorArtista() {
        // Arrange
        Long artistaId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Album> page = new PageImpl<>(List.of(album), pageable, 1);

        when(albumRepository.findByArtistasId(artistaId, pageable)).thenReturn(page);

        // Act
        Page<AlbumDTO> resultado = albumService.buscarPorArtista(artistaId, pageable);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(albumRepository, times(1)).findByArtistasId(artistaId, pageable);
    }

    @Test
    @DisplayName("Deve buscar álbum por ID com sucesso")
    void deveBuscarAlbumPorIdComSucesso() {
        // Arrange
        Long id = 1L;
        when(albumRepository.findById(id)).thenReturn(Optional.of(album));

        // Act
        AlbumDTO resultado = albumService.buscarPorId(id);

        // Assert
        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
        assertEquals("Harakiri", resultado.getTitulo());
        verify(albumRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve lançar exceção quando álbum não encontrado")
    void deveLancarExcecaoQuandoAlbumNaoEncontrado() {
        // Arrange
        Long id = 999L;
        when(albumRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> albumService.buscarPorId(id));
        verify(albumRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve criar novo álbum com sucesso")
    void deveCriarNovoAlbumComSucesso() {
        // Arrange
        // OTIMIZADO: Agora usa findAllById para buscar artistas em batch
        when(artistaRepository.findAllById(List.of(1L))).thenReturn(List.of(artista));
        when(albumRepository.save(any(Album.class))).thenReturn(album);

        // Act
        AlbumDTO resultado = albumService.criar(albumDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(albumDTO.getTitulo(), resultado.getTitulo());
        verify(artistaRepository, times(1)).findAllById(List.of(1L));
        verify(albumRepository, times(1)).save(any(Album.class));
        verify(webSocketService, times(1)).notificarNovoAlbum(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar álbum com artista inexistente")
    void deveLancarExcecaoAoCriarAlbumComArtistaInexistente() {
        // Arrange
        // OTIMIZADO: findAllById retorna lista vazia quando artista não existe
        when(artistaRepository.findAllById(List.of(999L))).thenReturn(Collections.emptyList());

        AlbumDTO dtoComArtistaInexistente = AlbumDTO.builder()
                .titulo("Teste")
                .artistasIds(List.of(999L))
                .build();

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> albumService.criar(dtoComArtistaInexistente));
        verify(artistaRepository, times(1)).findAllById(List.of(999L));
        verify(albumRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar álbum com sucesso")
    void deveAtualizarAlbumComSucesso() {
        // Arrange
        AlbumDTO dtoAtualizado = AlbumDTO.builder()
                .titulo("Harakiri (Remastered)")
                .anoLancamento(2012)
                .descricao("Versão remasterizada")
                .artistasIds(List.of(1L))
                .build();

        Album albumAtualizado = Album.builder()
                .id(1L)
                .titulo("Harakiri (Remastered)")
                .anoLancamento(2012)
                .descricao("Versão remasterizada")
                .artistas(new HashSet<>(Set.of(artista)))
                .imagensCapa(new HashSet<>())
                .build();

        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));
        // OTIMIZADO: Agora usa findAllById para buscar artistas em batch
        when(artistaRepository.findAllById(List.of(1L))).thenReturn(List.of(artista));
        when(albumRepository.save(any(Album.class))).thenReturn(albumAtualizado);

        // Act
        AlbumDTO resultado = albumService.atualizar(1L, dtoAtualizado);

        // Assert
        assertNotNull(resultado);
        assertEquals("Harakiri (Remastered)", resultado.getTitulo());
        verify(albumRepository, times(1)).findById(1L);
        verify(albumRepository, times(1)).save(any(Album.class));
        verify(webSocketService, times(1)).notificarAlbumAtualizado(any());
    }

    @Test
    @DisplayName("Deve deletar álbum com sucesso")
    void deveDeletarAlbumComSucesso() {
        // Arrange
        Long id = 1L;
        when(albumRepository.existsById(id)).thenReturn(true);
        doNothing().when(albumRepository).deleteById(id);

        // Act
        albumService.deletar(id);

        // Assert
        verify(albumRepository, times(1)).existsById(id);
        verify(albumRepository, times(1)).deleteById(id);
        verify(webSocketService, times(1)).notificarAlbumRemovido(id);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar álbum inexistente")
    void deveLancarExcecaoAoDeletarAlbumInexistente() {
        // Arrange
        Long id = 999L;
        when(albumRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> albumService.deletar(id));
        verify(albumRepository, times(1)).existsById(id);
        verify(albumRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve fazer upload de imagens com sucesso")
    void deveFazerUploadDeImagensComSucesso() {
        // Arrange
        Long albumId = 1L;
        MultipartFile file1 = mock(MultipartFile.class);
        MultipartFile file2 = mock(MultipartFile.class);
        List<MultipartFile> files = List.of(file1, file2);

        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(file1.getContentType()).thenReturn("image/jpeg");
        when(file1.getOriginalFilename()).thenReturn("capa1.jpg");
        when(file1.getSize()).thenReturn(1024L);
        when(file2.getContentType()).thenReturn("image/png");
        when(file2.getOriginalFilename()).thenReturn("capa2.png");
        when(file2.getSize()).thenReturn(2048L);

        when(minIOService.uploadFile(any())).thenReturn("uuid1.jpg", "uuid2.png");
        when(minIOService.getPresignedUrl(anyString())).thenReturn("http://minio/url1", "http://minio/url2");

        ImagemCapa imagem1 = ImagemCapa.builder()
                .id(1L)
                .album(album)
                .nomeArquivo("capa1.jpg")
                .caminhoMinIO("uuid1.jpg")
                .contentType("image/jpeg")
                .tamanho(1024L)
                .build();

        ImagemCapa imagem2 = ImagemCapa.builder()
                .id(2L)
                .album(album)
                .nomeArquivo("capa2.png")
                .caminhoMinIO("uuid2.png")
                .contentType("image/png")
                .tamanho(2048L)
                .build();

        when(imagemCapaRepository.save(any(ImagemCapa.class))).thenReturn(imagem1, imagem2);

        // Act
        var resultado = albumService.uploadImagens(albumId, files);

        // Assert
        assertNotNull(resultado);
        assertEquals(albumId, resultado.getAlbumId());
        assertEquals(2, resultado.getTotalImagens());
        assertEquals(2, resultado.getImagens().size());
        verify(minIOService, times(2)).uploadFile(any());
        verify(minIOService, times(2)).getPresignedUrl(anyString());
        verify(imagemCapaRepository, times(2)).save(any(ImagemCapa.class));
    }

    @Test
    @DisplayName("Deve ignorar arquivos que não são imagens")
    void deveIgnorarArquivosQueNaoSaoImagens() {
        // Arrange
        Long albumId = 1L;
        MultipartFile fileImagem = mock(MultipartFile.class);
        MultipartFile fileTexto = mock(MultipartFile.class);
        List<MultipartFile> files = List.of(fileImagem, fileTexto);

        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(fileImagem.getContentType()).thenReturn("image/jpeg");
        when(fileImagem.getOriginalFilename()).thenReturn("capa.jpg");
        when(fileImagem.getSize()).thenReturn(1024L);
        when(fileTexto.getContentType()).thenReturn("text/plain");
        when(fileTexto.getOriginalFilename()).thenReturn("documento.txt");

        when(minIOService.uploadFile(fileImagem)).thenReturn("uuid.jpg");
        when(minIOService.getPresignedUrl("uuid.jpg")).thenReturn("http://minio/url");

        ImagemCapa imagem = ImagemCapa.builder()
                .id(1L)
                .album(album)
                .nomeArquivo("capa.jpg")
                .caminhoMinIO("uuid.jpg")
                .contentType("image/jpeg")
                .tamanho(1024L)
                .build();

        when(imagemCapaRepository.save(any(ImagemCapa.class))).thenReturn(imagem);

        // Act
        var resultado = albumService.uploadImagens(albumId, files);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalImagens()); // Apenas a imagem foi processada
        verify(minIOService, times(1)).uploadFile(fileImagem);
        verify(minIOService, never()).uploadFile(fileTexto);
    }

    @Test
    @DisplayName("Deve lançar exceção ao fazer upload em álbum inexistente")
    void deveLancarExcecaoAoFazerUploadEmAlbumInexistente() {
        // Arrange
        Long albumId = 999L;
        MultipartFile file = mock(MultipartFile.class);
        List<MultipartFile> files = List.of(file);

        when(albumRepository.findById(albumId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> albumService.uploadImagens(albumId, files));
        verify(albumRepository, times(1)).findById(albumId);
        verify(minIOService, never()).uploadFile(any());
    }
}
