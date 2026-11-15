package com.lumina.backend.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes unitários da entidade Usuario")
class UsuarioTest {

    @Test
    @DisplayName("Deve identificar usuário ativo apenas quando flag ativo for true")
    void deveValidarStatusAtivo() {
        Usuario ativo = new Usuario();
        ativo.setAtivo(true);

        Usuario inativo = new Usuario();
        inativo.setAtivo(false);

        Usuario nulo = new Usuario();

        assertTrue(ativo.estaAtivo());
        assertFalse(inativo.estaAtivo());
        assertFalse(nulo.estaAtivo());
    }
}
