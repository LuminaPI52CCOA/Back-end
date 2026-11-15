package com.lumina.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "convenio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Convenio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idConvenio;

    private String nome;

    public Convenio(String nome) {
        this.nome = nome;
    }
}
