package com.lumina.backend.infrastructure.persistence.mapper;

import com.lumina.backend.domain.cliente.Cliente;
import com.lumina.backend.domain.cliente.ClienteCommand;
import com.lumina.backend.domain.cliente.ClienteId;
import com.lumina.backend.infrastructure.persistence.jpa.ClienteJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes unitários ClienteMapperJpa")
class ClienteMapperJpaTest {

    private ClienteCommand command(String nome, Cliente indicacao, Cliente responsavel) {
        return new ClienteCommand(nome, "12345678900", "123", LocalDate.of(2000, 1, 1),
                "SP", "Brasileira", 'F', "01001000", "Rua A", nome + "@mail.com",
                "11999999999", 1, indicacao, responsavel, "Mãe");
    }

    @Test
    @DisplayName("Deve retornar null para entradas nulas")
    void deveRetornarNull() {
        assertNull(ClienteMapperJpa.toJpa(null));
        assertNull(ClienteMapperJpa.toDomain(null));
        assertNull(ClienteMapperJpa.toCommand(null));
    }

    @Test
    @DisplayName("Deve converter domínio para JPA incluindo indicação e responsável")
    void deveConverterParaJpa() {
        Cliente indicacao = Cliente.reconstruir(ClienteId.of(2L), command("ind", null, null));
        Cliente responsavel = Cliente.reconstruir(ClienteId.of(3L), command("resp", null, null));
        Cliente cliente = Cliente.reconstruir(ClienteId.of(1L), command("ana", indicacao, responsavel));

        ClienteJpaEntity entity = ClienteMapperJpa.toJpa(cliente);

        assertEquals(1L, entity.getIdCliente());
        assertEquals("ana", entity.getNome());
        assertEquals('F', entity.getSexo());
        assertEquals(LocalDate.of(2000, 1, 1), entity.getDataNascimento());
        assertEquals(1, entity.getFkEstadoCivil());
        assertEquals(2L, entity.getClienteIndicacao().getIdCliente());
        assertEquals(3L, entity.getResponsavel().getIdCliente());
        assertEquals("Mãe", entity.getGrauParentescoResponsavel());
    }

    @Test
    @DisplayName("Deve converter JPA para domínio")
    void deveConverterParaDomain() {
        ClienteJpaEntity resp = new ClienteJpaEntity();
        resp.setIdCliente(3L);
        resp.setNome("resp");
        ClienteJpaEntity entity = new ClienteJpaEntity();
        entity.setIdCliente(1L);
        entity.setNome("ana");
        entity.setCpf("111");
        entity.setResponsavel(resp);

        Cliente cliente = ClienteMapperJpa.toDomain(entity);

        assertEquals(1L, cliente.getId().getValue());
        assertEquals("ana", cliente.getNome());
        assertEquals("111", cliente.getCpf());
        assertNull(cliente.getClienteIndicacao());
        assertEquals("resp", cliente.getResponsavel().getNome());
    }

    @Test
    @DisplayName("Deve converter JPA sem id para domínio novo")
    void deveConverterSemId() {
        ClienteJpaEntity entity = new ClienteJpaEntity();
        entity.setNome("novo");

        Cliente cliente = ClienteMapperJpa.toDomain(entity);

        assertEquals("novo", cliente.getNome());
        assertNull(ClienteMapperJpa.toJpa(cliente).getIdCliente());
    }

    @Test
    @DisplayName("Deve converter domínio para command")
    void deveConverterParaCommand() {
        ClienteCommand cmd = command("ana", null, null);

        assertEquals(cmd, ClienteMapperJpa.toCommand(Cliente.criar(cmd)));
    }
}
