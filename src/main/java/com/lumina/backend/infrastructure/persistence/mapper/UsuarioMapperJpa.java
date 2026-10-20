package com.lumina.backend.infrastructure.persistence.mapper;

import com.lumina.backend.domain.usuario.Usuario;
import com.lumina.backend.domain.usuario.UsuarioCommand;
import com.lumina.backend.domain.usuario.UsuarioId;
import com.lumina.backend.infrastructure.persistence.jpa.UsuarioJpaEntity;
import com.lumina.backend.model.Perfil;

public class UsuarioMapperJpa {

    public static UsuarioJpaEntity toJpa(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        UsuarioJpaEntity entity = new UsuarioJpaEntity();
        if (usuario.getId() != null && usuario.getId().getValue() != null) {
            entity.setIdUsuario(usuario.getId().getValue());
        }
        entity.setNome(usuario.getNome());
        entity.setCpf(usuario.getCpf());
        entity.setEmail(usuario.getEmail());
        entity.setSenha(usuario.getSenha());
        if (usuario.getFkPerfil() != null) {
            Perfil perfil = new Perfil();
            perfil.setIdPerfil(usuario.getFkPerfil());
            entity.setPerfil(perfil);
        }
        entity.setCro(usuario.getCro());
        entity.setAtivo(usuario.getAtivo());

        return entity;
    }

    public static Usuario toDomain(UsuarioJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        UsuarioId id = entity.getIdUsuario() != null ? UsuarioId.of(entity.getIdUsuario()) : null;

        UsuarioCommand command = new UsuarioCommand(
            entity.getNome(),
            entity.getCpf(),
            entity.getEmail(),
            entity.getSenha(),
            entity.getPerfil() != null ? entity.getPerfil().getIdPerfil() : null,
            entity.getCro(),
            entity.getAtivo()
        );
        
        Usuario usuario;
        if (id != null) {
            usuario = Usuario.reconstruir(id, command);
        } else {
            usuario = Usuario.criar(command);
        }
        
        return usuario;
    }

    public static UsuarioCommand toCommand(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        return new UsuarioCommand(
            usuario.getNome(),
            usuario.getCpf(),
            usuario.getEmail(),
            usuario.getSenha(),
            usuario.getFkPerfil(),
            usuario.getCro(),
            usuario.getAtivo()
        );
    }
}