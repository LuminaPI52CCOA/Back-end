package com.lumina.backend.dto.alexa;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta de geração do PIN de pareamento para a Alexa")
public class AlexaGerarPinResponse {

    @Schema(description = "Código PIN de 6 dígitos", example = "482913")
    private String codigo;

    @Schema(description = "Tempo de expiração do PIN em minutos", example = "10")
    private Integer expiraEmMinutos;

    @Schema(description = "Instrução para o dentista", example = "Diga à Alexa: 'Alexa, abra a Lumina e vincule o código 482913'")
    private String mensagem;

    public AlexaGerarPinResponse() {
    }

    public AlexaGerarPinResponse(String codigo, Integer expiraEmMinutos, String mensagem) {
        this.codigo = codigo;
        this.expiraEmMinutos = expiraEmMinutos;
        this.mensagem = mensagem;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Integer getExpiraEmMinutos() {
        return expiraEmMinutos;
    }

    public void setExpiraEmMinutos(Integer expiraEmMinutos) {
        this.expiraEmMinutos = expiraEmMinutos;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
}
