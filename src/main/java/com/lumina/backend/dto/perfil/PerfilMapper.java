package com.lumina.backend.dto.perfil;

import com.lumina.backend.model.Perfil;

import java.util.List;

public class PerfilMapper {

    public static Perfil toEntity(PerfilRequest dto) {
        if (dto == null) {
            return null;
        }

        Perfil entidade = new Perfil();
        entidade.setIdPerfil(dto.idPerfil());
        entidade.setNome(dto.nome());

        return entidade;
    }

    public static PerfilResponse toDto(Perfil model) {
        if (model == null) {
            return null;
        }

        return new PerfilResponse(
                model.getIdPerfil(),
                model.getNome()
        );
    }

    public static List<PerfilResponse> toDto(List<Perfil> entities) {
        return entities.stream()
                .map(PerfilMapper::toDto)
                .toList();
    }
}
