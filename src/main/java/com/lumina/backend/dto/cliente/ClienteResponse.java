package com.lumina.backend.dto.cliente;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Dados de resposta de cliente")
public class ClienteResponse {

    @Schema(description = "Identificador do cliente", example = "1", type = "integer", format = "int64")
    private Long idCliente;
    @Schema(description = "Nome completo do cliente", example = "Maria Oliveira", type = "string")
    private String nome;
    @Schema(description = "CPF do cliente", example = "12345678901", type = "string")
    private String cpf;
    @Schema(description = "RG do cliente", example = "123456789", type = "string")
    private String rg;
    @Schema(description = "Data de nascimento do cliente", example = "1990-05-10", type = "string", format = "date")
    private LocalDate dataNascimento;
    @Schema(description = "Numero de celular do cliente", example = "11999998888", type = "string")
    private String numeroCelular;
    @Schema(description = "Email do cliente", example = "maria.oliveira@lumina.com", type = "string", format = "email")
    private String email;
    @Schema(description = "Sexo do cliente", example = "F", type = "string")
    private Character sexo;
    @Schema(description = "Naturalidade do cliente", example = "Sao Paulo", type = "string")
    private String naturalidade;
    @Schema(description = "Nacionalidade do cliente", example = "Brasileira", type = "string")
    private String nacionalidade;
    @Schema(description = "ID do estado civil do cliente", example = "1", type = "integer", format = "int32")
    private Integer fkEstadoCivil;
    @Schema(description = "Endereco residencial do cliente", example = "Rua A, 100 - Centro", type = "string")
    private String enderecoResidencial;
    @Schema(description = "CEP do cliente", example = "09090000", type = "string")
    private String cep;
    @Schema(description = "ID do cliente que indicou", example = "2", type = "integer", format = "int32")
    private Integer fkClienteIndicacao;
    @Schema(description = "ID do responsavel quando aplicavel", example = "3", type = "integer", format = "int32")
    private Integer fkResponsavel;
    @Schema(description = "Grau de parentesco do responsavel", example = "Mae", type = "string")
    private String grauParentescoResponsavel;

    public ClienteResponse(Long idCliente, String nome, String cpf, String rg, LocalDate dataNascimento, String numeroCelular, String email, Character sexo, String naturalidade, String nacionalidade, Integer fkEstadoCivil, String enderecoResidencial, String cep, Integer fkClienteIndicacao, Integer fkResponsavel, String grauParentescoResponsavel) {
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

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
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
