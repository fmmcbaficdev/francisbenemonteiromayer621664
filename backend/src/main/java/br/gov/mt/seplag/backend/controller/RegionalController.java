package br.gov.mt.seplag.backend.controller;

import br.gov.mt.seplag.backend.dto.RegionalDTO;
import br.gov.mt.seplag.backend.dto.SyncResultDTO;
import br.gov.mt.seplag.backend.service.RegionalService;
import br.gov.mt.seplag.backend.service.RegionalSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/regionais")
@RequiredArgsConstructor
@Tag(name = "Regionais", description = "Gerenciamento de regionais e sincronização")
public class RegionalController {

    private final RegionalService regionalService;
    private final RegionalSyncService regionalSyncService;

    @GetMapping
    @Operation(summary = "Listar regionais", description = "Lista todas as regionais com paginação e filtro")
    public ResponseEntity<Page<RegionalDTO>> listar(
            @Parameter(description = "Filtrar por status ativo")
            @RequestParam(required = false) Boolean ativa,
            @PageableDefault(size = 10, sort = "nome") Pageable pageable
    ) {
        return ResponseEntity.ok(regionalService.listar(pageable, ativa));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar regional por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Regional encontrada"),
            @ApiResponse(responseCode = "404", description = "Regional não encontrada")
    })
    public ResponseEntity<RegionalDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(regionalService.buscarPorId(id));
    }

    @GetMapping("/codigo/{codigoExterno}")
    @Operation(summary = "Buscar regional por código externo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Regional encontrada"),
            @ApiResponse(responseCode = "404", description = "Regional não encontrada")
    })
    public ResponseEntity<RegionalDTO> buscarPorCodigoExterno(@PathVariable Integer codigoExterno) {
        return ResponseEntity.ok(regionalService.buscarPorCodigoExterno(codigoExterno));
    }

    @PostMapping("/sync")
    @Operation(
            summary = "Sincronizar regionais",
            description = "Sincroniza regionais com a API externa usando algoritmo O(n)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sincronização concluída")
    })
    public ResponseEntity<SyncResultDTO> sincronizar() {
        SyncResultDTO resultado = regionalSyncService.sincronizar();
        return ResponseEntity.ok(resultado);
    }
}