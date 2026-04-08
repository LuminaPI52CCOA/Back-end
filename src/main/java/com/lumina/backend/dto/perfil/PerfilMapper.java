package com.lumina.backend.dto.perfil;

import com.lumina.backend.dto.usuario.UsuarioMapper;
import com.lumina.backend.dto.usuario.UsuarioRequest;
import com.lumina.backend.dto.usuario.UsuarioResponse;
import com.lumina.backend.model.Perfil;
import com.lumina.backend.model.Usuario;

import java.util.List;

public class PerfilMapper {
    public static Perfil toEntity(PerfilRequest dto) {
        if (dto == null) {
            return null;
        }

        Perfil entidade = new Perfil();
        entidade.setIdPerfil(dto.getIdPerfil());
        entidade.setNome(dto.getNome());

        return entidade;
    }

    public static PerfilResponse toDto(Perfil model) {
        if (model == null) {
            return null;
        }

        PerfilResponse usuarioPerfilDto = new
                PerfilResponse();

        PerfilResponse dto = new PerfilResponse(
                model.getIdPerfil(),
                model.getNome()
        );

        return dto;
    }

    public static List<PerfilResponse> toDto(List<Perfil> entities) {
        return entities.stream()
                .map(PerfilMapper::toDto)
                .toList();
    }
}
