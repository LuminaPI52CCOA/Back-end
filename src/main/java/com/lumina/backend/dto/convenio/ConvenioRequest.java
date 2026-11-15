package com.lumina.backend.dto.convenio;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados de entrada para cadastro e atualizacao de convenio")
public record ConvenioRequest(
        @NotBlank
        @Schema(description = "Nome do convenio", example = "Unimed", type = "string")
        String nome
) {
}
