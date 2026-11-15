package com.lumina.backend.exception.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Estrutura padronizada para retorno de erros da API")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroRespostaDto(
        @Schema(description = "Timestamp do momento do erro", example = "2026-08-30T17:45:00")
        LocalDateTime timestamp,
        @Schema(description = "Codigo de status HTTP", example = "400")
        Integer status,
        @Schema(description = "Descricao do status HTTP", example = "Bad Request")
        String erro,
        @Schema(description = "Mensagem explicativa do erro", example = "Dados invalidos.")
        String mensagem,
        @Schema(description = "URI da requisicao que gerou o erro", example = "/clientes")
        String caminho,
        @Schema(description = "Lista detalhada de erros de validacao por campo")
        Map<String, String> campos
) {
    public ErroRespostaDto() {
        this(LocalDateTime.now(), null, null, null, null, null);
    }

    public ErroRespostaDto(Integer status, String erro, String mensagem, String caminho) {
        this(LocalDateTime.now(), status, erro, mensagem, caminho, null);
    }

    public ErroRespostaDto(Integer status, String erro, String mensagem, String caminho, Map<String, String> campos) {
        this(LocalDateTime.now(), status, erro, mensagem, caminho, campos);
    }
}
