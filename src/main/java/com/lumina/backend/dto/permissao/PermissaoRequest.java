package com.lumina.backend.dto.permissao;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de requisicao para ajuste de permissoes e nivel de acesso do usuario/dentista")
public class PermissaoRequest {

    @Schema(description = "Identificador do perfil de acesso (1 = ADMIN, 2 = DENTISTA, 3 = RECEPCIONISTA)", example = "2", type = "integer", format = "int32")
    private Integer fkPerfil;

    @Schema(description = "Indica se o usuario possui acesso ativo ao sistema", example = "true", type = "boolean")
    private Boolean ativo;

    @Schema(description = "Nivel de acesso selecionado na tela de permissoes ('todos', 'restrito', 'nenhum')", example = "todos", type = "string")
    private String nivelAcesso;

    public PermissaoRequest() {
    }

    public PermissaoRequest(Integer fkPerfil, Boolean ativo, String nivelAcesso) {
        this.fkPerfil = fkPerfil;
        this.ativo = ativo;
        this.nivelAcesso = nivelAcesso;
    }

    public Integer getFkPerfil() {
        return fkPerfil;
    }

    public void setFkPerfil(Integer fkPerfil) {
        this.fkPerfil = fkPerfil;
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
