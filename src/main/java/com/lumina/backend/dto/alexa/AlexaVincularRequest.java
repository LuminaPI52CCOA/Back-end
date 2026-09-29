package com.lumina.backend.dto.alexa;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para vincular dispositivo Alexa à conta do dentista")
public class AlexaVincularRequest {

    @NotBlank(message = "O código de pareamento é obrigatório")
    @Schema(description = "Código PIN de 6 dígitos", example = "482913")
    private String codigo;

    @NotBlank(message = "O identificador do usuário Alexa é obrigatório")
    @Schema(description = "Identificador único do usuário Alexa retornado no request envelope", example = "amzn1.ask.account.XYZ123")
    private String alexaUserId;

    @Schema(description = "Endpoint da API da Alexa para envio de notificações/lembretes", example = "https://api.amazonalexa.com")
    private String apiEndpoint;

    public AlexaVincularRequest() {
    }

    public AlexaVincularRequest(String codigo, String alexaUserId, String apiEndpoint) {
        this.codigo = codigo;
        this.alexaUserId = alexaUserId;
        this.apiEndpoint = apiEndpoint;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getAlexaUserId() {
        return alexaUserId;
    }

    public void setAlexaUserId(String alexaUserId) {
        this.alexaUserId = alexaUserId;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }
}
