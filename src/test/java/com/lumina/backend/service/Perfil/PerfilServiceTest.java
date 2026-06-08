package com.lumina.backend.service.Perfil;

import com.lumina.backend.controller.PerfilController;
import com.lumina.backend.model.Perfil;
import com.lumina.backend.repository.PerfilRespository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários PerfilService")
class PerfilServiceTest {
    @Mock
    private PerfilRespository repository;

     private PerfilService service;

@Test
@DisplayName("1.1 Deve retornar lista com todos os perfis cadastrados")
    void retornarListaComTodosOsPerfisCadastrados() {
        service = new PerfilService(repository);
        List<Perfil> listaPerfil = List.of(
                new Perfil()
        );
    Mockito.when(repository.findAll()).thenReturn(listaPerfil);

    List<Perfil> perfis = service.listar();
    Assertions.assertFalse(perfis.isEmpty());
    }

    @Test
    @DisplayName("1.2 Deve retornar lista vazia quando não houver perfis cadastrados")
    void retornarListaVaziaQuandoNaoHouverPerfisCadastrados() {
        service = new PerfilService(repository);
        var listaVazia = Collections.EMPTY_LIST;

        Mockito.when(repository.findAll()).thenReturn(listaVazia);

    List<Perfil> perfis = service.listar();
    Assertions.assertTrue(perfis.isEmpty());
    }
}