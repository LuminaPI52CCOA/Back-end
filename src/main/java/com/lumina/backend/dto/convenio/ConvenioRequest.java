package com.lumina.backend.dto.convenio;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ConvenioRequest {

    @NotBlank
    private String nome;

    public ConvenioRequest() {
    }

    public ConvenioRequest(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
