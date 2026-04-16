package com.lumina.backend.dto.cliente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class ClienteRequest {

    private Integer idCliente;
    @NotBlank
    private String nome;
    @NotBlank
    private String cpf;
    @NotBlank
    private String rg;
    @NotBlank
    private LocalDate dataNascimento;
    @NotBlank
    private String numeroCelular;
    @NotBlank
    private String email;
    @NotBlank
    private Character sexo;
    @NotBlank
    private String naturalidade;
    @NotBlank
    private String nacionalidade;
    @NotNull
    private Integer fkEstadoCivil;
    @NotBlank
    private String enderecoResidencial;
    @NotBlank
    private String cep;
    @NotNull
    private Integer fkClienteIndicacao;
    @NotNull
    private Integer fkResponsavel;
    @NotBlank
    private String grauParentescoResponsavel;

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
