package com.lumina.backend.controller.consulta;

import com.lumina.backend.controller.ConsultaController;
import com.lumina.backend.dto.consulta.AlterarStatusConsultaRequest;
import com.lumina.backend.dto.consulta.ConsultaRequest;
import com.lumina.backend.dto.consulta.ConsultaResponse;
import com.lumina.backend.dto.consulta.HorarioDisponivelResponse;
import com.lumina.backend.dto.consulta.ReagendamentoRequest;
import com.lumina.backend.exception.EntidadeNaoEncontrada;
import com.lumina.backend.model.Cliente;
import com.lumina.backend.model.Consulta;
import com.lumina.backend.model.Especialidade;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.repository.IntervaloHorario;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
class ConsultaControllerTest {

    @Mock
    private ConsultaService consultaService;

    @Mock
    private com.lumina.backend.service.alexa.AlexaService alexaService;

    @Mock
    private com.lumina.backend.repository.UsuarioRepository usuarioRepository;

    @InjectMocks
    private ConsultaController consultaController;

    @org.junit.jupiter.api.BeforeEach
    void autenticar() {
        autenticarComo("admin@lumina.com", "ROLE_ADMIN");
    }

    @org.junit.jupiter.api.AfterEach
    void limparAutenticacao() {
        SecurityContextHolder.clearContext();
    }

