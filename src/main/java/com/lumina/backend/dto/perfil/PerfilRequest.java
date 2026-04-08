package com.lumina.backend.dto.perfil;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;

public class PerfilRequest {
    private Integer idPerfil;
    @NotBlank
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
