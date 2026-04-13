package org.main.repository;

import java.util.Optional;

import org.main.enums.StatusFeira;
import org.main.models.Feira;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeiraRepository extends JpaRepository<Feira, Integer> {

	Optional<Feira> findFirstByStatusFeiraOrderByIdFeiraDesc(StatusFeira statusFeira);
}