package com.lumina.backend.dto.estadocivil;

import jakarta.validation.constraints.NotBlank;

public record EstadoCivilRequest(
        Integer idEstadoCivil,
        @NotBlank
        String descricao
) {
}
