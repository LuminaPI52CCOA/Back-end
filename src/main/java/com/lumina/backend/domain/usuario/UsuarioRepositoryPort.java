package com.lumina.backend.domain.usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepositoryPort {
    Usuario salvar(Usuario usuario);
    Optional<Usuario> buscarPorId(UsuarioId id);
    Optional<Usuario> buscarPorEmail(String email);
    List<Usuario> buscarTodos();
    void deletarLogicamente(UsuarioId id, Boolean ativo);
}