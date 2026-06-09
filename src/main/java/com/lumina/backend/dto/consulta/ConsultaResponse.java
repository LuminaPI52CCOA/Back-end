package com.lumina.backend.dto.consulta;

import com.lumina.backend.dto.cliente.ClienteResponse;
import com.lumina.backend.dto.usuario.UsuarioResponse;
import com.lumina.backend.model.Cliente;
import io.swagger.v3.oas.annotations.media.Schema;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "Dados de resposta de consulta")
public class ConsultaResponse {
    @Schema(description = "Identificador da consulta", example = "1", type = "integer", format = "int64")
    private Long id;
    @Schema(description = "Cliente vinculado a consulta")
    private ClienteResponse cliente;
    @Schema(description = "Usuario responsavel pela consulta")
    private UsuarioResponse usuario;
    @Schema(description = "Data da consulta", example = "2026-05-15", type = "string", format = "date")
    private LocalDate data;
    @Schema(description = "Horario de inicio da consulta", example = "09:00:00", type = "string")
    private LocalTime horarioInicio;
    @Schema(description = "Horario de termino da consulta", example = "09:30:00", type = "string")
    private LocalTime horarioFim;

    public ConsultaResponse() {
    }

    public ConsultaResponse(Long id,
                            ClienteResponse cliente,
                            UsuarioResponse usuario,
                            LocalDate data,
                            LocalTime horarioInicio,
                            LocalTime horarioFim) {
        this.id = id;
        this.cliente = cliente;
        this.usuario = usuario;
        this.data = data;
        this.horarioInicio = horarioInicio;
        this.horarioFim = horarioFim;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ClienteResponse getCliente() {
        return cliente;
    }

    public void setCliente(ClienteResponse cliente) {
        this.cliente = cliente;
    }

    public UsuarioResponse getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioResponse usuario) {
        this.usuario = usuario;
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
}
