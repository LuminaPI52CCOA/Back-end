package com.lumina.backend.dto.convenio;

import com.lumina.backend.model.Convenio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes unitários ConvenioMapper")
class ConvenioMapperTest {

    @Test
    @DisplayName("Deve retornar null quando entrada for nula")
    void deveRetornarNull() {
        assertNull(ConvenioMapper.toResponse((Convenio) null));
        assertNull(ConvenioMapper.toEntity(null));
    }

    @Test
    @DisplayName("Deve converter ConvenioRequest para Convenio e Convenio para ConvenioResponse")
    void deveConverterConvenio() {
        ConvenioRequest request = new ConvenioRequest("Unimed");
        Convenio entity = ConvenioMapper.toEntity(request);
        assertNotNull(entity);
        assertEquals("Unimed", entity.getNome());

        entity.setIdConvenio(1L);
        List<ConvenioResponse> responses = ConvenioMapper.toResponse(List.of(entity));
        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).getId());
        assertEquals("Unimed", responses.get(0).getNome());
    }
}
