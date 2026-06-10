package com.lumina.backend.controller.consulta;

import com.lumina.backend.controller.ConsultaController;
import com.lumina.backend.dto.consulta.ConsultaRequest;
import com.lumina.backend.dto.consulta.ConsultaResponse;
import com.lumina.backend.exception.EntidadeNaoEncontrada;
import com.lumina.backend.model.Cliente;
import com.lumina.backend.model.Consulta;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.service.consulta.ConsultaService;
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
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
class ConsultaControllerTest {

    @Mock
    private ConsultaService consultaService;

    @InjectMocks
    private ConsultaController consultaController;

    @Nested
    @DisplayName("1. Teste de listagem de consultas")
    class ListarTest {

        @Test
        @DisplayName("1.1 Deve retornar lista de consultas com status 200")
        void deveRetornarListaDeConsultasComSucesso() {
            Cliente cliente = new Cliente();
            cliente.setIdCliente(1L);
            cliente.setNome("Ana Souza");

            Usuario usuario = new Usuario();
            usuario.setIdUsuario(1L);
            usuario.setNome("Dr. Carlos");

            Consulta c1 = new Consulta();
            c1.setIdConsulta(1L);
            c1.setCliente(cliente);
            c1.setUsuario(usuario);
            c1.setData(LocalDate.of(2026, 6, 10));
            c1.setHorarioInicio(LocalTime.of(9, 0));
            c1.setHorarioFim(LocalTime.of(9, 30));

            Consulta c2 = new Consulta();
            c2.setIdConsulta(2L);
            c2.setCliente(cliente);
            c2.setUsuario(usuario);
            c2.setData(LocalDate.of(2026, 6, 11));
            c2.setHorarioInicio(LocalTime.of(10, 0));
            c2.setHorarioFim(LocalTime.of(10, 30));

            List<Consulta> consultas = List.of(c1, c2);

            Mockito.when(consultaService.listar()).thenReturn(consultas);

            ResponseEntity<List<ConsultaResponse>> response = consultaController.listar();

            Assertions.assertEquals(200, response.getStatusCodeValue());
            Assertions.assertNotNull(response.getBody());
            Assertions.assertFalse(response.getBody().isEmpty());
            Assertions.assertEquals(2, response.getBody().size());

            Mockito.verify(consultaService, Mockito.times(1)).listar();
        }

        @Test
        @DisplayName("1.2 Deve retornar lista vazia de consultas com status 200")
        void deveRetornarListaVaziaDeConsultas() {
            Mockito.when(consultaService.listar()).thenReturn(Collections.emptyList());

            ResponseEntity<List<ConsultaResponse>> response = consultaController.listar();

            Assertions.assertEquals(200, response.getStatusCodeValue());
            Assertions.assertNotNull(response.getBody());
            Assertions.assertTrue(response.getBody().isEmpty());

            Mockito.verify(consultaService, Mockito.times(1)).listar();
        }
    }

    @Nested
    @DisplayName("2. Teste de cadastro de consulta")
    class CadastrarTest {

        @Test
        @DisplayName("2.1 Deve cadastrar consulta com sucesso e retornar status 201")
        void deveCadastrarConsultaComSucesso() {
            ConsultaRequest request = new ConsultaRequest();
            request.setIdCliente(1L);
            request.setIdUsuario(1L);
            request.setData(LocalDate.of(2026, 6, 15));
            request.setHorarioInicio(LocalTime.of(9, 0));
            request.setHorarioFim(LocalTime.of(9, 30));

            Cliente cliente = new Cliente();
            cliente.setIdCliente(1L);

            Usuario usuario = new Usuario();
            usuario.setIdUsuario(1L);

            Consulta consultaSalva = new Consulta();
            consultaSalva.setIdConsulta(1L);
            consultaSalva.setCliente(cliente);
            consultaSalva.setUsuario(usuario);
            consultaSalva.setData(LocalDate.of(2026, 6, 15));
            consultaSalva.setHorarioInicio(LocalTime.of(9, 0));
            consultaSalva.setHorarioFim(LocalTime.of(9, 30));

            Mockito.when(consultaService.cadastrar(any(ConsultaRequest.class))).thenReturn(consultaSalva);

            ResponseEntity<ConsultaResponse> response = consultaController.cadastrar(request);

            Assertions.assertEquals(201, response.getStatusCodeValue());
            Assertions.assertNotNull(response.getBody());
            Assertions.assertEquals(1L, response.getBody().getId());

            Mockito.verify(consultaService, Mockito.times(1)).cadastrar(any(ConsultaRequest.class));
        }

