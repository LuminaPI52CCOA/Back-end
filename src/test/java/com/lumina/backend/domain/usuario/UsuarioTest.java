package com.lumina.backend.domain.usuario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes unitários de Domínio Usuario e UsuarioId")
class UsuarioTest {

    private UsuarioCommand criarCommand(Boolean ativo) {
        return new UsuarioCommand(
                "Dr. Carlos",
                "12345678900",
                "carlos@lumina.com",
                "senhaHash",
                2,
                "CRO-SP-12345",
                ativo
        );
    }

    @Test
    @DisplayName("Deve criar, reconstruir e atualizar usuário")
    void deveCriarReconstruirEAtualizarUsuario() {
        UsuarioCommand cmd = criarCommand(true);
        Usuario criado = Usuario.criar(cmd);

        assertNotNull(criado.getId());
        assertNull(criado.getId().getValue());
        assertEquals("Dr. Carlos", criado.getNome());
        assertTrue(criado.estaAtivo());

        Usuario reconstruido = Usuario.reconstruir(UsuarioId.of(5L), cmd);
        assertEquals(5L, reconstruido.getId().getValue());

        UsuarioCommand cmdInativo = criarCommand(false);
        Usuario atualizado = reconstruido.atualizar(cmdInativo);

        assertEquals(5L, atualizado.getId().getValue());
        assertFalse(atualizado.estaAtivo());
        assertEquals("CRO-SP-12345", atualizado.getCro());
    }

    @Test
    @DisplayName("Deve validar UsuarioId equals, hashCode e factory methods")
    void deveValidarUsuarioId() {
        assertNull(UsuarioId.of(null));
        UsuarioId id1 = UsuarioId.of(10L);
        UsuarioId id2 = UsuarioId.of(10L);
        UsuarioId id3 = UsuarioId.of(20L);

        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
        assertNotEquals(id1, id3);
        assertNotEquals(id1, null);
    }
}
