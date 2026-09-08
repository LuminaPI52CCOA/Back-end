package com.lumina.backend.controller.perfil;

import com.lumina.backend.controller.PerfilController;
import com.lumina.backend.model.Perfil;
import com.lumina.backend.service.Perfil.PerfilService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class PerfilControllerTest {

    @Mock
    private PerfilService perfilService;

    @InjectMocks
    private PerfilController perfilController;

    @Nested
    @DisplayName("1. Teste de listagem de perfis")
    class ListarTest {

        @Test
        @DisplayName("1.1 Deve retornar lista de perfis com status 200")
        void deveRetornarListaDePerfisComSucesso() {
            Perfil perfil1 = new Perfil();
            perfil1.setIdPerfil(1);
            perfil1.setNome("ADMIN");

            Perfil perfil2 = new Perfil();
            perfil2.setIdPerfil(2);
            perfil2.setNome("DENTISTA");

            Perfil perfil3 = new Perfil();
            perfil3.setIdPerfil(3);
            perfil3.setNome("RECEPCIONISTA");

            List<Perfil> perfis = List.of(perfil1, perfil2, perfil3);

            Mockito.when(perfilService.listar()).thenReturn(perfis);

            ResponseEntity<List<Perfil>> response = perfilController.listar();

            Assertions.assertEquals(200, response.getStatusCodeValue());
            Assertions.assertNotNull(response.getBody());
            Assertions.assertFalse(response.getBody().isEmpty());
            Assertions.assertEquals(3, response.getBody().size());

            Mockito.verify(perfilService, Mockito.times(1)).listar();
        }

        @Test
        @DisplayName("1.2 Deve retornar status 204 quando não houver perfis cadastrados")
        void deveRetornarStatus204QuandoListaVazia() {
            Mockito.when(perfilService.listar()).thenReturn(Collections.emptyList());

            ResponseEntity<List<Perfil>> response = perfilController.listar();

            Assertions.assertEquals(204, response.getStatusCodeValue());
            Assertions.assertNull(response.getBody());

            Mockito.verify(perfilService, Mockito.times(1)).listar();
        }
    }
}
