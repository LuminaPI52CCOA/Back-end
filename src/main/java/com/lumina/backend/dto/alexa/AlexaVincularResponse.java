package com.lumina.backend.dto.alexa;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta do vínculo com o dispositivo Alexa")
public record AlexaVincularResponse(
        @Schema(description = "Indica se o vínculo foi realizado com sucesso", example = "true")
        boolean sucesso,
        @Schema(description = "Mensagem descritiva do resultado", example = "Conta vinculada com sucesso ao Dr(a). John Doe.")
        String mensagem,
        @Schema(description = "Nome do dentista vinculado", example = "John Doe")
        String dentistaNome
) {
}