        @Test
        @DisplayName("2.2 Deve lançar EntidadeNaoEncontrada ao tentar cadastrar consulta com cliente inexistente")
        void deveLancarExcecaoQuandoClienteNaoEncontrado() {
            ConsultaRequest request = new ConsultaRequest();
            request.setIdCliente(99L);
            request.setIdUsuario(1L);

            Mockito.when(consultaService.cadastrar(any(ConsultaRequest.class)))
                    .thenThrow(new EntidadeNaoEncontrada("Cliente não encontrado"));

            EntidadeNaoEncontrada exception = assertThrows(
                    EntidadeNaoEncontrada.class,
                    () -> consultaController.cadastrar(request)
            );

            Assertions.assertEquals("Cliente não encontrado", exception.getMessage());
            Mockito.verify(consultaService, Mockito.times(1)).cadastrar(any(ConsultaRequest.class));
        }

        @Test
        @DisplayName("2.3 Deve lançar EntidadeNaoEncontrada ao tentar cadastrar consulta com usuário inexistente")
        void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
            ConsultaRequest request = new ConsultaRequest();
            request.setIdCliente(1L);
            request.setIdUsuario(99L);

            Mockito.when(consultaService.cadastrar(any(ConsultaRequest.class)))
                    .thenThrow(new EntidadeNaoEncontrada("Usuário não encontrado"));

            EntidadeNaoEncontrada exception = assertThrows(
                    EntidadeNaoEncontrada.class,
                    () -> consultaController.cadastrar(request)
            );

