package com.lumina.backend.dto.alexa;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "Dados da consulta formatados para a Alexa")
public class AlexaConsultaDto {

    @Schema(description = "Identificador da consulta", example = "1")
    private Long idConsulta;

    @Schema(description = "Nome do paciente", example = "Carlos Silva")
    private String pacienteNome;

    @Schema(description = "Data da consulta", example = "2026-09-21")
    private LocalDate data;

    @Schema(description = "Horário de início", example = "14:30:00")
    private LocalTime horarioInicio;

    @Schema(description = "Horário de fim", example = "15:00:00")
    private LocalTime horarioFim;

    @Schema(description = "Status da consulta", example = "AGENDADA")
    private String status;

    @Schema(description = "Texto otimizado para reprodução por voz na Alexa", example = "Doutor, sua próxima consulta é às 14 horas e 30 minutos com o paciente Carlos Silva.")
    private String mensagemVoz;

    public AlexaConsultaDto() {
    }

    public AlexaConsultaDto(Long idConsulta, String pacienteNome, LocalDate data, LocalTime horarioInicio, LocalTime horarioFim, String status, String mensagemVoz) {
        this.idConsulta = idConsulta;
        this.pacienteNome = pacienteNome;
        this.data = data;
        this.horarioInicio = horarioInicio;
        this.horarioFim = horarioFim;
        this.status = status;
        this.mensagemVoz = mensagemVoz;
    }

    public Long getIdConsulta() {
        return idConsulta;
    }

    public void setIdConsulta(Long idConsulta) {
        this.idConsulta = idConsulta;
    }

    public String getPacienteNome() {
        return pacienteNome;
    }

    public void setPacienteNome(String pacienteNome) {
        this.pacienteNome = pacienteNome;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMensagemVoz() {
        return mensagemVoz;
    }

    public void setMensagemVoz(String mensagemVoz) {
        this.mensagemVoz = mensagemVoz;
    }
}
