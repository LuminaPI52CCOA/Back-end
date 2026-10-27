package com.lumina.backend.dto.consulta;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Alteracao explicita de status. Transicoes aceitas: AGENDADA <-> CONFIRMADA e "
        + "AGENDADA/CONFIRMADA -> CANCELADA. Consultas CANCELADA ou CONCLUIDA nao mudam de status.")
public class AlterarStatusConsultaRequest {

    @NotBlank
    @Schema(description = "Novo status", example = "CONFIRMADA", allowableValues = {"AGENDADA", "CONFIRMADA", "CANCELADA"})
    private String status;

    public AlterarStatusConsultaRequest() {
    }

    public AlterarStatusConsultaRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
