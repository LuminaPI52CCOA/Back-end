package com.lumina.backend.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de sessao retornados apos autenticacao")
public class UsuarioSessaoDto {

    @Schema(description = "Identificador do usuario autenticado", example = "1", type = "integer", format = "int64")
    private Long userId;
    @Schema(description = "Nome do usuario autenticado", example = "Ana Maria Souza", type = "string")
    private String nome;
    @Schema(description = "Email do usuario autenticado", example = "ana.souza@lumina.com", type = "string", format = "email")
    private String email;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
