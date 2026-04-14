package com.lumina.backend.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.validator.constraints.br.CPF;

@Schema(description = "Dados de entrada para cadastro e atualizacao de usuario")
public class UsuarioRequest {
    @Schema(description = "Identificador unico do usuario", example = "1", type = "integer", format = "int32")
    private Integer idUsuario;

    @NotBlank
    @Schema(description = "Nome completo do usuario", example = "Ana Maria Souza", type = "string")
    private String nome;

    @NotBlank
    @CPF
    @Schema(description = "CPF do usuario contendo apenas numeros", example = "12345678901", type = "string")
    private String cpf;

    @NotBlank
    @Email
    @Schema(description = "Email valido para acesso do usuario", example = "ana.souza@lumina.com", type = "string", format = "email")
    private String email;

    @NotBlank
    @Schema(description = "Senha de acesso do usuario", example = "Senha@123", type = "string")
    private String senha;

    @NotNull
    @Schema(description = "Identificador do perfil vinculado ao usuario", example = "2", type = "integer", format = "int32")
    private Integer fkPerfil;

    @NotBlank
    @Schema(description = "Codigo CRO do profissional, quando aplicavel", example = "SP-CD-12345", type = "string")
    private String cro;

    @NotNull
    @Schema(description = "Indica se o usuario esta ativo no sistema", example = "true", type = "boolean")
    private Boolean ativo;

    public UsuarioRequest(){

    }

    public UsuarioRequest(Integer idUsuario, String nome, String cpf, String email, String senha, Integer fkPerfil, String cro, Boolean ativo) {
        this.idUsuario = idUsuario;
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.senha = senha;
        this.fkPerfil = fkPerfil;
        this.cro = cro;
        this.ativo = ativo;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Integer getFkPerfil() {
        return fkPerfil;
    }

    public void setFkPerfil(Integer fkPerfil) {
        this.fkPerfil = fkPerfil;
    }

    public String getCro() {
        return cro;
    }

    public void setCro(String cro) {
        this.cro = cro;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

}
