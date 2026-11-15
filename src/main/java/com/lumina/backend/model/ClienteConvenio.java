package com.lumina.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cliente_convenio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
}
