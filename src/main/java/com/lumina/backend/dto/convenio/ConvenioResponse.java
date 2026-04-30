package com.lumina.backend.dto.convenio;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de resposta de convenio")
public class ConvenioResponse {
    @Schema(description = "Identificador do convenio", example = "1", type = "integer", format = "int64")
    private Long id;
    @Schema(description = "Nome do convenio", example = "Unimed", type = "string")
    private String nome;

    public ConvenioResponse() {
    }

    public ConvenioResponse(Long id, String nome) {
        this.id = id;
        this.nome = nome;
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
}
