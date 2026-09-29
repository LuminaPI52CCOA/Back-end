package com.lumina.backend.infrastructure.persistence.jpa;

import com.lumina.backend.domain.usuario.Usuario;
import com.lumina.backend.domain.usuario.UsuarioId;
import com.lumina.backend.domain.usuario.UsuarioRepositoryPort;
import com.lumina.backend.infrastructure.persistence.mapper.UsuarioMapperJpa;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {

    private final UsuarioRepositoryJpaSpring jpaRepository;

    public UsuarioRepositoryAdapter(UsuarioRepositoryJpaSpring jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        UsuarioJpaEntity jpaEntity = UsuarioMapperJpa.toJpa(usuario);
        UsuarioJpaEntity saved = jpaRepository.save(jpaEntity);
        return UsuarioMapperJpa.toDomain(saved);
    }

    @Override
    public Optional<Usuario> buscarPorId(UsuarioId id) {
        if (id == null || id.getValue() == null) {
            return Optional.empty();
        }
        return jpaRepository.findById(id.getValue())
                .map(UsuarioMapperJpa::toDomain);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(UsuarioMapperJpa::toDomain);
    }

    @Override
    public List<Usuario> buscarTodos() {
        return jpaRepository.findAll()
                .stream()
                .map(UsuarioMapperJpa::toDomain)
                .toList();
    }

    @Override
    public void deletarLogicamente(UsuarioId id, Boolean ativo) {
        if (id == null || id.getValue() == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        jpaRepository.logicalDelete(ativo, id.getValue());
    }
}