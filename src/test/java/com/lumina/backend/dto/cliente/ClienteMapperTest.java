package com.lumina.backend.dto.cliente;

import com.lumina.backend.model.Cliente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes unitários ClienteMapper")
class ClienteMapperTest {

    @Test
    @DisplayName("Deve retornar null para entradas nulas")
    void deveRetornarNull() {
        assertNull(ClienteMapper.toDto((Cliente) null));
        assertNull(ClienteMapper.toEntity(null));
    }

    @Test
    @DisplayName("Deve converter ClienteRequest para entidade Cliente")
    void deveConverterRequestParaEntity() {
        ClienteRequest request = ClienteRequest.builder()
                .idCliente(1L)
                .nome("Ana")
                .cpf("12345678900")
                .dataNascimento(LocalDate.of(1995, 5, 10))
                .sexo('F')
                .email("ana@lumina.com")
                .build();

        Cliente entity = ClienteMapper.toEntity(request);
        assertNotNull(entity);
        assertEquals(1L, entity.getIdCliente());
        assertEquals("Ana", entity.getNome());
        assertEquals("12345678900", entity.getCpf());
    }

    @Test
    @DisplayName("Deve converter entidade Cliente com relações para ClienteResponse")
    void deveConverterParaDto() {
        Cliente indicacao = new Cliente();
        indicacao.setIdCliente(2L);

        Cliente responsavel = new Cliente();
        responsavel.setIdCliente(3L);

        Cliente cliente = new Cliente();
        cliente.setIdCliente(1L);
        cliente.setNome("Ana");
        cliente.setCpf("123");
        cliente.setClienteIndicacao(indicacao);
        cliente.setResponsavel(responsavel);
        cliente.setGrauParentescoResponsavel("Mãe");

        List<ClienteResponse> responses = ClienteMapper.toDto(List.of(cliente));
        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).idCliente());
        assertEquals(2, responses.get(0).fkClienteIndicacao());
        assertEquals(3, responses.get(0).fkResponsavel());
        assertEquals("Mãe", responses.get(0).grauParentescoResponsavel());
    }
}
