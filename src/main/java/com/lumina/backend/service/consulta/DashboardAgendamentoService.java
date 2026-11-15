package com.lumina.backend.service.consulta;

import com.lumina.backend.dto.consulta.DashboardAgendamentoResponse;
import com.lumina.backend.dto.consulta.DashboardAgendamentoResponse.ItemDistribuicao;
import com.lumina.backend.dto.consulta.DashboardAgendamentoResponse.PontoGrafico;
import com.lumina.backend.exception.AgendamentoInvalidoException;
import com.lumina.backend.model.ClienteConvenio;
import com.lumina.backend.model.Consulta;
import com.lumina.backend.repository.ClienteConvenioRepository;
import com.lumina.backend.repository.ConsultaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Calcula os indicadores do dashboard de agendamento.
 */
@Service
public class DashboardAgendamentoService {

    private static final String PERIODO_PADRAO = "30d";
    private static final long DURACAO_PADRAO_MINUTOS = 45L;
    private static final double MINUTOS_UTEIS_POR_DIA = 360.0;
    private static final String CONVENIO_PARTICULAR = "Particular";
    private static final String[] MESES_ABREV = {
            "Jan", "Fev", "Mar", "Abr", "Mai", "Jun",
            "Jul", "Ago", "Set", "Out", "Nov", "Dez"
    };

    private final ConsultaRepository consultaRepository;
    private final ClienteConvenioRepository clienteConvenioRepository;
    private final RegrasAgendamento regrasAgendamento;

    public DashboardAgendamentoService(ConsultaRepository consultaRepository,
                                       ClienteConvenioRepository clienteConvenioRepository,
                                       RegrasAgendamento regrasAgendamento) {
        this.consultaRepository = consultaRepository;
        this.clienteConvenioRepository = clienteConvenioRepository;
        this.regrasAgendamento = regrasAgendamento;
    }

    private record Intervalo(LocalDate inicio, LocalDate fim) {
        boolean contem(LocalDate data) {
            return !data.isBefore(inicio) && !data.isAfter(fim);
        }
    }

    private record Periodos(Intervalo atual, Intervalo anterior) {
    }

    private record FaixaGrafico(String rotulo, Intervalo intervalo) {
    }

    private static class ContagemStatus {
        long concluidas;
        long confirmadas;
        long agendadas;
        long canceladas;

        void registrar(StatusConsulta status) {
            switch (status) {
                case CONCLUIDA -> concluidas++;
                case CONFIRMADA -> confirmadas++;
                case AGENDADA -> agendadas++;
                case CANCELADA -> canceladas++;
            }
        }

        long total() {
            return concluidas + confirmadas + agendadas + canceladas;
        }
    }

