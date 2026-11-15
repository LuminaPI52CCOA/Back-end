package com.lumina.backend.dto.perfil;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de resposta de perfil")
public record PerfilResponse(
        @Schema(description = "Identificador unico do perfil", example = "1", type = "integer", format = "int32")
        Integer idPerfil,
        @Schema(description = "Nome do perfil de acesso", example = "ADMIN", type = "string")
        String nome
) {
}
