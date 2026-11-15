package com.lumina.backend.service.cliente;

import com.lumina.backend.dto.cliente.ClienteRequest;
import com.lumina.backend.exception.CpfDuplicadoException;
import com.lumina.backend.exception.EmailDuplicadoException;
import com.lumina.backend.exception.EntidadeNaoEncontradaException;
import com.lumina.backend.model.Cliente;
import com.lumina.backend.repository.AnamneseRepository;
import com.lumina.backend.repository.ClienteRepository;
import com.lumina.backend.repository.EstadoCivilRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    private Cliente criarCliente(Long id, String nome, String cpf, String email) {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(id);
        cliente.setNome(nome);
        cliente.setCpf(cpf);
        cliente.setRg("RG-123");
        cliente.setDataNascimento(LocalDate.of(1998, 5, 21));
        cliente.setNaturalidade("SP");
        cliente.setNacionalidade("Brasileira");
        cliente.setSexo('F');
        cliente.setCep("01010-000");
        cliente.setEnderecoResidencial("Rua Flores");
        cliente.setEmail(email);
        cliente.setNumeroCelular("(11) 98765-4321");
        cliente.setFkEstadoCivil(1);
        return cliente;
    }

    @Nested
    @DisplayName("1. Teste de listagem de todos os clientes cadastrados")
    class ListarTest {

        @Mock
        private ClienteRepository clienteRepository;

        @Mock
        private AnamneseRepository anamneseRepository;

        @Mock
        private EstadoCivilRepository estadoCivilRepository;

        @InjectMocks
        private ClienteService clienteService;

        @Test
        @DisplayName("1.1 Deve retornar uma lista cheia")
        void deveRetornarUmaListaCheia() {
            Cliente c1 = criarCliente(1L, "Ana", "111.111.111-11", "ana@email.com");
            Cliente c2 = criarCliente(2L, "Bruno", "222.222.222-22", "bruno@email.com");

            Mockito.when(clienteRepository.findAll()).thenReturn(List.of(c1, c2));

            List<Cliente> resultado = clienteService.listar();
            Assertions.assertFalse(resultado.isEmpty());
            Assertions.assertEquals(2, resultado.size());
        }

        @Test
        @DisplayName("1.2 Deve retornar uma lista vazia.")
        void deveRetornarUmaListaVazia() {
            Mockito.when(clienteRepository.findAll()).thenReturn(Collections.emptyList());

            List<Cliente> resultado = clienteService.listar();
            Assertions.assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("1.3 Deve retornar uma página de clientes usando PageRequest e Sort")
        void deveRetornarPaginaDeClientes() {
            Cliente c1 = criarCliente(1L, "Ana", "111.111.111-11", "ana@email.com");
            Cliente c2 = criarCliente(2L, "Bruno", "222.222.222-22", "bruno@email.com");
            Pageable pageable = PageRequest.of(0, 6, Sort.by(Sort.Direction.ASC, "nome"));
            Page<Cliente> pageEsperada = new PageImpl<>(List.of(c1, c2), pageable, 2);

            Mockito.when(clienteRepository.findAll(pageable)).thenReturn(pageEsperada);

            Page<Cliente> resultado = clienteService.listarComFiltrosPaginado(null, null, pageable);
            Assertions.assertFalse(resultado.isEmpty());
            Assertions.assertEquals(2, resultado.getTotalElements());
            Assertions.assertEquals(0, resultado.getNumber());
        }
    }

    @Nested
    @DisplayName("2. Teste de cadastro do cliente")
    class CadastrarTest {

        @Mock
        private ClienteRepository clienteRepository;

        @Mock
        private AnamneseRepository anamneseRepository;

        @Mock
        private EstadoCivilRepository estadoCivilRepository;

        @InjectMocks
        private ClienteService clienteService;

        @Test
        @DisplayName("2.1 Cadastro deve ser realizado com sucesso")
        void deveCriarClienteCorretamente() {
            ClienteRequest c1 = ClienteRequest.builder()
                    .idCliente(1L)
                    .nome("Ana Souza Lima")
                    .cpf("123.456.789-01")
                    .rg("MG-12.345.678")
                    .dataNascimento(LocalDate.of(1998, 5, 21))
                    .numeroCelular("(11) 98765-4321")
                    .email("ana.souza@email.com")
                    .sexo('F')
                    .naturalidade("São Paulo")
                    .nacionalidade("Brasileira")
                    .fkEstadoCivil(1)
                    .enderecoResidencial("Rua das Flores, 120")
                    .cep("01010-000")
                    .build();

            Cliente clienteSalvo = criarCliente(1L, "Ana Souza Lima", "123.456.789-01", "ana.souza@email.com");

            Mockito.when(clienteRepository.existsByEmail(any())).thenReturn(false);
            Mockito.when(clienteRepository.existsByCpf(any())).thenReturn(false);
            Mockito.when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteSalvo);

            Cliente resultado = clienteService.cadastrar(c1);

            Mockito.verify(clienteRepository, Mockito.times(1)).save(any(Cliente.class));
            Assertions.assertNotNull(resultado);
            Assertions.assertEquals("Ana Souza Lima", resultado.getNome());
        }

        @Test
        @DisplayName("2.2 Deve retornar uma exception de email duplicado")
        void deveRetornarUmaExceptionEmailDuplicado() {
            ClienteRequest c1 = ClienteRequest.builder()
                    .email("teste@gmail.com")
                    .build();

            Mockito.when(clienteRepository.existsByEmail("teste@gmail.com")).thenReturn(true);

            Assertions.assertThrows(EmailDuplicadoException.class, () -> clienteService.cadastrar(c1));
        }

        @Test
        @DisplayName("2.3 Deve retornar uma exception de CPF duplicado")
        void deveRetornarUmaExceptionCpfDuplicado() {
            ClienteRequest c1 = ClienteRequest.builder()
                    .email("teste@gmail.com")
                    .cpf("123.456.789-00")
                    .build();

            Mockito.when(clienteRepository.existsByEmail("teste@gmail.com")).thenReturn(false);
            Mockito.when(clienteRepository.existsByCpf("123.456.789-00")).thenReturn(true);

            Assertions.assertThrows(CpfDuplicadoException.class, () -> clienteService.cadastrar(c1));
        }
    }

    @Nested
    @DisplayName("3. Teste de busca por ID")
    class BuscarPorIdTest {

        @Mock
        private ClienteRepository clienteRepository;

        @Mock
        private AnamneseRepository anamneseRepository;

        @Mock
        private EstadoCivilRepository estadoCivilRepository;

        @InjectMocks
        private ClienteService clienteService;

        @Test
        @DisplayName("3.1 Deve retornar cliente quando o ID existir")
        void deveRetornarClienteQuandoIdExistir() {
            Cliente cliente = criarCliente(1L, "Ana", "111.111.111-11", "ana@email.com");
            Mockito.when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

            Cliente resultado = clienteService.buscarPorId(1L);

            Assertions.assertNotNull(resultado);
            Assertions.assertEquals("Ana", resultado.getNome());
        }

        @Test
        @DisplayName("3.2 Deve lançar EntidadeNaoEncontradaException quando ID não existir")
        void deveLancarExcecaoQuandoIdNaoExistir() {
            Mockito.when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

            Assertions.assertThrows(EntidadeNaoEncontradaException.class, () -> clienteService.buscarPorId(99L));
        }
    }
}
