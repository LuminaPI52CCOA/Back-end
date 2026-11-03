package com.lumina.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

@Entity
@Table(
        name = "notificacao_whatsapp",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_notificacao_consulta", columnNames = "fk_consulta"),
                @UniqueConstraint(name = "uk_notificacao_token", columnNames = "token_confirmacao")
        },
        indexes = {
                @Index(name = "idx_notificacao_data_status", columnList = "data_consulta, status"),
                @Index(name = "idx_notificacao_token", columnList = "token_confirmacao")
        }
)
public class NotificacaoWhatsapp {

    public static final String STATUS_PENDENTE = "PENDENTE";
    public static final String STATUS_DISPARADO = "DISPARADO";
    public static final String STATUS_FALHA_CONTATO_INVALIDO = "FALHA_CONTATO_INVALIDO";
    public static final String STATUS_CANCELADO = "CANCELADO";

    private static final ZoneId ZONE_SAO_PAULO = ZoneId.of("America/Sao_Paulo");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacao")
    private Long idNotificacao;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fk_consulta", nullable = false, unique = true)
    private Consulta consulta;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fk_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fk_destinatario", nullable = false)
    private Cliente destinatario;

    @Column(name = "nome_destinatario", nullable = false, length = 100)
    private String nomeDestinatario;

    @Column(name = "grau_parentesco", length = 30)
    private String grauParentesco;

    @Column(name = "numero_telefone_higienizado", length = 20)
    private String numeroTelefoneHigienizado;

    @Column(name = "status", nullable = false, length = 30)
    private String status = STATUS_PENDENTE;

    @Column(name = "motivo_falha", length = 255)
    private String motivoFalha;

    @Column(name = "data_consulta", nullable = false)
    private LocalDate dataConsulta;

    @Column(name = "horario_consulta", nullable = false)
    private LocalTime horarioConsulta;

    @Column(name = "token_confirmacao", nullable = false, unique = true, length = 64)
    private String tokenConfirmacao;

    @Column(name = "limite_confirmacao_em", nullable = false)
    private LocalDateTime limiteConfirmacaoEm;

    @Column(name = "confirmado_em")
    private LocalDateTime confirmadoEm;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @Column(name = "disparado_em")
    private LocalDateTime disparadoEm;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fk_usuario_disparo")
    private Usuario usuarioDisparo;

    public NotificacaoWhatsapp() {
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime agora = LocalDateTime.now(ZONE_SAO_PAULO);
        if (this.criadoEm == null) {
            this.criadoEm = agora;
        }
        if (this.atualizadoEm == null) {
            this.atualizadoEm = agora;
        }
        if (this.status == null) {
            this.status = STATUS_PENDENTE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.atualizadoEm = LocalDateTime.now(ZONE_SAO_PAULO);
    }

    public Long getIdNotificacao() {
        return idNotificacao;
    }

    public void setIdNotificacao(Long idNotificacao) {
        this.idNotificacao = idNotificacao;
    }

    public Consulta getConsulta() {
        return consulta;
    }

    public void setConsulta(Consulta consulta) {
        this.consulta = consulta;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Cliente getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(Cliente destinatario) {
        this.destinatario = destinatario;
    }

    public String getNomeDestinatario() {
        return nomeDestinatario;
    }

    public void setNomeDestinatario(String nomeDestinatario) {
        this.nomeDestinatario = nomeDestinatario;
    }

    public String getGrauParentesco() {
        return grauParentesco;
    }

    public void setGrauParentesco(String grauParentesco) {
        this.grauParentesco = grauParentesco;
    }

    public String getNumeroTelefoneHigienizado() {
        return numeroTelefoneHigienizado;
    }

    public void setNumeroTelefoneHigienizado(String numeroTelefoneHigienizado) {
        this.numeroTelefoneHigienizado = numeroTelefoneHigienizado;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMotivoFalha() {
        return motivoFalha;
    }

    public void setMotivoFalha(String motivoFalha) {
        this.motivoFalha = motivoFalha;
    }

    public LocalDate getDataConsulta() {
        return dataConsulta;
    }

    public void setDataConsulta(LocalDate dataConsulta) {
        this.dataConsulta = dataConsulta;
    }

    public LocalTime getHorarioConsulta() {
        return horarioConsulta;
    }

    public void setHorarioConsulta(LocalTime horarioConsulta) {
        this.horarioConsulta = horarioConsulta;
    }

    public String getTokenConfirmacao() {
        return tokenConfirmacao;
    }

    public void setTokenConfirmacao(String tokenConfirmacao) {
        this.tokenConfirmacao = tokenConfirmacao;
    }

    public LocalDateTime getLimiteConfirmacaoEm() {
        return limiteConfirmacaoEm;
    }

    public void setLimiteConfirmacaoEm(LocalDateTime limiteConfirmacaoEm) {
        this.limiteConfirmacaoEm = limiteConfirmacaoEm;
    }

    public LocalDateTime getConfirmadoEm() {
        return confirmadoEm;
    }

    public void setConfirmadoEm(LocalDateTime confirmadoEm) {
        this.confirmadoEm = confirmadoEm;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    public LocalDateTime getDisparadoEm() {
        return disparadoEm;
    }

    public void setDisparadoEm(LocalDateTime disparadoEm) {
        this.disparadoEm = disparadoEm;
    }

    public Usuario getUsuarioDisparo() {
        return usuarioDisparo;
    }

    public void setUsuarioDisparo(Usuario usuarioDisparo) {
        this.usuarioDisparo = usuarioDisparo;
    }
}
