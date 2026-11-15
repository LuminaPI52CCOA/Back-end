package com.lumina.backend.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de resposta de usuario")
public record UsuarioResponse(
        @Schema(description = "Identificador unico do usuario", example = "1", type = "integer", format = "int32")
        Long idUsuario,
        @Schema(description = "Nome completo do usuario", example = "Ana Maria Souza", type = "string")
        String nome,
        @Schema(description = "CPF do usuario contendo apenas numeros", example = "12345678901", type = "string")
        String cpf,
        @Schema(description = "Email de contato do usuario", example = "ana.souza@lumina.com", type = "string", format = "email")
        String email,
        @Schema(description = "Senha do usuario conforme retorno atual da API", example = "Senha@123", type = "string")
        String senha,
        @Schema(description = "Identificador do perfil vinculado ao usuario", example = "2", type = "integer", format = "int32")
        Integer fkPerfil,
        @Schema(description = "Codigo CRO do profissional", example = "SP-CD-12345", type = "string")
        String cro,
        @Schema(description = "Indica se o usuario esta ativo no sistema", example = "true", type = "boolean")
        Boolean ativo
) {
    public UsuarioResponse semSenha() {
        return new UsuarioResponse(idUsuario, nome, cpf, email, null, fkPerfil, cro, ativo);
    }
}
