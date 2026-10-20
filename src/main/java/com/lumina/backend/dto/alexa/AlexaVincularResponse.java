package com.lumina.backend.dto.alexa;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta do vínculo com o dispositivo Alexa")
public class AlexaVincularResponse {

    @Schema(description = "Indica se o vínculo foi realizado com sucesso", example = "true")
    private boolean sucesso;

    @Schema(description = "Mensagem descritiva do resultado", example = "Conta vinculada com sucesso ao Dr(a). John Doe.")
    private String mensagem;

    @Schema(description = "Nome do dentista vinculado", example = "John Doe")
    private String dentistaNome;

    public AlexaVincularResponse() {
    }

    public AlexaVincularResponse(boolean sucesso, String mensagem, String dentistaNome) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
        this.dentistaNome = dentistaNome;
    }

    public boolean isSucesso() {
        return sucesso;
    }

    public void setSucesso(boolean sucesso) {
        this.sucesso = sucesso;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getDentistaNome() {
        return dentistaNome;
    }

    public void setDentistaNome(String dentistaNome) {
        this.dentistaNome = dentistaNome;
    }
}
