package com.lumina.backend.dto.consulta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;

@Schema(description = "Intervalo livre na agenda do dentista para a duracao solicitada")
public class HorarioDisponivelResponse {

    @Schema(description = "Inicio do intervalo", example = "09:00:00", type = "string")
    private LocalTime horarioInicio;

    @Schema(description = "Fim do intervalo (inicio + duracaoMinutos)", example = "09:30:00", type = "string")
    private LocalTime horarioFim;

    public HorarioDisponivelResponse() {
    }

    public HorarioDisponivelResponse(LocalTime horarioInicio, LocalTime horarioFim) {
        this.horarioInicio = horarioInicio;
        this.horarioFim = horarioFim;
    }

    public LocalTime getHorarioInicio() {
        return horarioInicio;
    }

    public void setHorarioInicio(LocalTime horarioInicio) {
        this.horarioInicio = horarioInicio;
    }

    public LocalTime getHorarioFim() {
        return horarioFim;
    }

    public void setHorarioFim(LocalTime horarioFim) {
        this.horarioFim = horarioFim;
    }
}
