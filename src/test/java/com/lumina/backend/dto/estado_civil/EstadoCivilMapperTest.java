package com.lumina.backend.dto.estado_civil;

import com.lumina.backend.model.EstadoCivil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes unitários EstadoCivilMapper")
class EstadoCivilMapperTest {

    @Test
    @DisplayName("Deve retornar null quando entrada for nula")
    void deveRetornarNull() {
        assertNull(EstadoCivilMapper.toEntity(null));
        assertNull(EstadoCivilMapper.toDto((EstadoCivil) null));
    }

    @Test
    @DisplayName("Deve converter EstadoCivilRequest para EstadoCivil e EstadoCivil para EstadoCivilResponse")
    void deveConverterEstadoCivil() {
        EstadoCivilRequest request = new EstadoCivilRequest(1, "Solteiro(a)");
        EstadoCivil entity = EstadoCivilMapper.toEntity(request);

        assertNotNull(entity);
        assertEquals(1, entity.getIdEstadoCivil());
        assertEquals("Solteiro(a)", entity.getDescricao());

        List<EstadoCivilResponse> responses = EstadoCivilMapper.toDto(List.of(entity));
        assertEquals(1, responses.size());
        assertEquals(1, responses.get(0).getIdEstadoCivil());
        assertEquals("Solteiro(a)", responses.get(0).getDescricao());
    }
}
