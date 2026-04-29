package com.lumina.backend.dto.consulta;

import com.lumina.backend.dto.cliente.ClienteResponse;
import com.lumina.backend.dto.usuario.UsuarioResponse;
import com.lumina.backend.model.Cliente;

import java.sql.Time;
import java.time.LocalDate;

public class ConsultaResponse {
    private Long id;
    private ClienteResponse cliente;
    private UsuarioResponse usuario;
    private LocalDate data;
    private Time horarioInicio;
    private Time horarioFim;

    public ConsultaResponse() {
    }

    public ConsultaResponse(Long id, ClienteResponse cliente, UsuarioResponse usuario, LocalDate data, Time horarioInicio, Time horarioFim) {
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
