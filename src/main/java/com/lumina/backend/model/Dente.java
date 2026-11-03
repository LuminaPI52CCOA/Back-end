package com.lumina.backend.model;

import jakarta.persistence.*;

/**
 * Catálogo de dentes permanentes na numeração FDI (tabela de domínio "dente").
 */
@Entity
@Table(name = "dente")
public class Dente {

    @Id
    @Column(name = "numero_dente")
    private Integer numeroDente;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 15)
    private String arcada;

    @Column(nullable = false)
    private Integer quadrante;

    public Dente() {
    }

    public Dente(Integer numeroDente, String nome, String arcada, Integer quadrante) {
        this.numeroDente = numeroDente;
        this.nome = nome;
        this.arcada = arcada;
        this.quadrante = quadrante;
    }

    public Integer getNumeroDente() {
        return numeroDente;
    }

    public void setNumeroDente(Integer numeroDente) {
        this.numeroDente = numeroDente;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getArcada() {
        return arcada;
    }

    public void setArcada(String arcada) {
        this.arcada = arcada;
    }

    public Integer getQuadrante() {
        return quadrante;
    }

    public void setQuadrante(Integer quadrante) {
        this.quadrante = quadrante;
    }
}
