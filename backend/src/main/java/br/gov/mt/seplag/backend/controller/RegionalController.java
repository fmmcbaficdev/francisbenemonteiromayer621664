package br.gov.mt.seplag.backend.controller;

import br.gov.mt.seplag.backend.dto.RegionalDTO;
import br.gov.mt.seplag.backend.service.RegionalSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller REST para Regionais
 */
@RestController
@RequestMapping("/v1/regionais")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Regionais", description = "Gerenciamento de Regionais da Polícia Civil")
@SecurityRequirement(name = "Bearer Authentication")
public class RegionalController {

    private final br.gov.mt.seplag.backend.service.RegionalService regionalService;
    private final RegionalSyncService regionalSyncService;

    /**
     * GET /v1/regionais
     * Listar todas as regionais com paginação
     */
    @GetMapping
    @Operation(summary = "Listar regionais", description = "Lista todas as regionais com paginação")
    public ResponseEntity<Page<RegionalDTO>> listar(
            @PageableDefault(size = 10, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(required = false) Boolean ativa
    ) {
        log.debug("GET /v1/regionais - página: {}, ativa: {}", pageable.getPageNumber(), ativa);
        Page<RegionalDTO> regionais = regionalService.listar(pageable, ativa);
        return ResponseEntity.ok(regionais);
    }

    /**
     * GET /v1/regionais/{id}
     * Buscar regional por ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID", description = "Busca regional por ID")
    public ResponseEntity<RegionalDTO> buscarPorId(@PathVariable Long id) {
        log.debug("GET /v1/regionais/{}", id);
        RegionalDTO regional = regionalService.buscarPorId(id);
        return ResponseEntity.ok(regional);
    }

    /**
     * GET /v1/regionais/codigo-externo/{codigoExterno}
     * Buscar regional por código externo
     */
    @GetMapping("/codigo-externo/{codigoExterno}")
    @Operation(summary = "Buscar por código externo", description = "Busca regional por código externo da API")
    public ResponseEntity<RegionalDTO> buscarPorCodigoExterno(@PathVariable Integer codigoExterno) {
        log.debug("GET /v1/regionais/codigo-externo/{}", codigoExterno);
        RegionalDTO regional = regionalService.buscarPorCodigoExterno(codigoExterno);
        return ResponseEntity.ok(regional);
    }

    /**
     * POST /v1/regionais/sincronizar
     * Sincronizar regionais com API externa manualmente
     */
    @PostMapping("/sincronizar")
    @Operation(
            summary = "Sincronizar regionais",
            description = "Sincroniza regionais com a API externa da Polícia Civil. " +
                    "Algoritmo O(n) com complexidade linear."
    )
    public ResponseEntity<Map<String, Object>> sincronizar() {
        log.info("POST /v1/regionais/sincronizar - Iniciando sincronização manual");
        Map<String, Object> resultado = regionalSyncService.sincronizar();
        return ResponseEntity.ok(resultado);
    }
}
