package com.lumina.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "convenios")
public class Convenio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idConvenio;

    private String nome;

    public Convenio() {
    }

    public Convenio(String nome) {
        this.nome = nome;
    }

    public Convenio(Long idConvenio, String nome) {
        this.idConvenio = idConvenio;
        this.nome = nome;
    }

    public Long getIdConvenio() {
        return idConvenio;
    }

    public void setIdConvenio(Long idConvenio) {
        this.idConvenio = idConvenio;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
