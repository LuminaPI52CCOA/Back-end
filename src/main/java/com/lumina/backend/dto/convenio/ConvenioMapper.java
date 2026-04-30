package com.lumina.backend.dto.convenio;

import com.lumina.backend.model.Convenio;

import java.util.List;

public class ConvenioMapper {
    public static ConvenioResponse toResponse(Convenio convenio) {
        if(convenio == null) {
            return null;
        }

        return new ConvenioResponse(
                convenio.getId(),
                convenio.getNome()
        );
    }

    public static List<ConvenioResponse> toResponse(List<Convenio> convenios) {
        return convenios.stream()
                .map(ConvenioMapper::toResponse)
                .toList();
    }

    public static Convenio toEntity(ConvenioRequest convenioRequest) {
        if(convenioRequest == null) {
            return null;
        }

        return new Convenio(
                convenioRequest.getNome()
        );
    }
}
