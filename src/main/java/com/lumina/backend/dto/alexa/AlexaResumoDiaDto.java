package com.lumina.backend.dto.alexa;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;

@Schema(description = "Resumo da agenda do dia para a Alexa")
public record AlexaResumoDiaDto(
        @Schema(description = "Quantidade total de consultas agendadas para hoje", example = "4")
        int totalConsultas,
        @Schema(description = "Horário da primeira consulta do dia", example = "09:00:00")
        LocalTime primeiroHorario,
        @Schema(description = "Horário da última consulta do dia", example = "17:30:00")
        LocalTime ultimoHorario,
        @Schema(description = "Nome do próximo paciente a ser atendido", example = "Carlos Silva")
        String proximoPaciente,
        @Schema(description = "Horário do próximo atendimento", example = "14:30:00")
        LocalTime proximoHorario,
        @Schema(description = "Texto otimizado para reprodução por voz na Alexa", example = "Doutor, você tem 4 consultas agendadas para hoje. A próxima é às 14 horas e 30 minutos com Carlos Silva.")
        String mensagemVoz
) {
}
