package com.lumina.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "tipo_midia")
@Getter
@Setter
@NoArgsConstructor
public class TipoMidia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_midia")
    private Long idTipoMidia;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 255)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_usuario")
    private Usuario usuario;

    @Column(name = "ativo")
    private Boolean ativo = true;

    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    public TipoMidia(Long idTipoMidia, String nome, String descricao, Usuario usuario, Boolean ativo) {
        this.idTipoMidia = idTipoMidia;
        this.nome = nome;
        this.descricao = descricao;
        this.usuario = usuario;
        this.ativo = ativo;
    }

    @PrePersist
    protected void onCreate() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now(ZoneId.of("America/Sao_Paulo"));
        }
        if (ativo == null) {
            ativo = true;
        }
    }
}
