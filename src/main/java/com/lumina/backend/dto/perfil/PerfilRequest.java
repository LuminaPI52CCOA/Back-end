package com.lumina.backend.dto.perfil;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados de entrada para cadastro e atualizacao de perfil")
public record PerfilRequest(
        @Schema(description = "Identificador unico do perfil", example = "1", type = "integer", format = "int32")
        Integer idPerfil,
        @NotBlank
        @Schema(description = "Nome do perfil de acesso", example = "ADMIN", type = "string")
        String nome
) {
}
