package com.lumina.backend.repository;

import com.lumina.backend.model.Cliente;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Object existsByCpf(String cpf);

    Optional<Cliente> findByEmail(String email);

    Optional<Cliente> findByCpf(String cpf);

    List<Cliente> findByNomeContainingIgnoreCase(String nome);
}
