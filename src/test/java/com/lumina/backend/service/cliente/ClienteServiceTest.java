package com.lumina.backend.service.cliente;

import com.lumina.backend.dto.cliente.ClienteMapper;
import com.lumina.backend.dto.cliente.ClienteRequest;
//import com.lumina.backend.exceptionAtributoJaCadastradoException;
//import com.lumina.backend.exception.CampoNuloOuIncorretoException;
import com.lumina.backend.exception.EntidadeNaoEncontrada;
import com.lumina.backend.model.Cliente;
import com.lumina.backend.repository.ClienteRepository;
import io.jsonwebtoken.lang.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.availability.LivenessState;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Nested
    @DisplayName("Teste de listagem de todos os clientes cadastrados")
    class ListarTest{

        @Mock
        private ClienteRepository clienteRepository;

        @InjectMocks
        private ClienteService clienteService;

        @Test
        @DisplayName("Deve retornar uma lista cheia.")
        void deveRetornarUmaListaCheiaComSucesso(){
            Cliente c1 = new Cliente();
            c1.setIdCliente(1L);
            c1.setNome("Ana Souza Lima");
            c1.setCpf("123.456.789-01");
            c1.setRg("MG-12.345.678");
            c1.setDataNascimento(LocalDate.of(1998, 5, 21));
            c1.setNumeroCelular("(11) 98765-4321");
            c1.setEmail("ana.souza@email.com");
            c1.setSexo('F');
            c1.setNaturalidade("São Paulo");
            c1.setNacionalidade("Brasileira");
            c1.setFkEstadoCivil(1);
            c1.setEnderecoResidencial("Rua das Flores, 120");
            c1.setCep("01010-000");
            c1.setFkClienteIndicacao(null);
            c1.setFkResponsavel(null);
            c1.setGrauParentescoResponsavel(null);

            Cliente c2 = new Cliente();
            c2.setIdCliente(2L);
            c2.setNome("Bruno Oliveira Santos");
            c2.setCpf("987.654.321-00");
            c2.setRg("SP-98.765.432");
            c2.setDataNascimento(LocalDate.of(1992, 10, 14));
            c2.setNumeroCelular("(11) 97654-3210");
            c2.setEmail("bruno.oliveira@email.com");
            c2.setSexo('M');
            c2.setNaturalidade("Campinas");
            c2.setNacionalidade("Brasileira");
            c2.setFkEstadoCivil(2);
            c2.setEnderecoResidencial("Av. Paulista, 1000");
            c2.setCep("01310-100");
            c2.setFkClienteIndicacao(1);
            c2.setFkResponsavel(null);
            c2.setGrauParentescoResponsavel(null);

            Cliente c3 = new Cliente();
            c3.setIdCliente(3L);
            c3.setNome("Carla Mendes Rocha");
            c3.setCpf("111.222.333-44");
            c3.setRg("RJ-11.222.333");
            c3.setDataNascimento(LocalDate.of(2001, 3, 9));
            c3.setNumeroCelular("(21) 99888-7766");
            c3.setEmail("carla.mendes@email.com");
            c3.setSexo('F');
            c3.setNaturalidade("Rio de Janeiro");
            c3.setNacionalidade("Brasileira");
            c3.setFkEstadoCivil(1);
            c3.setEnderecoResidencial("Rua Atlântica, 55");
            c3.setCep("22070-001");
            c3.setFkClienteIndicacao(2);
            c3.setFkResponsavel(5);
            c3.setGrauParentescoResponsavel("Mãe");

            Cliente c4 = new Cliente();
            c4.setIdCliente(4L);
            c4.setNome("Diego Pereira Alves");
            c4.setCpf("222.333.444-55");
            c4.setRg("PR-22.333.444");
            c4.setDataNascimento(LocalDate.of(1985, 7, 30));
            c4.setNumeroCelular("(41) 98877-6655");
            c4.setEmail("diego.alves@email.com");
            c4.setSexo('M');
            c4.setNaturalidade("Curitiba");
            c4.setNacionalidade("Brasileira");
            c4.setFkEstadoCivil(3);
            c4.setEnderecoResidencial("Rua XV de Novembro, 200");
            c4.setCep("80020-310");
            c4.setFkClienteIndicacao(null);
            c4.setFkResponsavel(null);
            c4.setGrauParentescoResponsavel(null);

            Cliente c5 = new Cliente();
            c5.setIdCliente(5L);
            c5.setNome("Eduarda Ribeiro Costa");
            c5.setCpf("333.444.555-66");
            c5.setRg("RS-33.444.555");
            c5.setDataNascimento(LocalDate.of(1995, 12, 2));
            c5.setNumeroCelular("(51) 97766-5544");
            c5.setEmail("eduarda.costa@email.com");
            c5.setSexo('F');
            c5.setNaturalidade("Porto Alegre");
            c5.setNacionalidade("Brasileira");
            c5.setFkEstadoCivil(2);
            c5.setEnderecoResidencial("Av. Ipiranga, 500");
            c5.setCep("90160-091");
            c5.setFkClienteIndicacao(3);
            c5.setFkResponsavel(null);
            c5.setGrauParentescoResponsavel(null);

            var listaCheia = List.of(
                    c1, c2, c3, c4, c5
            );

            Mockito.when(clienteRepository.findAll())
                    .thenReturn(listaCheia);

            List<Cliente> resultado = clienteService.listar();
            Assertions.assertFalse(resultado.isEmpty());

        }
        @Test
        @DisplayName("Deve retornar uma lista vazia.")
        void deveRetornarUmaListaVazia(){


            var listaCheia = Collections.EMPTY_LIST;

            Mockito.when(clienteRepository.findAll())
                    .thenReturn(listaCheia);

            List<Cliente> resultado = clienteService.listar();
            Assertions.assertTrue(resultado.isEmpty());

        }
    }

    @Nested
    @DisplayName("Teste de cadastro do cliente")
    class cadatrarTest{
        @Mock
        private ClienteRepository clienteRepository;

        @InjectMocks
        private ClienteService clienteService;

        @Test
        @DisplayName("Cadastro deve ser realizado com sucesso")
        void deveCriarClienteCorretamente(){
            ClienteRequest c1 = new ClienteRequest();
            c1.setIdCliente(1L);
            c1.setNome("Ana Souza Lima");
            c1.setCpf("123.456.789-01");
            c1.setRg("MG-12.345.678");
            c1.setDataNascimento(LocalDate.of(1998, 5, 21));
            c1.setNumeroCelular("(11) 98765-4321");
            c1.setEmail("ana.souza@email.com");
            c1.setSexo('F');
            c1.setNaturalidade("São Paulo");
            c1.setNacionalidade("Brasileira");
            c1.setFkEstadoCivil(1);
            c1.setEnderecoResidencial("Rua das Flores, 120");
            c1.setCep("01010-000");
            c1.setFkClienteIndicacao(1);
            c1.setFkResponsavel(1);
            c1.setGrauParentescoResponsavel(null);

            Cliente cliente = ClienteMapper.toEntity(c1);

            Mockito.when(clienteRepository.save(any(Cliente.class)))
                    .thenReturn(cliente);

            Cliente resultado = clienteService.cadastrar(c1);

            Mockito.verify(clienteRepository, Mockito.times(1))
                    .save(any(Cliente.class));

            Assertions.assertNotNull(resultado);
        }
    }
    @Nested
    @DisplayName("Teste de busca por id")
    class bucarPorIdTest{

        @Mock
        private ClienteRepository clienteRepository;

        @InjectMocks
        private ClienteService clienteService;

        @Test
        @DisplayName("Deve retornar o cliente buscado pelo id especifico corretamente")
        void deveBuscarCorretamenteOClientePorId(){
            Cliente c1 = new Cliente();
            c1.setIdCliente(1L);
            c1.setNome("Ana Souza Lima");
            c1.setCpf("123.456.789-01");
            c1.setRg("MG-12.345.678");
            c1.setDataNascimento(LocalDate.of(1998, 5, 21));
            c1.setNumeroCelular("(11) 98765-4321");
            c1.setEmail("ana.souza@email.com");
            c1.setSexo('F');
            c1.setNaturalidade("São Paulo");
            c1.setNacionalidade("Brasileira");
            c1.setFkEstadoCivil(1);
            c1.setEnderecoResidencial("Rua das Flores, 120");
            c1.setCep("01010-000");
            c1.setFkClienteIndicacao(1);
            c1.setFkResponsavel(1);
            c1.setGrauParentescoResponsavel(null);

            Mockito.when(clienteRepository.findById(1L))
                    .thenReturn(Optional.of(c1));

            Cliente restultado = clienteService.buscarPorId(1L);

            Assertions.assertEquals(
                    c1.getIdCliente(),
                    restultado.getIdCliente()
            );
        }

        @Test
        @DisplayName("Não deve retornar o cliente buscado pelo id especifico corretamente")
        void naoDeveBuscarOClientePorId(){

            Mockito.when(clienteRepository.findById(1L))
                    .thenReturn(Optional.empty());

            EntidadeNaoEncontrada entidadeNaoEncontrada = assertThrows(
                    EntidadeNaoEncontrada.class,
                    () -> clienteService.buscarPorId(1L)
            );
        }
    }

    @Nested
    @DisplayName("Testes de atualizar o cliente")
    class AtualizarClienteTest{
        @Mock
        private ClienteRepository clienteRepository;

        @InjectMocks
        private ClienteService clienteService;
        @Test
        @DisplayName("Deve atualizar o cliente com sucesso")
        void deveAtualizarOClienteComSucesso(){
            ClienteRequest c1 = new ClienteRequest();
            c1.setIdCliente(1L);
            c1.setNome("Ana Souza Lima");
            c1.setCpf("123.456.789-01");
            c1.setRg("MG-12.345.678");
            c1.setDataNascimento(LocalDate.of(1998, 5, 21));
            c1.setNumeroCelular("(11) 98765-4321");
            c1.setEmail("ana.souza@email.com");
            c1.setSexo('F');
            c1.setNaturalidade("São Paulo");
            c1.setNacionalidade("Brasileira");
            c1.setFkEstadoCivil(1);
            c1.setEnderecoResidencial("Rua das Flores, 120");
            c1.setCep("01010-000");
            c1.setFkClienteIndicacao(1);
            c1.setFkResponsavel(1);
            c1.setGrauParentescoResponsavel(null);

            Optional<Cliente> teste = Optional.of(ClienteMapper.toEntity(c1));

            Mockito.when(clienteRepository.findById(anyLong()))
                    .thenReturn(teste);

            Cliente cliente = ClienteMapper.toEntity(c1);

            Mockito.when(clienteRepository.save(any(Cliente.class)))
                    .thenReturn(cliente);

            Cliente resultado = clienteService.atualizar(c1, 1L);

            Mockito.verify(clienteRepository, Mockito.times(1))
                    .findById(anyLong());
            Mockito.verify(clienteRepository, Mockito.times(1))
                    .save(any(Cliente.class));

            Assertions.assertNotNull(resultado);
        }

        @Test
        @DisplayName("Deve lançar EntidadeNaoEncontrada ao tentar atualizar um cliente inexistente")
        void deveLancarExcecaoAoAtualizarOClienteInexistente() {

            ClienteRequest request = new ClienteRequest();
            request.setNome("Ana");


            Mockito.when(clienteRepository.findById(1L))
                    .thenReturn(java.util.Optional.ofNullable(null));


            EntidadeNaoEncontrada ex = assertThrows(
                    EntidadeNaoEncontrada.class,
                    () -> clienteService.atualizar(request, 1L)
            );

            Mockito.verify(clienteRepository, Mockito.never()).save(any(Cliente.class));
        }

    }
}