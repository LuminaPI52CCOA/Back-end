package com.lumina.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "cliente_midia")
@Getter
@Setter
@NoArgsConstructor
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
}
