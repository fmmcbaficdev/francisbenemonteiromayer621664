package br.gov.mt.seplag.backend.service;

import br.gov.mt.seplag.backend.dto.UsuarioDTO;
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
import java.util.stream.Collectors;

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
    public List<UsuarioDTO> listar() {
        log.debug("Listando todos os usuários");
        return usuarioRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Buscar usuário por ID
     */
    @Transactional(readOnly = true)
    public UsuarioDTO buscarPorId(Long id) {
        log.debug("Buscando usuário por ID: {}", id);
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário", id));
        return convertToDTO(usuario);
    }

    /**
     * Buscar usuário por username
     */
    @Transactional(readOnly = true)
    public UsuarioDTO buscarPorUsername(String username) {
        log.debug("Buscando usuário por username: {}", username);
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com username: " + username));
        return convertToDTO(usuario);
    }

    /**
     * Criar novo usuário
     */
    @Transactional
    public UsuarioDTO criar(UsuarioDTO dto) {
        log.info("Criando novo usuário: {}", dto.getUsername());

        // Verificar se username já existe
        if (usuarioRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new ValidationException("Username já existe: " + dto.getUsername());
        }

        // Encriptar senha
        String senhaEncriptada = passwordEncoder.encode(dto.getPassword());

        Usuario usuario = Usuario.builder()
                .username(dto.getUsername())
                .password(senhaEncriptada)
                .nome(dto.getNome())
                .build();

        Usuario saved = usuarioRepository.save(usuario);
        log.info("Usuário criado com ID: {}", saved.getId());

        return convertToDTO(saved);
    }

    /**
     * Atualizar usuário existente
     */
    @Transactional
    public UsuarioDTO atualizar(Long id, UsuarioDTO dto) {
        log.info("Atualizando usuário ID: {}", id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário", id));

        // Atualizar campos (não atualiza password aqui)
        usuario.setNome(dto.getNome());

        // Se mudou username, verificar se novo username já existe
        if (!usuario.getUsername().equals(dto.getUsername())) {
            if (usuarioRepository.findByUsername(dto.getUsername()).isPresent()) {
                throw new ValidationException("Username já existe: " + dto.getUsername());
            }
            usuario.setUsername(dto.getUsername());
        }

        Usuario updated = usuarioRepository.save(usuario);
        log.info("Usuário atualizado: {}", updated.getId());

        return convertToDTO(updated);
    }

    /**
     * Atualizar senha do usuário
     */
    @Transactional
    public void atualizarSenha(Long id, String novaSenha) {
        log.info("Atualizando senha do usuário ID: {}", id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário", id));

        String senhaEncriptada = passwordEncoder.encode(novaSenha);
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
     * Converter Entity para DTO
     */
    private UsuarioDTO convertToDTO(Usuario usuario) {
        return UsuarioDTO.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .nome(usuario.getNome())
                // NÃO retornar password no DTO!
                .build();
    }
}
