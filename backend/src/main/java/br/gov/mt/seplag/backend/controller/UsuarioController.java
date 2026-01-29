package br.gov.mt.seplag.backend.controller;

import br.gov.mt.seplag.backend.dto.UsuarioDTO;
import br.gov.mt.seplag.backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller REST para Usuários
 */
@RestController
@RequestMapping("/v1/usuarios")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Usuários", description = "Gerenciamento de Usuários")
@SecurityRequirement(name = "Bearer Authentication")
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * GET /v1/usuarios
     * Listar todos os usuários
     */
    @GetMapping
    @Operation(summary = "Listar usuários", description = "Lista todos os usuários cadastrados")
    public ResponseEntity<List<UsuarioDTO>> listar() {
        log.debug("GET /v1/usuarios");
        List<UsuarioDTO> usuarios = usuarioService.listar();
        return ResponseEntity.ok(usuarios);
    }

    /**
     * GET /v1/usuarios/{id}
     * Buscar usuário por ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID", description = "Busca usuário por ID")
    public ResponseEntity<UsuarioDTO> buscarPorId(@PathVariable Long id) {
        log.debug("GET /v1/usuarios/{}", id);
        UsuarioDTO usuario = usuarioService.buscarPorId(id);
        return ResponseEntity.ok(usuario);
    }

    /**
     * GET /v1/usuarios/username/{username}
     * Buscar usuário por username
     */
    @GetMapping("/username/{username}")
    @Operation(summary = "Buscar por username", description = "Busca usuário por username")
    public ResponseEntity<UsuarioDTO> buscarPorUsername(@PathVariable String username) {
        log.debug("GET /v1/usuarios/username/{}", username);
        UsuarioDTO usuario = usuarioService.buscarPorUsername(username);
        return ResponseEntity.ok(usuario);
    }

    /**
     * POST /v1/usuarios
     * Criar novo usuário
     */
    @PostMapping
    @Operation(summary = "Criar usuário", description = "Cria um novo usuário")
    public ResponseEntity<UsuarioDTO> criar(@Valid @RequestBody UsuarioDTO dto) {
        log.info("POST /v1/usuarios - {}", dto.getUsername());

        // Validação de senha obrigatória na criação
        // Nota: Idealmente isso deveria ser feito com Bean Validation (@NotBlank no DTO)
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new br.gov.mt.seplag.backend.exception.ValidationException("Senha é obrigatória para criar usuário");
        }

        UsuarioDTO criado = usuarioService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    /**
     * PUT /v1/usuarios/{id}
     * Atualizar usuário
     */
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuário", description = "Atualiza dados do usuário (não atualiza senha)")
    public ResponseEntity<UsuarioDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioDTO dto
    ) {
        log.info("PUT /v1/usuarios/{}", id);
        UsuarioDTO atualizado = usuarioService.atualizar(id, dto);
        return ResponseEntity.ok(atualizado);
    }

    /**
     * PATCH /v1/usuarios/{id}/senha
     * Atualizar apenas a senha do usuário
     */
    @PatchMapping("/{id}/senha")
    @Operation(summary = "Atualizar senha", description = "Atualiza apenas a senha do usuário")
    public ResponseEntity<Map<String, String>> atualizarSenha(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload
    ) {
        log.info("PATCH /v1/usuarios/{}/senha", id);

        String novaSenha = payload.get("novaSenha");
        if (novaSenha == null || novaSenha.isBlank()) {
            throw new br.gov.mt.seplag.backend.exception.ValidationException("Nova senha é obrigatória");
        }

        if (novaSenha.length() < 6) {
            throw new br.gov.mt.seplag.backend.exception.ValidationException("Senha deve ter no mínimo 6 caracteres");
        }

        usuarioService.atualizarSenha(id, novaSenha);

        return ResponseEntity.ok(Map.of("mensagem", "Senha atualizada com sucesso"));
    }

    /**
     * DELETE /v1/usuarios/{id}
     * Deletar usuário
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar usuário", description = "Deleta usuário por ID")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        log.info("DELETE /v1/usuarios/{}", id);
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /v1/usuarios/check/{username}
     * Verificar se username existe
     */
    @GetMapping("/check/{username}")
    @Operation(summary = "Verificar username", description = "Verifica se username já existe")
    public ResponseEntity<Map<String, Boolean>> verificarUsername(@PathVariable String username) {
        log.debug("GET /v1/usuarios/check/{}", username);
        boolean existe = usuarioService.usernameExiste(username);
        return ResponseEntity.ok(Map.of("existe", existe));
    }
}
