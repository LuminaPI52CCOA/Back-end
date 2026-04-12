package com.lumina.backend.dto.cliente;

import java.time.LocalDate;

public class ClienteResponse {

    private Integer idCliente;
    private String nome;
    private String cpf;
    private String rg;
    private LocalDate dataNascimento;
    private String numeroCelular;
    private String email;
    private Character sexo;
    private String naturalidade;
    private String nacionalidade;
    private Integer fkEstadoCivil;
    private String enderecoResidencial;
    private String cep;
    private Integer fkClienteIndicacao;
    private Integer fkResponsavel;
    private String grauParentescoResponsavel;

    public ClienteResponse(Integer idCliente, String nome, String cpf, String rg, LocalDate dataNascimento, String numeroCelular, String email, Character sexo, String naturalidade, String nacionalidade, Integer fkEstadoCivil, String enderecoResidencial, String cep, Integer fkClienteIndicacao, Integer fkResponsavel, String grauParentescoResponsavel) {
        this.idCliente = idCliente;
        this.nome = nome;
        this.cpf = cpf;
        this.rg = rg;
        this.dataNascimento = dataNascimento;
        this.numeroCelular = numeroCelular;
        this.email = email;
        this.sexo = sexo;
        this.naturalidade = naturalidade;
        this.nacionalidade = nacionalidade;
        this.fkEstadoCivil = fkEstadoCivil;
        this.enderecoResidencial = enderecoResidencial;
        this.cep = cep;
        this.fkClienteIndicacao = fkClienteIndicacao;
        this.fkResponsavel = fkResponsavel;
        this.grauParentescoResponsavel = grauParentescoResponsavel;
    }

    public ClienteResponse(){

    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getRg() {
        return rg;
    }

    public void setRg(String rg) {
        this.rg = rg;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getNumeroCelular() {
        return numeroCelular;
    }

    public void setNumeroCelular(String numeroCelular) {
        this.numeroCelular = numeroCelular;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Character getSexo() {
        return sexo;
    }

    public void setSexo(Character sexo) {
        this.sexo = sexo;
    }

    public String getNaturalidade() {
        return naturalidade;
    }

    public void setNaturalidade(String naturalidade) {
        this.naturalidade = naturalidade;
    }

    public String getNacionalidade() {
        return nacionalidade;
    }

    public void setNacionalidade(String nacionalidade) {
        this.nacionalidade = nacionalidade;
    }

    public Integer getFkEstadoCivil() {
        return fkEstadoCivil;
    }

    public void setFkEstadoCivil(Integer fkEstadoCivil) {
        this.fkEstadoCivil = fkEstadoCivil;
    }

    public String getEnderecoResidencial() {
        return enderecoResidencial;
    }

    public void setEnderecoResidencial(String enderecoResidencial) {
        this.enderecoResidencial = enderecoResidencial;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public Integer getFkClienteIndicacao() {
        return fkClienteIndicacao;
    }

    public void setFkClienteIndicacao(Integer fkClienteIndicacao) {
        this.fkClienteIndicacao = fkClienteIndicacao;
    }

    public Integer getFkResponsavel() {
        return fkResponsavel;
    }

    public void setFkResponsavel(Integer fkResponsavel) {
        this.fkResponsavel = fkResponsavel;
    }

    public String getGrauParentescoResponsavel() {
        return grauParentescoResponsavel;
    }

    public void setGrauParentescoResponsavel(String grauParentescoResponsavel) {
        this.grauParentescoResponsavel = grauParentescoResponsavel;
    }
}
