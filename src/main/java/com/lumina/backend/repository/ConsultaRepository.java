package com.lumina.backend.repository;

import com.lumina.backend.model.Consulta;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

@Tag(name = "RepositorioConsulta", description = "Repositorio JPA para operacoes de persistencia de consultas")
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {
}