    @Transactional(readOnly = true)
    public DashboardAgendamentoResponse obterDashboardAgendamento(String periodo, Long idUsuario) {
        String periodoNormalizado = normalizarPeriodo(periodo);
        LocalDate hoje = regrasAgendamento.agora().toLocalDate();
        Periodos periodos = calcularPeriodos(periodoNormalizado, hoje);
        Intervalo atual = periodos.atual();
        Intervalo anterior = periodos.anterior();

        List<Consulta> todasConsultas = consultaRepository.buscarPorPeriodo(idUsuario, null, hoje);

        Map<Long, LocalDate> primeiraConsultaCliente = new HashMap<>();
        Set<String> diasDentistaAtivos = new HashSet<>();
        Set<Long> idsClientesNoPeriodo = new HashSet<>();
        List<Consulta> consultasNoPeriodo = new ArrayList<>();
        ContagemStatus contagemStatus = new ContagemStatus();
        long consultasRealizadasAnterior = 0L;
        long minutosOcupados = 0L;

        for (Consulta c : todasConsultas) {
            if (c.getData() == null) {
                continue;
            }
            StatusConsulta status = StatusConsulta.de(c.getStatus());
            Long idCliente = idCliente(c);
            if (status != StatusConsulta.CANCELADA && idCliente != null) {
                primeiraConsultaCliente.merge(idCliente, c.getData(),
                        (existente, nova) -> nova.isBefore(existente) ? nova : existente);
            }

            if (atual.contem(c.getData())) {
                consultasNoPeriodo.add(c);
                contagemStatus.registrar(status);
                if (status != StatusConsulta.CANCELADA) {
                    minutosOcupados += duracaoEmMinutos(c);
                    diasDentistaAtivos.add(idDentista(c) + "|" + c.getData());
                    if (idCliente != null) {
                        idsClientesNoPeriodo.add(idCliente);
                    }
                }
            } else if (anterior.contem(c.getData()) && status == StatusConsulta.CONCLUIDA) {
                consultasRealizadasAnterior++;
            }
        }

        long consultasRealizadas = contagemStatus.concluidas;
        long novosPacientes = contarPrimeirasConsultas(primeiraConsultaCliente, atual);
        long novosPacientesAnterior = contarPrimeirasConsultas(primeiraConsultaCliente, anterior);

        return new DashboardAgendamentoResponse(
                periodoNormalizado,
                atual.inicio(),
                atual.fim(),
                consultasRealizadas,
                consultasRealizadasAnterior,
                calcularVariacao(consultasRealizadas, consultasRealizadasAnterior),
                novosPacientes,
                novosPacientesAnterior,
                calcularTaxaOcupacao(minutosOcupados, diasDentistaAtivos.size()),
                montarGraficoVolume(periodoNormalizado, atual.inicio(), hoje, todasConsultas),
                montarGraficoConfirmacao(periodoNormalizado, atual.inicio(), hoje, todasConsultas),
                montarGraficoConvenio(consultasNoPeriodo, idsClientesNoPeriodo),
                montarGraficoStatus(contagemStatus)
        );
    }

    private static Periodos calcularPeriodos(String periodo, LocalDate hoje) {
        LocalDate inicio;
        LocalDate inicioAnterior;
        LocalDate fimAnterior;
        switch (periodo) {
            case "7d" -> {
                inicio = hoje.minusDays(6);
                inicioAnterior = inicio.minusDays(7);
                fimAnterior = inicio.minusDays(1);
            }
            case "90d" -> {
                inicio = hoje.minusDays(89);
                inicioAnterior = inicio.minusDays(90);
                fimAnterior = inicio.minusDays(1);
            }
            case "ano" -> {
                inicio = LocalDate.of(hoje.getYear(), 1, 1);
                long diasNoPeriodo = ChronoUnit.DAYS.between(inicio, hoje) + 1;
                fimAnterior = inicio.minusDays(1);
                inicioAnterior = fimAnterior.minusDays(diasNoPeriodo - 1);
            }
            default -> {
                inicio = hoje.minusDays(29);
                inicioAnterior = inicio.minusDays(30);
                fimAnterior = inicio.minusDays(1);
            }
        }
        return new Periodos(new Intervalo(inicio, hoje), new Intervalo(inicioAnterior, fimAnterior));
    }

    private static Long idCliente(Consulta consulta) {
        return consulta.getCliente() != null ? consulta.getCliente().getIdCliente() : null;
    }

    private static Long idDentista(Consulta consulta) {
        return consulta.getUsuario() != null ? consulta.getUsuario().getIdUsuario() : 0L;
    }

    private static long duracaoEmMinutos(Consulta c) {
        boolean intervaloValido = c.getHorarioInicio() != null && c.getHorarioFim() != null
                && c.getHorarioFim().isAfter(c.getHorarioInicio());
        return intervaloValido
                ? Duration.between(c.getHorarioInicio(), c.getHorarioFim()).toMinutes()
                : DURACAO_PADRAO_MINUTOS;
    }

    private static long contarPrimeirasConsultas(Map<Long, LocalDate> primeiraConsultaCliente, Intervalo intervalo) {
        return primeiraConsultaCliente.values().stream().filter(intervalo::contem).count();
    }

