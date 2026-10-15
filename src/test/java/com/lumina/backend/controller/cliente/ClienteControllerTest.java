package com.lumina.backend.controller.cliente;

import com.lumina.backend.controller.ClienteController;
import com.lumina.backend.domain.cliente.Cliente;
import com.lumina.backend.domain.cliente.ClienteCommand;
import com.lumina.backend.domain.cliente.ClienteId;
import com.lumina.backend.dto.anamnese.AnamneseRequest;
import com.lumina.backend.dto.anamnese.AnamneseResponse;
import com.lumina.backend.dto.cliente.ClienteRequest;
import com.lumina.backend.dto.cliente.ClienteResponse;
import com.lumina.backend.dto.convenio.ConvenioResponse;
import com.lumina.backend.dto.estado_civil.EstadoCivilResponse;
import com.lumina.backend.exception.EmailDuplicadoException;
import com.lumina.backend.exception.EntidadeNaoEncontrada;
import com.lumina.backend.model.Anamnese;
import com.lumina.backend.model.Convenio;
import com.lumina.backend.model.EstadoCivil;
import com.lumina.backend.service.cliente.ClienteService;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
class ClienteControllerTest {

    @Mock
    private ClienteService clienteService;

    @Mock
    private ConvenioService convenioService;

    @InjectMocks
    private ClienteController clienteController;

    private Cliente criarClienteDomain(Long id, String nome, String cpf, String email) {
        ClienteCommand command = new ClienteCommand(
                nome, cpf, "RG-123", LocalDate.of(1998, 5, 21), "SP", "Brasileira",
                'F', "01010-000", "Rua Flores", email, "(11) 98765-4321", 1, null, null, null
        );
        return Cliente.reconstruir(ClienteId.of(id), command);
    }

    @Nested
    @DisplayName("1. Teste de listagem de todos os clientes")
    class ListarTest {

        @Test
        @DisplayName("1.1 Deve retornar uma lista cheia de clientes com status 200")
        void deveRetornarListaCheiaComSucesso() {
            Cliente c1 = criarClienteDomain(1L, "Ana Souza Lima", "123.456.789-01", "ana.souza@email.com");
            Cliente c2 = criarClienteDomain(2L, "Bruno Oliveira Santos", "987.654.321-00", "bruno.oliveira@email.com");

            List<Cliente> listaCheia = List.of(c1, c2);

            Mockito.when(clienteService.listarComFiltros(null, null)).thenReturn(listaCheia);

            ResponseEntity<List<ClienteResponse>> response = clienteController.listar(null, null);

            Assertions.assertEquals(200, response.getStatusCodeValue());
            Assertions.assertNotNull(response.getBody());
            Assertions.assertFalse(response.getBody().isEmpty());
            Assertions.assertEquals(2, response.getBody().size());

            Mockito.verify(clienteService, Mockito.times(1)).listarComFiltros(null, null);
        }

        @Test
        @DisplayName("1.2 Deve retornar status 204 quando a lista estiver vazia")
        void deveRetornarStatus204QuandoListaVazia() {
            Mockito.when(clienteService.listarComFiltros(null, null)).thenReturn(Collections.emptyList());

            ResponseEntity<List<ClienteResponse>> response = clienteController.listar(null, null);

            Assertions.assertEquals(204, response.getStatusCodeValue());
            Assertions.assertNull(response.getBody());

            Mockito.verify(clienteService, Mockito.times(1)).listarComFiltros(null, null);
        }
    }

    @Nested
    @DisplayName("2. Teste de cadastro de cliente")
    class CadastrarTest {

        @Test
        @DisplayName("2.1 Deve cadastrar cliente com sucesso e retornar status 201")
        void deveCadastrarClienteComSucesso() {
            ClienteRequest request = new ClienteRequest();
            request.setIdCliente(1L);
            request.setNome("Ana Souza Lima");
            request.setCpf("123.456.789-01");
            request.setEmail("ana.souza@email.com");

            Cliente clienteSalvo = criarClienteDomain(1L, "Ana Souza Lima", "123.456.789-01", "ana.souza@email.com");

            Mockito.when(clienteService.cadastrar(any(ClienteRequest.class))).thenReturn(clienteSalvo);

            ResponseEntity<ClienteResponse> response = clienteController.cadastrar(request);

            Assertions.assertEquals(201, response.getStatusCodeValue());
            Assertions.assertNotNull(response.getBody());
            Assertions.assertEquals("Ana Souza Lima", response.getBody().getNome());

            Mockito.verify(clienteService, Mockito.times(1)).cadastrar(any(ClienteRequest.class));
        }

