package com.lumina.backend.controller.anamnese;

import com.lumina.backend.controller.AnamneseController;
import com.lumina.backend.dto.anamnese.AnamneseResponse;
import com.lumina.backend.exception.AnamneseVazio;
import com.lumina.backend.exception.EntidadeNaoEncontrada;
import com.lumina.backend.model.Anamnese;
import com.lumina.backend.model.Cliente;
import com.lumina.backend.service.anamnese.AnamneseService;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

@ExtendWith(MockitoExtension.class)
class AnamneseControllerTest {

    @Mock
    private AnamneseService anamneseService;

    @InjectMocks
    private AnamneseController anamneseController;

    @Nested
    @DisplayName("1. Teste de upload de anamnese")
    class UploadTest {

        @Test
        @DisplayName("1.1 Deve processar imagem com sucesso e retornar status 200")
        void deveProcessarImagemComSucesso() throws Exception {
            MultipartFile file = Mockito.mock(MultipartFile.class);

            Cliente cliente = new Cliente();
            cliente.setIdCliente(1L);

            Anamnese anamnese = new Anamnese();
            anamnese.setIdAnamnese(1L);
            anamnese.setDataAnamnese(LocalDate.now());
            anamnese.setFazendoTratamento(true);
            anamnese.setDescricaoTratamento("Tratamento cardíaco");
            anamnese.setAlergiaMedicamentos(false);
            anamnese.setFkCliente(cliente);

            Mockito.when(anamneseService.processImage(any(MultipartFile.class))).thenReturn(anamnese);

            ResponseEntity<Anamnese> response = anamneseController.upload(file);

            Assertions.assertEquals(200, response.getStatusCodeValue());
            Assertions.assertNotNull(response.getBody());
            Assertions.assertEquals(1L, response.getBody().getIdAnamnese());
            Assertions.assertTrue(response.getBody().getFazendoTratamento());

            Mockito.verify(anamneseService, Mockito.times(1)).processImage(any(MultipartFile.class));
        }

        @Test
        @DisplayName("1.2 Deve lançar AnamneseVazio ao tentar upload com arquivo vazio")
        void deveLancarExcecaoAoEnviarArquivoVazio() throws Exception {
            MultipartFile file = Mockito.mock(MultipartFile.class);

            Mockito.when(anamneseService.processImage(any(MultipartFile.class)))
                    .thenThrow(new AnamneseVazio("Arquivo não encontrado."));

            AnamneseVazio exception = assertThrows(
                    AnamneseVazio.class,
                    () -> anamneseController.upload(file)
            );

            Assertions.assertEquals("Arquivo não encontrado.", exception.getMessage());
            Mockito.verify(anamneseService, Mockito.times(1)).processImage(any(MultipartFile.class));
        }
    }

    @Nested
    @DisplayName("2. Teste de busca por ID")
    class BuscarPorIdTest {

        @Test
        @DisplayName("2.1 Deve retornar anamnese encontrada com status 200")
        void deveRetornarAnamneseEncontradaComSucesso() {
            Cliente cliente = new Cliente();
            cliente.setIdCliente(1L);

            Anamnese anamnese = new Anamnese();
            anamnese.setIdAnamnese(1L);
            anamnese.setDataAnamnese(LocalDate.now());
            anamnese.setDescricaoTratamento("Tratamento ortodôntico");
            anamnese.setFazendoTratamento(true);
            anamnese.setFkCliente(cliente);

            Mockito.when(anamneseService.buscarPorId(1)).thenReturn(anamnese);

            ResponseEntity<AnamneseResponse> response = anamneseController.buscarPorId(1);

            Assertions.assertEquals(200, response.getStatusCodeValue());
            Assertions.assertNotNull(response.getBody());
            Assertions.assertEquals("Tratamento ortodôntico", response.getBody().getDescricaoTratamento());
            Assertions.assertTrue(response.getBody().getFazendoTratamento());

            Mockito.verify(anamneseService, Mockito.times(1)).buscarPorId(1);
        }

        @Test
        @DisplayName("2.2 Deve lançar EntidadeNaoEncontrada quando anamnese não existir")
        void deveLancarExcecaoQuandoAnamneseNaoEncontrada() {
            Mockito.when(anamneseService.buscarPorId(anyInt()))
                    .thenThrow(new EntidadeNaoEncontrada("Anamnese não encontrada!"));

            EntidadeNaoEncontrada exception = assertThrows(
                    EntidadeNaoEncontrada.class,
                    () -> anamneseController.buscarPorId(99)
            );

            Assertions.assertEquals("Anamnese não encontrada!", exception.getMessage());
            Mockito.verify(anamneseService, Mockito.times(1)).buscarPorId(99);
        }
    }
}
