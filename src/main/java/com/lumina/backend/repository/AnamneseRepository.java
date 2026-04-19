package com.lumina.backend.repository;

import com.lumina.backend.model.Anamnese;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnamneseRepository extends JpaRepository<Anamnese, Integer> {
    List<Anamnese> findAnamneseByFkCliente_IdCliente(Integer id);
}
