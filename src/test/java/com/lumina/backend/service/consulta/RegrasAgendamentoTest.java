package com.lumina.backend.service.consulta;

import com.lumina.backend.exception.AgendamentoInvalidoException;
import com.lumina.backend.repository.IntervaloHorario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes unitários RegrasAgendamento")
class RegrasAgendamentoTest {

    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    // Segunda-feira, 02/11/2026, 10:00 em Sao Paulo
    private static final Clock RELOGIO = Clock.fixed(ZonedDateTime.of(2026, 11, 2, 10, 0, 0, 0, ZONA).toInstant(), ZONA);
    private static final LocalDate HOJE = LocalDate.of(2026, 11, 2);
    private static final LocalDate AMANHA = HOJE.plusDays(1);

    private final RegrasAgendamento regras = new RegrasAgendamento(RELOGIO, LocalTime.of(8, 0), LocalTime.of(18, 0));

    @Test
    @DisplayName("Deve calcular a duração de um intervalo válido")
    void deveCalcularDuracao() {
        assertEquals(45, regras.validarIntervalo(AMANHA, LocalTime.of(9, 0), LocalTime.of(9, 45)));
    }

    @Test
    @DisplayName("Deve aceitar hoje com início no futuro e rejeitar início já passado")
    void deveValidarHoje() {
        assertEquals(30, regras.validarIntervalo(HOJE, LocalTime.of(10, 15), LocalTime.of(10, 45)));
        assertThrows(AgendamentoInvalidoException.class,
                () -> regras.validarIntervalo(HOJE, LocalTime.of(10, 0), LocalTime.of(10, 30)));
        assertThrows(AgendamentoInvalidoException.class,
                () -> regras.validarIntervalo(HOJE.minusDays(1), LocalTime.of(11, 0), LocalTime.of(11, 30)));
    }

    @Test
    @DisplayName("Deve rejeitar duração fora de 15/30/45/60, término após 18:00, início antes de 08:00 e fim antes do início")
    void deveRejeitarIntervalosInvalidos() {
        assertThrows(AgendamentoInvalidoException.class,
                () -> regras.validarIntervalo(AMANHA, LocalTime.of(9, 0), LocalTime.of(10, 15)));
        assertThrows(AgendamentoInvalidoException.class,
                () -> regras.validarIntervalo(AMANHA, LocalTime.of(17, 30), LocalTime.of(18, 30)));
        assertThrows(AgendamentoInvalidoException.class,
                () -> regras.validarIntervalo(AMANHA, LocalTime.of(7, 45), LocalTime.of(8, 15)));
        assertThrows(AgendamentoInvalidoException.class,
                () -> regras.validarIntervalo(AMANHA, LocalTime.of(10, 0), LocalTime.of(9, 30)));
        assertThrows(AgendamentoInvalidoException.class,
                () -> regras.validarIntervalo(AMANHA, LocalTime.of(9, 5), LocalTime.of(9, 35)));
    }

    @Test
    @DisplayName("Deve aceitar a última consulta terminando exatamente às 18:00")
    void deveAceitarTerminoAs18() {
        assertEquals(60, regras.validarIntervalo(AMANHA, LocalTime.of(17, 0), LocalTime.of(18, 0)));
    }

    @Test
    @DisplayName("Intervalos consecutivos não se sobrepõem; intervalos cruzados sim")
    void deveDetectarSobreposicao() {
        assertFalse(RegrasAgendamento.sobrepoe(LocalTime.of(9, 30), LocalTime.of(10, 0), LocalTime.of(9, 0), LocalTime.of(9, 30)));
        assertFalse(RegrasAgendamento.sobrepoe(LocalTime.of(8, 30), LocalTime.of(9, 0), LocalTime.of(9, 0), LocalTime.of(9, 30)));
        assertTrue(RegrasAgendamento.sobrepoe(LocalTime.of(9, 15), LocalTime.of(9, 45), LocalTime.of(9, 0), LocalTime.of(9, 30)));
        assertTrue(RegrasAgendamento.sobrepoe(LocalTime.of(8, 0), LocalTime.of(12, 0), LocalTime.of(9, 0), LocalTime.of(9, 30)));
    }

    @Test
    @DisplayName("Horários de amanhã devem respeitar jornada, duração e ocupação")
    void deveListarHorariosDeAmanha() {
        List<IntervaloHorario> livres = regras.horariosDisponiveis(AMANHA, 30,
                List.of(new IntervaloHorario(LocalTime.of(9, 0), LocalTime.of(9, 30))));

        assertEquals(LocalTime.of(8, 0), livres.get(0).inicio());
        assertEquals(LocalTime.of(17, 30), livres.get(livres.size() - 1).inicio());
        assertTrue(livres.stream().noneMatch(l -> l.fim().isAfter(LocalTime.of(18, 0))));
        assertTrue(livres.contains(new IntervaloHorario(LocalTime.of(8, 30), LocalTime.of(9, 0))));
        assertTrue(livres.contains(new IntervaloHorario(LocalTime.of(9, 30), LocalTime.of(10, 0))));
        assertFalse(livres.stream().anyMatch(l -> l.inicio().equals(LocalTime.of(8, 45))));
        assertFalse(livres.stream().anyMatch(l -> l.inicio().equals(LocalTime.of(9, 0))));
        assertFalse(livres.stream().anyMatch(l -> l.inicio().equals(LocalTime.of(9, 15))));
    }

    @Test
    @DisplayName("Horários de hoje devem excluir os já passados")
    void deveExcluirHorariosPassadosDeHoje() {
        List<IntervaloHorario> livres = regras.horariosDisponiveis(HOJE, 60, List.of());

        assertEquals(LocalTime.of(10, 15), livres.get(0).inicio());
        assertEquals(LocalTime.of(17, 0), livres.get(livres.size() - 1).inicio());
    }

    @Test
    @DisplayName("Data passada não possui horários e duração inválida é rejeitada")
    void deveTratarDataPassadaEDuracaoInvalida() {
        assertTrue(regras.horariosDisponiveis(HOJE.minusDays(1), 30, List.of()).isEmpty());
        assertThrows(AgendamentoInvalidoException.class, () -> regras.horariosDisponiveis(AMANHA, 20, List.of()));
    }

    @Test
    @DisplayName("Numeração suportada pelo odontograma")
    void deveConhecerDentesSuportados() {
        assertEquals(32, RegrasAgendamento.DENTES_SUPORTADOS.size());
        assertTrue(RegrasAgendamento.DENTES_SUPORTADOS.containsAll(List.of(11, 18, 21, 28, 31, 38, 41, 48)));
        assertFalse(RegrasAgendamento.DENTES_SUPORTADOS.contains(19));
        assertFalse(RegrasAgendamento.DENTES_SUPORTADOS.contains(51));
    }
}
