package com.lumina.backend.dto.alexa;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;

@Schema(description = "Resumo da agenda do dia para a Alexa")
public class AlexaResumoDiaDto {

    @Schema(description = "Quantidade total de consultas agendadas para hoje", example = "4")
    private int totalConsultas;

    @Schema(description = "Horário da primeira consulta do dia", example = "09:00:00")
    private LocalTime primeiroHorario;

    @Schema(description = "Horário da última consulta do dia", example = "17:30:00")
    private LocalTime ultimoHorario;

    @Schema(description = "Nome do próximo paciente a ser atendido", example = "Carlos Silva")
    private String proximoPaciente;

    @Schema(description = "Horário do próximo atendimento", example = "14:30:00")
    private LocalTime proximoHorario;

    @Schema(description = "Texto otimizado para reprodução por voz na Alexa", example = "Doutor, você tem 4 consultas agendadas para hoje. A próxima é às 14 horas e 30 minutos com Carlos Silva.")
    private String mensagemVoz;

    public AlexaResumoDiaDto() {
    }

    public AlexaResumoDiaDto(int totalConsultas, LocalTime primeiroHorario, LocalTime ultimoHorario, String proximoPaciente, LocalTime proximoHorario, String mensagemVoz) {
        this.totalConsultas = totalConsultas;
        this.primeiroHorario = primeiroHorario;
        this.ultimoHorario = ultimoHorario;
        this.proximoPaciente = proximoPaciente;
        this.proximoHorario = proximoHorario;
        this.mensagemVoz = mensagemVoz;
    }

    public int getTotalConsultas() {
        return totalConsultas;
    }

    public void setTotalConsultas(int totalConsultas) {
        this.totalConsultas = totalConsultas;
    }

    public LocalTime getPrimeiroHorario() {
        return primeiroHorario;
    }

    public void setPrimeiroHorario(LocalTime primeiroHorario) {
        this.primeiroHorario = primeiroHorario;
    }

    public LocalTime getUltimoHorario() {
        return ultimoHorario;
    }

    public void setUltimoHorario(LocalTime ultimoHorario) {
        this.ultimoHorario = ultimoHorario;
    }

    public String getProximoPaciente() {
        return proximoPaciente;
    }

    public void setProximoPaciente(String proximoPaciente) {
        this.proximoPaciente = proximoPaciente;
    }

    public LocalTime getProximoHorario() {
        return proximoHorario;
    }

    public void setProximoHorario(LocalTime proximoHorario) {
        this.proximoHorario = proximoHorario;
    }

    public String getMensagemVoz() {
        return mensagemVoz;
    }

    public void setMensagemVoz(String mensagemVoz) {
        this.mensagemVoz = mensagemVoz;
    }
}
