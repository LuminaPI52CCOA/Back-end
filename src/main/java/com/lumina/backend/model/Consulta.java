package com.lumina.backend.model;

import jakarta.persistence.*;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "consulta")
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idConsulta;

    @ManyToOne
    @JoinColumn(name = "fk_cliente")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "fk_usuario")
    private Usuario usuario;

    private LocalDate data;

    private LocalTime horarioInicio;

    private LocalTime horarioFim;

    @Column(name = "status")
    private String status = "AGENDADA";

    @Column(name = "lembrete_enviado")
    private Boolean lembreteEnviado = false;

    @Column(name = "alexa_reminder_id")
    private String alexaReminderId;

    public Consulta() {
    }

    public Consulta(Long idConsulta, Cliente cliente, Usuario usuario, LocalDate data, LocalTime horarioInicio, LocalTime horarioFim) {
        this.idConsulta = idConsulta;
        this.cliente = cliente;
        this.usuario = usuario;
        this.data = data;
        this.horarioInicio = horarioInicio;
        this.horarioFim = horarioFim;
        this.status = "AGENDADA";
        this.lembreteEnviado = false;
    }

    public Consulta(Long idConsulta, Cliente cliente, Usuario usuario, LocalDate data, LocalTime horarioInicio, LocalTime horarioFim, String status, Boolean lembreteEnviado, String alexaReminderId) {
        this.idConsulta = idConsulta;
        this.cliente = cliente;
        this.usuario = usuario;
        this.data = data;
        this.horarioInicio = horarioInicio;
        this.horarioFim = horarioFim;
        this.status = status;
        this.lembreteEnviado = lembreteEnviado;
        this.alexaReminderId = alexaReminderId;
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

    public Boolean getLembreteEnviado() {
        return lembreteEnviado;
    }

    public void setLembreteEnviado(Boolean lembreteEnviado) {
        this.lembreteEnviado = lembreteEnviado;
    }

    public String getAlexaReminderId() {
        return alexaReminderId;
    }

    public void setAlexaReminderId(String alexaReminderId) {
        this.alexaReminderId = alexaReminderId;
    }
}
