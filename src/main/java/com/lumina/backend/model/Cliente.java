package com.lumina.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.Period;

@Entity
@Table(name = "cliente")
@Getter
@Setter
@NoArgsConstructor
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCliente;
    private String nome;
    private Integer fkEstadoCivil;
    private String cpf;
    private String rg;
    private LocalDate dataNascimento;
    private String naturalidade;
    private String nacionalidade;
    private Character sexo;
    private String cep;
    private String enderecoResidencial;
    private String email;
    private String numeroCelular;

    @ManyToOne
    @JoinColumn(name = "fk_cliente_indicacao")
    private Cliente clienteIndicacao;

    @ManyToOne
    @JoinColumn(name = "fk_responsavel")
    private Cliente responsavel;
    private String grauParentescoResponsavel;

    @Column(name = "ativo")
    private Boolean ativo;

    public boolean ehMaiorDeIdade() {
        if (this.dataNascimento == null) {
            return false;
        }
        return Period.between(this.dataNascimento, LocalDate.now()).getYears() >= 18;
    }
}
