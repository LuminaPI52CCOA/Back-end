package com.lumina.backend.service.cliente;

import com.lumina.backend.dto.anamnese.AnamneseRequest;
import com.lumina.backend.dto.cliente.ClienteMapper;
import com.lumina.backend.dto.cliente.ClienteRequest;
import com.lumina.backend.exception.*;
import com.lumina.backend.model.Anamnese;
import com.lumina.backend.model.Cliente;
import com.lumina.backend.repository.AnamneseRepository;
import com.lumina.backend.repository.ClienteRepository;
import com.lumina.backend.service.anamnese.AnamneseService;
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
    @DisplayName("1. Teste de listagem de todos os clientes cadastrados")
    class ListarTest{

        @Mock
        private ClienteRepository clienteRepository;

        @InjectMocks
        private ClienteService clienteService;

        @Test
        @DisplayName("1.1 Deve retornar uma lista cheia.")
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
        @DisplayName("1.2 Deve retornar uma lista vazia.")
        void deveRetornarUmaListaVazia(){


            var listaCheia = Collections.EMPTY_LIST;

            Mockito.when(clienteRepository.findAll())
                    .thenReturn(listaCheia);

            List<Cliente> resultado = clienteService.listar();
            Assertions.assertTrue(resultado.isEmpty());

        }
    }

    @Nested
    @DisplayName("2. Teste de cadastro do cliente")
    class cadatrarTest{
        @Mock
        private ClienteRepository clienteRepository;

        @InjectMocks
        private ClienteService clienteService;

        @Test
        @DisplayName("2.1 Cadastro deve ser realizado com sucesso")
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

        @Test
        @DisplayName("2.2 Deve retornar uma exception de email duplicado")
        void deveRetornarUmaExceptionEmailDuplicado(){
            ClienteRequest c1 = new ClienteRequest();
            c1.setEmail("teste@gmail.com");

            Mockito.when(clienteRepository.findByEmail("teste@gmail.com"))
                    .thenReturn(Optional.of(ClienteMapper.toEntity(c1)));

            EmailDuplicadoException emailDuplicadoException = assertThrows(
                    EmailDuplicadoException.class,
                    () -> clienteService.cadastrar(c1)
            );
        }

            @Test
            @DisplayName("2.3 Deve retornar uma exception de cpf duplicado")
            void NaoDeveCadastrarOClienteCorretamente(){
                ClienteRequest c1 = new ClienteRequest();
                c1.setCpf("1111111111");
                c1.setEmail("ana.souza@email.com");

                Cliente c2 = new Cliente();
                c2.setCpf("1111111111");
    
                Mockito.when(clienteRepository.findByEmail(Mockito.anyString()))
                        .thenReturn(Optional.empty());
    
                Mockito.when(clienteRepository.findByCpf("1111111111"))
                        .thenReturn(Optional.of(c2));
    
                CpfDuplicadoException cpfDuplicadoException = assertThrows(
                        CpfDuplicadoException.class,
                        () -> clienteService.cadastrar(c1)
                );
        }
    }
    @Nested
    @DisplayName("3. Teste de busca por id")
    class bucarPorIdTest{

        @Mock
        private ClienteRepository clienteRepository;

        @InjectMocks
        private ClienteService clienteService;

        @Test
        @DisplayName("3.1 Deve retornar o cliente buscado pelo id especifico corretamente")
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
        @DisplayName("3.2 Não deve retornar o cliente buscado pelo id especifico corretamente")
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
    @DisplayName("4. Testes de atualizar o cliente")
    class AtualizarClienteTest{
        @Mock
        private ClienteRepository clienteRepository;

        @InjectMocks
        private ClienteService clienteService;
        @Test
        @DisplayName("4.1 Deve atualizar o cliente com sucesso")
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
        @DisplayName("4.2 Deve lançar EntidadeNaoEncontrada ao tentar atualizar um cliente inexistente")
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
    @Nested
    @DisplayName("5. Testes do Método Listar Anamnese")
    class ListarAnamneseTest {

        @Mock
        private AnamneseRepository anamneseRepository;

        @Mock
        private ClienteRepository repository;

        @InjectMocks
        private ClienteService service;

        @Test
        @DisplayName("5.1 Deve retornar a lista de anamneses com sucesso quando existirem registros.")
        void deveRetornarListaDeAnamnesesComSucesso() {
            Integer clienteId = 1;
            Anamnese a1 = new Anamnese();
            a1.setIdAnamnese(1L);
            a1.setDescricaoTratamento("Tratamento ortodôntico");

            List<Anamnese> listaCheia = List.of(a1);

            Mockito.when(anamneseRepository.findAnamneseByFkCliente_IdCliente(clienteId))
                    .thenReturn(listaCheia);


            List<Anamnese> resultado = service.listarAnamnese(clienteId);


            assertFalse(resultado.isEmpty());
            assertEquals(1, resultado.size());
            assertEquals("Tratamento ortodôntico", resultado.get(0).getDescricaoTratamento());

        }

        @Test
        @DisplayName("5.2 Deve lançar EntidadeNaoEncontrada quando a lista de anamneses vier vazia.")
        void deveLancarExcecaoQuandoListaVazia() {

            Integer clienteId = 2;
            Mockito.when(anamneseRepository.findAnamneseByFkCliente_IdCliente(clienteId))
                    .thenReturn(Collections.emptyList());


            EntidadeNaoEncontrada excecao = assertThrows(EntidadeNaoEncontrada.class, () -> {
                service.listarAnamnese(clienteId);
            });
        }
    }
    @Nested
    @DisplayName("6. Testes do Método Cadastrar Anamnese")
    class CadastrarAnamneseTest {

        @Mock
        private AnamneseRepository anamneseRepository;

        @Mock
        private ClienteRepository repository;

        @InjectMocks
        private ClienteService service;

        @Test
        @DisplayName("6.1 Deve cadastrar anamnese com sucesso quando o cliente for encontrado.")
        void deveCadastrarAnamneseComSucesso() {

            Long clienteId = 10L;
            Cliente clienteExistente = new Cliente();

            AnamneseRequest request = new AnamneseRequest();
            request.setIdAnamnese(100L);
            request.setDataAnamnese(LocalDate.now());
            request.setFazendoTratamento(true);
            request.setDescricaoTratamento("Cardíaco");
            request.setAlergiaMedicamentos(false);

            Mockito.when(repository.findById(clienteId)).thenReturn(Optional.of(clienteExistente));

            Anamnese resultado = service.cadastrarAnamnese(clienteId, request);

            assertNotNull(resultado);
            assertEquals(100L, resultado.getIdAnamnese());
            assertEquals("Cardíaco", resultado.getDescricaoTratamento());
            assertTrue(resultado.getFazendoTratamento());


            Mockito.verify(repository, Mockito.times(1)).findById(clienteId);
            Mockito.verify(anamneseRepository, Mockito.times(1)).save(any(Anamnese.class));
        }

        @Test
        @DisplayName("6.2 Deve lançar EntidadeNaoEncontrada ao tentar cadastrar anamnese para cliente inexistente.")
        void deveLancarExcecaoQuandoClienteNaoEncontrado() {
            Long clienteId = 99L;
            AnamneseRequest request = new AnamneseRequest();

            Mockito.when(repository.findById(clienteId)).thenReturn(Optional.empty());


            EntidadeNaoEncontrada excecao = assertThrows(EntidadeNaoEncontrada.class, () -> {
                service.cadastrarAnamnese(clienteId, request);
            });

            assertEquals("Cliente não encontrado!", excecao.getMessage());


            Mockito.verify(repository, Mockito.times(1)).findById(clienteId);
            Mockito.verify(anamneseRepository, Mockito.never()).save(any(Anamnese.class));
        }
    }
}