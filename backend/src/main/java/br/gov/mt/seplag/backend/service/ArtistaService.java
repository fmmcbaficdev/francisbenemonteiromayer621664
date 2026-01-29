package br.gov.mt.seplag.backend.service;

import br.gov.mt.seplag.backend.dto.ArtistaDTO;
import br.gov.mt.seplag.backend.exception.EntityNotFoundException;
import br.gov.mt.seplag.backend.model.Artista;
import br.gov.mt.seplag.backend.repository.ArtistaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service para lógica de negócio de Artistas
 *
 * RECURSOS IMPLEMENTADOS:
 * - CRUD completo com paginação e busca
 * - Auditoria automática (created_by, updated_by via Spring Data Auditing)
 * - Optimistic Locking (version para controle de concorrência)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ArtistaService {

    private final ArtistaRepository artistaRepository;

    /**
     * Listar todos os artistas com paginação
     */
    @Transactional(readOnly = true)
    public Page<ArtistaDTO> listar(Pageable pageable) {
        log.debug("Listando artistas - página: {}", pageable.getPageNumber());
        return artistaRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    /**
     * Buscar artistas por nome
     */
    @Transactional(readOnly = true)
    public Page<ArtistaDTO> buscarPorNome(String nome, Pageable pageable) {
        log.debug("Buscando artistas por nome: {}", nome);
        return artistaRepository.findByNomeContainingIgnoreCase(nome, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Buscar artista por ID
     */
    @Transactional(readOnly = true)
    public ArtistaDTO buscarPorId(Long id) {
        log.debug("Buscando artista por ID: {}", id);
        Artista artista = artistaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Artista", id));
        return convertToDTO(artista);
    }

    /**
     * Criar novo artista
     *
     * AUDITORIA: created_by e created_at são preenchidos automaticamente
     * pelo Spring Data Auditing (AuditingEntityListener + AuditorAware)
     */
    @Transactional
    public ArtistaDTO criar(ArtistaDTO dto) {
        log.info("Criando novo artista: {}", dto.getNome());

        Artista artista = Artista.builder()
                .nome(dto.getNome())
                .biografia(dto.getBiografia())
                .build();

        Artista saved = artistaRepository.save(artista);
        log.info("Artista criado com ID: {} por usuário: {}", saved.getId(), saved.getCreatedBy());

        return convertToDTO(saved);
    }

    /**
     * Atualizar artista existente
     *
     * OPTIMISTIC LOCKING:
     * - Se dto.version != null, usa para controle de concorrência
     * - Se outro usuário modificou o registro, JPA lança OptimisticLockException
     * - GlobalExceptionHandler converte para HTTP 409 Conflict
     *
     * AUDITORIA: updated_by e updated_at são preenchidos automaticamente
     */
    @Transactional
    public ArtistaDTO atualizar(Long id, ArtistaDTO dto) {
        log.info("Atualizando artista ID: {}", id);

        Artista artista = artistaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Artista", id));

        // Optimistic Locking: se frontend enviou version, usar para validação
        // O JPA vai comparar automaticamente e lançar exceção se versões diferem
        if (dto.getVersion() != null) {
            artista.setVersion(dto.getVersion());
        }

        artista.setNome(dto.getNome());
        artista.setBiografia(dto.getBiografia());

        Artista updated = artistaRepository.save(artista);
        log.info("Artista atualizado: {} por usuário: {}", updated.getId(), updated.getUpdatedBy());

        return convertToDTO(updated);
    }

    /**
     * Deletar artista
     */
    @Transactional
    public void deletar(Long id) {
        log.info("Deletando artista ID: {}", id);

        if (!artistaRepository.existsById(id)) {
            throw new EntityNotFoundException("Artista", id);
        }

        artistaRepository.deleteById(id);
        log.info("Artista deletado: {}", id);
    }

    /**
     * Converter Entity para DTO incluindo campos de auditoria
     */
    private ArtistaDTO convertToDTO(Artista artista) {
        return ArtistaDTO.builder()
                .id(artista.getId())
                .nome(artista.getNome())
                .biografia(artista.getBiografia())
                .totalAlbuns(artista.getAlbuns() != null ? artista.getAlbuns().size() : 0)
                // Auditoria
                .createdAt(artista.getCreatedAt())
                .createdBy(artista.getCreatedBy())
                .updatedAt(artista.getUpdatedAt())
                .updatedBy(artista.getUpdatedBy())
                // Optimistic Locking
                .version(artista.getVersion())
                .build();
    }
}
