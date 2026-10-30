package com.lumina.backend.domain.cliente;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ClienteRepositoryPort {
    Cliente salvar(Cliente cliente);
    Optional<Cliente> buscarPorId(ClienteId id);
    Optional<Cliente> buscarPorCpf(String cpf);
    Optional<Cliente> buscarPorEmail(String email);
    List<Cliente> buscarPorNome(String nome);
    Page<Cliente> buscarPorNome(String nome, Pageable pageable);
    List<Cliente> buscarTodos();
    Page<Cliente> buscarTodos(Pageable pageable);
    boolean existePorCpf(String cpf);
    boolean existePorEmail(String email);
}