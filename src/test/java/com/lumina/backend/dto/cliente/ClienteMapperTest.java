package com.lumina.backend.dto.cliente;

import com.lumina.backend.domain.cliente.Cliente;
import com.lumina.backend.domain.cliente.ClienteCommand;
import com.lumina.backend.domain.cliente.ClienteId;
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
        assertNull(ClienteMapper.toCommand(null));
        assertNull(ClienteMapper.toDto((Cliente) null));
        assertNull(ClienteMapper.toDto((com.lumina.backend.model.Cliente) null));
        assertNull(ClienteMapper.toEntity(null));
    }

    @Test
    @DisplayName("Deve converter ClienteRequest para ClienteCommand e model.Cliente")
    void deveConverterRequestParaCommandEEntity() {
        ClienteRequest request = new ClienteRequest();
        request.setIdCliente(1L);
        request.setNome("Ana");
        request.setCpf("12345678900");
        request.setDataNascimento(LocalDate.of(1995, 5, 10));
        request.setSexo('F');
        request.setEmail("ana@lumina.com");

        ClienteCommand command = ClienteMapper.toCommand(request);
        assertNotNull(command);
        assertEquals("Ana", command.nome());
        assertEquals("12345678900", command.cpf());

        com.lumina.backend.model.Cliente entity = ClienteMapper.toEntity(request);
        assertNotNull(entity);
        assertEquals(1L, entity.getIdCliente());
        assertEquals("Ana", entity.getNome());
    }

    @Test
    @DisplayName("Deve converter Cliente de domínio e model.Cliente para ClienteResponse")
    void deveConverterParaDto() {
        ClienteCommand baseCmd = new ClienteCommand(
                "Ana", "123", "456", LocalDate.of(1995, 5, 10),
                "SP", "Brasileira", 'F', "01001000", "Rua A", "ana@lumina.com",
                "11999999999", 1, null, null, null
        );
        Cliente indicacao = Cliente.reconstruir(ClienteId.of(2L), baseCmd);
        Cliente responsavel = Cliente.reconstruir(ClienteId.of(3L), baseCmd);

        ClienteCommand cmdComRelacoes = new ClienteCommand(
                "Ana", "123", "456", LocalDate.of(1995, 5, 10),
                "SP", "Brasileira", 'F', "01001000", "Rua A", "ana@lumina.com",
                "11999999999", 1, indicacao, responsavel, "Mãe"
        );
        Cliente domainCliente = Cliente.reconstruir(ClienteId.of(1L), cmdComRelacoes);

        List<ClienteResponse> responses = ClienteMapper.toDto(List.of(domainCliente));
        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).getIdCliente());
        assertEquals(2, responses.get(0).getFkClienteIndicacao());
        assertEquals(3, responses.get(0).getFkResponsavel());

        com.lumina.backend.model.Cliente modelIndicacao = new com.lumina.backend.model.Cliente();
        modelIndicacao.setIdCliente(2L);
        com.lumina.backend.model.Cliente modelCliente = new com.lumina.backend.model.Cliente();
        modelCliente.setIdCliente(1L);
        modelCliente.setNome("Ana");
        modelCliente.setClienteIndicacao(modelIndicacao);

        ClienteResponse modelResponse = ClienteMapper.toDto(modelCliente);
        assertNotNull(modelResponse);
        assertEquals(1L, modelResponse.getIdCliente());
        assertEquals(2, modelResponse.getFkClienteIndicacao());
    }
}
