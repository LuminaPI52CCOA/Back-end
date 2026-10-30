package com.lumina.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "cliente_midia")
public class ClienteMidia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente_midia")
    private Long idClienteMidia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fk_tipo_midia", nullable = false)
    private TipoMidia tipoMidia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_consulta")
    private Consulta consulta;

    @Column(name = "chave_s3", nullable = false, length = 500, unique = true)
    private String chaveS3;

    @Column(name = "nome_original", nullable = false, length = 255)
    private String nomeOriginal;

    @Column(nullable = false, length = 20)
    private String extensao;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "tamanho_bytes", nullable = false)
    private Long tamanhoBytes;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(length = 500)
    private String legenda;

    @Column(name = "ativo")
    private Boolean ativo = true;

    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @Column(name = "data_exclusao")
    private LocalDateTime dataExclusao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_usuario_exclusao")
    private Usuario usuarioExclusao;

    public ClienteMidia() {
    }

    @PrePersist
    protected void onCreate() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now(ZoneId.of("America/Sao_Paulo"));
        }
        if (atualizadoEm == null) {
            atualizadoEm = criadoEm;
        }
        if (status == null) {
            status = "PENDING";
        }
        if (ativo == null) {
            ativo = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        atualizadoEm = LocalDateTime.now(ZoneId.of("America/Sao_Paulo"));
    }

    public Long getIdClienteMidia() {
        return idClienteMidia;
    }

    public void setIdClienteMidia(Long idClienteMidia) {
        this.idClienteMidia = idClienteMidia;
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

    public TipoMidia getTipoMidia() {
        return tipoMidia;
    }

    public void setTipoMidia(TipoMidia tipoMidia) {
        this.tipoMidia = tipoMidia;
    }

    public Consulta getConsulta() {
        return consulta;
    }

    public void setConsulta(Consulta consulta) {
        this.consulta = consulta;
    }

    public String getChaveS3() {
        return chaveS3;
    }

    public void setChaveS3(String chaveS3) {
        this.chaveS3 = chaveS3;
    }

    public String getNomeOriginal() {
        return nomeOriginal;
    }

    public void setNomeOriginal(String nomeOriginal) {
        this.nomeOriginal = nomeOriginal;
    }

    public String getExtensao() {
        return extensao;
    }

    public void setExtensao(String extensao) {
        this.extensao = extensao;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getTamanhoBytes() {
        return tamanhoBytes;
    }

    public void setTamanhoBytes(Long tamanhoBytes) {
        this.tamanhoBytes = tamanhoBytes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLegenda() {
        return legenda;
    }

    public void setLegenda(String legenda) {
        this.legenda = legenda;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
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

    public LocalDateTime getDataExclusao() {
        return dataExclusao;
    }

    public void setDataExclusao(LocalDateTime dataExclusao) {
        this.dataExclusao = dataExclusao;
    }

    public Usuario getUsuarioExclusao() {
        return usuarioExclusao;
    }

    public void setUsuarioExclusao(Usuario usuarioExclusao) {
        this.usuarioExclusao = usuarioExclusao;
    }
}
