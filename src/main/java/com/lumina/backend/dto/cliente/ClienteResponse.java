package com.lumina.backend.dto.cliente;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Dados de resposta de cliente")
public record ClienteResponse(
        @Schema(description = "Identificador do cliente", example = "1", type = "integer", format = "int64")
        Long idCliente,
        @Schema(description = "Nome completo do cliente", example = "Maria Oliveira", type = "string")
        String nome,
        @Schema(description = "CPF do cliente", example = "12345678901", type = "string")
        String cpf,
        @Schema(description = "RG do cliente", example = "123456789", type = "string")
        String rg,
        @Schema(description = "Data de nascimento do cliente", example = "1990-05-10", type = "string", format = "date")
        LocalDate dataNascimento,
        @Schema(description = "Numero de celular do cliente", example = "11999998888", type = "string")
        String numeroCelular,
        @Schema(description = "Email do cliente", example = "maria.oliveira@lumina.com", type = "string", format = "email")
        String email,
        @Schema(description = "Sexo do cliente", example = "F", type = "string")
        Character sexo,
        @Schema(description = "Naturalidade do cliente", example = "Sao Paulo", type = "string")
        String naturalidade,
        @Schema(description = "Nacionalidade do cliente", example = "Brasileira", type = "string")
        String nacionalidade,
        @Schema(description = "ID do estado civil do cliente", example = "1", type = "integer", format = "int32")
        Integer fkEstadoCivil,
        @Schema(description = "Endereco residencial do cliente", example = "Rua A, 100 - Centro", type = "string")
        String enderecoResidencial,
        @Schema(description = "CEP do cliente", example = "09090000", type = "string")
        String cep,
        @Schema(description = "ID do cliente que indicou", example = "2", type = "integer", format = "int32")
        Integer fkClienteIndicacao,
        @Schema(description = "ID do responsavel quando aplicavel", example = "3", type = "integer", format = "int32")
        Integer fkResponsavel,
        @Schema(description = "Grau de parentesco do responsavel", example = "Mae", type = "string")
        String grauParentescoResponsavel,
        @Schema(description = "Plano ou convenio principal do cliente", example = "Particular", type = "string")
        String plano,
        @Schema(description = "Lista com os nomes dos convenios do cliente")
        List<String> convenios,
        @Schema(description = "Data da proxima consulta agendada", example = "2026-10-25", type = "string", format = "date")
        LocalDate proximaConsulta
) {
    public ClienteResponse(Long idCliente, String nome, String cpf, String rg, LocalDate dataNascimento,
                           String numeroCelular, String email, Character sexo, String naturalidade,
                           String nacionalidade, Integer fkEstadoCivil, String enderecoResidencial, String cep,
                           Integer fkClienteIndicacao, Integer fkResponsavel, String grauParentescoResponsavel) {
        this(idCliente, nome, cpf, rg, dataNascimento, numeroCelular, email, sexo, naturalidade, nacionalidade,
                fkEstadoCivil, enderecoResidencial, cep, fkClienteIndicacao, fkResponsavel, grauParentescoResponsavel,
                null, null, null);
    }

    public ClienteResponse withEnriquecimento(String plano, List<String> convenios, LocalDate proximaConsulta) {
        return new ClienteResponse(
                idCliente, nome, cpf, rg, dataNascimento, numeroCelular, email, sexo,
                naturalidade, nacionalidade, fkEstadoCivil, enderecoResidencial, cep,
                fkClienteIndicacao, fkResponsavel, grauParentescoResponsavel,
                plano, convenios, proximaConsulta
        );
    }
}
