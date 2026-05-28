package com.lumina.backend.dto.perfil;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de entrada para cadastro e atualizacao de perfil")
public class PerfilRequest {
    @Schema(description = "Identificador unico do perfil", example = "1", type = "integer", format = "int32")
    private Integer idPerfil;

    @NotBlank
    @Schema(description = "Nome do perfil de acesso", example = "ADMIN", type = "string")
    private String nome;

    public PerfilRequest(Integer idPerfil, String nome) {
        this.idPerfil = idPerfil;
        this.nome = nome;
    }

    public PerfilRequest(){

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
