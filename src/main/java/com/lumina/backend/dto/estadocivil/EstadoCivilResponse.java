package com.lumina.backend.dto.estadocivil;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de resposta de estado civil")
public record EstadoCivilResponse(
        @Schema(description = "ID do estado civil", example = "1", type = "integer", format = "int32")
        Integer idEstadoCivil,
        @Schema(description = "Descrição do estado civil", example = "Solteiro(a)", type = "string")
        String descricao
) {
}
