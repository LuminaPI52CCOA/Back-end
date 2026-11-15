package com.lumina.backend.service.consulta;

import com.lumina.backend.exception.AgendamentoInvalidoException;
import com.lumina.backend.model.Cliente;
import com.lumina.backend.model.Consulta;
import com.lumina.backend.model.Perfil;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.repository.ClienteConvenioRepository;
import com.lumina.backend.repository.ConsultaRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de dashboard de agendamento")
class DashboardAgendamentoServiceTest {

    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    // Segunda-feira, 02/11/2026, 10:00 em Sao Paulo
    private static final Clock RELOGIO = Clock.fixed(ZonedDateTime.of(2026, 11, 2, 10, 0, 0, 0, ZONA).toInstant(), ZONA);
    private static final LocalDate HOJE = LocalDate.of(2026, 11, 2);

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private ClienteConvenioRepository clienteConvenioRepository;

    private DashboardAgendamentoService service;

    @BeforeEach
    void criarServico() {
        RegrasAgendamento regras = RegrasAgendamento.comJornada(RELOGIO, LocalTime.of(8, 0), LocalTime.of(18, 0));
        service = new DashboardAgendamentoService(consultaRepository, clienteConvenioRepository, regras);
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

    @Test
    @DisplayName("8.1 Calcula realizadas, novos pacientes, taxa de ocupação e séries temporais")
    void deveCalcularIndicadoresDashboard() {
        Consulta concluidaHoje = new Consulta(1L, cliente(1L), dentista(2L), HOJE,
                LocalTime.of(9, 0), LocalTime.of(9, 45), "CONCLUIDA", true, null);
        Consulta confirmadaOntem = new Consulta(2L, cliente(2L), dentista(2L), HOJE.minusDays(1),
                LocalTime.of(10, 0), LocalTime.of(10, 45), "CONFIRMADA", true, null);
        Consulta canceladaOntem = new Consulta(3L, cliente(3L), dentista(2L), HOJE.minusDays(1),
                LocalTime.of(11, 0), LocalTime.of(11, 45), "CANCELADA", false, null);
        Consulta concluidaMesAnterior = new Consulta(4L, cliente(1L), dentista(2L), HOJE.minusDays(35),
                LocalTime.of(9, 0), LocalTime.of(9, 45), "CONCLUIDA", true, null);

        Mockito.when(consultaRepository.buscarPorPeriodo(null, null, HOJE))
                .thenReturn(List.of(concluidaMesAnterior, confirmadaOntem, canceladaOntem, concluidaHoje));

        var resp = service.obterDashboardAgendamento("30d", null);

        Assertions.assertEquals("30d", resp.periodo());
        Assertions.assertEquals(1L, resp.consultasRealizadas());
        Assertions.assertEquals(1L, resp.consultasRealizadasPeriodoAnterior());
        Assertions.assertEquals(1L, resp.novosPacientes()); // cliente 2 teve primeira consulta no periodo; cliente 1 ja tinha ha 35 dias
        Assertions.assertEquals(6, resp.graficoVolume().size());
        Assertions.assertEquals(6, resp.graficoConfirmacao().size());
    }

    @Test
    @DisplayName("8.2 Rejeita período inválido no dashboard")
    void deveRejeitarPeriodoInvalido() {
        assertThrows(AgendamentoInvalidoException.class,
                () -> service.obterDashboardAgendamento("invalido", null));
    }
}
