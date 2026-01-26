package br.gov.mt.seplag.backend.repository;

import br.gov.mt.seplag.backend.model.Artista;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArtistaRepository extends JpaRepository<Artista, Long> {

    Page<Artista> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    @Query("SELECT a FROM Artista a LEFT JOIN FETCH a.albuns WHERE a.id = :id")
    Optional<Artista> findByIdWithAlbuns(Long id);
}
