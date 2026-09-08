package com.lumina.backend.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepositoryJpaSpring extends JpaRepository<ClienteJpaEntity, Long> {
    boolean existsByCpf(String cpf);
    Optional<ClienteJpaEntity> findByEmail(String email);
    Optional<ClienteJpaEntity> findByCpf(String cpf);
    List<ClienteJpaEntity> findByNomeContainingIgnoreCase(String nome);
}