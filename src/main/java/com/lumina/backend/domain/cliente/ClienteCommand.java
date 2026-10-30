package com.lumina.backend.domain.cliente;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    String grauParentescoResponsavel,
    Boolean consentimentoLgpd,
    LocalDateTime dataConsentimentoLgpd
) {
    public ClienteCommand(
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
    ) {
        this(
            nome,
            cpf,
            rg,
            dataNascimento,
            naturalidade,
            nacionalidade,
            sexo,
            cep,
            enderecoResidencial,
            email,
            numeroCelular,
            fkEstadoCivil,
            clienteIndicacao,
            responsavel,
            grauParentescoResponsavel,
            true,
            null
        );
    }
}