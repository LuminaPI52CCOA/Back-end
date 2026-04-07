package com.lumina.backend.dto.perfil;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class PerfilResponse {
    private Integer idPerfil;
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
