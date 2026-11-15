package com.lumina.backend.dto.anamnese;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record AnamneseRequest(
        Long idAnamnese,
        Long fkCliente,
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
}
