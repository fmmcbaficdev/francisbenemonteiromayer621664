package br.gov.mt.seplag.backend.repository;

import br.gov.mt.seplag.backend.model.Regional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegionalRepository extends JpaRepository<Regional, Long>, JpaSpecificationExecutor<Regional> {

    Optional<Regional> findByCodigoExterno(Integer codigoExterno);

    List<Regional> findByAtivaTrue();

    @Query("SELECT r FROM Regional r")
    List<Regional> findAllForSync();
}