package com.lumina.backend.repository;

import com.lumina.backend.model.Cliente;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

@Tag(name = "RepositorioCliente", description = "Repositorio JPA para operacoes de persistencia de clientes")
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

}
