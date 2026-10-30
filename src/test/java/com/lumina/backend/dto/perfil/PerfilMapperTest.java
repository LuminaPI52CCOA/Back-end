package com.lumina.backend.dto.perfil;

import com.lumina.backend.model.Perfil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes unitários PerfilMapper")
class PerfilMapperTest {

    @Test
    @DisplayName("Deve retornar null quando entrada for nula")
    void deveRetornarNull() {
        assertNull(PerfilMapper.toEntity(null));
        assertNull(PerfilMapper.toDto((Perfil) null));
    }

    @Test
    @DisplayName("Deve converter PerfilRequest para Perfil e Perfil para PerfilResponse")
    void deveConverterPerfil() {
        PerfilRequest request = new PerfilRequest();
        request.setIdPerfil(1);
        request.setNome("ADMIN");

        Perfil entity = PerfilMapper.toEntity(request);
        assertNotNull(entity);
        assertEquals(1, entity.getIdPerfil());
        assertEquals("ADMIN", entity.getNome());

        List<PerfilResponse> responses = PerfilMapper.toDto(List.of(entity));
        assertEquals(1, responses.size());
        assertEquals(1, responses.get(0).getIdPerfil());
        assertEquals("ADMIN", responses.get(0).getNome());
    }
}