            Assertions.assertEquals("Usuário não encontrado", exception.getMessage());
            Mockito.verify(consultaService, Mockito.times(1)).cadastrar(any(ConsultaRequest.class));
        }
    }

    @Nested
    @DisplayName("3. Teste de busca por ID")
    class BuscarPorIdTest {

        @Test
        @DisplayName("3.1 Deve retornar consulta encontrada com status 200")
        void deveRetornarConsultaEncontradaComSucesso() {
            Cliente cliente = new Cliente();
            cliente.setIdCliente(1L);

            Usuario usuario = new Usuario();
            usuario.setIdUsuario(1L);

            Consulta consulta = new Consulta();
            consulta.setIdConsulta(1L);
            consulta.setCliente(cliente);
            consulta.setUsuario(usuario);
            consulta.setData(LocalDate.of(2026, 6, 10));
            consulta.setHorarioInicio(LocalTime.of(9, 0));
            consulta.setHorarioFim(LocalTime.of(9, 30));

            Mockito.when(consultaService.buscarPorId(1L)).thenReturn(consulta);

            ResponseEntity<ConsultaResponse> response = consultaController.buscarPorId(1L);

            Assertions.assertEquals(200, response.getStatusCodeValue());
            Assertions.assertNotNull(response.getBody());
            Assertions.assertEquals(1L, response.getBody().getId());

            Mockito.verify(consultaService, Mockito.times(1)).buscarPorId(1L);
        }

        @Test
        @DisplayName("3.2 Deve lançar EntidadeNaoEncontrada quando consulta não existir")
        void deveLancarExcecaoQuandoConsultaNaoEncontrada() {
            Mockito.when(consultaService.buscarPorId(99L))
                    .thenThrow(new EntidadeNaoEncontrada("Consulta não encontrada"));

            EntidadeNaoEncontrada exception = assertThrows(
                    EntidadeNaoEncontrada.class,
                    () -> consultaController.buscarPorId(99L)
            );

            Assertions.assertEquals("Consulta não encontrada", exception.getMessage());
            Mockito.verify(consultaService, Mockito.times(1)).buscarPorId(99L);
        }
    }

    @Nested
    @DisplayName("4. Teste de reagendamento de consulta")
    class ReagendarTest {

        @Test
        @DisplayName("4.1 Deve reagendar consulta com sucesso e retornar status 200")
        void deveReagendarConsultaComSucesso() {
            ConsultaRequest request = new ConsultaRequest();
            request.setData(LocalDate.of(2026, 6, 20));
            request.setHorarioInicio(LocalTime.of(14, 0));
            request.setHorarioFim(LocalTime.of(14, 30));

            Cliente cliente = new Cliente();
            cliente.setIdCliente(1L);

            Usuario usuario = new Usuario();
            usuario.setIdUsuario(1L);

            Consulta consultaAtualizada = new Consulta();
            consultaAtualizada.setIdConsulta(1L);
            consultaAtualizada.setCliente(cliente);
            consultaAtualizada.setUsuario(usuario);
            consultaAtualizada.setData(LocalDate.of(2026, 6, 20));
            consultaAtualizada.setHorarioInicio(LocalTime.of(14, 0));
            consultaAtualizada.setHorarioFim(LocalTime.of(14, 30));

            Mockito.when(consultaService.reagendar(anyLong(), any(ConsultaRequest.class)))
                    .thenReturn(consultaAtualizada);

            ResponseEntity<ConsultaResponse> response = consultaController.reagendar(1L, request);

            Assertions.assertEquals(200, response.getStatusCodeValue());
            Assertions.assertNotNull(response.getBody());
            Assertions.assertEquals(LocalDate.of(2026, 6, 20), response.getBody().getData());

            Mockito.verify(consultaService, Mockito.times(1)).reagendar(anyLong(), any(ConsultaRequest.class));
        }

        @Test
        @DisplayName("4.2 Deve lançar EntidadeNaoEncontrada ao tentar reagendar consulta inexistente")
        void deveLancarExcecaoAoReagendarConsultaInexistente() {
            ConsultaRequest request = new ConsultaRequest();
            request.setData(LocalDate.of(2026, 6, 20));

            Mockito.when(consultaService.reagendar(anyLong(), any(ConsultaRequest.class)))
                    .thenThrow(new EntidadeNaoEncontrada("Consulta não encontrada"));

            EntidadeNaoEncontrada exception = assertThrows(
                    EntidadeNaoEncontrada.class,
                    () -> consultaController.reagendar(99L, request)
            );

            Assertions.assertEquals("Consulta não encontrada", exception.getMessage());
            Mockito.verify(consultaService, Mockito.times(1)).reagendar(anyLong(), any(ConsultaRequest.class));
        }
    }

    @Nested
    @DisplayName("5. Teste de cancelamento de consulta")
    class CancelarTest {

        @Test
        @DisplayName("5.1 Deve cancelar consulta com sucesso e retornar status 204")
        void deveCancelarConsultaComSucesso() {
            Mockito.doNothing().when(consultaService).cancelar(1L);

            ResponseEntity<Void> response = consultaController.cancelar(1L);

            Assertions.assertEquals(204, response.getStatusCodeValue());

            Mockito.verify(consultaService, Mockito.times(1)).cancelar(1L);
        }

        @Test
        @DisplayName("5.2 Deve lançar EntidadeNaoEncontrada ao tentar cancelar consulta inexistente")
        void deveLancarExcecaoAoCancelarConsultaInexistente() {
            Mockito.doThrow(new EntidadeNaoEncontrada("Consulta não encontrada"))
                    .when(consultaService).cancelar(99L);

            EntidadeNaoEncontrada exception = assertThrows(
                    EntidadeNaoEncontrada.class,
                    () -> consultaController.cancelar(99L)
            );

            Assertions.assertEquals("Consulta não encontrada", exception.getMessage());
            Mockito.verify(consultaService, Mockito.times(1)).cancelar(99L);
        }
    }
}
