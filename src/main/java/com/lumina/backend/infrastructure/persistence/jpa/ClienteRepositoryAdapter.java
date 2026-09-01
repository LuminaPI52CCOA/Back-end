package com.lumina.backend.infrastructure.persistence.jpa;

import com.lumina.backend.domain.cliente.Cliente;
import com.lumina.backend.domain.cliente.ClienteId;
import com.lumina.backend.domain.cliente.ClienteRepositoryPort;
import com.lumina.backend.infrastructure.persistence.mapper.ClienteMapperJpa;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ClienteRepositoryAdapter implements ClienteRepositoryPort {

    private final ClienteRepositoryJpaSpring jpaRepository;

    public ClienteRepositoryAdapter(ClienteRepositoryJpaSpring jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Cliente salvar(Cliente cliente) {
        ClienteJpaEntity jpaEntity = ClienteMapperJpa.toJpa(cliente);
        ClienteJpaEntity saved = jpaRepository.save(jpaEntity);
        return ClienteMapperJpa.toDomain(saved);
    }

    @Override
    public Optional<Cliente> buscarPorId(ClienteId id) {
        if (id == null || id.getValue() == null) {
            return Optional.empty();
        }
        return jpaRepository.findById(id.getValue())
                .map(ClienteMapperJpa::toDomain);
    }

    @Override
    public Optional<Cliente> buscarPorCpf(String cpf) {
        return jpaRepository.findByCpf(cpf)
                .map(ClienteMapperJpa::toDomain);
    }

    @Override
    public Optional<Cliente> buscarPorEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(ClienteMapperJpa::toDomain);
    }

    @Override
    public List<Cliente> buscarPorNome(String nome) {
        return jpaRepository.findByNomeContainingIgnoreCase(nome)
                .stream()
                .map(ClienteMapperJpa::toDomain)
                .toList();
    }

    @Override
    public List<Cliente> buscarTodos() {
        return jpaRepository.findAll()
                .stream()
                .map(ClienteMapperJpa::toDomain)
                .toList();
    }

    @Override
    public boolean existePorCpf(String cpf) {
        return jpaRepository.existsByCpf(cpf);
    }

    @Override
    public boolean existePorEmail(String email) {
        return jpaRepository.findByEmail(email).isPresent();
    }
}