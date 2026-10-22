package com.lumina.backend.dto.consulta;

import com.lumina.backend.model.Cliente;
import com.lumina.backend.model.Consulta;
import com.lumina.backend.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes unitários ConsultaMapper")
class ConsultaMapperTest {

    @Test
    @DisplayName("Deve retornar null quando entrada for nula")
    void deveRetornarNull() {
        assertNull(ConsultaMapper.toResponse((Consulta) null));
        assertNull(ConsultaMapper.toEntity(null));
    }

    @Test
    @DisplayName("Deve converter ConsultaRequest para Consulta e Consulta para ConsultaResponse")
    void deveConverterRequestEEntity() {
        ConsultaRequest request = new ConsultaRequest();
        request.setData(LocalDate.of(2026, 10, 23));
        request.setHorarioInicio(LocalTime.of(10, 0));
        request.setHorarioFim(LocalTime.of(11, 0));

        Consulta entity = ConsultaMapper.toEntity(request);
        assertNotNull(entity);
        assertEquals(LocalDate.of(2026, 10, 23), entity.getData());
        assertEquals(LocalTime.of(10, 0), entity.getHorarioInicio());
        assertEquals(LocalTime.of(11, 0), entity.getHorarioFim());

        Cliente cliente = new Cliente();
        cliente.setIdCliente(1L);
        cliente.setNome("Ana");

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(2L);
        usuario.setNome("Dr. Carlos");

        entity.setIdConsulta(50L);
        entity.setCliente(cliente);
        entity.setUsuario(usuario);

        List<ConsultaResponse> responses = ConsultaMapper.toResponse(List.of(entity));
        assertEquals(1, responses.size());
        assertEquals(50L, responses.get(0).getId());
        assertEquals("Ana", responses.get(0).getCliente().getNome());
        assertEquals("Dr. Carlos", responses.get(0).getUsuario().getNome());
    }
}
