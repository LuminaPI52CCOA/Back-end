package com.lumina.backend.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "consulta")
@Getter
@Setter
@NoArgsConstructor
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idConsulta;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fk_cliente")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fk_usuario")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fk_especialidade")
    private Especialidade especialidade;

    private LocalDate data;

    private LocalTime horarioInicio;

    private LocalTime horarioFim;

    @Column(name = "status")
    private String status = "AGENDADA";

    @Column(name = "lembrete_enviado")
    private Boolean lembreteEnviado = false;

    @Column(name = "alexa_reminder_id")
    private String alexaReminderId;

    @Column(name = "observacoes", length = 500)
    private String observacoes;

    @Column(name = "tipo_dente", length = 45)
    private String tipoDente;

    @Column(name = "pular_odontograma", nullable = false)
    private Boolean pularOdontograma = false;

    // Somente a seleção do odontograma; procedimentos continuam em consulta_procedimento.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "consulta_dente", joinColumns = @JoinColumn(name = "fk_consulta"))
    @Column(name = "fk_dente", nullable = false)
    @OrderBy
    @Fetch(FetchMode.SUBSELECT)
    @Setter(AccessLevel.NONE)
    private Set<Integer> dentes = new LinkedHashSet<>();

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

    public void setDentes(Collection<Integer> dentes) {
        if (this.dentes == null) {
            this.dentes = new LinkedHashSet<>();
        }
        this.dentes.clear();
        if (dentes != null) {
            this.dentes.addAll(dentes);
        }
    }
}
