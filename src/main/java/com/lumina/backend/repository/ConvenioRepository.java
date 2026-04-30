package com.lumina.backend.repository;

import com.lumina.backend.model.Convenio;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

@Tag(name = "RepositorioConvenio", description = "Repositorio JPA para operacoes de persistencia de convenios")
public interface ConvenioRepository extends JpaRepository<Convenio, Long> {
}
