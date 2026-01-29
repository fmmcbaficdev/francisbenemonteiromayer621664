package br.gov.mt.seplag.backend.service;

import br.gov.mt.seplag.backend.dto.RegionalDTO;
import br.gov.mt.seplag.backend.exception.EntityNotFoundException;
import br.gov.mt.seplag.backend.model.Regional;
import br.gov.mt.seplag.backend.repository.RegionalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service para lógica de negócio de Regionais
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RegionalService {

    private final RegionalRepository regionalRepository;

    /**
     * Listar todas as regionais com paginação e filtro opcional
     */
    @Transactional(readOnly = true)
    public Page<RegionalDTO> listar(Pageable pageable, Boolean ativa) {
        log.debug("Listando regionais - página: {}, ativa: {}", pageable.getPageNumber(), ativa);

        Specification<Regional> spec = (root, query, cb) -> {
            if (ativa != null) {
                return cb.equal(root.get("ativa"), ativa);
            }
            return cb.conjunction(); // Sempre verdadeiro se não houver filtro
        };

        return regionalRepository.findAll(spec, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Buscar regional por ID
     */
    @Transactional(readOnly = true)
    public RegionalDTO buscarPorId(Long id) {
        log.debug("Buscando regional por ID: {}", id);
        Regional regional = regionalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Regional", id));
        return convertToDTO(regional);
    }

    /**
     * Buscar regional por código externo
     */
    @Transactional(readOnly = true)
    public RegionalDTO buscarPorCodigoExterno(Integer codigoExterno) {
        log.debug("Buscando regional por código externo: {}", codigoExterno);
        Regional regional = regionalRepository.findByCodigoExterno(codigoExterno)
                .orElseThrow(() -> new EntityNotFoundException("Regional não encontrada com código externo: " + codigoExterno));
        return convertToDTO(regional);
    }

    /**
     * Converter Entity para DTO
     */
    private RegionalDTO convertToDTO(Regional regional) {
        return RegionalDTO.builder()
                .id(regional.getId())
                .codigoExterno(regional.getCodigoExterno())
                .nome(regional.getNome())
                .ativa(regional.getAtiva())
                .ultimaSincronizacao(regional.getUltimaSincronizacao())
                .build();
    }
}
