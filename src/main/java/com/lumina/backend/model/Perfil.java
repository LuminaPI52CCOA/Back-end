package com.lumina.backend.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
@Schema(description = "Perfil de acesso utilizado para classificar permissoes de usuarios")
public class Perfil {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador do perfil", example = "1", type = "integer", format = "int32")
    private Integer idPerfil;
    @Schema(description = "Nome do perfil", example = "ADMIN", type = "string")
    private String nome;

    public Perfil(Integer id, String nome) {
        this.idPerfil = id;
        this.nome = nome;
    }

    public Perfil(){

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
