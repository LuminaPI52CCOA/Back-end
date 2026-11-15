package com.lumina.backend.dto.alexa;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para vincular dispositivo Alexa à conta do dentista")
public record AlexaVincularRequest(
        @NotBlank(message = "O código de pareamento é obrigatório")
        @Schema(description = "Código PIN de 6 dígitos", example = "482913")
        String codigo,
        @NotBlank(message = "O identificador do usuário Alexa é obrigatório")
        @Schema(description = "Identificador único do usuário Alexa retornado no request envelope", example = "amzn1.ask.account.XYZ123")
        String alexaUserId,
        @Schema(description = "Endpoint da API da Alexa para envio de notificações/lembretes", example = "https://api.amazonalexa.com")
        String apiEndpoint
) {
}
