package com.lumina.backend.domain.usuario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepositoryPort {
    Usuario salvar(Usuario usuario);
    Optional<Usuario> buscarPorId(UsuarioId id);
    Optional<Usuario> buscarPorEmail(String email);
    List<Usuario> buscarTodos();
    Page<Usuario> buscarTodos(Pageable pageable);
    Page<Usuario> buscarPaginado(Integer fkPerfil, String nome, Pageable pageable);
    void deletarLogicamente(UsuarioId id, Boolean ativo);
}