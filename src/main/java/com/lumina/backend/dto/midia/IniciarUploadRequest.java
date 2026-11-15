package com.lumina.backend.dto.midia;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record IniciarUploadRequest(
        @NotNull(message = "O ID do tipo de mídia é obrigatório")
        Long tipoId,
        Long consultaId,
        String legenda,
        @NotEmpty(message = "A lista de arquivos não pode estar vazia")
        @Valid
        List<UploadArquivoItemRequest> arquivos
) {
}