        @Test
        @DisplayName("2.2 Deve lançar EmailDuplicadoException ao tentar cadastrar cliente com email duplicado")
        void deveLancarExcecaoEmailDuplicado() {
            ClienteRequest request = new ClienteRequest();
            request.setEmail("teste@gmail.com");

            Mockito.doThrow(new EmailDuplicadoException("Email já cadastrado"))
                    .when(clienteService).cadastrar(any(ClienteRequest.class));

            EmailDuplicadoException exception = assertThrows(
                    EmailDuplicadoException.class,
                    () -> clienteController.cadastrar(request)
            );

            Assertions.assertEquals("Email já cadastrado", exception.getMessage());
            Mockito.verify(clienteService, Mockito.times(1)).cadastrar(any(ClienteRequest.class));
        }
    }

    @Nested
    @DisplayName("3. Teste de busca por ID")
    class BuscarPorIdTest {

        @Test
        @DisplayName("3.1 Deve retornar cliente encontrado com status 200")
        void deveRetornarClienteEncontradoComSucesso() {
            Cliente cliente = criarClienteDomain(1L, "Ana Souza Lima", "123.456.789-01", "ana.souza@email.com");

            Mockito.when(clienteService.buscarPorId(1L)).thenReturn(cliente);

            ResponseEntity<ClienteResponse> response = clienteController.buscarPorId(1L);

            Assertions.assertEquals(200, response.getStatusCodeValue());
            Assertions.assertNotNull(response.getBody());
            Assertions.assertEquals("Ana Souza Lima", response.getBody().getNome());
            Assertions.assertEquals(1L, response.getBody().getIdCliente());

            Mockito.verify(clienteService, Mockito.times(1)).buscarPorId(1L);
        }

        @Test
        @DisplayName("3.2 Deve lançar EntidadeNaoEncontrada quando cliente não existir")
        void deveLancarExcecaoQuandoClienteNaoEncontrado() {
            Mockito.when(clienteService.buscarPorId(1L))
                    .thenThrow(new EntidadeNaoEncontrada("Cliente não encontrado"));

            EntidadeNaoEncontrada exception = assertThrows(
                    EntidadeNaoEncontrada.class,
                    () -> clienteController.buscarPorId(1L)
            );

            Assertions.assertEquals("Cliente não encontrado", exception.getMessage());
            Mockito.verify(clienteService, Mockito.times(1)).buscarPorId(1L);
        }
    }

    @Nested
    @DisplayName("4. Teste de atualização de cliente")
    class AtualizarTest {

        @Test
        @DisplayName("4.1 Deve atualizar cliente com sucesso e retornar status 200")
        void deveAtualizarClienteComSucesso() {
            ClienteRequest request = new ClienteRequest();
            request.setIdCliente(1L);
            request.setNome("Ana Souza Atualizado");
            request.setEmail("ana.atualizado@email.com");

            Cliente clienteAtualizado = criarClienteDomain(1L, "Ana Souza Atualizado", "123.456.789-01", "ana.atualizado@email.com");

            Mockito.when(clienteService.atualizar(any(ClienteRequest.class), anyLong()))
                    .thenReturn(clienteAtualizado);

            ResponseEntity<ClienteResponse> response = clienteController.atualizar(request, 1L);

            Assertions.assertEquals(200, response.getStatusCodeValue());
            Assertions.assertNotNull(response.getBody());
            Assertions.assertEquals("Ana Souza Atualizado", response.getBody().getNome());

            Mockito.verify(clienteService, Mockito.times(1)).atualizar(any(ClienteRequest.class), anyLong());
        }

