package com.lumina.backend.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.hibernate.validator.constraints.br.CPF;

@Builder
@Schema(description = "Dados de entrada para cadastro e atualizacao de usuario")
public record UsuarioRequest(
        @Schema(description = "Identificador unico do usuario", example = "1", type = "integer", format = "int32")
        Long idUsuario,

        @NotBlank
        @Schema(description = "Nome completo do usuario", example = "Ana Maria Souza", type = "string")
        String nome,

        @NotBlank
        @CPF
        @Schema(description = "CPF do usuario contendo apenas numeros", example = "12345678901", type = "string")
        String cpf,

        @NotBlank
        @Email
        @Schema(description = "Email valido para acesso do usuario", example = "ana.souza@lumina.com", type = "string", format = "email")
        String email,

        @NotBlank
        @Schema(description = "Senha de acesso do usuario", example = "Senha@123", type = "string")
        String senha,

        @NotNull
        @Schema(description = "Identificador do perfil vinculado ao usuario", example = "2", type = "integer", format = "int32")
        Integer fkPerfil,

        @NotBlank
        @Schema(description = "Codigo CRO do profissional, quando aplicavel", example = "SP-CD-12345", type = "string")
        String cro,

        @NotNull
        @Schema(description = "Indica se o usuario esta ativo no sistema", example = "true", type = "boolean")
        Boolean ativo
) {
}
