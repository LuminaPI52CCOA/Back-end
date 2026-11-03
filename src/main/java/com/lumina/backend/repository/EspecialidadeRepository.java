package com.lumina.backend.repository;

import com.lumina.backend.model.Especialidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EspecialidadeRepository extends JpaRepository<Especialidade, Integer> {

    List<Especialidade> findAllByOrderByNomeAsc();
}
