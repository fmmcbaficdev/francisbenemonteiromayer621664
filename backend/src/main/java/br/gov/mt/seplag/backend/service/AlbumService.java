package br.gov.mt.seplag.backend.service;

import br.gov.mt.seplag.backend.dto.AlbumDTO;
import br.gov.mt.seplag.backend.dto.ArtistaDTO;
import br.gov.mt.seplag.backend.dto.UploadImagemResponse;
import br.gov.mt.seplag.backend.exception.EntityNotFoundException;
import br.gov.mt.seplag.backend.model.Album;
import br.gov.mt.seplag.backend.model.Artista;
import br.gov.mt.seplag.backend.model.ImagemCapa;
import br.gov.mt.seplag.backend.repository.AlbumRepository;
import br.gov.mt.seplag.backend.repository.ArtistaRepository;
import br.gov.mt.seplag.backend.repository.ImagemCapaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistaRepository artistaRepository;
    private final ImagemCapaRepository imagemCapaRepository;
    private final WebSocketNotificationService webSocketService;
    private final MinioService minioService;

    @Transactional(readOnly = true)
    public Page<AlbumDTO> listar(Pageable pageable) {
        log.debug("Listando álbuns - página: {}", pageable.getPageNumber());
        return albumRepository.findAll(pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AlbumDTO> buscarPorTitulo(String titulo, Pageable pageable) {
        log.debug("Buscando álbuns por título: {}", titulo);
        return albumRepository.findByTituloContainingIgnoreCase(titulo, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AlbumDTO> buscarPorArtista(Long artistaId, Pageable pageable) {
        log.debug("Buscando álbuns do artista ID: {}", artistaId);
        return albumRepository.findByArtistasId(artistaId, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public AlbumDTO buscarPorId(Long id) {
        log.debug("Buscando álbum por ID: {}", id);
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Álbum", id));  // ✅ SEM caminho completo
        return toDTO(album);
    }

    @Transactional
    public AlbumDTO criar(AlbumDTO dto) {
        log.info("Criando novo álbum: {}", dto.titulo());

        Set<Artista> artistas = buscarArtistas(dto.artistasIds());

        Album album = Album.builder()
                .titulo(dto.titulo())
                .anoLancamento(dto.anoLancamento())
                .descricao(dto.descricao())
                .artistas(artistas)
                .build();

        Album saved = albumRepository.save(album);
        log.info("Álbum criado com ID: {}", saved.getId());

        AlbumDTO resultado = toDTO(saved);
        webSocketService.notificarNovoAlbum(resultado);

        return resultado;
    }

    @Transactional
    public AlbumDTO atualizar(Long id, AlbumDTO dto) {
        log.info("Atualizando álbum ID: {}", id);

        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Álbum", id));  // ✅ OK

        album.setTitulo(dto.titulo());
        album.setAnoLancamento(dto.anoLancamento());
        album.setDescricao(dto.descricao());

        if (dto.artistasIds() != null) {
            Set<Artista> artistas = buscarArtistas(dto.artistasIds());
            album.setArtistas(artistas);
        }

        Album updated = albumRepository.save(album);
        log.info("Álbum atualizado: {}", updated.getId());

        AlbumDTO resultado = toDTO(updated);
        webSocketService.notificarAlbumAtualizado(resultado);

        return resultado;
    }

    @Transactional
    public void deletar(Long id) {
        log.info("Deletando álbum ID: {}", id);

        if (!albumRepository.existsById(id)) {
            throw new EntityNotFoundException("Álbum", id);  // ✅ OK
        }

        albumRepository.deleteById(id);
        log.info("Álbum deletado: {}", id);

        webSocketService.notificarAlbumRemovido(id);
    }

    @Transactional
    public UploadImagemResponse uploadImagens(Long albumId, List<MultipartFile> files) {
        log.info("Upload de {} imagem(ns) para álbum ID: {}", files.size(), albumId);

        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new EntityNotFoundException("Álbum", albumId));

        List<UploadImagemResponse.ImagemInfo> imagensInfo = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                if (!isImageFile(file)) {
                    log.warn("Arquivo ignorado (não é imagem): {}", file.getOriginalFilename());
                    continue;
                }

                String caminhoMinIO = minioService.uploadFile(file);

                ImagemCapa imagemCapa = ImagemCapa.builder()
                        .album(album)
                        .nomeArquivo(file.getOriginalFilename())
                        .caminhoMinIO(caminhoMinIO)
                        .contentType(file.getContentType())
                        .tamanho(file.getSize())
                        .build();

                ImagemCapa saved = imagemCapaRepository.save(imagemCapa);
                String urlPresigned = minioService.getPresignedUrl(caminhoMinIO);


                var info = new UploadImagemResponse.ImagemInfo(
                        saved.getId(),
                        saved.getNomeArquivo(),
                        urlPresigned,
                        saved.getContentType(),
                        saved.getTamanho()
                );

                imagensInfo.add(info);
                log.info("Imagem {} salva com sucesso", file.getOriginalFilename());

            } catch (Exception e) {
                log.error("Erro ao fazer upload da imagem: {}", file.getOriginalFilename(), e);
                throw new RuntimeException("Erro ao fazer upload: " + e.getMessage());
            }
        }

        return new UploadImagemResponse(
                albumId,
                String.format("%d imagem(ns) enviada(s) com sucesso", imagensInfo.size()),
                imagensInfo
        );
    }
    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    private Set<Artista> buscarArtistas(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }

        List<Artista> artistas = artistaRepository.findAllById(ids);

        if (artistas.size() != ids.size()) {
            Set<Long> encontrados = artistas.stream()
                    .map(Artista::getId)
                    .collect(Collectors.toSet());

            List<Long> naoEncontrados = ids.stream()
                    .filter(id -> !encontrados.contains(id))
                    .toList();

            throw new EntityNotFoundException("Artista(s) não encontrado(s): " + naoEncontrados);  // ✅ OK
        }

        return new HashSet<>(artistas);
    }

    private AlbumDTO toDTO(Album album) {
        List<ArtistaDTO> artistasDTO = album.getArtistas().stream()
                .map(a -> new ArtistaDTO(a.getId(), a.getNome(), null, null))
                .toList();

        List<String> imagensUrls = List.of();
        if (album.getImagensCapa() != null && !album.getImagensCapa().isEmpty()) {
            imagensUrls = album.getImagensCapa().stream()
                    .map(img -> minioService.getPresignedUrl(img.getCaminhoMinIO()))
                    .toList();
        }

        return new AlbumDTO(
                album.getId(),
                album.getTitulo(),
                album.getAnoLancamento(),
                album.getDescricao(),
                null,
                artistasDTO,
                imagensUrls
        );
    }
}