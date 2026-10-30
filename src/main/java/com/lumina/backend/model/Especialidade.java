package com.lumina.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "especialidade")
public class Especialidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idEspecialidade;

    @Column(nullable = false, length = 45)
    private String nome;

    public Especialidade() {
    }

    public Especialidade(Integer idEspecialidade, String nome) {
        this.idEspecialidade = idEspecialidade;
        this.nome = nome;
    }

    public Integer getIdEspecialidade() {
        return idEspecialidade;
    }

    public void setIdEspecialidade(Integer idEspecialidade) {
        this.idEspecialidade = idEspecialidade;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
