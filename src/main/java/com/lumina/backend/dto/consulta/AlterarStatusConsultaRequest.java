package com.lumina.backend.dto.consulta;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Alteracao explicita de status. Transicoes aceitas: AGENDADA <-> CONFIRMADA, "
        + "AGENDADA/CONFIRMADA -> CONCLUIDA e AGENDADA/CONFIRMADA -> CANCELADA. Consultas CANCELADA ou CONCLUIDA nao mudam de status.")
public record AlterarStatusConsultaRequest(
        @NotBlank
        @Schema(description = "Novo status", example = "CONFIRMADA", allowableValues = {"AGENDADA", "CONFIRMADA", "CONCLUIDA", "CANCELADA"})
        String status
) {
}
