package com.lumina.backend.dto.cliente;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "Dados de entrada para cadastro e atualizacao de cliente")
public class ClienteRequest {

    @Schema(description = "Identificador do cliente", example = "1", type = "integer", format = "int64")
    private Long idCliente;
    @NotBlank
    @Schema(description = "Nome completo do cliente", example = "Maria Oliveira", type = "string")
    private String nome;
    @NotNull
    @Schema(description = "ID do estado civil do cliente", example = "1", type = "integer", format = "int32")
    private Integer fkEstadoCivil;
    @NotBlank
    @Schema(description = "CPF do cliente", example = "12345678901", type = "string")
    private String cpf;
    @NotBlank
    @Schema(description = "RG do cliente", example = "123456789", type = "string")
    private String rg;
    @NotNull
    @JsonFormat(pattern = "dd/MM/yyyy")
    @Schema(description = "Data de nascimento do cliente", example = "1990-05-10", type = "string", format = "date")
    private LocalDate dataNascimento;
    @NotBlank
    @Schema(description = "Naturalidade do cliente", example = "Sao Paulo", type = "string")
    private String naturalidade;
    @NotBlank
    @Schema(description = "Nacionalidade do cliente", example = "Brasileira", type = "string")
    private String nacionalidade;
    @NotBlank
    @Schema(description = "Numero de celular do cliente", example = "11999998888", type = "string")
    private String numeroCelular;
    @NotBlank
    @Schema(description = "Email do cliente", example = "maria.oliveira@lumina.com", type = "string", format = "email")
    private String email;
    @NotNull
    @Schema(description = "Sexo do cliente", example = "F", type = "string")
    private Character sexo;
    @NotBlank
    @Schema(description = "Endereco residencial do cliente", example = "Rua A, 100 - Centro", type = "string")
    private String enderecoResidencial;
    @NotBlank
    @Schema(description = "CEP do cliente", example = "09090000", type = "string")
    private String cep;

    @Schema(description = "ID do cliente que indicou", example = "2", type = "integer", format = "int32")
    private Integer fkClienteIndicacao;

    @Schema(description = "Responsável do cliente")
    private ResponsavelRequest responsavel;
    @Schema(description = "Grau de parentesco do responsavel", example = "Mae", type = "string")
    private String grauParentescoResponsavel;

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

    public ResponsavelRequest getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(ResponsavelRequest responsavel) {
        this.responsavel = responsavel;
    }

    public String getGrauParentescoResponsavel() {
        return grauParentescoResponsavel;
    }

    public void setGrauParentescoResponsavel(String grauParentescoResponsavel) {
        this.grauParentescoResponsavel = grauParentescoResponsavel;
    }
}
