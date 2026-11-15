package com.lumina.backend.dto.estadocivil;

import com.lumina.backend.model.EstadoCivil;

import java.util.List;

public class EstadoCivilMapper {

    public static EstadoCivil toEntity(EstadoCivilRequest dto) {
        if (dto == null) {
            return null;
        }

        EstadoCivil entidade = new EstadoCivil();
        entidade.setIdEstadoCivil(dto.idEstadoCivil());
        entidade.setDescricao(dto.descricao());

        return entidade;
    }

    public static EstadoCivilResponse toDto(EstadoCivil model) {
        if (model == null) {
            return null;
        }

        return new EstadoCivilResponse(
                model.getIdEstadoCivil(),
                model.getDescricao()
        );
    }

    public static List<EstadoCivilResponse> toDto(List<EstadoCivil> entities) {
        return entities.stream()
                .map(EstadoCivilMapper::toDto)
                .toList();
    }
}
