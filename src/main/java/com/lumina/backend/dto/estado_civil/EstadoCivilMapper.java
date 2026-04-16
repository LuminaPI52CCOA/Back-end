package com.lumina.backend.dto.estado_civil;
import com.lumina.backend.model.Cliente;
import com.lumina.backend.model.EstadoCivil;

import java.util.List;

public class EstadoCivilMapper {
    public static EstadoCivil toEntity(EstadoCivilRequest dto) {
        if (dto == null) {
            return null;
        }

        EstadoCivil entidade = new EstadoCivil();
        entidade.setIdEstadoCivil(dto.getIdEstadoCivil());
        entidade.setDescricao(dto.getDescricao());

        return entidade;
    }

    public static EstadoCivilResponse toDto(EstadoCivil model) {
        if (model == null) {
            return null;
        }

        EstadoCivilResponse estadoCivilResponse = new
                EstadoCivilResponse();

        EstadoCivilResponse dto = new EstadoCivilResponse(
                model.getIdEstadoCivil(),
                model.getDescricao()
        );

        return dto;
    }

    public static List<EstadoCivilResponse> toDto(List<EstadoCivil> entities) {
        return entities.stream()
                .map(EstadoCivilMapper::toDto)
                .toList();
    }
}
