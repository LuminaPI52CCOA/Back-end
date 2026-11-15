package com.lumina.backend.dto.consulta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;

@Schema(description = "Intervalo livre na agenda do dentista para a duracao solicitada")
public record HorarioDisponivelResponse(
        @Schema(description = "Inicio do intervalo", example = "09:00:00", type = "string")
        LocalTime horarioInicio,
        @Schema(description = "Fim do intervalo (inicio + duracaoMinutos)", example = "09:30:00", type = "string")
        LocalTime horarioFim
) {
}
