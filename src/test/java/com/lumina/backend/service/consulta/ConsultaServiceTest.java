package com.lumina.backend.service.consulta;

import com.lumina.backend.dto.consulta.ConsultaRequest;
import com.lumina.backend.dto.consulta.ReagendamentoRequest;
import com.lumina.backend.exception.AgendamentoInvalidoException;
import com.lumina.backend.exception.ConflitoHorarioException;
import com.lumina.backend.exception.EntidadeNaoEncontrada;
import com.lumina.backend.model.Cliente;
import com.lumina.backend.model.Consulta;
import com.lumina.backend.model.Dente;
import com.lumina.backend.model.Especialidade;
import com.lumina.backend.model.NotificacaoWhatsapp;
import com.lumina.backend.model.Perfil;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.repository.ClienteRepository;
import com.lumina.backend.repository.ConsultaRepository;
import com.lumina.backend.repository.DenteRepository;
import com.lumina.backend.repository.EspecialidadeRepository;
import com.lumina.backend.repository.IntervaloHorario;
import com.lumina.backend.repository.NotificacaoWhatsappRepository;
import com.lumina.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;

@ExtendWith(MockitoExtension.class)
class ConsultaServiceTest {

    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    // Segunda-feira, 02/11/2026, 10:00 em Sao Paulo
    private static final Clock RELOGIO = Clock.fixed(ZonedDateTime.of(2026, 11, 2, 10, 0, 0, 0, ZONA).toInstant(), ZONA);
    private static final LocalDate HOJE = LocalDate.of(2026, 11, 2);
    private static final LocalDate AMANHA = HOJE.plusDays(1);

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private EspecialidadeRepository especialidadeRepository;

    @Mock
    private DenteRepository denteRepository;

    @Mock
    private NotificacaoWhatsappRepository notificacaoWhatsappRepository;

    private ConsultaService consultaService;

    @BeforeEach
    void criarServico() {
        RegrasAgendamento regras = new RegrasAgendamento(RELOGIO, LocalTime.of(8, 0), LocalTime.of(18, 0));
        consultaService = new ConsultaService(consultaRepository, usuarioRepository, clienteRepository,
                especialidadeRepository, denteRepository, notificacaoWhatsappRepository, regras);
    }

    private static Usuario dentista(Long id) {
        return new Usuario(id, "Dra. Ana", "98765432100", "ana@lumina.com", "hash", new Perfil(2, "Dentista"), "SP-1", true);
    }

    private static Cliente cliente(Long id) {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(id);
        cliente.setNome("Carlos Silva");
        return cliente;
    }

    private static ConsultaRequest request(LocalDate data, LocalTime inicio, LocalTime fim) {
        return new ConsultaRequest(1L, 2L, 1, data, inicio, fim, "  Sensibilidade ao frio  ",
                List.of(26, 16), "Primeiro Molar", false);
    }

