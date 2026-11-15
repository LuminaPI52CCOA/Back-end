package com.lumina.backend.dto.anamnese;

import java.time.LocalDate;

public record AnamneseResponse(
        Long idAnamnese,
        AnamneseCliente anamneseCliente,
        LocalDate dataAnamnese,
        Boolean fazendoTratamento,
        String descricaoTratamento,
        Boolean doresCabecaFaceAtm,
        Boolean alergiaMedicamentos,
        String descricaoAlergiaMedicamentos,
        Boolean reacaoAnestesiaLocal,
        Boolean sensibilidadeDentaria,
        Boolean bruxismoApertamento,
        Boolean sangramentoGengival,
        Boolean possuiHabito,
        String descricaoHabito,
        Boolean historicoDiabetes,
        Boolean sangramentoExcessivo,
        Boolean problemaCardiaco,
        String descricaoProblemaCardiaco,
        Boolean pressaoArterialNormal,
        String descricaoPressaoArterial,
        Boolean historicoDesmaioConvulsao,
        Boolean gestante
) {
    public record AnamneseCliente(
            Long idCliente,
            String nome,
            String cpf,
            String rg,
            LocalDate dataNascimento,
            String numeroCelular,
            String email,
            Character sexo,
            String naturalidade,
            String nacionalidade,
            Integer fkEstadoCivil,
            String enderecoResidencial,
            String cep,
            Integer fkClienteIndicacao,
            Integer fkResponsavel,
            String grauParentescoResponsavel
    ) {
    }
}
