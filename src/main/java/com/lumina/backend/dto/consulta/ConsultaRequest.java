package com.lumina.backend.dto.consulta;

import com.lumina.backend.dto.cliente.ClienteMapper;
import com.lumina.backend.dto.usuario.UsuarioMapper;
import com.lumina.backend.model.Consulta;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.sql.Time;
import java.time.LocalDate;
import java.util.List;

public class ConsultaRequest {

    @NotBlank
    @Positive
    private Long idCliente;

    @NotBlank
    @Positive
    private Long idUsuario;

    @NotBlank
    @Future
    private LocalDate data;

    @NotBlank
    private Time horarioInicio;

    @NotBlank
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
