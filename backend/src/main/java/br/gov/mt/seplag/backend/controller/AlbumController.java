package br.gov.mt.seplag.backend.controller;

import br.gov.mt.seplag.backend.dto.AlbumDTO;
import br.gov.mt.seplag.backend.dto.UploadImagemResponseDTO;
import br.gov.mt.seplag.backend.service.AlbumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controller REST para Álbuns
 */
@RestController
@RequestMapping("/v1/albuns")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Álbuns", description = "CRUD de Álbuns")
@SecurityRequirement(name = "Bearer Authentication")
public class AlbumController {

    private final AlbumService albumService;

    /**
     * GET /v1/albuns
     */
    @GetMapping
    @Operation(summary = "Listar álbuns", description = "Retorna lista paginada de álbuns. Use page=0, size=10, sort=id ou sort=titulo")
    public ResponseEntity<Page<AlbumDTO>> listar(
            @ParameterObject @PageableDefault(size = 10, sort = "titulo", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        log.debug("GET /v1/albuns");
        Page<AlbumDTO> albuns = albumService.listar(pageable);
        return ResponseEntity.ok(albuns);
    }

    /**
     * GET /v1/albuns/buscar?titulo=xxx
     */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar por título")
    public ResponseEntity<Page<AlbumDTO>> buscarPorTitulo(
            @RequestParam String titulo,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable
    ) {
        log.debug("GET /v1/albuns/buscar?titulo={}", titulo);
        Page<AlbumDTO> albuns = albumService.buscarPorTitulo(titulo, pageable);
        return ResponseEntity.ok(albuns);
    }

    /**
     * GET /v1/albuns/artista/{artistaId}
     */
    @GetMapping("/artista/{artistaId}")
    @Operation(summary = "Listar álbuns de um artista")
    public ResponseEntity<Page<AlbumDTO>> buscarPorArtista(
            @PathVariable Long artistaId,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable
    ) {
        log.debug("GET /v1/albuns/artista/{}", artistaId);
        Page<AlbumDTO> albuns = albumService.buscarPorArtista(artistaId, pageable);
        return ResponseEntity.ok(albuns);
    }

    /**
     * GET /v1/albuns/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID")
    public ResponseEntity<AlbumDTO> buscarPorId(@PathVariable Long id) {
        log.debug("GET /v1/albuns/{}", id);
        AlbumDTO album = albumService.buscarPorId(id);
        return ResponseEntity.ok(album);
    }

    /**
     * POST /v1/albuns
     */
    @PostMapping
    @Operation(summary = "Criar álbum")
    public ResponseEntity<AlbumDTO> criar(@Valid @RequestBody AlbumDTO dto) {
        log.info("POST /v1/albuns - {}", dto.getTitulo());
        AlbumDTO criado = albumService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    /**
     * PUT /v1/albuns/{id}
     */
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar álbum")
    public ResponseEntity<AlbumDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AlbumDTO dto
    ) {
        log.info("PUT /v1/albuns/{}", id);
        AlbumDTO atualizado = albumService.atualizar(id, dto);
        return ResponseEntity.ok(atualizado);
    }

    /**
     * DELETE /v1/albuns/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar álbum")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        log.info("DELETE /v1/albuns/{}", id);
        albumService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /v1/albuns/{albumId}/imagens
     * Upload de uma ou mais imagens de capa para um álbum
     */
    @PostMapping(value = "/{albumId}/imagens", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload de imagens de capa", description = "Faz upload de uma ou mais imagens de capa para um álbum")
    public ResponseEntity<UploadImagemResponseDTO> uploadImagens(
            @PathVariable Long albumId,
            @RequestParam("files") List<MultipartFile> files
    ) {
        log.info("POST /v1/albuns/{}/imagens - {} arquivo(s)", albumId, files.size());

        if (files == null || files.isEmpty()) {
            throw new RuntimeException("Nenhum arquivo enviado");
        }

        UploadImagemResponseDTO response = albumService.uploadImagens(albumId, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * DELETE /v1/albuns/{albumId}/imagens/{imagemId}
     * Remove uma imagem de capa do álbum (MinIO + banco).
     */
    @DeleteMapping("/{albumId}/imagens/{imagemId}")
    @Operation(summary = "Remover imagem de capa", description = "Remove uma imagem do álbum (arquivo no MinIO e registro no banco)")
    public ResponseEntity<Void> deletarImagem(
            @PathVariable Long albumId,
            @PathVariable Long imagemId
    ) {
        log.info("DELETE /v1/albuns/{}/imagens/{}", albumId, imagemId);
        albumService.deletarImagem(albumId, imagemId);
        return ResponseEntity.noContent().build();
    }
}