    private static double calcularVariacao(long atual, long anterior) {
        if (anterior > 0) {
            return arredondarUmaCasa(((atual - anterior) * 100.0) / anterior);
        }
        return atual > 0 ? 100.0 : 0.0;
    }

    private static double calcularTaxaOcupacao(long minutosOcupados, int diasDentistaAtivos) {
        if (diasDentistaAtivos == 0) {
            return 0.0;
        }
        return Math.min(100.0, arredondarUmaCasa((minutosOcupados * 100.0) / (diasDentistaAtivos * MINUTOS_UTEIS_POR_DIA)));
    }

    private static List<PontoGrafico> montarGraficoVolume(String periodo, LocalDate inicio, LocalDate hoje,
                                                          List<Consulta> consultas) {
        List<PontoGrafico> pontos = new ArrayList<>();
        for (FaixaGrafico faixa : construirFaixasGrafico(periodo, inicio, hoje)) {
            long atendimentos = consultas.stream()
                    .filter(c -> c.getData() != null && faixa.intervalo().contem(c.getData()))
                    .filter(c -> StatusConsulta.de(c.getStatus()) != StatusConsulta.CANCELADA)
                    .count();
            pontos.add(new PontoGrafico(faixa.rotulo(), atendimentos));
        }
        return pontos;
    }

    private static List<PontoGrafico> montarGraficoConfirmacao(String periodo, LocalDate inicio, LocalDate hoje,
                                                               List<Consulta> consultas) {
        List<PontoGrafico> pontos = new ArrayList<>();
        for (FaixaGrafico faixa : construirFaixasGrafico(periodo, inicio, hoje)) {
            List<StatusConsulta> statusDaFaixa = consultas.stream()
                    .filter(c -> c.getData() != null && faixa.intervalo().contem(c.getData()))
                    .map(c -> StatusConsulta.de(c.getStatus()))
                    .toList();
            long confirmadasOuConcluidas = statusDaFaixa.stream()
                    .filter(s -> s == StatusConsulta.CONFIRMADA || s == StatusConsulta.CONCLUIDA)
                    .count();
            double taxa = statusDaFaixa.isEmpty()
                    ? 0.0
                    : arredondarUmaCasa((confirmadasOuConcluidas * 100.0) / statusDaFaixa.size());
            pontos.add(new PontoGrafico(faixa.rotulo(), taxa));
        }
        return pontos;
    }

    private static List<ItemDistribuicao> montarGraficoStatus(ContagemStatus contagem) {
        long total = contagem.total();
        return List.of(
                itemDistribuicao("Concluída", contagem.concluidas, total),
                itemDistribuicao("Confirmada", contagem.confirmadas, total),
                itemDistribuicao("Agendada", contagem.agendadas, total),
                itemDistribuicao("Cancelada", contagem.canceladas, total)
        );
    }

    private List<ItemDistribuicao> montarGraficoConvenio(List<Consulta> consultasNoPeriodo, Set<Long> idsClientes) {
        Map<Long, String> convenioPorCliente = buscarConvenioPorCliente(idsClientes);

        Map<String, Long> contagemPorConvenio = new LinkedHashMap<>();
        long totalAtendimentos = 0L;
        for (Consulta c : consultasNoPeriodo) {
            if (StatusConsulta.de(c.getStatus()) == StatusConsulta.CANCELADA) {
                continue;
            }
            Long idCliente = idCliente(c);
            String nomeConvenio = idCliente != null
                    ? convenioPorCliente.getOrDefault(idCliente, CONVENIO_PARTICULAR)
                    : CONVENIO_PARTICULAR;
            contagemPorConvenio.merge(nomeConvenio, 1L, Long::sum);
            totalAtendimentos++;
        }

        final long total = totalAtendimentos;
        return contagemPorConvenio.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .map(entry -> itemDistribuicao(entry.getKey(), entry.getValue(), total))
                .toList();
    }

