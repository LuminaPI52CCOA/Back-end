package com.lumina.backend.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta minima de dentista ativo para seletores de agendamento")
public class DentistaOpcaoResponse {

    @Schema(description = "Identificador do usuario dentista", example = "2", type = "integer", format = "int64")
    private Long idUsuario;

    @Schema(description = "Nome do dentista", example = "Dra. Ana Maria Souza")
    private String nome;

    @Schema(description = "Registro CRO", example = "SP-654321")
    private String cro;

    public DentistaOpcaoResponse() {
    }

    public DentistaOpcaoResponse(Long idUsuario, String nome, String cro) {
        this.idUsuario = idUsuario;
        this.nome = nome;
        this.cro = cro;
    }

    public static DentistaOpcaoResponse of(com.lumina.backend.model.Usuario usuario) {
        return new DentistaOpcaoResponse(usuario.getIdUsuario(), usuario.getNome(), usuario.getCro());
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

    public String getCro() {
        return cro;
    }

    public void setCro(String cro) {
        this.cro = cro;
    }
}
