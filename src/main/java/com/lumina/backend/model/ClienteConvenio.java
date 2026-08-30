package com.lumina.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "clientes_convenios")
public class ClienteConvenio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idClienteConvenio;

    @ManyToOne
    @JoinColumn(name = "fk_cliente")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "fk_convenio")
    private Convenio convenio;

    private String numeroInscricao;

    public ClienteConvenio() {
    }

    public ClienteConvenio(Long idClienteConvenio, Cliente cliente, Convenio convenio, String numeroInscricao) {
        this.idClienteConvenio = idClienteConvenio;
        this.cliente = cliente;
        this.convenio = convenio;
        this.numeroInscricao = numeroInscricao;
    }

    public Long getIdClienteConvenio() {
        return idClienteConvenio;
    }

    public void setIdClienteConvenio(Long idClienteConvenio) {
        this.idClienteConvenio = idClienteConvenio;
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
