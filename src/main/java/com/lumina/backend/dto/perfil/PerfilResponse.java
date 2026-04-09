package com.lumina.backend.dto.perfil;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de resposta de perfil")
public class PerfilResponse {
    @Schema(description = "Identificador unico do perfil", example = "1", type = "integer", format = "int32")
    private Integer idPerfil;

    @Schema(description = "Nome do perfil de acesso", example = "ADMIN", type = "string")
    private String nome;

    public PerfilResponse(Integer idPerfil, String nome) {
        this.idPerfil = idPerfil;
        this.nome = nome;
    }

    public PerfilResponse(){

    }

    public Integer getIdPerfil() {
        return idPerfil;
    }

    public void setIdPerfil(Integer idPerfil) {
        this.idPerfil = idPerfil;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
