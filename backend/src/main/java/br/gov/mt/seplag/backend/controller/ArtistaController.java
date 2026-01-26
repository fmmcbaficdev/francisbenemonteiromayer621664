package br.gov.mt.seplag.backend.controller;

import br.gov.mt.seplag.backend.dto.ArtistaDTO;
import br.gov.mt.seplag.backend.service.ArtistaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/artistas")
@RequiredArgsConstructor
@Tag(name = "Artistas", description = "Gerenciamento de artistas")
public class ArtistaController {

    private final ArtistaService artistaService;

    @GetMapping
    @Operation(summary = "Listar artistas", description = "Lista todos os artistas com paginação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    public ResponseEntity<Page<ArtistaDTO>> listar(
            @Parameter(description = "Filtro por nome")
            @RequestParam(required = false) String nome,
            @PageableDefault(size = 10, sort = "nome") Pageable pageable
    ) {
        Page<ArtistaDTO> artistas = nome != null
                ? artistaService.buscarPorNome(nome, pageable)
                : artistaService.listar(pageable);
        return ResponseEntity.ok(artistas);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar artista por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Artista encontrado"),
            @ApiResponse(responseCode = "404", description = "Artista não encontrado")
    })
    public ResponseEntity<ArtistaDTO> buscarPorId(
            @Parameter(description = "ID do artista")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(artistaService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Criar artista")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Artista criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<ArtistaDTO> criar(
            @Valid @RequestBody ArtistaDTO dto
    ) {
        ArtistaDTO criado = artistaService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar artista")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Artista atualizado"),
            @ApiResponse(responseCode = "404", description = "Artista não encontrado")
    })
    public ResponseEntity<ArtistaDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ArtistaDTO dto
    ) {
        return ResponseEntity.ok(artistaService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar artista")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Artista deletado"),
            @ApiResponse(responseCode = "404", description = "Artista não encontrado")
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        artistaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