        @Test
        @DisplayName("4.2 Deve lançar EntidadeNaoEncontrada ao tentar atualizar cliente inexistente")
        void deveLancarExcecaoAoAtualizarClienteInexistente() {
            ClienteRequest request = new ClienteRequest();
            request.setNome("Teste");

            Mockito.when(clienteService.atualizar(any(ClienteRequest.class), anyLong()))
                    .thenThrow(new EntidadeNaoEncontrada("Cliente não encontrado"));

            EntidadeNaoEncontrada exception = assertThrows(
                    EntidadeNaoEncontrada.class,
                    () -> clienteController.atualizar(request, 1L)
            );

            Assertions.assertEquals("Cliente não encontrado", exception.getMessage());
            Mockito.verify(clienteService, Mockito.times(1)).atualizar(any(ClienteRequest.class), anyLong());
        }
    }

    @Nested
    @DisplayName("5. Teste de listagem de convênios do cliente")
    class ListarConveniosTest {

        @Test
        @DisplayName("5.1 Deve retornar lista de convênios com status 200")
        void deveRetornarListaDeConveniosComSucesso() {
            Convenio conv1 = new Convenio();
            conv1.setIdConvenio(1L);
            conv1.setNome("Unimed");

            Convenio conv2 = new Convenio();
            conv2.setIdConvenio(2L);
            conv2.setNome("Bradesco");

            List<Convenio> convenios = List.of(conv1, conv2);

            Mockito.when(convenioService.listarConveniosCliente(1L)).thenReturn(convenios);

            ResponseEntity<List<ConvenioResponse>> response = clienteController.listarConvenios(1L);

            Assertions.assertEquals(200, response.getStatusCodeValue());
            Assertions.assertNotNull(response.getBody());
            Assertions.assertFalse(response.getBody().isEmpty());
            Assertions.assertEquals(2, response.getBody().size());

            Mockito.verify(convenioService, Mockito.times(1)).listarConveniosCliente(1L);
        }

        @Test
        @DisplayName("5.2 Deve retornar lista vazia de convênios com status 200")
        void deveRetornarListaVaziaDeConvenios() {
            Mockito.when(convenioService.listarConveniosCliente(1L)).thenReturn(Collections.emptyList());

            ResponseEntity<List<ConvenioResponse>> response = clienteController.listarConvenios(1L);

            Assertions.assertEquals(200, response.getStatusCodeValue());
            Assertions.assertNotNull(response.getBody());
            Assertions.assertTrue(response.getBody().isEmpty());

            Mockito.verify(convenioService, Mockito.times(1)).listarConveniosCliente(1L);
        }
    }

    @Nested
    @DisplayName("6. Teste de listagem de anamneses do cliente")
    class ListarAnamneseTest {

        @Test
        @DisplayName("6.1 Deve retornar lista de anamneses com status 200")
        void deveRetornarListaDeAnamnesesComSucesso() {
            com.lumina.backend.model.Cliente cliente = new com.lumina.backend.model.Cliente();
            cliente.setIdCliente(1L);

            Anamnese a1 = new Anamnese();
            a1.setIdAnamnese(1L);
            a1.setDescricaoTratamento("Tratamento ortodôntico");
            a1.setFkCliente(cliente);

            Anamnese a2 = new Anamnese();
            a2.setIdAnamnese(2L);
            a2.setDescricaoTratamento("Tratamento cardíaco");
            a2.setFkCliente(cliente);

            List<Anamnese> anamneses = List.of(a1, a2);

            Mockito.when(clienteService.listarAnamnese(1)).thenReturn(anamneses);

            ResponseEntity<List<AnamneseResponse>> response = clienteController.listarAnamnese(1);

            Assertions.assertEquals(200, response.getStatusCodeValue());
            Assertions.assertNotNull(response.getBody());
            Assertions.assertFalse(response.getBody().isEmpty());
            Assertions.assertEquals(2, response.getBody().size());

            Mockito.verify(clienteService, Mockito.times(1)).listarAnamnese(1);
        }

        @Test
        @DisplayName("6.2 Deve lançar EntidadeNaoEncontrada quando não houver anamneses")
        void deveLancarExcecaoQuandoNaoHouverAnamneses() {
            Mockito.when(clienteService.listarAnamnese(1))
                    .thenThrow(new EntidadeNaoEncontrada("Anamneses não encontradas"));

            EntidadeNaoEncontrada exception = assertThrows(
                    EntidadeNaoEncontrada.class,
                    () -> clienteController.listarAnamnese(1)
            );

            Assertions.assertEquals("Anamneses não encontradas", exception.getMessage());
            Mockito.verify(clienteService, Mockito.times(1)).listarAnamnese(1);
        }
    }

