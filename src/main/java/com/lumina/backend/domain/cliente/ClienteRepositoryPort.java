package com.lumina.backend.domain.cliente;

import java.util.List;
import java.util.Optional;

public interface ClienteRepositoryPort {
    Cliente salvar(Cliente cliente);
    Optional<Cliente> buscarPorId(ClienteId id);
    Optional<Cliente> buscarPorCpf(String cpf);
    Optional<Cliente> buscarPorEmail(String email);
    List<Cliente> buscarPorNome(String nome);
    List<Cliente> buscarTodos();
    boolean existePorCpf(String cpf);
    boolean existePorEmail(String email);
}