package br.gov.mt.seplag.backend.service;

import br.gov.mt.seplag.backend.dto.AlbumDTO;
import br.gov.mt.seplag.backend.dto.ArtistaDTO;
import br.gov.mt.seplag.backend.dto.UploadImagemResponseDTO;
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

/**
 * Service para lógica de negócio de Álbuns
 *
 * RECURSOS IMPLEMENTADOS:
 * - CRUD completo com paginação e busca
 * - Auditoria automática (created_by, updated_by via Spring Data Auditing)
 * - Optimistic Locking (version para controle de concorrência)
 * - Notificações em tempo real via WebSocket
 * - Upload de imagens para MinIO
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistaRepository artistaRepository;
    private final ImagemCapaRepository imagemCapaRepository;
    private final WebSocketNotificationService webSocketService;
    private final MinIOService minIOService;

    /**
     * Listar todos os álbuns
     */
    @Transactional(readOnly = true)
    public Page<AlbumDTO> listar(Pageable pageable) {
        log.debug("Listando álbuns - página: {}", pageable.getPageNumber());
        return albumRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    /**
     * Buscar álbuns por título
     */
    @Transactional(readOnly = true)
    public Page<AlbumDTO> buscarPorTitulo(String titulo, Pageable pageable) {
        log.debug("Buscando álbuns por título: {}", titulo);
        return albumRepository.findByTituloContainingIgnoreCase(titulo, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Buscar álbuns de um artista
     */
    @Transactional(readOnly = true)
    public Page<AlbumDTO> buscarPorArtista(Long artistaId, Pageable pageable) {
        log.debug("Buscando álbuns do artista ID: {}", artistaId);
        return albumRepository.findByArtistasId(artistaId, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Buscar álbum por ID
     */
    @Transactional(readOnly = true)
    public AlbumDTO buscarPorId(Long id) {
        log.debug("Buscando álbum por ID: {}", id);
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Álbum", id));
        return convertToDTO(album);
    }

    /**
     * Criar novo álbum
     */
    @Transactional
    public AlbumDTO criar(AlbumDTO dto) {
        log.info("Criando novo álbum: {}", dto.getTitulo());

        Set<Artista> artistas = buscarArtistas(dto.getArtistasIds());

        Album album = Album.builder()
                .titulo(dto.getTitulo())
                .anoLancamento(dto.getAnoLancamento())
                .descricao(dto.getDescricao())
                .artistas(artistas)
                .build();

        Album saved = albumRepository.save(album);
        log.info("Álbum criado com ID: {}", saved.getId());

        AlbumDTO resultado = convertToDTO(saved);

        // NOVO: Notificar via WebSocket
        webSocketService.notificarNovoAlbum(resultado);

        return resultado;
    }

    /**
     * Atualizar álbum
     *
     * OPTIMISTIC LOCKING:
     * - Se dto.version != null, usa para controle de concorrência
     * - Se outro usuário modificou o registro, JPA lança OptimisticLockException
     * - GlobalExceptionHandler converte para HTTP 409 Conflict
     *
     * AUDITORIA: updated_by e updated_at são preenchidos automaticamente
     */
    @Transactional
    public AlbumDTO atualizar(Long id, AlbumDTO dto) {
        log.info("Atualizando álbum ID: {}", id);

        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Álbum", id));

        // Optimistic Locking: se frontend enviou version, usar para validação
        if (dto.getVersion() != null) {
            album.setVersion(dto.getVersion());
        }

        album.setTitulo(dto.getTitulo());
        album.setAnoLancamento(dto.getAnoLancamento());
        album.setDescricao(dto.getDescricao());

        if (dto.getArtistasIds() != null) {
            Set<Artista> artistas = buscarArtistas(dto.getArtistasIds());
            album.setArtistas(artistas);
        }

        Album updated = albumRepository.save(album);
        log.info("Álbum atualizado: {} por usuário: {}", updated.getId(), updated.getUpdatedBy());

        AlbumDTO resultado = convertToDTO(updated);

        // Notificar via WebSocket
        webSocketService.notificarAlbumAtualizado(resultado);

        return resultado;
    }

    /**
     * Deletar álbum
     */
    @Transactional
    public void deletar(Long id) {
        log.info("Deletando álbum ID: {}", id);

        if (!albumRepository.existsById(id)) {
            throw new EntityNotFoundException("Álbum", id);
        }

        albumRepository.deleteById(id);
        log.info("Álbum deletado: {}", id);

        // NOVO: Notificar via WebSocket
        webSocketService.notificarAlbumRemovido(id);
    }

    /**
     * Buscar artistas por IDs
     * OTIMIZADO: Usa findAllById para uma única query ao invés de N queries
     */
    private Set<Artista> buscarArtistas(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }

        // Uma única query para buscar todos os artistas - O(1) ao invés de O(n)
        List<Artista> artistas = artistaRepository.findAllById(ids);

        // Validar se todos os IDs foram encontrados
        if (artistas.size() != ids.size()) {
            Set<Long> encontrados = artistas.stream()
                    .map(Artista::getId)
                    .collect(Collectors.toSet());

            List<Long> naoEncontrados = ids.stream()
                    .filter(id -> !encontrados.contains(id))
                    .toList();

            throw new EntityNotFoundException(
                    "Artista(s) não encontrado(s): " + naoEncontrados
            );
        }

        return new HashSet<>(artistas);
    }

    /**
     * Upload de imagens de capa para um álbum
     */
    @Transactional
    public UploadImagemResponseDTO uploadImagens(Long albumId, List<MultipartFile> files) {
        log.info("Upload de {} imagem(ns) para álbum ID: {}", files.size(), albumId);

        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new EntityNotFoundException("Álbum", albumId));

        List<UploadImagemResponseDTO.ImagemInfoDTO> imagensInfo = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                // Validar tipo de arquivo
                if (!isImageFile(file)) {
                    log.warn("Arquivo ignorado (não é imagem): {}", file.getOriginalFilename());
                    continue;
                }

                // Verificar se já existe imagem com mesmo nome - deletar antiga
                imagemCapaRepository.findByAlbumIdAndNomeArquivo(albumId, file.getOriginalFilename())
                        .ifPresent(existing -> {
                            log.info("Substituindo imagem existente: {}", existing.getNomeArquivo());
                            try {
                                minIOService.deleteFile(existing.getCaminhoMinIO());
                            } catch (Exception e) {
                                log.warn("Não foi possível deletar arquivo antigo do MinIO: {}", e.getMessage());
                            }
                            imagemCapaRepository.delete(existing);
                            imagemCapaRepository.flush(); // Forçar DELETE antes do INSERT
                        });

                // Upload para MinIO
                String caminhoMinIO = minIOService.uploadFile(file);

                // Salvar registro no banco
                ImagemCapa imagemCapa = ImagemCapa.builder()
                        .album(album)
                        .nomeArquivo(file.getOriginalFilename())
                        .caminhoMinIO(caminhoMinIO)
                        .contentType(file.getContentType())
                        .tamanho(file.getSize())
                        .build();

                ImagemCapa saved = imagemCapaRepository.save(imagemCapa);

                // Gerar URL presigned
                String urlPresigned = minIOService.getPresignedUrl(caminhoMinIO);

                UploadImagemResponseDTO.ImagemInfoDTO info = UploadImagemResponseDTO.ImagemInfoDTO.builder()
                        .id(saved.getId())
                        .nomeArquivo(saved.getNomeArquivo())
                        .urlPresigned(urlPresigned)
                        .contentType(saved.getContentType())
                        .tamanho(saved.getTamanho())
                        .build();

                imagensInfo.add(info);
                log.info("Imagem {} salva com sucesso", file.getOriginalFilename());

            } catch (Exception e) {
                log.error("Erro ao fazer upload da imagem: {}", file.getOriginalFilename(), e);
                throw new RuntimeException("Erro ao fazer upload da imagem " + file.getOriginalFilename() + ": " + e.getMessage());
            }
        }

        return UploadImagemResponseDTO.builder()
                .albumId(albumId)
                .mensagem(String.format("%d imagem(ns) enviada(s) com sucesso", imagensInfo.size()))
                .imagens(imagensInfo)
                .totalImagens(imagensInfo.size())
                .build();
    }

    /**
     * Validar se arquivo é uma imagem
     */
    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    /**
     * Converter Entity para DTO incluindo campos de auditoria
     */
    private AlbumDTO convertToDTO(Album album) {
        List<ArtistaDTO> artistasDTO = album.getArtistas().stream()
                .map(a -> ArtistaDTO.builder()
                        .id(a.getId())
                        .nome(a.getNome())
                        .build())
                .collect(Collectors.toList());

        // Buscar imagens do álbum e gerar URLs presigned
        List<String> imagensUrls = new ArrayList<>();
        if (album.getImagensCapa() != null && !album.getImagensCapa().isEmpty()) {
            imagensUrls = album.getImagensCapa().stream()
                    .map(img -> minIOService.getPresignedUrl(img.getCaminhoMinIO()))
                    .collect(Collectors.toList());
        }

        return AlbumDTO.builder()
                .id(album.getId())
                .titulo(album.getTitulo())
                .anoLancamento(album.getAnoLancamento())
                .descricao(album.getDescricao())
                .artistas(artistasDTO)
                .imagensUrls(imagensUrls)
                // Auditoria
                .createdAt(album.getCreatedAt())
                .createdBy(album.getCreatedBy())
                .updatedAt(album.getUpdatedAt())
                .updatedBy(album.getUpdatedBy())
                // Optimistic Locking
                .version(album.getVersion())
                .build();
    }
}