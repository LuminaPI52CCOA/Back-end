package com.lumina.backend.model;

import jakarta.persistence.*;

import java.sql.Time;
import java.time.LocalDate;

@Entity
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idConsulta;

    @ManyToOne
    private Cliente cliente;

    @ManyToOne
    private Usuario usuario;

    private LocalDate data;

    private Time horarioInicio;

    private Time horarioFim;

    public Consulta() {
    }

    public Consulta(Long idConsulta, Cliente cliente, Usuario usuario, LocalDate data, Time horarioInicio, Time horarioFim) {
        this.idConsulta = idConsulta;
        this.cliente = cliente;
        this.usuario = usuario;
        this.data = data;
        this.horarioInicio = horarioInicio;
        this.horarioFim = horarioFim;
    }

    public Long getIdConsulta() {
        return idConsulta;
    }

    public void setIdConsulta(Long idConsulta) {
        this.idConsulta = idConsulta;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
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
