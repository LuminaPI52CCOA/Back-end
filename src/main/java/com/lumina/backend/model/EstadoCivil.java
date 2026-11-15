package com.lumina.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "estado_civil")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EstadoCivil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idEstadoCivil;
    private String descricao;
}
