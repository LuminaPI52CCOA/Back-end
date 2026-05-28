package com.lumina.backend.service.convenio;

import com.lumina.backend.dto.convenio.ConvenioMapper;
import com.lumina.backend.dto.convenio.ConvenioRequest;
import com.lumina.backend.exception.EntidadeNaoEncontrada;
import com.lumina.backend.model.Convenio;
import com.lumina.backend.repository.ConvenioRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ConvenioServiceTest {

    @Nested
    @DisplayName("1. Teste de listagem de todos os convênios cadastrados")
    class ListarTest {

        @Mock
        private ConvenioRepository convenioRepository;

        @InjectMocks
        private ConvenioService convenioService;

        @Test
        @DisplayName("1.1 Deve retornar uma lista cheia.")
        void deveRetornarUmaListaCheiaComSucesso() {
            Convenio c1 = new Convenio();
            c1.setIdConvenio(1L);
            c1.setNome("Unimed");

            Convenio c2 = new Convenio();
            c2.setIdConvenio(2L);
            c2.setNome("Amil");

            Convenio c3 = new Convenio();
            c3.setIdConvenio(3L);
            c3.setNome("Bradesco Saúde");

            var listaCheia = List.of(c1, c2, c3);

            Mockito.when(convenioRepository.findAll())
                    .thenReturn(listaCheia);

            List<Convenio> resultado = convenioService.listar();

            Assertions.assertFalse(resultado.isEmpty());
            Assertions.assertEquals(3, resultado.size());
        }

        @Test
        @DisplayName("1.2 Deve retornar uma lista vazia.")
        void deveRetornarUmaListaVazia() {
            var listaVazia = Collections.<Convenio>emptyList();

            Mockito.when(convenioRepository.findAll())
                    .thenReturn(listaVazia);

            List<Convenio> resultado = convenioService.listar();

            Assertions.assertTrue(resultado.isEmpty());
        }
    }

    @Nested
    @DisplayName("2. Teste de cadastro do convênio")
    class CadastrarTest {

        @Mock
        private ConvenioRepository convenioRepository;

        @InjectMocks
        private ConvenioService convenioService;

        @Test
        @DisplayName("2.1 Cadastro deve ser realizado com sucesso")
        void deveCriarConvenioCorretamente() {
            ConvenioRequest convenioRequest = new ConvenioRequest();
            convenioRequest.setNome("Unimed");

            Convenio convenio = ConvenioMapper.toEntity(convenioRequest);
            convenio.setIdConvenio(1L);

            Mockito.when(convenioRepository.save(any(Convenio.class)))
                    .thenReturn(convenio);

            Convenio resultado = convenioService.cadastrar(convenioRequest);

            Mockito.verify(convenioRepository, Mockito.times(1))
                    .save(any(Convenio.class));

            Assertions.assertNotNull(resultado);
            Assertions.assertEquals("Unimed", resultado.getNome());
        }
    }

    @Nested
    @DisplayName("3. Testes de atualizar o convênio")
    class AtualizarConvenioTest {

        @Mock
        private ConvenioRepository convenioRepository;

        @InjectMocks
        private ConvenioService convenioService;

        @Test
        @DisplayName("3.1 Deve atualizar o convênio com sucesso")
        void deveAtualizarOConvenioComSucesso() {
            ConvenioRequest convenioRequest = new ConvenioRequest();
            convenioRequest.setNome("Unimed Atualizado");

            Convenio convenioExistente = new Convenio();
            convenioExistente.setIdConvenio(1L);
            convenioExistente.setNome("Unimed");

            Convenio convenioAtualizado = new Convenio();
            convenioAtualizado.setIdConvenio(1L);
            convenioAtualizado.setNome("Unimed Atualizado");

            Mockito.when(convenioRepository.findById(1L))
                    .thenReturn(Optional.of(convenioExistente));

            Mockito.when(convenioRepository.save(any(Convenio.class)))
                    .thenReturn(convenioAtualizado);

            Convenio resultado = convenioService.atualizar(1L, convenioRequest);

            Mockito.verify(convenioRepository, Mockito.times(1))
                    .findById(1L);
            Mockito.verify(convenioRepository, Mockito.times(1))
                    .save(any(Convenio.class));

            Assertions.assertNotNull(resultado);
            Assertions.assertEquals("Unimed Atualizado", resultado.getNome());
        }

        @Test
        @DisplayName("3.2 Deve lançar EntidadeNaoEncontrada ao tentar atualizar um convênio inexistente")
        void deveLancarExcecaoAoAtualizarOConvenioInexistente() {
            ConvenioRequest convenioRequest = new ConvenioRequest();
            convenioRequest.setNome("Unimed");

            Mockito.when(convenioRepository.findById(1L))
                    .thenReturn(Optional.empty());

            assertThrows(
                    EntidadeNaoEncontrada.class,
                    () -> convenioService.atualizar(1L, convenioRequest)
            );

            Mockito.verify(convenioRepository, Mockito.never()).save(any(Convenio.class));
        }
    }

    @Nested
    @DisplayName("4. Testes de deletar o convênio")
    class DeletarConvenioTest {

        @Mock
        private ConvenioRepository convenioRepository;

        @InjectMocks
        private ConvenioService convenioService;

        @Test
        @DisplayName("4.1 Deve deletar o convênio com sucesso")
        void deveDeletarOConvenioComSucesso() {
            Convenio convenio = new Convenio();
            convenio.setIdConvenio(1L);
            convenio.setNome("Unimed");

            Mockito.when(convenioRepository.findById(1L))
                    .thenReturn(Optional.of(convenio));

            Mockito.doNothing().when(convenioRepository).delete(any(Convenio.class));

            convenioService.deletar(1L);

            Mockito.verify(convenioRepository, Mockito.times(1))
                    .findById(1L);
            Mockito.verify(convenioRepository, Mockito.times(1))
                    .delete(any(Convenio.class));
        }

        @Test
        @DisplayName("4.2 Deve lançar EntidadeNaoEncontrada ao tentar deletar um convênio inexistente")
        void deveLancarExcecaoAoDeletarOConvenioInexistente() {
            Mockito.when(convenioRepository.findById(1L))
                    .thenReturn(Optional.empty());

            assertThrows(
                    EntidadeNaoEncontrada.class,
                    () -> convenioService.deletar(1L)
            );

            Mockito.verify(convenioRepository, Mockito.never()).delete(any(Convenio.class));
        }
    }
}
