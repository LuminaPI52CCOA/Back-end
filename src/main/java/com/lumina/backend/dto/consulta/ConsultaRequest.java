package com.lumina.backend.dto.consulta;

import com.lumina.backend.dto.cliente.ClienteMapper;
import com.lumina.backend.dto.usuario.UsuarioMapper;
import com.lumina.backend.model.Consulta;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.sql.Time;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Dados de entrada para cadastro de consulta")
public class ConsultaRequest {

    @NotBlank
    @Positive
    @Schema(description = "ID do cliente da consulta", example = "1", type = "integer", format = "int64")
    private Long idCliente;

    @NotBlank
    @Positive
    @Schema(description = "ID do usuario responsavel pela consulta", example = "2", type = "integer", format = "int64")
    private Long idUsuario;

    @NotBlank
    @Future
    @Schema(description = "Data agendada para a consulta", example = "2026-05-15", type = "string", format = "date")
    private LocalDate data;

    @NotBlank
    @Schema(description = "Horario de inicio da consulta", example = "09:00:00", type = "string")
    private Time horarioInicio;

    @NotBlank
    @Schema(description = "Horario de termino da consulta", example = "09:30:00", type = "string")
    private Time horarioFim;

    public ConsultaRequest() {
    }

    public ConsultaRequest(Long idCliente, Long idUsuario, LocalDate data, Time horarioInicio, Time horarioFim) {
        this.idCliente = idCliente;
        this.idUsuario = idUsuario;
        this.data = data;
        this.horarioInicio = horarioInicio;
        this.horarioFim = horarioFim;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public Time getHorarioInicio() {
        return horarioInicio;
    }

    public void setHorarioInicio(Time horarioInicio) {
        this.horarioInicio = horarioInicio;
    }

    public Time getHorarioFim() {
        return horarioFim;
    }

    public void setHorarioFim(Time horarioFim) {
        this.horarioFim = horarioFim;
    }
}
