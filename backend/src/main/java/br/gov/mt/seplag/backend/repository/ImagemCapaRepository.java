package br.gov.mt.seplag.backend.repository;

import br.gov.mt.seplag.backend.model.ImagemCapa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImagemCapaRepository extends JpaRepository<ImagemCapa, Long> {
    List<ImagemCapa> findByAlbumId(Long albumId);
}
