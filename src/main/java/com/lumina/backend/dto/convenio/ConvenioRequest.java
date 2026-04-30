package com.lumina.backend.dto.convenio;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados de entrada para cadastro e atualizacao de convenio")
public class ConvenioRequest {

    @NotBlank
    @Schema(description = "Nome do convenio", example = "Unimed", type = "string")
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
