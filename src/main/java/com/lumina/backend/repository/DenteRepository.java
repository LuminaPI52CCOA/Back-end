package com.lumina.backend.repository;

import com.lumina.backend.model.Dente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DenteRepository extends JpaRepository<Dente, Integer> {
}