    private static void autenticarComo(String email, String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null, List.of(new SimpleGrantedAuthority(role))));
    }

    private void autenticarDentista(Long idUsuario) {
        autenticarComo("dentista@lumina.com", "ROLE_DENTISTA");
        Usuario logado = new Usuario();
        logado.setIdUsuario(idUsuario);
        Mockito.when(usuarioRepository.findByEmail("dentista@lumina.com")).thenReturn(Optional.of(logado));
    }

    private static Consulta consultaDoDentista(Long idConsulta, Long idDentista) {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(1L);
        cliente.setNome("Ana Souza");

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(idDentista);
        usuario.setNome("Dr. Carlos");
        usuario.setSenha("hash-secreto");

        Consulta consulta = new Consulta();
        consulta.setIdConsulta(idConsulta);
        consulta.setCliente(cliente);
        consulta.setUsuario(usuario);
        consulta.setEspecialidade(new Especialidade(1, "Clínica Geral"));
        consulta.setData(LocalDate.of(2026, 6, 10));
        consulta.setHorarioInicio(LocalTime.of(9, 0));
        consulta.setHorarioFim(LocalTime.of(9, 30));
        return consulta;
    }

    @Nested
    @DisplayName("1. Teste de listagem de consultas")
    class ListarTest {

        @Test
        @DisplayName("1.1 Deve retornar lista de consultas com status 200")
        void deveRetornarListaDeConsultasComSucesso() {
            Mockito.when(consultaService.listar(null, null))
                    .thenReturn(List.of(consultaDoDentista(1L, 1L), consultaDoDentista(2L, 1L)));

            ResponseEntity<List<ConsultaResponse>> response = consultaController.listar(null, null);

            Assertions.assertEquals(200, response.getStatusCode().value());
            Assertions.assertEquals(2, response.getBody().size());
            Assertions.assertNull(response.getBody().get(0).getUsuario().getSenha());
        }

        @Test
        @DisplayName("1.2 Deve retornar lista vazia de consultas com status 200")
        void deveRetornarListaVaziaDeConsultas() {
            Mockito.when(consultaService.listar(null, null)).thenReturn(Collections.emptyList());

            ResponseEntity<List<ConsultaResponse>> response = consultaController.listar(null, null);

            Assertions.assertEquals(200, response.getStatusCode().value());
            Assertions.assertTrue(response.getBody().isEmpty());
        }

        @Test
        @DisplayName("1.3 Deve repassar o filtro de período")
        void deveRepassarPeriodo() {
            LocalDate inicio = LocalDate.of(2026, 11, 2);
            LocalDate fim = LocalDate.of(2026, 11, 8);
            Mockito.when(consultaService.listar(inicio, fim)).thenReturn(List.of(consultaDoDentista(1L, 1L)));

            Assertions.assertEquals(1, consultaController.listar(inicio, fim).getBody().size());
        }

        @Test
        @DisplayName("1.4 Dentista não lista a agenda geral (403)")
        void dentistaNaoListaAgendaGeral() {
            autenticarComo("dentista@lumina.com", "ROLE_DENTISTA");

            Assertions.assertEquals(403, consultaController.listar(null, null).getStatusCode().value());
            Mockito.verifyNoInteractions(consultaService);
        }

        @Test
        @DisplayName("1.5 Dentista só lista a própria agenda")
        void dentistaListaSomenteAPropriaAgenda() {
            autenticarDentista(1L);
            Mockito.when(consultaService.listarPorDentista(1L, null, null)).thenReturn(List.of(consultaDoDentista(1L, 1L)));

            Assertions.assertEquals(200, consultaController.listarConsultasDentista(1L, null, null).getStatusCode().value());
            Assertions.assertEquals(403, consultaController.listarConsultasDentista(2L, null, null).getStatusCode().value());
        }
    }

    @Nested
    @DisplayName("2. Teste de cadastro de consulta")
    class CadastrarTest {

        private ConsultaRequest request() {
            return new ConsultaRequest(1L, 1L, 1, LocalDate.of(2026, 6, 15),
                    LocalTime.of(9, 0), LocalTime.of(9, 30), "Obs", List.of(16), "Primeiro Molar", false);
        }

        @Test
        @DisplayName("2.1 Deve cadastrar consulta com sucesso e retornar status 201 com os campos adicionais")
        void deveCadastrarConsultaComSucesso() {
            Consulta consultaSalva = consultaDoDentista(1L, 1L);
            consultaSalva.setStatus("AGENDADA");
            consultaSalva.setObservacoes("Obs");
            consultaSalva.setDentes(List.of(16));
            consultaSalva.setTipoDente("Primeiro Molar");

            Mockito.when(consultaService.cadastrar(any(ConsultaRequest.class))).thenReturn(consultaSalva);

            ResponseEntity<ConsultaResponse> response = consultaController.cadastrar(request());

            Assertions.assertEquals(201, response.getStatusCode().value());
            ConsultaResponse body = response.getBody();
            Assertions.assertEquals(1L, body.getId());
            Assertions.assertEquals("AGENDADA", body.getStatus());
            Assertions.assertEquals(30, body.getDuracaoMinutos());
            Assertions.assertEquals("Clínica Geral", body.getEspecialidade().getNome());
            Assertions.assertEquals(List.of(16), body.getDentes());
            Assertions.assertEquals("Primeiro Molar", body.getTipoDente());
            Assertions.assertEquals("Obs", body.getObservacoes());
        }

        @Test
        @DisplayName("2.2 Deve lançar EntidadeNaoEncontrada ao tentar cadastrar consulta com cliente inexistente")
        void deveLancarExcecaoQuandoClienteNaoEncontrado() {
            Mockito.when(consultaService.cadastrar(any(ConsultaRequest.class)))
                    .thenThrow(new EntidadeNaoEncontrada("Cliente não encontrado"));

            EntidadeNaoEncontrada exception = assertThrows(EntidadeNaoEncontrada.class,
                    () -> consultaController.cadastrar(request()));

            Assertions.assertEquals("Cliente não encontrado", exception.getMessage());
        }

        @Test
        @DisplayName("2.3 Dentista não pode cadastrar consulta (403)")
        void dentistaNaoPodeCadastrar() {
            autenticarComo("dentista@lumina.com", "ROLE_DENTISTA");

            Assertions.assertEquals(403, consultaController.cadastrar(request()).getStatusCode().value());
            Mockito.verifyNoInteractions(consultaService);
        }

        @Test
        @DisplayName("2.4 Recepcionista pode cadastrar consulta")
        void recepcionistaPodeCadastrar() {
            autenticarComo("recepcao@lumina.com", "ROLE_RECEPCIONISTA");
            Mockito.when(consultaService.cadastrar(any(ConsultaRequest.class))).thenReturn(consultaDoDentista(1L, 1L));

            Assertions.assertEquals(201, consultaController.cadastrar(request()).getStatusCode().value());
        }
    }

    @Nested
    @DisplayName("3. Teste de busca por ID")
    class BuscarPorIdTest {

        @Test
        @DisplayName("3.1 Deve retornar consulta encontrada com status 200")
        void deveRetornarConsultaEncontradaComSucesso() {
            Mockito.when(consultaService.buscarPorId(1L)).thenReturn(consultaDoDentista(1L, 1L));

            ResponseEntity<ConsultaResponse> response = consultaController.buscarPorId(1L);

            Assertions.assertEquals(200, response.getStatusCode().value());
            Assertions.assertEquals(1L, response.getBody().getId());
        }

        @Test
        @DisplayName("3.2 Deve lançar EntidadeNaoEncontrada quando consulta não existir")
        void deveLancarExcecaoQuandoConsultaNaoEncontrada() {
            Mockito.when(consultaService.buscarPorId(99L))
                    .thenThrow(new EntidadeNaoEncontrada("Consulta não encontrada"));

            assertThrows(EntidadeNaoEncontrada.class, () -> consultaController.buscarPorId(99L));
        }

        @Test
        @DisplayName("3.3 Dentista não acessa consulta de outro dentista")
        void dentistaNaoAcessaConsultaDeOutro() {
            autenticarDentista(1L);
            Mockito.when(consultaService.buscarPorId(5L)).thenReturn(consultaDoDentista(5L, 2L));

            Assertions.assertEquals(403, consultaController.buscarPorId(5L).getStatusCode().value());
        }
    }

    @Nested
    @DisplayName("4. Teste de reagendamento de consulta")
    class ReagendarTest {

        private final ReagendamentoRequest request =
                new ReagendamentoRequest(LocalDate.of(2026, 6, 20), LocalTime.of(14, 0), LocalTime.of(14, 30), null);

        @Test
        @DisplayName("4.1 Deve reagendar consulta com sucesso e retornar status 200")
        void deveReagendarConsultaComSucesso() {
            Consulta consultaAtualizada = consultaDoDentista(1L, 1L);
            consultaAtualizada.setData(LocalDate.of(2026, 6, 20));
            Mockito.when(consultaService.buscarPorId(1L)).thenReturn(consultaDoDentista(1L, 1L));
            Mockito.when(consultaService.reagendar(anyLong(), any(ReagendamentoRequest.class)))
                    .thenReturn(consultaAtualizada);

            ResponseEntity<ConsultaResponse> response = consultaController.reagendar(1L, request);

            Assertions.assertEquals(200, response.getStatusCode().value());
            Assertions.assertEquals(LocalDate.of(2026, 6, 20), response.getBody().getData());
        }

        @Test
        @DisplayName("4.2 Deve lançar EntidadeNaoEncontrada ao tentar reagendar consulta inexistente")
        void deveLancarExcecaoAoReagendarConsultaInexistente() {
            Mockito.when(consultaService.buscarPorId(99L))
                    .thenThrow(new EntidadeNaoEncontrada("Consulta não encontrada"));

            assertThrows(EntidadeNaoEncontrada.class, () -> consultaController.reagendar(99L, request));
            Mockito.verify(consultaService, Mockito.never()).reagendar(anyLong(), any(ReagendamentoRequest.class));
        }

        @Test
        @DisplayName("4.3 Dentista reagenda somente as próprias consultas")
        void dentistaReagendaSomenteAsProprias() {
            autenticarDentista(1L);
            Mockito.when(consultaService.buscarPorId(5L)).thenReturn(consultaDoDentista(5L, 2L));
            Mockito.when(consultaService.buscarPorId(6L)).thenReturn(consultaDoDentista(6L, 1L));
            Mockito.when(consultaService.reagendar(Mockito.eq(6L), any(ReagendamentoRequest.class)))
                    .thenReturn(consultaDoDentista(6L, 1L));

            Assertions.assertEquals(403, consultaController.reagendar(5L, request).getStatusCode().value());
            Assertions.assertEquals(200, consultaController.reagendar(6L, request).getStatusCode().value());
            Mockito.verify(consultaService, Mockito.never()).reagendar(Mockito.eq(5L), any(ReagendamentoRequest.class));
        }
    }

    @Nested
    @DisplayName("5. Teste de cancelamento e status de consulta")
    class CancelarTest {

        @Test
        @DisplayName("5.1 Deve cancelar consulta com sucesso e retornar status 204")
        void deveCancelarConsultaComSucesso() {
            Mockito.when(consultaService.buscarPorId(1L)).thenReturn(consultaDoDentista(1L, 1L));

            ResponseEntity<Void> response = consultaController.cancelar(1L);

            Assertions.assertEquals(204, response.getStatusCode().value());
            Mockito.verify(consultaService, Mockito.times(1)).cancelar(1L);
        }

        @Test
        @DisplayName("5.2 Deve lançar EntidadeNaoEncontrada ao tentar cancelar consulta inexistente")
        void deveLancarExcecaoAoCancelarConsultaInexistente() {
            Mockito.when(consultaService.buscarPorId(99L))
                    .thenThrow(new EntidadeNaoEncontrada("Consulta não encontrada"));

            assertThrows(EntidadeNaoEncontrada.class, () -> consultaController.cancelar(99L));
            Mockito.verify(consultaService, Mockito.never()).cancelar(anyLong());
        }

        @Test
        @DisplayName("5.3 Dentista não cancela nem altera status de consulta de outro dentista")
        void dentistaNaoCancelaConsultaDeOutro() {
            autenticarDentista(1L);
            Mockito.when(consultaService.buscarPorId(5L)).thenReturn(consultaDoDentista(5L, 2L));

            Assertions.assertEquals(403, consultaController.cancelar(5L).getStatusCode().value());
            Assertions.assertEquals(403, consultaController
                    .alterarStatus(5L, new AlterarStatusConsultaRequest("CONFIRMADA")).getStatusCode().value());
            Mockito.verify(consultaService, Mockito.never()).cancelar(anyLong());
            Mockito.verify(consultaService, Mockito.never()).alterarStatus(anyLong(), any());
        }

        @Test
        @DisplayName("5.4 Deve alterar status explicitamente")
        void deveAlterarStatus() {
            Consulta confirmada = consultaDoDentista(1L, 1L);
            confirmada.setStatus("CONFIRMADA");
            Mockito.when(consultaService.buscarPorId(1L)).thenReturn(consultaDoDentista(1L, 1L));
            Mockito.when(consultaService.alterarStatus(1L, "CONFIRMADA")).thenReturn(confirmada);

            ResponseEntity<ConsultaResponse> response =
                    consultaController.alterarStatus(1L, new AlterarStatusConsultaRequest("CONFIRMADA"));

            Assertions.assertEquals(200, response.getStatusCode().value());
            Assertions.assertEquals("CONFIRMADA", response.getBody().getStatus());
        }
    }

    @Nested
    @DisplayName("6. Teste de horários disponíveis")
    class HorariosDisponiveisTest {

        @Test
        @DisplayName("6.1 Deve converter os intervalos livres")
        void deveRetornarHorarios() {
            LocalDate data = LocalDate.of(2026, 11, 3);
            Mockito.when(consultaService.listarHorariosDisponiveis(2L, data, 30, null))
                    .thenReturn(List.of(new IntervaloHorario(LocalTime.of(8, 0), LocalTime.of(8, 30))));

            ResponseEntity<List<HorarioDisponivelResponse>> response =
                    consultaController.listarHorariosDisponiveis(2L, data, 30, null);

            Assertions.assertEquals(200, response.getStatusCode().value());
            Assertions.assertEquals(LocalTime.of(8, 30), response.getBody().get(0).getHorarioFim());
        }

        @Test
        @DisplayName("6.2 Dentista só consulta a própria disponibilidade")
        void dentistaConsultaSomenteAPropriaDisponibilidade() {
            autenticarDentista(1L);

            Assertions.assertEquals(403, consultaController
                    .listarHorariosDisponiveis(2L, LocalDate.of(2026, 11, 3), 30, null).getStatusCode().value());
            Mockito.verifyNoInteractions(consultaService);
        }
    }

    @Nested
    @DisplayName("7. Teste de dashboard de agendamento")
    class DashboardAgendamentoTest {

        @Test
        @DisplayName("7.1 Deve retornar os indicadores do dashboard para o período solicitado")
        void deveRetornarDashboard() {
            com.lumina.backend.dto.consulta.DashboardAgendamentoResponse esperado =
                    new com.lumina.backend.dto.consulta.DashboardAgendamentoResponse(
                            "30d",
                            LocalDate.of(2026, 10, 1),
                            LocalDate.of(2026, 10, 30),
                            148L,
                            132L,
                            12.1,
                            18L,
                            14L,
                            89.2,
                            List.of(new com.lumina.backend.dto.consulta.DashboardAgendamentoResponse.PontoGrafico("30/10", 28)),
                            List.of(new com.lumina.backend.dto.consulta.DashboardAgendamentoResponse.PontoGrafico("30/10", 95.0)),
                            List.of(new com.lumina.backend.dto.consulta.DashboardAgendamentoResponse.ItemDistribuicao("Particular", 60L, 40.5)),
                            List.of(new com.lumina.backend.dto.consulta.DashboardAgendamentoResponse.ItemDistribuicao("Concluída", 148L, 85.0))
                    );
            Mockito.when(consultaService.obterDashboardAgendamento("30d", null)).thenReturn(esperado);

            ResponseEntity<com.lumina.backend.dto.consulta.DashboardAgendamentoResponse> response =
                    consultaController.obterDashboard("30d", null);

            Assertions.assertEquals(200, response.getStatusCode().value());
            Assertions.assertEquals(148L, response.getBody().consultasRealizadas());
            Assertions.assertEquals(89.2, response.getBody().taxaOcupacao());
        }

        @Test
        @DisplayName("7.2 Dentista não pode filtrar indicadores de outro dentista")
        void dentistaNaoConsultaOutroDentistaNoDashboard() {
            autenticarDentista(1L);

            Assertions.assertEquals(403, consultaController.obterDashboard("30d", 2L).getStatusCode().value());
            Mockito.verifyNoInteractions(consultaService);
        }
    }
}
