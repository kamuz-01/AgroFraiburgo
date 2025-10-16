package org.main.repository;

import java.util.Optional;

import org.main.models.DocumentosProdutor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentosProdutorRepository extends JpaRepository<DocumentosProdutor, Integer> {
	
	Optional<DocumentosProdutor> findByIdProdutor(Integer idProdutor);
}