package com.lumina.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Catálogo de dentes permanentes na numeração FDI (tabela de domínio "dente").
 */
@Entity
@Table(name = "dente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
}
