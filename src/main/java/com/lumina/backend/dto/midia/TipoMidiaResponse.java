package com.lumina.backend.dto.midia;

import com.lumina.backend.model.TipoMidia;

public class TipoMidiaResponse {

    private Long id;
    private String nome;
    private String descricao;
    private Boolean personalizado;

    public TipoMidiaResponse() {
    }

    public TipoMidiaResponse(Long id, String nome, String descricao, Boolean personalizado) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.personalizado = personalizado;
    }

    public static TipoMidiaResponse fromEntity(TipoMidia entity) {
        if (entity == null) return null;
        return new TipoMidiaResponse(
                entity.getIdTipoMidia(),
                entity.getNome(),
                entity.getDescricao(),
                entity.getUsuario() != null
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Boolean getPersonalizado() {
        return personalizado;
    }

    public void setPersonalizado(Boolean personalizado) {
        this.personalizado = personalizado;
    }
}
