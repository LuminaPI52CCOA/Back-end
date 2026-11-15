package com.lumina.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuario_alexa")
@Getter
@Setter
@NoArgsConstructor
public class UsuarioAlexa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario_alexa")
    private Long idUsuarioAlexa;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fk_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "alexa_user_id", unique = true, nullable = false)
    private String alexaUserId;

    @Column(name = "api_endpoint")
    private String apiEndpoint = "https://api.amazonalexa.com";

    @Column(name = "codigo_pareamento", length = 6)
    private String codigoPareamento;

    @Column(name = "codigo_expiracao")
    private LocalDateTime codigoExpiracao;

    @Column(name = "ativo")
    private Boolean ativo = true;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @PrePersist
    public void prePersist() {
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
        if (this.ativo == null) {
            this.ativo = true;
        }
        if (this.apiEndpoint == null || this.apiEndpoint.isBlank()) {
            this.apiEndpoint = "https://api.amazonalexa.com";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }

    public UsuarioAlexa(Usuario usuario, String alexaUserId, String apiEndpoint) {
        this.usuario = usuario;
        this.alexaUserId = alexaUserId;
        this.apiEndpoint = (apiEndpoint != null && !apiEndpoint.isBlank()) ? apiEndpoint : "https://api.amazonalexa.com";
        this.ativo = true;
    }
}
