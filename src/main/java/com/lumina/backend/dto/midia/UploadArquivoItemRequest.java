package com.lumina.backend.dto.midia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UploadArquivoItemRequest(
        @NotBlank(message = "O nome original do arquivo é obrigatório")
        String nomeOriginal,
        @NotBlank(message = "O MIME type do arquivo é obrigatório")
        String mimeType,
        @NotNull(message = "O tamanho em bytes é obrigatório")
        @Positive(message = "O tamanho do arquivo deve ser maior que zero")
        Long tamanhoBytes
) {
}
