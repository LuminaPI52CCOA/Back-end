package com.lumina.backend.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de sessao retornados apos autenticacao")
public record UsuarioSessaoDto(
        @Schema(description = "Identificador do usuario autenticado", example = "1", type = "integer", format = "int64")
        Long userId,
        @Schema(description = "Nome do usuario autenticado", example = "Ana Maria Souza", type = "string")
        String nome,
        @Schema(description = "Email do usuario autenticado", example = "ana.souza@lumina.com", type = "string", format = "email")
        String email,
        @Schema(description = "Perfil de acesso do usuario autenticado", example = "DENTISTA", allowableValues = {"ADMIN", "DENTISTA", "RECEPCIONISTA"})
        String perfil
) {
}
