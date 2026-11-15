package com.lumina.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "especialidade")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Especialidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idEspecialidade;

    @Column(nullable = false, length = 45)
    private String nome;
}
