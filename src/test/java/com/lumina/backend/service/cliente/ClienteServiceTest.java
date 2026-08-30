package com.lumina.backend.service.cliente;

import com.lumina.backend.domain.cliente.Cliente;
import com.lumina.backend.domain.cliente.ClienteCommand;
import com.lumina.backend.domain.cliente.ClienteId;
import com.lumina.backend.domain.cliente.ClienteRepositoryPort;
import com.lumina.backend.dto.anamnese.AnamneseRequest;
import com.lumina.backend.dto.cliente.ClienteMapper;
import com.lumina.backend.dto.cliente.ClienteRequest;
import com.lumina.backend.dto.cliente.ResponsavelRequest;
import com.lumina.backend.exception.*;
import com.lumina.backend.model.Anamnese;
import com.lumina.backend.model.EstadoCivil;
import com.lumina.backend.repository.AnamneseRepository;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    private Cliente criarClienteDomain(Long id, String nome, String cpf, String email) {
        ClienteCommand command = new ClienteCommand(
                nome, cpf, "RG-123", LocalDate.of(1998, 5, 21), "SP", "Brasileira",
                'F', "01010-000", "Rua Flores", email, "(11) 98765-4321", 1, null, null, null
        );
        return Cliente.reconstruir(ClienteId.of(id), command);
    }

    @Nested
    @DisplayName("1. Teste de listagem de todos os clientes cadastrados")
    class ListarTest {

        @Mock
        private ClienteRepositoryPort repositoryPort;

        @Mock
        private AnamneseRepository anamneseRepository;

        @Mock
        private EstadoCivilRepository estadoCivilRepository;

        @InjectMocks
        private ClienteService clienteService;

        @Test
        @DisplayName("1.1 Deve retornar uma lista cheia com 5 clientes")
        void deveRetornarUmaListaCheia() {
            Cliente c1 = criarClienteDomain(1L, "Ana", "111.111.111-11", "ana@email.com");
            Cliente c2 = criarClienteDomain(2L, "Bruno", "222.222.222-22", "bruno@email.com");

            Mockito.when(repositoryPort.buscarTodos()).thenReturn(List.of(c1, c2));

            List<Cliente> resultado = clienteService.listar();
            Assertions.assertFalse(resultado.isEmpty());
            Assertions.assertEquals(2, resultado.size());
        }

        @Test
        @DisplayName("1.2 Deve retornar uma lista vazia.")
        void deveRetornarUmaListaVazia() {
            Mockito.when(repositoryPort.buscarTodos()).thenReturn(Collections.emptyList());

            List<Cliente> resultado = clienteService.listar();
            Assertions.assertTrue(resultado.isEmpty());
        }
    }

    @Nested
    @DisplayName("2. Teste de cadastro do cliente")
    class cadatrarTest {

        @Mock
        private ClienteRepositoryPort repositoryPort;

        @Mock
        private AnamneseRepository anamneseRepository;

        @Mock
        private EstadoCivilRepository estadoCivilRepository;

        @InjectMocks
        private ClienteService clienteService;

        @Test
        @DisplayName("2.1 Cadastro deve ser realizado com sucesso")
        void deveCriarClienteCorretamente() {
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

            Cliente clienteDomain = criarClienteDomain(1L, "Ana Souza Lima", "123.456.789-01", "ana.souza@email.com");

            Mockito.when(repositoryPort.existePorEmail(any())).thenReturn(false);
            Mockito.when(repositoryPort.existePorCpf(any())).thenReturn(false);
            Mockito.when(repositoryPort.salvar(any(Cliente.class))).thenReturn(clienteDomain);

            Cliente resultado = clienteService.cadastrar(c1);

            Mockito.verify(repositoryPort, Mockito.times(1)).salvar(any(Cliente.class));
            Assertions.assertNotNull(resultado);
            Assertions.assertEquals("Ana Souza Lima", resultado.getNome());
        }

        @Test
        @DisplayName("2.2 Deve retornar uma exception de email duplicado")
        void deveRetornarUmaExceptionEmailDuplicado() {
            ClienteRequest c1 = new ClienteRequest();
            c1.setEmail("teste@gmail.com");

            Mockito.when(repositoryPort.existePorEmail("teste@gmail.com")).thenReturn(true);

            Assertions.assertThrows(EmailDuplicadoException.class, () -> clienteService.cadastrar(c1));
        }

        @Test
        @DisplayName("2.3 Deve retornar uma exception de CPF duplicado")
        void deveRetornarUmaExceptionCpfDuplicado() {
            ClienteRequest c1 = new ClienteRequest();
            c1.setEmail("teste@gmail.com");
            c1.setCpf("123.456.789-00");

            Mockito.when(repositoryPort.existePorEmail("teste@gmail.com")).thenReturn(false);
            Mockito.when(repositoryPort.existePorCpf("123.456.789-00")).thenReturn(true);

            Assertions.assertThrows(CpfDuplicadoException.class, () -> clienteService.cadastrar(c1));
        }
    }

    @Nested
    @DisplayName("3. Teste de busca por ID")
    class BuscarPorIdTest {

        @Mock
        private ClienteRepositoryPort repositoryPort;

        @Mock
        private AnamneseRepository anamneseRepository;

        @Mock
        private EstadoCivilRepository estadoCivilRepository;

        @InjectMocks
        private ClienteService clienteService;

        @Test
        @DisplayName("3.1 Deve retornar cliente quando o ID existir")
        void deveRetornarClienteQuandoIdExistir() {
            Cliente clienteDomain = criarClienteDomain(1L, "Ana", "111.111.111-11", "ana@email.com");
            Mockito.when(repositoryPort.buscarPorId(ClienteId.of(1L))).thenReturn(Optional.of(clienteDomain));

            Cliente resultado = clienteService.buscarPorId(1L);

            Assertions.assertNotNull(resultado);
            Assertions.assertEquals("Ana", resultado.getNome());
        }

        @Test
        @DisplayName("3.2 Deve lançar EntidadeNaoEncontrada quando ID não existir")
        void deveLancarExcecaoQuandoIdNaoExistir() {
            Mockito.when(repositoryPort.buscarPorId(ClienteId.of(99L))).thenReturn(Optional.empty());

            Assertions.assertThrows(EntidadeNaoEncontrada.class, () -> clienteService.buscarPorId(99L));
        }
    }
}
