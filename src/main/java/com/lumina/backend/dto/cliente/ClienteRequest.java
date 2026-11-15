package com.lumina.backend.dto.cliente;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;

@Builder
@Schema(description = "Dados de entrada para cadastro e atualizacao de cliente")
public record ClienteRequest(
        @Schema(description = "Identificador do cliente", example = "1", type = "integer", format = "int64")
        Long idCliente,

        @NotBlank
        @Schema(description = "Nome completo do cliente", example = "Maria Oliveira", type = "string")
        String nome,

        @NotNull
        @Schema(description = "ID do estado civil do cliente", example = "1", type = "integer", format = "int32")
        Integer fkEstadoCivil,

        @NotBlank
        @Schema(description = "CPF do cliente", example = "12345678901", type = "string")
        String cpf,

        @NotBlank
        @Schema(description = "RG do cliente", example = "123456789", type = "string")
        String rg,

        @NotNull
        @JsonFormat(pattern = "dd/MM/yyyy")
        @Schema(description = "Data de nascimento do cliente", example = "1990-05-10", type = "string", format = "date")
        LocalDate dataNascimento,

        @NotBlank
        @Schema(description = "Naturalidade do cliente", example = "Sao Paulo", type = "string")
        String naturalidade,

        @NotBlank
        @Schema(description = "Nacionalidade do cliente", example = "Brasileira", type = "string")
        String nacionalidade,

        @NotBlank
        @Schema(description = "Numero de celular do cliente", example = "11999998888", type = "string")
        String numeroCelular,

        @NotBlank
        @Schema(description = "Email do cliente", example = "maria.oliveira@lumina.com", type = "string", format = "email")
        String email,

        @NotNull
        @Schema(description = "Sexo do cliente", example = "F", type = "string")
        Character sexo,

        @NotBlank
        @Schema(description = "Endereco residencial do cliente", example = "Rua A, 100 - Centro", type = "string")
        String enderecoResidencial,

        @NotBlank
        @Schema(description = "CEP do cliente", example = "09090000", type = "string")
        String cep,

        @Schema(description = "ID do cliente que indicou", example = "2", type = "integer", format = "int32")
        Integer fkClienteIndicacao,

        @Schema(description = "Responsável do cliente")
        ResponsavelRequest responsavel,

        @Schema(description = "Grau de parentesco do responsavel", example = "Mae", type = "string")
        String grauParentescoResponsavel
) {
}