    private Map<Long, String> buscarConvenioPorCliente(Set<Long> idsClientes) {
        Map<Long, String> convenioPorCliente = new HashMap<>();
        if (idsClientes.isEmpty()) {
            return convenioPorCliente;
        }
        List<ClienteConvenio> vinculos = clienteConvenioRepository
                .findByClienteIdsWithConvenio(new ArrayList<>(idsClientes));
        for (ClienteConvenio vinculo : vinculos) {
            if (vinculo.getCliente() != null && vinculo.getCliente().getIdCliente() != null
                    && vinculo.getConvenio() != null && vinculo.getConvenio().getNome() != null) {
                convenioPorCliente.putIfAbsent(vinculo.getCliente().getIdCliente(), vinculo.getConvenio().getNome());
            }
        }
        return convenioPorCliente;
    }

    private static ItemDistribuicao itemDistribuicao(String rotulo, long quantidade, long total) {
        double percentual = total > 0 ? arredondarUmaCasa((quantidade * 100.0) / total) : 0.0;
        return new ItemDistribuicao(rotulo, quantidade, percentual);
    }

    private static List<FaixaGrafico> construirFaixasGrafico(String periodo, LocalDate dataInicio, LocalDate hoje) {
        DateTimeFormatter formatoDiaMes = DateTimeFormatter.ofPattern("dd/MM");
        List<FaixaGrafico> faixas = new ArrayList<>();

        switch (periodo) {
            case "7d" -> {
                for (int i = 6; i >= 0; i--) {
                    LocalDate dia = hoje.minusDays(i);
                    faixas.add(new FaixaGrafico(dia.format(formatoDiaMes), new Intervalo(dia, dia)));
                }
            }
            case "30d" -> adicionarFaixasFixas(faixas, dataInicio, hoje, 5, formatoDiaMes);
            case "90d" -> adicionarFaixasFixas(faixas, dataInicio, hoje, 15, formatoDiaMes);
            case "ano" -> {
                int mesAtual = hoje.getMonthValue();
                for (int mes = 1; mes <= mesAtual; mes++) {
                    YearMonth anoMes = YearMonth.of(hoje.getYear(), mes);
                    LocalDate fim = (mes == mesAtual) ? hoje : anoMes.atEndOfMonth();
                    faixas.add(new FaixaGrafico(MESES_ABREV[mes - 1], new Intervalo(anoMes.atDay(1), fim)));
                }
            }
            default -> {
                // período sem faixas de gráfico
            }
        }
        return faixas;
    }

    private static void adicionarFaixasFixas(List<FaixaGrafico> faixas, LocalDate dataInicio, LocalDate hoje,
                                             int diasPorFaixa, DateTimeFormatter formato) {
        final int quantidadeFaixas = 6;
        for (int i = 0; i < quantidadeFaixas; i++) {
            LocalDate inicio = dataInicio.plusDays((long) i * diasPorFaixa);
            LocalDate fim = (i == quantidadeFaixas - 1) ? hoje : inicio.plusDays(diasPorFaixa - 1L);
            faixas.add(new FaixaGrafico(fim.format(formato), new Intervalo(inicio, fim)));
        }
    }

    private static String normalizarPeriodo(String periodo) {
        if (periodo == null || periodo.isBlank()) {
            return PERIODO_PADRAO;
        }
        return switch (periodo.trim().toLowerCase(Locale.ROOT)) {
            case "7d", "ultimos_7_dias", "últimos 7 dias", "ultimos 7 dias" -> "7d";
            case "30d", "ultimos_30_dias", "últimos 30 dias", "ultimos 30 dias" -> "30d";
            case "90d", "ultimos_90_dias", "últimos 90 dias", "ultimos 90 dias" -> "90d";
            case "ano", "este_ano", "este ano" -> "ano";
            default -> throw new AgendamentoInvalidoException(
                    "Período inválido: " + periodo + ". Use 7d, 30d, 90d ou ano.");
        };
    }

    private static double arredondarUmaCasa(double valor) {
        return Math.round(valor * 10.0) / 10.0;
    }
}
