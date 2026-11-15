package com.lumina.backend.dto.alexa;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta de geração do PIN de pareamento para a Alexa")
public record AlexaGerarPinResponse(
        @Schema(description = "Código PIN de 6 dígitos", example = "482913")
        String codigo,
        @Schema(description = "Tempo de expiração do PIN em minutos", example = "10")
        Integer expiraEmMinutos,
        @Schema(description = "Instrução para o dentista", example = "Diga à Alexa: 'Alexa, abra a Lumina e vincule o código 482913'")
        String mensagem
) {
}
