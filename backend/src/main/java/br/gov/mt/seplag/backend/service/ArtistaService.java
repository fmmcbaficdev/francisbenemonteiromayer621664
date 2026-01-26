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
                .map(this::toDTO);
    }

    /**
     * Buscar artistas por nome
     */
    @Transactional(readOnly = true)
    public Page<ArtistaDTO> buscarPorNome(String nome, Pageable pageable) {
        log.debug("Buscando artistas por nome: {}", nome);
        return artistaRepository.findByNomeContainingIgnoreCase(nome, pageable)
                .map(this::toDTO);
    }

    /**
     * Buscar artista por ID
     */
    @Transactional(readOnly = true)
    public ArtistaDTO buscarPorId(Long id) {
        log.debug("Buscando artista por ID: {}", id);
        Artista artista = artistaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Artista", id));
        return toDTO(artista);
    }

    /**
     * Criar novo artista
     */
    @Transactional
    public ArtistaDTO criar(ArtistaDTO dto) {
        log.info("Criando novo artista: {}", dto.nome());

        Artista artista = Artista.builder()
                .nome(dto.nome())
                .biografia(dto.biografia())
                .build();

        Artista saved = artistaRepository.save(artista);
        log.info("Artista criado com ID: {}", saved.getId());

        return toDTO(saved);
    }

    /**
     * Atualizar artista existente
     */
    @Transactional
    public ArtistaDTO atualizar(Long id, ArtistaDTO dto) {
        log.info("Atualizando artista ID: {}", id);

        Artista artista = artistaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Artista", id));

        artista.setNome(dto.nome());
        artista.setBiografia(dto.biografia());

        Artista updated = artistaRepository.save(artista);
        log.info("Artista atualizado: {}", updated.getId());

        return toDTO(updated);
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
     * Converter Entity para DTO (Record)
     */
    private ArtistaDTO toDTO(Artista artista) {
        int totalAlbuns = artista.getAlbuns() != null ? artista.getAlbuns().size() : 0;

        return new ArtistaDTO(
                artista.getId(),
                artista.getNome(),
                artista.getBiografia(),
                totalAlbuns
        );
    }
}