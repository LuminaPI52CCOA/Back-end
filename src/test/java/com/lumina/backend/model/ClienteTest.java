package com.lumina.backend.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes unitários da entidade Cliente")
class ClienteTest {

    @Test
    @DisplayName("Deve validar maioridade corretamente")
    void deveValidarMaioridade() {
        Cliente maior = new Cliente();
        maior.setDataNascimento(LocalDate.now().minusYears(18));

        Cliente menor = new Cliente();
        menor.setDataNascimento(LocalDate.now().minusYears(17));

        Cliente semData = new Cliente();

        assertTrue(maior.ehMaiorDeIdade());
        assertFalse(menor.ehMaiorDeIdade());
        assertFalse(semData.ehMaiorDeIdade());
    }
}
