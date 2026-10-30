package com.lumina.backend.domain.cliente;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes unitários de Domínio Cliente e ClienteId")
class ClienteTest {

    private ClienteCommand criarCommand(LocalDate dataNascimento) {
        return new ClienteCommand(
                "Ana Silva",
                "12345678900",
                "1234567",
                dataNascimento,
                "São Paulo",
                "Brasileira",
                'F',
                "01001000",
                "Rua A, 100",
                "ana@lumina.com",
                "11999999999",
                1,
                null,
                null,
                null
        );
    }

    @Test
    @DisplayName("Deve criar e atualizar cliente mantendo ID")
    void deveCriarEAtualizarCliente() {
        ClienteCommand command = criarCommand(LocalDate.now().minusYears(25));
        Cliente criado = Cliente.criar(command);

        assertNotNull(criado.getId());
        assertNull(criado.getId().getValue());
        assertEquals("Ana Silva", criado.getNome());
        assertEquals("12345678900", criado.getCpf());

        Cliente reconstruido = Cliente.reconstruir(ClienteId.of(10L), command);
        assertEquals(10L, reconstruido.getId().getValue());

        ClienteCommand novoCommand = new ClienteCommand(
                "Ana Silva Atualizada",
                "12345678900",
                "1234567",
                LocalDate.now().minusYears(25),
                "São Paulo",
                "Brasileira",
                'F',
                "01001000",
                "Rua B, 200",
                "ana.nova@lumina.com",
                "11988887777",
                2,
                null,
                null,
                null
        );

        Cliente atualizado = reconstruido.atualizar(novoCommand);
        assertEquals(10L, atualizado.getId().getValue());
        assertEquals("Ana Silva Atualizada", atualizado.getNome());
        assertEquals("ana.nova@lumina.com", atualizado.getEmail());
        assertEquals("Rua B, 200", atualizado.getEnderecoResidencial());
    }

    @Test
    @DisplayName("Deve validar maioridade corretamente")
    void deveValidarMaioridade() {
        Cliente maior = Cliente.criar(criarCommand(LocalDate.now().minusYears(18)));
        Cliente menor = Cliente.criar(criarCommand(LocalDate.now().minusYears(17)));
        Cliente semData = Cliente.criar(criarCommand(null));

        assertTrue(maior.ehMaiorDeIdade());
        assertFalse(menor.ehMaiorDeIdade());
        assertFalse(semData.ehMaiorDeIdade());
    }

    @Test
    @DisplayName("Deve validar ClienteId equals, hashCode e factory methods")
    void deveValidarClienteId() {
        assertNull(ClienteId.of(null));
        ClienteId id1 = ClienteId.of(1L);
        ClienteId id2 = ClienteId.of(1L);
        ClienteId id3 = ClienteId.of(2L);

        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
        assertNotEquals(id1, id3);
        assertNotEquals(id1, null);
        assertNotEquals(id1, "1");
    }
}