    private void prepararCadastroValido(List<IntervaloHorario> ocupados) {
        Mockito.when(usuarioRepository.bloquearParaAgendamento(2L)).thenReturn(List.of(2L));
        Mockito.when(usuarioRepository.findById(2L)).thenReturn(Optional.of(dentista(2L)));
        Mockito.when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente(1L)));
        Mockito.when(especialidadeRepository.findById(1)).thenReturn(Optional.of(new Especialidade(1, "Clínica Geral")));
        Mockito.when(consultaRepository.buscarIntervalosOcupados(eq(2L), any(LocalDate.class), isNull()))
                .thenReturn(ocupados);
        Mockito.lenient().when(consultaRepository.save(any(Consulta.class))).thenAnswer(inv -> {
            Consulta salva = inv.getArgument(0);
            salva.setIdConsulta(10L);
            return salva;
        });
    }

    private void prepararDentesCadastrados() {
        Mockito.when(denteRepository.findAllById(Set.of(16, 26))).thenReturn(List.of(
                new Dente(16, "1º Molar Superior Direito", "SUPERIOR", 1),
                new Dente(26, "1º Molar Superior Esquerdo", "SUPERIOR", 2)));
    }

    @Nested
    @DisplayName("1. Teste de listagem de todas as consultas")
    class ListarTest {

        @Test
        @DisplayName("1.1 Deve retornar uma lista cheia com sucesso")
        void deveRetornarUmaListaCheiaComSucesso() {
            Consulta c1 = new Consulta();
            c1.setIdConsulta(1L);
            Consulta c2 = new Consulta();
            c2.setIdConsulta(2L);

            Mockito.when(consultaRepository.findAll()).thenReturn(List.of(c1, c2));

            List<Consulta> resultado = consultaService.listar();

            Assertions.assertEquals(2, resultado.size());
            Mockito.verify(consultaRepository, Mockito.times(1)).findAll();
        }

        @Test
        @DisplayName("1.2 Deve retornar uma lista vazia")
        void deveRetornarUmaListaVazia() {
            Mockito.when(consultaRepository.findAll()).thenReturn(Collections.emptyList());

            Assertions.assertTrue(consultaService.listar(null, null).isEmpty());
        }

        @Test
        @DisplayName("1.3 Deve filtrar por período e rejeitar período invertido")
        void deveFiltrarPorPeriodo() {
            Mockito.when(consultaRepository.buscarPorPeriodo(null, HOJE, AMANHA)).thenReturn(List.of(new Consulta()));

            Assertions.assertEquals(1, consultaService.listar(HOJE, AMANHA).size());
            assertThrows(AgendamentoInvalidoException.class, () -> consultaService.listar(AMANHA, HOJE));
        }

        @Test
        @DisplayName("1.4 Deve filtrar consultas do dentista por período")
        void deveFiltrarConsultasDoDentistaPorPeriodo() {
            Mockito.when(consultaRepository.buscarPorPeriodo(2L, HOJE, null)).thenReturn(List.of(new Consulta()));

            Assertions.assertEquals(1, consultaService.listarPorDentista(2L, HOJE, null).size());
        }
    }

    @Nested
    @DisplayName("2. Teste de cadastro de consulta")
    class CadastrarTest {

        @Test
        @DisplayName("2.1 Cadastro válido persiste campos adicionais com status AGENDADA")
        void deveCriarConsultaCorretamente() {
            prepararCadastroValido(List.of());
            prepararDentesCadastrados();

            Consulta resultado = consultaService.cadastrar(request(AMANHA, LocalTime.of(9, 0), LocalTime.of(9, 30)));

            Assertions.assertEquals(10L, resultado.getIdConsulta());
            Assertions.assertEquals(1L, resultado.getCliente().getIdCliente());
            Assertions.assertEquals(2L, resultado.getUsuario().getIdUsuario());
            Assertions.assertEquals("Clínica Geral", resultado.getEspecialidade().getNome());
            Assertions.assertEquals("AGENDADA", resultado.getStatus());
            Assertions.assertEquals("Sensibilidade ao frio", resultado.getObservacoes());
            Assertions.assertEquals(List.of(16, 26), resultado.getDentes().stream().sorted().toList());
            Assertions.assertEquals("Primeiro Molar", resultado.getTipoDente());
            Assertions.assertFalse(resultado.getPularOdontograma());
            Assertions.assertFalse(resultado.getLembreteEnviado());
        }

        @Test
        @DisplayName("2.2 Bloqueia a agenda do dentista antes de qualquer leitura e da busca de conflitos")
        void deveBloquearDentistaAntesDeConsultarConflitos() {
            prepararCadastroValido(List.of());
            prepararDentesCadastrados();

            consultaService.cadastrar(request(AMANHA, LocalTime.of(9, 0), LocalTime.of(9, 30)));

            InOrder ordem = Mockito.inOrder(usuarioRepository, clienteRepository, consultaRepository);
            ordem.verify(usuarioRepository).bloquearParaAgendamento(2L);
            ordem.verify(usuarioRepository).findById(2L);
            ordem.verify(clienteRepository).findById(1L);
            ordem.verify(consultaRepository).buscarIntervalosOcupados(2L, AMANHA, null);
            ordem.verify(consultaRepository).save(any(Consulta.class));
        }

        @Test
        @DisplayName("2.3 Pular odontograma descarta dentes e tipo de dente")
        void devePularOdontograma() {
            prepararCadastroValido(List.of());
            ConsultaRequest request = request(AMANHA, LocalTime.of(9, 0), LocalTime.of(9, 30));
            request.setPularOdontograma(true);
            request.setDentes(List.of(16, 16, 99));
            request.setTipoDente("qualquer coisa");

            Consulta resultado = consultaService.cadastrar(request);

            Assertions.assertTrue(resultado.getDentes().isEmpty());
            Assertions.assertNull(resultado.getTipoDente());
            Assertions.assertTrue(resultado.getPularOdontograma());
            Mockito.verifyNoInteractions(denteRepository);
        }

        @Test
        @DisplayName("2.4 Hoje com horário futuro é aceito")
        void deveAceitarHojeComHorarioFuturo() {
            prepararCadastroValido(List.of());
            prepararDentesCadastrados();

            Consulta resultado = consultaService.cadastrar(request(HOJE, LocalTime.of(10, 30), LocalTime.of(11, 0)));

            Assertions.assertEquals(HOJE, resultado.getData());
        }

        @Test
        @DisplayName("2.5 Horário passado é rejeitado sem tocar no banco")
        void deveRejeitarHorarioPassado() {
            assertThrows(AgendamentoInvalidoException.class,
                    () -> consultaService.cadastrar(request(HOJE, LocalTime.of(9, 0), LocalTime.of(9, 30))));
            assertThrows(AgendamentoInvalidoException.class,
                    () -> consultaService.cadastrar(request(HOJE.minusDays(1), LocalTime.of(11, 0), LocalTime.of(11, 30))));

            Mockito.verifyNoInteractions(usuarioRepository, consultaRepository);
        }

        @Test
        @DisplayName("2.6 Duração inválida é rejeitada")
        void deveRejeitarDuracaoInvalida() {
            AgendamentoInvalidoException ex = assertThrows(AgendamentoInvalidoException.class,
                    () -> consultaService.cadastrar(request(AMANHA, LocalTime.of(9, 0), LocalTime.of(10, 15))));

            Assertions.assertTrue(ex.getMessage().contains("Duração inválida"));
            Mockito.verify(consultaRepository, Mockito.never()).save(any(Consulta.class));
        }

        @Test
        @DisplayName("2.7 Término após 18:00 é rejeitado")
        void deveRejeitarTerminoAposJornada() {
            assertThrows(AgendamentoInvalidoException.class,
                    () -> consultaService.cadastrar(request(AMANHA, LocalTime.of(17, 30), LocalTime.of(18, 30))));
            Mockito.verify(consultaRepository, Mockito.never()).save(any(Consulta.class));
        }

        @Test
        @DisplayName("2.8 Sobreposição com consulta do mesmo dentista gera conflito")
        void deveRejeitarSobreposicao() {
            prepararCadastroValido(List.of(new IntervaloHorario(LocalTime.of(9, 15), LocalTime.of(9, 45))));
            prepararDentesCadastrados();

            ConflitoHorarioException ex = assertThrows(ConflitoHorarioException.class,
                    () -> consultaService.cadastrar(request(AMANHA, LocalTime.of(9, 0), LocalTime.of(9, 30))));

            Assertions.assertTrue(ex.getMessage().contains("09:15"));
            Mockito.verify(consultaRepository, Mockito.never()).save(any(Consulta.class));
        }

        @Test
        @DisplayName("2.9 Horários consecutivos são permitidos")
        void devePermitirHorariosConsecutivos() {
            prepararCadastroValido(List.of(
                    new IntervaloHorario(LocalTime.of(8, 30), LocalTime.of(9, 0)),
                    new IntervaloHorario(LocalTime.of(9, 30), LocalTime.of(10, 0))));
            prepararDentesCadastrados();

            Consulta resultado = consultaService.cadastrar(request(AMANHA, LocalTime.of(9, 0), LocalTime.of(9, 30)));

            Assertions.assertNotNull(resultado);
        }

        @Test
        @DisplayName("2.10 Dentista inexistente retorna EntidadeNaoEncontrada")
        void deveLancarExcecaoQuandoDentistaNaoExistir() {
            Mockito.when(usuarioRepository.bloquearParaAgendamento(2L)).thenReturn(List.of());

            EntidadeNaoEncontrada ex = assertThrows(EntidadeNaoEncontrada.class,
                    () -> consultaService.cadastrar(request(AMANHA, LocalTime.of(9, 0), LocalTime.of(9, 30))));

            Assertions.assertEquals("Dentista não encontrado", ex.getMessage());
            Mockito.verify(consultaRepository, Mockito.never()).save(any(Consulta.class));
        }

        @Test
        @DisplayName("2.11 Usuário sem perfil DENTISTA ou inativo é rejeitado")
        void deveRejeitarDentistaInvalido() {
            Usuario recepcionista = dentista(2L);
            recepcionista.setPerfil(new Perfil(3, "Recepcionista"));
            Mockito.when(usuarioRepository.bloquearParaAgendamento(2L)).thenReturn(List.of(2L));
            Mockito.when(usuarioRepository.findById(2L)).thenReturn(Optional.of(recepcionista));

            assertThrows(AgendamentoInvalidoException.class,
                    () -> consultaService.cadastrar(request(AMANHA, LocalTime.of(9, 0), LocalTime.of(9, 30))));

            Usuario inativo = dentista(2L);
            inativo.setAtivo(false);
            Mockito.when(usuarioRepository.findById(2L)).thenReturn(Optional.of(inativo));

            assertThrows(AgendamentoInvalidoException.class,
                    () -> consultaService.cadastrar(request(AMANHA, LocalTime.of(9, 0), LocalTime.of(9, 30))));
            Mockito.verify(consultaRepository, Mockito.never()).save(any(Consulta.class));
        }

        @Test
        @DisplayName("2.12 Cliente inexistente ou inativo é rejeitado")
        void deveRejeitarClienteInvalido() {
            Mockito.when(usuarioRepository.bloquearParaAgendamento(2L)).thenReturn(List.of(2L));
            Mockito.when(usuarioRepository.findById(2L)).thenReturn(Optional.of(dentista(2L)));
            Mockito.when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

            EntidadeNaoEncontrada ex = assertThrows(EntidadeNaoEncontrada.class,
                    () -> consultaService.cadastrar(request(AMANHA, LocalTime.of(9, 0), LocalTime.of(9, 30))));
            Assertions.assertEquals("Cliente não encontrado", ex.getMessage());

            Cliente inativo = cliente(1L);
            inativo.setAtivo(false);
            Mockito.when(clienteRepository.findById(1L)).thenReturn(Optional.of(inativo));

            assertThrows(AgendamentoInvalidoException.class,
                    () -> consultaService.cadastrar(request(AMANHA, LocalTime.of(9, 0), LocalTime.of(9, 30))));
        }

        @Test
        @DisplayName("2.13 Especialidade inexistente retorna EntidadeNaoEncontrada")
        void deveRejeitarEspecialidadeInexistente() {
            Mockito.when(usuarioRepository.bloquearParaAgendamento(2L)).thenReturn(List.of(2L));
            Mockito.when(usuarioRepository.findById(2L)).thenReturn(Optional.of(dentista(2L)));
            Mockito.when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente(1L)));
            Mockito.when(especialidadeRepository.findById(1)).thenReturn(Optional.empty());

            assertThrows(EntidadeNaoEncontrada.class,
                    () -> consultaService.cadastrar(request(AMANHA, LocalTime.of(9, 0), LocalTime.of(9, 30))));
        }

        @Test
        @DisplayName("2.14 Dentes duplicados, fora da numeração ou tipo inválido são rejeitados")
        void deveRejeitarOdontogramaInvalido() {
            ConsultaRequest duplicado = request(AMANHA, LocalTime.of(9, 0), LocalTime.of(9, 30));
            duplicado.setDentes(List.of(16, 16));
            assertThrows(AgendamentoInvalidoException.class, () -> consultaService.cadastrar(duplicado));

            ConsultaRequest foraDaNumeracao = request(AMANHA, LocalTime.of(9, 0), LocalTime.of(9, 30));
            foraDaNumeracao.setDentes(List.of(19));
            assertThrows(AgendamentoInvalidoException.class, () -> consultaService.cadastrar(foraDaNumeracao));

            ConsultaRequest tipoInvalido = request(AMANHA, LocalTime.of(9, 0), LocalTime.of(9, 30));
            tipoInvalido.setTipoDente("Molar Gigante");
            assertThrows(AgendamentoInvalidoException.class, () -> consultaService.cadastrar(tipoInvalido));

            Mockito.verifyNoInteractions(usuarioRepository, consultaRepository);
        }

        @Test
        @DisplayName("2.15 Dente fora do cadastro retorna EntidadeNaoEncontrada")
        void deveRejeitarDenteNaoCadastrado() {
            Mockito.when(usuarioRepository.bloquearParaAgendamento(2L)).thenReturn(List.of(2L));
            Mockito.when(usuarioRepository.findById(2L)).thenReturn(Optional.of(dentista(2L)));
            Mockito.when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente(1L)));
            Mockito.when(especialidadeRepository.findById(1)).thenReturn(Optional.of(new Especialidade(1, "Clínica Geral")));
            Mockito.when(denteRepository.findAllById(Set.of(16, 26)))
                    .thenReturn(List.of(new Dente(16, "1º Molar Superior Direito", "SUPERIOR", 1)));

            EntidadeNaoEncontrada ex = assertThrows(EntidadeNaoEncontrada.class,
                    () -> consultaService.cadastrar(request(AMANHA, LocalTime.of(9, 0), LocalTime.of(9, 30))));
            Assertions.assertTrue(ex.getMessage().contains("26"));
        }
    }

    @Nested
    @DisplayName("3. Teste de busca por id")
    class BuscarPorIdTest {

        @Test
        @DisplayName("3.1 Deve retornar a consulta buscada pelo id especifico corretamente")
        void deveBuscarCorretamenteAConsultaPorId() {
            Consulta consulta = new Consulta();
            consulta.setIdConsulta(10L);

            Mockito.when(consultaRepository.findById(10L)).thenReturn(Optional.of(consulta));

            Assertions.assertEquals(10L, consultaService.buscarPorId(10L).getIdConsulta());
        }

        @Test
        @DisplayName("3.2 Deve lançar EntidadeNaoEncontrada se consulta não existir")
        void naoDeveBuscarAConsultaPorId() {
            Mockito.when(consultaRepository.findById(10L)).thenReturn(Optional.empty());

            EntidadeNaoEncontrada exception = assertThrows(EntidadeNaoEncontrada.class,
                    () -> consultaService.buscarPorId(10L));

            Assertions.assertEquals("Consulta não encontrada", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("4. Testes de reagendamento de consulta")
    class ReagendarTest {

        private Consulta consultaExistente(String status) {
            Consulta consulta = new Consulta(10L, cliente(1L), dentista(2L), AMANHA,
                    LocalTime.of(9, 0), LocalTime.of(9, 30), status, true, "reminder-1");
            consulta.setEspecialidade(new Especialidade(1, "Clínica Geral"));
            consulta.setDentes(List.of(16));
            consulta.setObservacoes("Original");
            return consulta;
        }

        @Test
        @DisplayName("4.1 Reagenda sem conflitar consigo mesma e preserva os demais dados")
        void deveReagendarAConsultaComSucesso() {
            Consulta existente = consultaExistente("CONFIRMADA");
            NotificacaoWhatsapp notificacao = new NotificacaoWhatsapp();
            notificacao.setStatus(NotificacaoWhatsapp.STATUS_DISPARADO);
            notificacao.setTokenConfirmacao("token-antigo");
            notificacao.setConfirmadoEm(LocalDateTime.of(2026, 11, 2, 8, 0));

            Mockito.when(consultaRepository.bloquearParaAtualizacao(10L)).thenReturn(List.of(2));
            Mockito.when(usuarioRepository.bloquearParaAgendamento(2L)).thenReturn(List.of(2L));
            Mockito.when(consultaRepository.findById(10L)).thenReturn(Optional.of(existente));
            Mockito.when(usuarioRepository.findById(2L)).thenReturn(Optional.of(dentista(2L)));
            // A propria consulta (09:00-09:30) e excluida da busca pelo idConsultaIgnorada.
            Mockito.when(consultaRepository.buscarIntervalosOcupados(2L, AMANHA, 10L)).thenReturn(List.of());
            Mockito.when(notificacaoWhatsappRepository.findByConsultaIdConsulta(10L)).thenReturn(Optional.of(notificacao));
            Mockito.when(consultaRepository.save(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));

            Consulta resultado = consultaService.reagendar(10L,
                    new ReagendamentoRequest(AMANHA, LocalTime.of(9, 15), LocalTime.of(9, 45), null));

            Assertions.assertEquals(LocalTime.of(9, 15), resultado.getHorarioInicio());
            Assertions.assertEquals(LocalTime.of(9, 45), resultado.getHorarioFim());
            Assertions.assertEquals("AGENDADA", resultado.getStatus());
            Assertions.assertEquals("Original", resultado.getObservacoes());
            Assertions.assertEquals(1L, resultado.getCliente().getIdCliente());
            Assertions.assertEquals(Set.of(16), resultado.getDentes());
            Assertions.assertFalse(resultado.getLembreteEnviado());
            Assertions.assertNull(resultado.getAlexaReminderId());

            Assertions.assertEquals(NotificacaoWhatsapp.STATUS_PENDENTE, notificacao.getStatus());
            Assertions.assertNotEquals("token-antigo", notificacao.getTokenConfirmacao());
            Assertions.assertNull(notificacao.getConfirmadoEm());
            Assertions.assertEquals(LocalTime.of(9, 15), notificacao.getHorarioConsulta());
        }

        @Test
        @DisplayName("4.2 Reagendamento para horário ocupado por outra consulta gera conflito")
        void deveRejeitarReagendamentoComConflito() {
            Mockito.when(consultaRepository.bloquearParaAtualizacao(10L)).thenReturn(List.of(2L));
            Mockito.when(usuarioRepository.bloquearParaAgendamento(2L)).thenReturn(List.of(2L));
            Mockito.when(consultaRepository.findById(10L)).thenReturn(Optional.of(consultaExistente("AGENDADA")));
            Mockito.when(usuarioRepository.findById(2L)).thenReturn(Optional.of(dentista(2L)));
            Mockito.when(consultaRepository.buscarIntervalosOcupados(2L, AMANHA, 10L))
                    .thenReturn(List.of(new IntervaloHorario(LocalTime.of(10, 0), LocalTime.of(10, 30))));

            assertThrows(ConflitoHorarioException.class, () -> consultaService.reagendar(10L,
                    new ReagendamentoRequest(AMANHA, LocalTime.of(10, 15), LocalTime.of(10, 45), null)));
            Mockito.verify(consultaRepository, Mockito.never()).save(any(Consulta.class));
        }

        @Test
        @DisplayName("4.3 Reagendamento aplica as mesmas validações de horário")
        void deveValidarHorarioNoReagendamento() {
            Mockito.when(consultaRepository.bloquearParaAtualizacao(10L)).thenReturn(List.of(2L));
            Mockito.when(usuarioRepository.bloquearParaAgendamento(2L)).thenReturn(List.of(2L));
            Mockito.when(consultaRepository.findById(10L)).thenReturn(Optional.of(consultaExistente("AGENDADA")));

            assertThrows(AgendamentoInvalidoException.class, () -> consultaService.reagendar(10L,
                    new ReagendamentoRequest(HOJE, LocalTime.of(9, 0), LocalTime.of(9, 30), null)));
            assertThrows(AgendamentoInvalidoException.class, () -> consultaService.reagendar(10L,
                    new ReagendamentoRequest(AMANHA, LocalTime.of(17, 45), LocalTime.of(18, 15), null)));
        }

        @Test
        @DisplayName("4.4 Alterar só observações não revalida horário")
        void deveAlterarSomenteObservacoes() {
            Consulta existente = consultaExistente("AGENDADA");
            existente.setData(HOJE.minusDays(3));
            Mockito.when(consultaRepository.bloquearParaAtualizacao(10L)).thenReturn(List.of(2L));
            Mockito.when(usuarioRepository.bloquearParaAgendamento(2L)).thenReturn(List.of(2L));
            Mockito.when(consultaRepository.findById(10L)).thenReturn(Optional.of(existente));
            Mockito.when(consultaRepository.save(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));

            Consulta resultado = consultaService.reagendar(10L, new ReagendamentoRequest(
                    HOJE.minusDays(3), LocalTime.of(9, 0), LocalTime.of(9, 30), "Nova anotação"));

            Assertions.assertEquals("Nova anotação", resultado.getObservacoes());
            Assertions.assertTrue(resultado.getLembreteEnviado());
            Mockito.verify(consultaRepository, Mockito.never()).buscarIntervalosOcupados(anyLong(), any(), any());
        }

        @Test
        @DisplayName("4.5 Consulta cancelada não pode ser reagendada")
        void naoDeveReagendarConsultaCancelada() {
            Mockito.when(consultaRepository.bloquearParaAtualizacao(10L)).thenReturn(List.of(2L));
            Mockito.when(usuarioRepository.bloquearParaAgendamento(2L)).thenReturn(List.of(2L));
            Mockito.when(consultaRepository.findById(10L)).thenReturn(Optional.of(consultaExistente("CANCELADA")));

            assertThrows(AgendamentoInvalidoException.class, () -> consultaService.reagendar(10L,
                    new ReagendamentoRequest(AMANHA, LocalTime.of(14, 0), LocalTime.of(14, 30), null)));
        }

        @Test
        @DisplayName("4.6 Deve lançar EntidadeNaoEncontrada ao tentar reagendar uma consulta inexistente")
        void deveLancarExcecaoAoReagendarConsultaInexistente() {
            Mockito.when(consultaRepository.bloquearParaAtualizacao(1L)).thenReturn(List.of());

            EntidadeNaoEncontrada exception = assertThrows(EntidadeNaoEncontrada.class,
                    () -> consultaService.reagendar(1L, new ReagendamentoRequest()));

            Assertions.assertEquals("Consulta não encontrada", exception.getMessage());
            Mockito.verify(consultaRepository, Mockito.never()).save(any(Consulta.class));
        }
    }

    @Nested
    @DisplayName("5. Testes de cancelamento de consulta")
    class CancelarTest {

        @Test
        @DisplayName("5.1 Cancelamento é lógico: status CANCELADA, sem exclusão física")
        void deveCancelarAConsultaComSucesso() {
            Consulta consultaExistente = new Consulta();
            consultaExistente.setIdConsulta(1L);
            NotificacaoWhatsapp notificacao = new NotificacaoWhatsapp();
            notificacao.setStatus(NotificacaoWhatsapp.STATUS_PENDENTE);

            Mockito.when(consultaRepository.findById(1L)).thenReturn(Optional.of(consultaExistente));
            Mockito.when(notificacaoWhatsappRepository.findByConsultaIdConsulta(1L)).thenReturn(Optional.of(notificacao));

            consultaService.cancelar(1L);

            Assertions.assertEquals("CANCELADA", consultaExistente.getStatus());
            Assertions.assertEquals(NotificacaoWhatsapp.STATUS_CANCELADO, notificacao.getStatus());
            Mockito.verify(consultaRepository, Mockito.times(1)).save(consultaExistente);
            Mockito.verify(consultaRepository, Mockito.never()).delete(any(Consulta.class));
        }

        @Test
        @DisplayName("5.2 Cancelar consulta já cancelada é idempotente e concluída é rejeitada")
        void deveTratarCancelamentoRepetidoEConcluida() {
            Consulta cancelada = new Consulta();
            cancelada.setIdConsulta(1L);
            cancelada.setStatus("CANCELADA");
            Consulta concluida = new Consulta();
            concluida.setIdConsulta(2L);
            concluida.setStatus("CONCLUIDA");
            Mockito.when(consultaRepository.findById(1L)).thenReturn(Optional.of(cancelada));
            Mockito.when(consultaRepository.findById(2L)).thenReturn(Optional.of(concluida));

            consultaService.cancelar(1L);
            assertThrows(AgendamentoInvalidoException.class, () -> consultaService.cancelar(2L));

            Mockito.verify(consultaRepository, Mockito.never()).save(any(Consulta.class));
        }

        @Test
        @DisplayName("5.3 Deve lançar EntidadeNaoEncontrada ao tentar cancelar uma consulta inexistente")
        void deveLancarExcecaoAoCancelarConsultaInexistente() {
            Mockito.when(consultaRepository.findById(1L)).thenReturn(Optional.empty());

            EntidadeNaoEncontrada exception = assertThrows(EntidadeNaoEncontrada.class,
                    () -> consultaService.cancelar(1L));

            Assertions.assertEquals("Consulta não encontrada", exception.getMessage());
            Mockito.verify(consultaRepository, Mockito.never()).save(any(Consulta.class));
        }
    }

    @Nested
    @DisplayName("6. Testes de alteração de status")
    class AlterarStatusTest {

        private Consulta consultaCom(String status) {
            Consulta consulta = new Consulta();
            consulta.setIdConsulta(1L);
            consulta.setStatus(status);
            Mockito.when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));
            return consulta;
        }

        @Test
        @DisplayName("6.1 AGENDADA -> CONFIRMADA")
        void deveConfirmar() {
            Consulta consulta = consultaCom("AGENDADA");
            Mockito.when(consultaRepository.save(consulta)).thenReturn(consulta);

            Assertions.assertEquals("CONFIRMADA", consultaService.alterarStatus(1L, "confirmada").getStatus());
        }

        @Test
        @DisplayName("6.2 CONFIRMADA -> AGENDADA desfaz a confirmação da notificação")
        void deveDesfazerConfirmacao() {
            Consulta consulta = consultaCom("CONFIRMADA");
            NotificacaoWhatsapp notificacao = new NotificacaoWhatsapp();
            notificacao.setConfirmadoEm(LocalDateTime.of(2026, 11, 2, 8, 0));
            Mockito.when(notificacaoWhatsappRepository.findByConsultaIdConsulta(1L)).thenReturn(Optional.of(notificacao));
            Mockito.when(consultaRepository.save(consulta)).thenReturn(consulta);

            Assertions.assertEquals("AGENDADA", consultaService.alterarStatus(1L, "AGENDADA").getStatus());
            Assertions.assertNull(notificacao.getConfirmadoEm());
        }

        @Test
        @DisplayName("6.3 Transições a partir de CANCELADA e status desconhecido são rejeitados")
        void deveRejeitarTransicoesInvalidas() {
            consultaCom("CANCELADA");

            assertThrows(AgendamentoInvalidoException.class, () -> consultaService.alterarStatus(1L, "CONFIRMADA"));
            assertThrows(AgendamentoInvalidoException.class, () -> consultaService.alterarStatus(1L, "ATRASADA"));
            Mockito.verify(consultaRepository, Mockito.never()).save(any(Consulta.class));
        }

        @Test
        @DisplayName("6.4 -> CANCELADA aplica o cancelamento lógico")
        void deveCancelarPorStatus() {
            Consulta consulta = consultaCom("CONFIRMADA");
            Mockito.when(consultaRepository.save(consulta)).thenReturn(consulta);

            Assertions.assertEquals("CANCELADA", consultaService.alterarStatus(1L, "CANCELADA").getStatus());
        }
    }

    @Nested
    @DisplayName("7. Testes de horários disponíveis")
    class HorariosDisponiveisTest {

        @Test
        @DisplayName("7.1 Usa as consultas ocupadas do dentista e as regras de jornada")
        void deveListarHorariosDisponiveis() {
            Mockito.when(usuarioRepository.findById(2L)).thenReturn(Optional.of(dentista(2L)));
            Mockito.when(consultaRepository.buscarIntervalosOcupados(2L, AMANHA, null))
                    .thenReturn(List.of(new IntervaloHorario(LocalTime.of(8, 0), LocalTime.of(12, 0))));

            List<IntervaloHorario> livres = consultaService.listarHorariosDisponiveis(2L, AMANHA, 60, null);

            Assertions.assertEquals(LocalTime.of(12, 0), livres.get(0).inicio());
            Assertions.assertEquals(LocalTime.of(17, 0), livres.get(livres.size() - 1).inicio());
        }

        @Test
        @DisplayName("7.2 Rejeita duração inválida e usuário que não é dentista")
        void deveRejeitarParametrosInvalidos() {
            assertThrows(AgendamentoInvalidoException.class,
                    () -> consultaService.listarHorariosDisponiveis(2L, AMANHA, 50, null));

            Usuario recepcionista = dentista(4L);
            recepcionista.setPerfil(new Perfil(3, "Recepcionista"));
            Mockito.when(usuarioRepository.findById(4L)).thenReturn(Optional.of(recepcionista));
            assertThrows(AgendamentoInvalidoException.class,
                    () -> consultaService.listarHorariosDisponiveis(4L, AMANHA, 30, null));
        }
    }
}
