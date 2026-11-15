package com.lumina.backend.dto.midia;

import com.lumina.backend.model.TipoMidia;

public record TipoMidiaResponse(
        Long id,
        String nome,
        String descricao,
        Boolean personalizado
) {
    public static TipoMidiaResponse fromEntity(TipoMidia entity) {
        if (entity == null) {
            return null;
        }
        return new TipoMidiaResponse(
                entity.getIdTipoMidia(),
                entity.getNome(),
                entity.getDescricao(),
                entity.getUsuario() != null
        );
    }
}
