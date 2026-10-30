package com.lumina.backend.service.consulta;

import com.lumina.backend.exception.AgendamentoInvalidoException;
import com.lumina.backend.repository.IntervaloHorario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Regras de data, horario e jornada compartilhadas pelo cadastro, reagendamento
 * e pela consulta de horarios disponiveis.
 */
@Component
public class RegrasAgendamento {

    public static final List<Integer> DURACOES_PERMITIDAS = List.of(15, 30, 45, 60);
    public static final int GRANULARIDADE_MINUTOS = 15;

    /** Numeracao FDI exibida no odontograma da tela (dentes permanentes). */
    public static final Set<Integer> DENTES_SUPORTADOS = IntStream.of(10, 20, 30, 40)
            .flatMap(base -> IntStream.rangeClosed(base + 1, base + 8))
            .boxed()
            .collect(Collectors.toUnmodifiableSet());

    /** Agrupamentos oferecidos pelo seletor "Tipo de dente" do odontograma. */
    public static final Set<String> TIPOS_DENTE = Set.of(
            "Incisivo Central",
            "Incisivo Lateral",
            "Canino",
            "Primeiro Pré-molar",
            "Segundo Pré-molar",
            "Primeiro Molar",
            "Segundo Molar",
            "Terceiro Molar (Siso)"
    );

    private final Clock clock;
    private final LocalTime inicioJornada;
    private final LocalTime fimJornada;

    @Autowired
    public RegrasAgendamento(Clock clock,
                             @Value("${lumina.agenda.inicio-jornada:08:00}") String inicioJornada,
                             @Value("${lumina.agenda.fim-jornada:18:00}") String fimJornada) {
        this(clock, LocalTime.parse(inicioJornada), LocalTime.parse(fimJornada));
    }

    public RegrasAgendamento(Clock clock, LocalTime inicioJornada, LocalTime fimJornada) {
        if (!fimJornada.isAfter(inicioJornada)) {
            throw new IllegalArgumentException("O fim da jornada deve ser posterior ao inicio.");
        }
        this.clock = clock;
        this.inicioJornada = inicioJornada;
        this.fimJornada = fimJornada;
    }

    public LocalDateTime agora() {
        return LocalDateTime.now(clock);
    }

    public LocalTime getInicioJornada() {
        return inicioJornada;
    }

    public LocalTime getFimJornada() {
        return fimJornada;
    }

    /**
     * Valida data/horarios de uma consulta e devolve a duracao em minutos.
     */
    public int validarIntervalo(LocalDate data, LocalTime inicio, LocalTime fim) {
        if (data == null || inicio == null || fim == null) {
            throw new AgendamentoInvalidoException("Data, horário de início e horário de término são obrigatórios.");
        }
        if (!fim.isAfter(inicio)) {
            throw new AgendamentoInvalidoException("O horário de término deve ser posterior ao horário de início, no mesmo dia.");
        }
        if (!alinhado(inicio) || !alinhado(fim)) {
            throw new AgendamentoInvalidoException(
                    "Os horários devem seguir intervalos de " + GRANULARIDADE_MINUTOS + " minutos (ex.: 09:00, 09:15, 09:30).");
        }

        int duracao = (int) Duration.between(inicio, fim).toMinutes();
        validarDuracao(duracao);

        if (inicio.isBefore(inicioJornada) || fim.isAfter(fimJornada)) {
            throw new AgendamentoInvalidoException(
                    "A consulta deve ocorrer dentro da jornada, entre " + inicioJornada + " e " + fimJornada + ".");
        }
        if (!LocalDateTime.of(data, inicio).isAfter(agora())) {
            throw new AgendamentoInvalidoException("Não é possível agendar consultas em data ou horário passado.");
        }
        return duracao;
    }

    public void validarDuracao(Integer duracaoMinutos) {
        if (duracaoMinutos == null || !DURACOES_PERMITIDAS.contains(duracaoMinutos)) {
            throw new AgendamentoInvalidoException(
                    "Duração inválida: " + duracaoMinutos + " minutos. Permitidas: 15, 30, 45 ou 60 minutos.");
        }
    }

    /**
     * Gera os inicios livres (a cada 15 minutos) para a duracao informada, aplicando jornada,
     * horarios ja passados e sobreposicao com as consultas ocupadas.
     */
    public List<IntervaloHorario> horariosDisponiveis(LocalDate data, int duracaoMinutos, List<IntervaloHorario> ocupados) {
        validarDuracao(duracaoMinutos);
        LocalDateTime agora = agora();
        List<IntervaloHorario> livres = new ArrayList<>();
        if (data.isBefore(agora.toLocalDate())) {
            return livres;
        }

        int inicioMinutos = inicioJornada.toSecondOfDay() / 60;
        int fimMinutos = fimJornada.toSecondOfDay() / 60;
        for (int minuto = inicioMinutos; minuto + duracaoMinutos <= fimMinutos; minuto += GRANULARIDADE_MINUTOS) {
            LocalTime inicio = LocalTime.ofSecondOfDay(minuto * 60L);
            LocalTime fim = inicio.plusMinutes(duracaoMinutos);
            if (!LocalDateTime.of(data, inicio).isAfter(agora)) {
                continue;
            }
            boolean conflita = ocupados.stream().anyMatch(o -> sobrepoe(inicio, fim, o.inicio(), o.fim()));
            if (!conflita) {
                livres.add(new IntervaloHorario(inicio, fim));
            }
        }
        return livres;
    }

    /**
     * novoInicio < fimExistente E novoFim > inicioExistente. Intervalos consecutivos nao conflitam.
     */
    public static boolean sobrepoe(LocalTime novoInicio, LocalTime novoFim, LocalTime inicioExistente, LocalTime fimExistente) {
        return novoInicio.isBefore(fimExistente) && novoFim.isAfter(inicioExistente);
    }

    private static boolean alinhado(LocalTime horario) {
        return horario.getSecond() == 0 && horario.getNano() == 0
                && horario.getMinute() % GRANULARIDADE_MINUTOS == 0;
    }
}
