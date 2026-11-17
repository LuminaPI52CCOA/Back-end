package com.lumina.backend.service.consulta;

import com.lumina.backend.dto.consulta.ConsultaResponse;
import com.lumina.backend.dto.consulta.ReagendamentoRequest;
import com.lumina.backend.exception.AgendamentoInvalidoException;
import com.lumina.backend.model.Cliente;
import com.lumina.backend.model.Consulta;
import com.lumina.backend.model.Perfil;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.repository.ClienteConvenioRepository;
import com.lumina.backend.repository.ClienteRepository;
import com.lumina.backend.repository.ConsultaRepository;
import com.lumina.backend.repository.DenteRepository;
import com.lumina.backend.repository.EspecialidadeRepository;
import com.lumina.backend.repository.NotificacaoWhatsappRepository;
import com.lumina.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultaServiceAgendaCacheTest {

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
    @Mock
    private ClienteConvenioRepository clienteConvenioRepository;
    @Mock
    private AgendaCacheService agendaCache;

    private ConsultaService consultaService;

    @BeforeEach
    void criarServico() {
        RegrasAgendamento regras = new RegrasAgendamento(RELOGIO, LocalTime.of(8, 0), LocalTime.of(18, 0));
        consultaService = new ConsultaService(consultaRepository, usuarioRepository, clienteRepository,
                especialidadeRepository, denteRepository, notificacaoWhatsappRepository,
                clienteConvenioRepository, regras, agendaCache);
        lenient().when(consultaRepository.save(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private static Consulta consulta(Long id, LocalDate data, String status) {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(1L);
        cliente.setNome("Carlos Silva");
        cliente.setAtivo(true);
        Consulta consulta = new Consulta();
        consulta.setIdConsulta(id);
        consulta.setCliente(cliente);
        consulta.setUsuario(new Usuario(2L, "Dra. Ana", "98765432100", "ana@lumina.com", "hash",
                new Perfil(2, "Dentista"), "SP-1", true));
        consulta.setData(data);
        consulta.setHorarioInicio(LocalTime.of(14, 0));
        consulta.setHorarioFim(LocalTime.of(14, 30));
        consulta.setStatus(status);
        return consulta;
    }

    @Test
    @DisplayName("Periodo cacheavel passa pelo cache, e o carregador busca todos os dentistas")
    @SuppressWarnings("unchecked")
    void periodoCacheavelUsaCache() {
        when(agendaCache.cacheavel(HOJE, AMANHA)).thenReturn(true);
        when(agendaCache.obterPeriodo(eq(HOJE), eq(AMANHA), eq(2L), any())).thenAnswer(inv -> {
            Supplier<List<ConsultaResponse>> carregar = inv.getArgument(3);
            return carregar.get();
        });
        when(consultaRepository.buscarPorPeriodo(null, HOJE, AMANHA))
                .thenReturn(List.of(consulta(1L, HOJE, "AGENDADA")));

        List<ConsultaResponse> agenda = consultaService.listarAgenda(2L, HOJE, AMANHA);

        assertEquals(1, agenda.size());
        assertEquals(1L, agenda.get(0).getId());
        verify(consultaRepository, never()).buscarPorPeriodo(eq(2L), any(), any());
    }

    @Test
    @DisplayName("Periodo nao cacheavel vai direto ao banco com o filtro de dentista")
    void periodoNaoCacheavelVaiAoBanco() {
        when(agendaCache.cacheavel(HOJE, null)).thenReturn(false);
        when(consultaRepository.buscarPorPeriodo(2L, HOJE, null)).thenReturn(List.of(consulta(1L, HOJE, "AGENDADA")));

        assertEquals(1, consultaService.listarAgenda(2L, HOJE, null).size());
        verify(agendaCache, never()).obterPeriodo(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Periodo invalido falha antes de consultar o cache")
    void periodoInvalidoNaoConsultaCache() {
        assertThrows(AgendamentoInvalidoException.class, () -> consultaService.listarAgenda(null, AMANHA, HOJE));
        verifyNoInteractions(agendaCache);
    }

    @Test
    @DisplayName("Reagendar invalida o dia antigo e o novo")
    void reagendarInvalidaOsDoisDias() {
        when(consultaRepository.bloquearParaAtualizacao(5L)).thenReturn(List.of(2L));
        when(usuarioRepository.bloquearParaAgendamento(2L)).thenReturn(List.of(2L));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(new Usuario(2L, "Dra. Ana", "98765432100",
                "ana@lumina.com", "hash", new Perfil(2, "Dentista"), "SP-1", true)));
        when(consultaRepository.findById(5L)).thenReturn(Optional.of(consulta(5L, AMANHA, "AGENDADA")));
        LocalDate novoDia = AMANHA.plusDays(1);
        when(consultaRepository.buscarIntervalosOcupados(2L, novoDia, 5L)).thenReturn(List.of());
        when(notificacaoWhatsappRepository.findByConsultaIdConsulta(5L)).thenReturn(Optional.empty());

        ReagendamentoRequest request = new ReagendamentoRequest();
        request.setData(novoDia);
        request.setHorarioInicio(LocalTime.of(9, 0));
        request.setHorarioFim(LocalTime.of(9, 30));
        consultaService.reagendar(5L, request);

        verify(agendaCache).invalidar(AMANHA, novoDia);
    }

    @Test
    @DisplayName("Cancelar e alterar status invalidam o dia da consulta")
    void cancelarEAlterarStatusInvalidam() {
        when(consultaRepository.findById(5L)).thenReturn(Optional.of(consulta(5L, AMANHA, "AGENDADA")));
        when(consultaRepository.findById(6L)).thenReturn(Optional.of(consulta(6L, HOJE, "AGENDADA")));
        lenient().when(notificacaoWhatsappRepository.findByConsultaIdConsulta(any())).thenReturn(Optional.empty());

        consultaService.cancelar(5L);
        consultaService.alterarStatus(6L, "CONFIRMADA");

        verify(agendaCache).invalidar(AMANHA);
        verify(agendaCache).invalidar(HOJE);
    }
}
