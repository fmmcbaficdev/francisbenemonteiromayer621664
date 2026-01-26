package br.gov.mt.seplag.backend.repository;

import br.gov.mt.seplag.backend.model.Album;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    Page<Album> findByTituloContainingIgnoreCase(String titulo, Pageable pageable);

    @Query("SELECT a FROM Album a JOIN a.artistas art WHERE art.id = :artistaId")
    Page<Album> findByArtistasId(Long artistaId, Pageable pageable);

    @Query("SELECT a FROM Album a LEFT JOIN FETCH a.imagensCapa WHERE a.id = :id")
    Optional<Album> findByIdWithImagens(Long id);
}

