package com.lumina.backend.dto.alexa;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "Dados da consulta formatados para a Alexa")
public record AlexaConsultaDto(
        @Schema(description = "Identificador da consulta", example = "1")
        Long idConsulta,
        @Schema(description = "Nome do paciente", example = "Carlos Silva")
        String pacienteNome,
        @Schema(description = "Data da consulta", example = "2026-09-21")
        LocalDate data,
        @Schema(description = "Horário de início", example = "14:30:00")
        LocalTime horarioInicio,
        @Schema(description = "Horário de fim", example = "15:00:00")
        LocalTime horarioFim,
        @Schema(description = "Status da consulta", example = "AGENDADA")
        String status,
        @Schema(description = "Texto otimizado para reprodução por voz na Alexa", example = "Doutor, sua próxima consulta é às 14 horas e 30 minutos com o paciente Carlos Silva.")
        String mensagemVoz
) {
}
