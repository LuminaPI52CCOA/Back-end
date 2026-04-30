package com.lumina.backend.repository;

import com.lumina.backend.model.Perfil;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Tag(name = "RepositorioPerfil", description = "Repositorio JPA para operacoes de persistencia de perfis")
public interface PerfilRespository extends JpaRepository<Perfil, Integer>{
}
