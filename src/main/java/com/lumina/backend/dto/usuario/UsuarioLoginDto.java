package com.lumina.backend.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Credenciais de autenticacao do usuario")
public record UsuarioLoginDto(
        @Schema(description = "E-mail do usuário", example = "john@doe.com")
        String email,
        @Schema(description = "Senha do usuário", example = "123456")
        String senha
) {
}
