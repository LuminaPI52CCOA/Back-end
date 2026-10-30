package com.lumina.backend.dto.midia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TipoMidiaRequest {

    @NotBlank(message = "O nome do tipo de mídia é obrigatório")
    @Size(max = 100, message = "O nome do tipo de mídia deve ter no máximo 100 caracteres")
    private String nome;

    @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
    private String descricao;

    public TipoMidiaRequest() {
    }

    public TipoMidiaRequest(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
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
}
