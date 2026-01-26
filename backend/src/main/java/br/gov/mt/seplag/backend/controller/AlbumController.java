package br.gov.mt.seplag.backend.controller;

import br.gov.mt.seplag.backend.dto.AlbumDTO;
import br.gov.mt.seplag.backend.dto.UploadImagemResponse;
import br.gov.mt.seplag.backend.service.AlbumService;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/albuns")
@RequiredArgsConstructor
@Tag(name = "Álbuns", description = "Gerenciamento de álbuns")
public class AlbumController {

    private final AlbumService albumService;

    @GetMapping
    @Operation(summary = "Listar álbuns", description = "Lista todos os álbuns com paginação")
    public ResponseEntity<Page<AlbumDTO>> listar(
            @Parameter(description = "Filtro por título")
            @RequestParam(required = false) String titulo,
            @Parameter(description = "Filtro por artista")
            @RequestParam(required = false) Long artistaId,
            @PageableDefault(size = 10, sort = "titulo") Pageable pageable
    ) {
        Page<AlbumDTO> albuns;

        if (artistaId != null) {
            albuns = albumService.buscarPorArtista(artistaId, pageable);
        } else if (titulo != null) {
            albuns = albumService.buscarPorTitulo(titulo, pageable);
        } else {
            albuns = albumService.listar(pageable);
        }

        return ResponseEntity.ok(albuns);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar álbum por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Álbum encontrado"),
            @ApiResponse(responseCode = "404", description = "Álbum não encontrado")
    })
    public ResponseEntity<AlbumDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(albumService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Criar álbum")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Álbum criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<AlbumDTO> criar(@Valid @RequestBody AlbumDTO dto) {
        AlbumDTO criado = albumService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar álbum")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Álbum atualizado"),
            @ApiResponse(responseCode = "404", description = "Álbum não encontrado")
    })
    public ResponseEntity<AlbumDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AlbumDTO dto
    ) {
        return ResponseEntity.ok(albumService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar álbum")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Álbum deletado"),
            @ApiResponse(responseCode = "404", description = "Álbum não encontrado")
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        albumService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/imagens", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload de imagens de capa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Imagens enviadas com sucesso"),
            @ApiResponse(responseCode = "404", description = "Álbum não encontrado")
    })
    public ResponseEntity<UploadImagemResponse> uploadImagens(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files
    ) {
        return ResponseEntity.ok(albumService.uploadImagens(id, files));
    }
}
