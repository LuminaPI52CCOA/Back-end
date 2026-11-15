package com.lumina.backend.dto.midia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TipoMidiaRequest(
        @NotBlank(message = "O nome do tipo de mídia é obrigatório")
        @Size(max = 100, message = "O nome do tipo de mídia deve ter no máximo 100 caracteres")
        String nome,
        @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
        String descricao
) {
}
