package com.lumina.backend.dto.consulta;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "Dados para reagendar uma consulta. Paciente, dentista, especialidade e odontograma sao preservados.")
public record ReagendamentoRequest(
        @NotNull
        @Schema(description = "Nova data da consulta", example = "2026-11-04", type = "string", format = "date")
        LocalDate data,
        @NotNull
        @Schema(description = "Novo horario de inicio (multiplo de 15 minutos)", example = "14:00:00", type = "string")
        LocalTime horarioInicio,
        @NotNull
        @Schema(description = "Novo horario de termino (duracao de 15, 30, 45 ou 60 minutos, ate 18:00)", example = "14:30:00", type = "string")
        LocalTime horarioFim,
        @Size(max = 500)
        @Schema(description = "Observacoes. Ausente/null mantem o valor atual; texto vazio remove as observacoes.",
                example = "Paciente pediu horario a tarde.", maxLength = 500, nullable = true)
        String observacoes
) {
}
