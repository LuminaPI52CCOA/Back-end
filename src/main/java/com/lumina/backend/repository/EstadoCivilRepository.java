package com.lumina.backend.repository;

import com.lumina.backend.model.EstadoCivil;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Tag(name = "RepositorioEstadoCivil", description = "Repositorio JPA para operacoes de persistencia de estado civil")
public interface EstadoCivilRepository extends JpaRepository<EstadoCivil, Integer> {
}
