package com.lumina.backend.dto.permissao;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de resposta do gerenciamento de permissoes do usuario/dentista")
public class PermissaoResponse {

    @Schema(description = "Identificador unico do usuario", example = "1", type = "integer", format = "int64")
    private Long idUsuario;

    @Schema(description = "Nome completo do profissional", example = "Dra. Ana Maria Souza", type = "string")
    private String nome;

    @Schema(description = "CPF do profissional", example = "12345678901", type = "string")
    private String cpf;

    @Schema(description = "Email do profissional", example = "ana.souza@lumina.com", type = "string", format = "email")
    private String email;

    @Schema(description = "Codigo CRO do dentista", example = "SP-CD-12345", type = "string")
    private String cro;

    @Schema(description = "Identificador do perfil vinculado (1 = ADMIN, 2 = DENTISTA, 3 = RECEPCIONISTA)", example = "2", type = "integer", format = "int32")
    private Integer fkPerfil;

    @Schema(description = "Nome do perfil de acesso", example = "DENTISTA", type = "string")
    private String nomePerfil;

    @Schema(description = "Indica se o profissional esta ativo no sistema", example = "true", type = "boolean")
    private Boolean ativo;

    @Schema(description = "Nivel de acesso atual ('todos', 'restrito', 'nenhum')", example = "restrito", type = "string")
    private String nivelAcesso;

    public PermissaoResponse() {
    }

    public PermissaoResponse(Long idUsuario, String nome, String cpf, String email, String cro,
                             Integer fkPerfil, String nomePerfil, Boolean ativo, String nivelAcesso) {
        this.idUsuario = idUsuario;
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.cro = cro;
        this.fkPerfil = fkPerfil;
        this.nomePerfil = nomePerfil;
        this.ativo = ativo;
        this.nivelAcesso = nivelAcesso;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
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

    public String getCro() {
        return cro;
    }

    public void setCro(String cro) {
        this.cro = cro;
    }

    public Integer getFkPerfil() {
        return fkPerfil;
    }

    public void setFkPerfil(Integer fkPerfil) {
        this.fkPerfil = fkPerfil;
    }

    public String getNomePerfil() {
        return nomePerfil;
    }

    public void setNomePerfil(String nomePerfil) {
        this.nomePerfil = nomePerfil;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public String getNivelAcesso() {
        return nivelAcesso;
    }

    public void setNivelAcesso(String nivelAcesso) {
        this.nivelAcesso = nivelAcesso;
    }
}
