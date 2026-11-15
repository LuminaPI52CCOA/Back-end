package com.lumina.backend.dto.alexa;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Resposta contendo o status de conexão da Alexa com o dentista")
public record AlexaStatusResponse(
        @Schema(description = "Indica se o dentista possui um dispositivo Alexa conectado", example = "true")
        Boolean conectado,
        @Schema(description = "Identificador único do dispositivo Alexa", example = "amzn1.ask.account.TESTE")
        String alexaUserId,
        @Schema(description = "Endpoint da API da Alexa", example = "https://api.amazonalexa.com")
        String apiEndpoint,
        @Schema(description = "Data e hora em que a conexão foi realizada", example = "2026-09-24T14:30:00")
        LocalDateTime vinculadoEm,
        @Schema(description = "Nome do dentista associado", example = "Dr. Ricardo Alves")
        String dentistaNome
) {
}
