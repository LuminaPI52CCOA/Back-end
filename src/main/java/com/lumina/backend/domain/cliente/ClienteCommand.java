package com.lumina.backend.domain.cliente;

import java.time.LocalDate;

public record ClienteCommand(
    String nome,
    String cpf,
    String rg,
    LocalDate dataNascimento,
    String naturalidade,
    String nacionalidade,
    Character sexo,
    String cep,
    String enderecoResidencial,
    String email,
    String numeroCelular,
    Integer fkEstadoCivil,
    Cliente clienteIndicacao,
    Cliente responsavel,
    String grauParentescoResponsavel
) {}