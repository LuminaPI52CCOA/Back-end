package com.lumina.backend.model;

import jakarta.persistence.Entity;

@Entity
public class ClienteConvenio {
    private Cliente cliente;
    private Convenio convenio;
    private String numeroInscricao;

    public ClienteConvenio() {
    }

    public ClienteConvenio(String numeroInscricao, Convenio convenio, Cliente cliente) {
        this.numeroInscricao = numeroInscricao;
        this.convenio = convenio;
        this.cliente = cliente;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Convenio getConvenio() {
        return convenio;
    }

    public void setConvenio(Convenio convenio) {
        this.convenio = convenio;
    }

    public String getNumeroInscricao() {
        return numeroInscricao;
    }

    public void setNumeroInscricao(String numeroInscricao) {
        this.numeroInscricao = numeroInscricao;
    }
}
