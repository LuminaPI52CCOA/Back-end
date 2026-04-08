package com.lumina.backend.dto.usuario;

import com.lumina.backend.model.Usuario;

import java.util.List;

public class UsuarioMapper {

    public static Usuario toEntity(UsuarioRequest dto) {
        if (dto == null) {
            return null;
        }

        Usuario entidade = new Usuario();
        entidade.setIdUsuario(dto.getIdUsuario());
        entidade.setNome(dto.getNome());
        entidade.setCpf(dto.getCpf());
        entidade.setEmail(dto.getEmail());
        entidade.setSenha(dto.getSenha());
        entidade.setCro(dto.getCro());
        entidade.setAtivo(dto.getAtivo());
        entidade.setFkPerfil(dto.getFkPerfil());

        return entidade;
    }

    public static UsuarioResponse toDto(Usuario model) {
        if (model == null) {
            return null;
        }

        Integer perfil = model.getFkPerfil();

        UsuarioResponse.UsuarioPerfil usuarioPerfilDto = new
                UsuarioResponse.UsuarioPerfil();

        UsuarioResponse dto = new UsuarioResponse(
                model.getIdUsuario(),
                model.getNome(),
                model.getCpf(),
                model.getEmail(),
                model.getSenha(),
                perfil,
                model.getCro(),
                model.getAtivo()
        );

        return dto;
    }

    public static List<UsuarioResponse> toDto(List<Usuario> entities) {
        return entities.stream()
                .map(UsuarioMapper::toDto)
                .toList();
    }
}
