package br.gov.mt.seplag.backend.controller;

import br.gov.mt.seplag.backend.dto.ArtistaDTO;
import br.gov.mt.seplag.backend.service.ArtistaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para Artistas
 */
@RestController
@RequestMapping("/v1/artistas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Artistas", description = "CRUD de Artistas")
@SecurityRequirement(name = "Bearer Authentication")
public class ArtistaController {

    private final ArtistaService artistaService;

    /**
     * GET /v1/artistas
     * Listar todos os artistas com paginação
     */
    @GetMapping
    @Operation(summary = "Listar artistas", description = "Lista todos os artistas com paginação")
    public ResponseEntity<Page<ArtistaDTO>> listar(
            @PageableDefault(size = 10, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        log.debug("GET /v1/artistas - página: {}", pageable.getPageNumber());
        Page<ArtistaDTO> artistas = artistaService.listar(pageable);
        return ResponseEntity.ok(artistas);
    }

    /**
     * GET /v1/artistas/buscar?nome=xxx
     * Buscar artistas por nome
     */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar por nome", description = "Busca artistas por nome (case-insensitive)")
    public ResponseEntity<Page<ArtistaDTO>> buscarPorNome(
            @RequestParam String nome,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        log.debug("GET /v1/artistas/buscar?nome={}", nome);
        Page<ArtistaDTO> artistas = artistaService.buscarPorNome(nome, pageable);
        return ResponseEntity.ok(artistas);
    }

    /**
     * GET /v1/artistas/{id}
     * Buscar artista por ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID", description = "Busca artista por ID")
    public ResponseEntity<ArtistaDTO> buscarPorId(@PathVariable Long id) {
        log.debug("GET /v1/artistas/{}", id);
        ArtistaDTO artista = artistaService.buscarPorId(id);
        return ResponseEntity.ok(artista);
    }

    /**
     * POST /v1/artistas
     * Criar novo artista
     */
    @PostMapping
    @Operation(summary = "Criar artista", description = "Cria um novo artista")
    public ResponseEntity<ArtistaDTO> criar(@Valid @RequestBody ArtistaDTO dto) {
        log.info("POST /v1/artistas - {}", dto.getNome());
        ArtistaDTO criado = artistaService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    /**
     * PUT /v1/artistas/{id}
     * Atualizar artista
     */
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar artista", description = "Atualiza artista existente")
    public ResponseEntity<ArtistaDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ArtistaDTO dto
    ) {
        log.info("PUT /v1/artistas/{}", id);
        ArtistaDTO atualizado = artistaService.atualizar(id, dto);
        return ResponseEntity.ok(atualizado);
    }

    /**
     * DELETE /v1/artistas/{id}
     * Deletar artista
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar artista", description = "Deleta artista por ID")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        log.info("DELETE /v1/artistas/{}", id);
        artistaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