    @Nested
    @DisplayName("7. Teste de cadastro de anamnese")
    class CadastrarAnamneseTest {

        @Test
        @DisplayName("7.1 Deve cadastrar anamnese com sucesso e retornar status 200")
        void deveCadastrarAnamneseComSucesso() {
            com.lumina.backend.model.Cliente cliente = new com.lumina.backend.model.Cliente();
            cliente.setIdCliente(1L);

            AnamneseRequest request = new AnamneseRequest();
            request.setIdAnamnese(100L);
            request.setDataAnamnese(LocalDate.now());
            request.setFazendoTratamento(true);
            request.setDescricaoTratamento("Cardíaco");
            request.setAlergiaMedicamentos(false);

            Anamnese anamneseSalva = new Anamnese();
            anamneseSalva.setIdAnamnese(100L);
            anamneseSalva.setDescricaoTratamento("Cardíaco");
            anamneseSalva.setFazendoTratamento(true);
            anamneseSalva.setFkCliente(cliente);

            Mockito.when(clienteService.cadastrarAnamnese(anyLong(), any(AnamneseRequest.class)))
                    .thenReturn(anamneseSalva);

            ResponseEntity<AnamneseResponse> response = clienteController.cadastroAnamnese(1L, request);

            Assertions.assertEquals(200, response.getStatusCodeValue());
            Assertions.assertNotNull(response.getBody());
            Assertions.assertEquals("Cardíaco", response.getBody().getDescricaoTratamento());
            Assertions.assertTrue(response.getBody().getFazendoTratamento());

            Mockito.verify(clienteService, Mockito.times(1)).cadastrarAnamnese(anyLong(), any(AnamneseRequest.class));
        }

        @Test
        @DisplayName("7.2 Deve lançar EntidadeNaoEncontrada ao tentar cadastrar anamnese para cliente inexistente")
        void deveLancarExcecaoAoCadastrarAnamneseParaClienteInexistente() {
            AnamneseRequest request = new AnamneseRequest();

            Mockito.when(clienteService.cadastrarAnamnese(anyLong(), any(AnamneseRequest.class)))
                    .thenThrow(new EntidadeNaoEncontrada("Cliente não encontrado"));

            EntidadeNaoEncontrada exception = assertThrows(
                    EntidadeNaoEncontrada.class,
                    () -> clienteController.cadastroAnamnese(99L, request)
            );

            Assertions.assertEquals("Cliente não encontrado", exception.getMessage());
            Mockito.verify(clienteService, Mockito.times(1)).cadastrarAnamnese(anyLong(), any(AnamneseRequest.class));
        }
    }

    @Nested
    @DisplayName("8. Testes do Método Listar Estados Civis")
    class ListarEstadosCivisTest {

        @Test
        @DisplayName("8.1 Deve retornar lista de estados civis com status 200")
        void deveRetornarListaDeEstadosCivisComSucesso() {
            EstadoCivil ec1 = new EstadoCivil(1, "Solteiro(a)");
            EstadoCivil ec2 = new EstadoCivil(2, "Casado(a)");
            List<EstadoCivil> lista = List.of(ec1, ec2);

            Mockito.when(clienteService.listarEstadosCivis()).thenReturn(lista);

            ResponseEntity<List<EstadoCivilResponse>> response = clienteController.listarEstadosCivis();

            Assertions.assertEquals(200, response.getStatusCodeValue());
            Assertions.assertNotNull(response.getBody());
            Assertions.assertEquals(2, response.getBody().size());
            Assertions.assertEquals("Solteiro(a)", response.getBody().get(0).getDescricao());

            Mockito.verify(clienteService, Mockito.times(1)).listarEstadosCivis();
        }

        @Test
        @DisplayName("8.2 Deve retornar status 204 quando não houver estados civis")
        void deveRetornarStatus204QuandoListaVazia() {
            Mockito.when(clienteService.listarEstadosCivis()).thenReturn(Collections.emptyList());

            ResponseEntity<List<EstadoCivilResponse>> response = clienteController.listarEstadosCivis();

            Assertions.assertEquals(204, response.getStatusCodeValue());
            Assertions.assertNull(response.getBody());

            Mockito.verify(clienteService, Mockito.times(1)).listarEstadosCivis();
        }
    }
}
