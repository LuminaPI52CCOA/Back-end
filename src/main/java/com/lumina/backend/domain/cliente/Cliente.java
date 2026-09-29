package com.lumina.backend.domain.cliente;

import java.time.LocalDate;
import java.util.Objects;

public class Cliente {
    private final ClienteId id;
    private final String nome;
    private final String cpf;
    private final String rg;
    private final LocalDate dataNascimento;
    private final String naturalidade;
    private final String nacionalidade;
    private final Character sexo;
    private final String cep;
    private final String enderecoResidencial;
    private final String email;
    private final String numeroCelular;
    private final Integer fkEstadoCivil;
    private final Cliente clienteIndicacao;
    private final Cliente responsavel;
    private final String grauParentescoResponsavel;

    private Cliente(ClienteId id, String nome, String cpf, String rg, LocalDate dataNascimento,
                   String naturalidade, String nacionalidade, Character sexo, String cep,
                   String enderecoResidencial, String email, String numeroCelular,
                   Integer fkEstadoCivil, Cliente clienteIndicacao, Cliente responsavel,
                   String grauParentescoResponsavel) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.rg = rg;
        this.dataNascimento = dataNascimento;
        this.naturalidade = naturalidade;
        this.nacionalidade = nacionalidade;
        this.sexo = sexo;
        this.cep = cep;
        this.enderecoResidencial = enderecoResidencial;
        this.email = email;
        this.numeroCelular = numeroCelular;
        this.fkEstadoCivil = fkEstadoCivil;
        this.clienteIndicacao = clienteIndicacao;
        this.responsavel = responsavel;
        this.grauParentescoResponsavel = grauParentescoResponsavel;
    }

    public static Cliente criar(ClienteCommand command) {
        return new Cliente(
            ClienteId.generate(),
            command.nome(),
            command.cpf(),
            command.rg(),
            command.dataNascimento(),
            command.naturalidade(),
            command.nacionalidade(),
            command.sexo(),
            command.cep(),
            command.enderecoResidencial(),
            command.email(),
            command.numeroCelular(),
            command.fkEstadoCivil(),
            command.clienteIndicacao(),
            command.responsavel(),
            command.grauParentescoResponsavel()
        );
    }

    public static Cliente reconstruir(ClienteId id, ClienteCommand command) {
        return new Cliente(
            id,
            command.nome(),
            command.cpf(),
            command.rg(),
            command.dataNascimento(),
            command.naturalidade(),
            command.nacionalidade(),
            command.sexo(),
            command.cep(),
            command.enderecoResidencial(),
            command.email(),
            command.numeroCelular(),
            command.fkEstadoCivil(),
            command.clienteIndicacao(),
            command.responsavel(),
            command.grauParentescoResponsavel()
        );
    }

    public Cliente atualizar(ClienteCommand command) {
        return new Cliente(
            this.id,
            command.nome(),
            command.cpf(),
            command.rg(),
            command.dataNascimento(),
            command.naturalidade(),
            command.nacionalidade(),
            command.sexo(),
            command.cep(),
            command.enderecoResidencial(),
            command.email(),
            command.numeroCelular(),
            command.fkEstadoCivil(),
            command.clienteIndicacao(),
            command.responsavel(),
            command.grauParentescoResponsavel()
        );
    }

    public boolean ehMaiorDeIdade() {
        if (dataNascimento == null) {
            return false;
        }
        return LocalDate.now().minusYears(18).isAfter(dataNascimento) ||
               LocalDate.now().minusYears(18).isEqual(dataNascimento);
    }

    // Getters
    public ClienteId getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCpf() {
        return cpf;
    }

    public String getRg() {
        return rg;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public String getNaturalidade() {
        return naturalidade;
    }

    public String getNacionalidade() {
        return nacionalidade;
    }

    public Character getSexo() {
        return sexo;
    }

    public String getCep() {
        return cep;
    }

    public String getEnderecoResidencial() {
        return enderecoResidencial;
    }

    public String getEmail() {
        return email;
    }

    public String getNumeroCelular() {
        return numeroCelular;
    }

    public Integer getFkEstadoCivil() {
        return fkEstadoCivil;
    }

    public Cliente getClienteIndicacao() {
        return clienteIndicacao;
    }

    public Cliente getResponsavel() {
        return responsavel;
    }

    public String getGrauParentescoResponsavel() {
        return grauParentescoResponsavel;
    }
}