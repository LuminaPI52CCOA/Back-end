package com.lumina.backend.dto.convenio;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de resposta de convenio")
public record ConvenioResponse(
        @Schema(description = "Identificador do convenio", example = "1", type = "integer", format = "int64")
        Long id,
        @Schema(description = "Nome do convenio", example = "Unimed", type = "string")
        String nome
) {
}
