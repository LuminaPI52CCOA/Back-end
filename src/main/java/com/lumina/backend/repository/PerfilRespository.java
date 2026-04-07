package com.lumina.backend.repository;

import com.lumina.backend.model.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PerfilRespository extends JpaRepository<Perfil, Integer>{
}
