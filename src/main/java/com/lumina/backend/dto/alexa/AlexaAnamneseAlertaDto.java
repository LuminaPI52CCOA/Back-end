package com.lumina.backend.dto.alexa;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Alertas médicos críticos da anamnese para a Alexa")
public record AlexaAnamneseAlertaDto(
        @Schema(description = "Nome do paciente", example = "Carlos Silva")
        String pacienteNome,
        @Schema(description = "Indica se o paciente possui restrições ou alertas médicos", example = "true")
        boolean possuiRestricoes,
        @Schema(description = "Lista de alertas identificados", example = "[\"Alergia a Penicilina\", \"Hipertensão arterial\"]")
        List<String> alertas,
        @Schema(description = "Texto otimizado para reprodução por voz na Alexa", example = "Atenção Doutor: O paciente Carlos Silva relatou alergia a Penicilina e histórico de hipertensão.")
        String mensagemVoz
) {
}
