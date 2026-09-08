package com.lumina.backend.controller.convenio;

import com.lumina.backend.controller.ConvenioController;
import com.lumina.backend.dto.convenio.ConvenioRequest;
import com.lumina.backend.dto.convenio.ConvenioResponse;
import com.lumina.backend.exception.EntidadeNaoEncontrada;
import com.lumina.backend.model.Convenio;
import com.lumina.backend.service.convenio.ConvenioService;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
class ConvenioControllerTest {

    @Mock
    private ConvenioService convenioService;

    @InjectMocks
    private ConvenioController convenioController;

    @Nested
    @DisplayName("1. Teste de listagem de convênios")
    class ListarTest {

        @Test
        @DisplayName("1.1 Deve retornar lista de convênios com status 200")
        void deveRetornarListaDeConveniosComSucesso() {
            Convenio conv1 = new Convenio();
            conv1.setIdConvenio(1L);
            conv1.setNome("Unimed");

            Convenio conv2 = new Convenio();
            conv2.setIdConvenio(2L);
            conv2.setNome("Bradesco");

            Convenio conv3 = new Convenio();
            conv3.setIdConvenio(3L);
            conv3.setNome("SulAmerica");

            List<Convenio> convenios = List.of(conv1, conv2, conv3);

            Mockito.when(convenioService.listar()).thenReturn(convenios);

            ResponseEntity<List<ConvenioResponse>> response = convenioController.listar();

            Assertions.assertEquals(200, response.getStatusCodeValue());
            Assertions.assertNotNull(response.getBody());
            Assertions.assertFalse(response.getBody().isEmpty());
            Assertions.assertEquals(3, response.getBody().size());

            Mockito.verify(convenioService, Mockito.times(1)).listar();
        }

        @Test
        @DisplayName("1.2 Deve retornar lista vazia de convênios com status 200")
        void deveRetornarListaVaziaDeConvenios() {
            Mockito.when(convenioService.listar()).thenReturn(Collections.emptyList());

            ResponseEntity<List<ConvenioResponse>> response = convenioController.listar();

            Assertions.assertEquals(200, response.getStatusCodeValue());
            Assertions.assertNotNull(response.getBody());
            Assertions.assertTrue(response.getBody().isEmpty());

            Mockito.verify(convenioService, Mockito.times(1)).listar();
        }
    }

    @Nested
    @DisplayName("2. Teste de cadastro de convênio")
    class CadastrarTest {

        @Test
        @DisplayName("2.1 Deve cadastrar convênio com sucesso e retornar status 201")
        void deveCadastrarConvenioComSucesso() {
            ConvenioRequest request = new ConvenioRequest();
            request.setNome("Unimed");

            Convenio convenioSalvo = new Convenio();
            convenioSalvo.setIdConvenio(1L);
            convenioSalvo.setNome("Unimed");

            Mockito.when(convenioService.cadastrar(any(ConvenioRequest.class))).thenReturn(convenioSalvo);

            ResponseEntity<ConvenioResponse> response = convenioController.cadastrar(request);

            Assertions.assertEquals(201, response.getStatusCodeValue());
            Assertions.assertNotNull(response.getBody());
            Assertions.assertEquals("Unimed", response.getBody().getNome());
            Assertions.assertEquals(1L, response.getBody().getId());

            Mockito.verify(convenioService, Mockito.times(1)).cadastrar(any(ConvenioRequest.class));
        }
    }

    @Nested
    @DisplayName("3. Teste de atualização de convênio")
    class AtualizarTest {

        @Test
        @DisplayName("3.1 Deve atualizar convênio com sucesso e retornar status 200")
        void deveAtualizarConvenioComSucesso() {
            ConvenioRequest request = new ConvenioRequest();
            request.setNome("Unimed Atualizado");

            Convenio convenioAtualizado = new Convenio();
            convenioAtualizado.setIdConvenio(1L);
            convenioAtualizado.setNome("Unimed Atualizado");

            Mockito.when(convenioService.atualizar(anyLong(), any(ConvenioRequest.class)))
                    .thenReturn(convenioAtualizado);

            ResponseEntity<ConvenioResponse> response = convenioController.atualizar(1L, request);

            Assertions.assertEquals(200, response.getStatusCodeValue());
            Assertions.assertNotNull(response.getBody());
            Assertions.assertEquals("Unimed Atualizado", response.getBody().getNome());

            Mockito.verify(convenioService, Mockito.times(1)).atualizar(anyLong(), any(ConvenioRequest.class));
        }

        @Test
        @DisplayName("3.2 Deve lançar EntidadeNaoEncontrada ao tentar atualizar convênio inexistente")
        void deveLancarExcecaoAoAtualizarConvenioInexistente() {
            ConvenioRequest request = new ConvenioRequest();
            request.setNome("Teste");

            Mockito.when(convenioService.atualizar(anyLong(), any(ConvenioRequest.class)))
                    .thenThrow(new EntidadeNaoEncontrada("Convênio não encontrado!"));

            EntidadeNaoEncontrada exception = assertThrows(
                    EntidadeNaoEncontrada.class,
                    () -> convenioController.atualizar(99L, request)
            );

            Assertions.assertEquals("Convênio não encontrado!", exception.getMessage());
            Mockito.verify(convenioService, Mockito.times(1)).atualizar(anyLong(), any(ConvenioRequest.class));
        }
    }

    @Nested
    @DisplayName("4. Teste de exclusão de convênio")
    class DeletarTest {

        @Test
        @DisplayName("4.1 Deve deletar convênio com sucesso e retornar status 204")
        void deveDeletarConvenioComSucesso() {
            Mockito.doNothing().when(convenioService).deletar(1L);

            ResponseEntity<Void> response = convenioController.deletar(1L);

            Assertions.assertEquals(204, response.getStatusCodeValue());

            Mockito.verify(convenioService, Mockito.times(1)).deletar(1L);
        }

        @Test
        @DisplayName("4.2 Deve lançar EntidadeNaoEncontrada ao tentar deletar convênio inexistente")
        void deveLancarExcecaoAoDeletarConvenioInexistente() {
            Mockito.doThrow(new EntidadeNaoEncontrada("Convênio não encontrado!"))
                    .when(convenioService).deletar(99L);

            EntidadeNaoEncontrada exception = assertThrows(
                    EntidadeNaoEncontrada.class,
                    () -> convenioController.deletar(99L)
            );

            Assertions.assertEquals("Convênio não encontrado!", exception.getMessage());
            Mockito.verify(convenioService, Mockito.times(1)).deletar(99L);
        }
    }
}
