package br.gov.mt.seplag.backend.service;


import br.gov.mt.seplag.backend.dto.PasswordUpdateRequest;
import br.gov.mt.seplag.backend.dto.UsuarioCreateRequest;
import br.gov.mt.seplag.backend.dto.UsuarioResponse;
import br.gov.mt.seplag.backend.dto.UsuarioUpdateRequest;
import br.gov.mt.seplag.backend.exception.EntityNotFoundException;
import br.gov.mt.seplag.backend.exception.ValidationException;
import br.gov.mt.seplag.backend.model.Usuario;
import br.gov.mt.seplag.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para lógica de negócio de Usuários
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Listar todos os usuários
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        log.debug("Listando todos os usuários");
        return usuarioRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Buscar usuário por ID
     */
    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        log.debug("Buscando usuário por ID: {}", id);
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário", id));
        return toResponse(usuario);
    }

    /**
     * Buscar usuário por username
     */
    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorUsername(String username) {
        log.debug("Buscando usuário por username: {}", username);
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuário", username));
        return toResponse(usuario);
    }

    /**
     * Criar novo usuário
     */
    @Transactional
    public UsuarioResponse criar(UsuarioCreateRequest request) {
        log.info("Criando novo usuário: {}", request.username());

        // Verificar se username já existe
        if (usuarioRepository.findByUsername(request.username()).isPresent()) {
            throw new ValidationException("Username já existe: " + request.username());
        }

        // Encriptar senha
        String senhaEncriptada = passwordEncoder.encode(request.password());

        Usuario usuario = Usuario.builder()
                .username(request.username())
                .password(senhaEncriptada)
                .nome(request.nome())
                .build();

        Usuario saved = usuarioRepository.save(usuario);
        log.info("Usuário criado com ID: {}", saved.getId());

        return toResponse(saved);
    }

    /**
     * Atualizar usuário existente (sem alterar senha)
     */
    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioUpdateRequest request) {
        log.info("Atualizando usuário ID: {}", id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário", id));

        // Atualizar nome
        usuario.setNome(request.nome());

        // Se mudou username, verificar se novo username já existe
        if (!usuario.getUsername().equals(request.username())) {
            if (usuarioRepository.findByUsername(request.username()).isPresent()) {
                throw new ValidationException("Username já existe: " + request.username());
            }
            usuario.setUsername(request.username());
        }

        Usuario updated = usuarioRepository.save(usuario);
        log.info("Usuário atualizado: {}", updated.getId());

        return toResponse(updated);
    }

    /**
     * Atualizar senha do usuário
     */
    @Transactional
    public void atualizarSenha(Long id, PasswordUpdateRequest request) {
        log.info("Atualizando senha do usuário ID: {}", id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário", id));

        String senhaEncriptada = passwordEncoder.encode(request.novaSenha());
        usuario.setPassword(senhaEncriptada);

        usuarioRepository.save(usuario);
        log.info("Senha atualizada para usuário: {}", usuario.getUsername());
    }

    /**
     * Deletar usuário
     */
    @Transactional
    public void deletar(Long id) {
        log.info("Deletando usuário ID: {}", id);

        if (!usuarioRepository.existsById(id)) {
            throw new EntityNotFoundException("Usuário", id);
        }

        usuarioRepository.deleteById(id);
        log.info("Usuário deletado: {}", id);
    }

    /**
     * Verificar se username existe
     */
    @Transactional(readOnly = true)
    public boolean usernameExiste(String username) {
        return usuarioRepository.findByUsername(username).isPresent();
    }

    /**
     * Converter Entity para Response (nunca expõe password)
     */
    private UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNome()
        );
    }
}
