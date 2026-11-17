package com.lumina.backend.service.consulta;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumina.backend.dto.consulta.ConsultaResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Cache da agenda no Redis, uma chave por dia ({@code lumina:agenda:dia:2026-11-17}) com a
 * lista de consultas de todos os dentistas daquele dia, ja no formato de resposta da API.
 * A semana e montada lendo 7 chaves; o filtro por dentista e feito em memoria.
 *
 * O MySQL continua sendo a fonte da verdade: qualquer falha do Redis vira "cache vazio" e a
 * leitura segue pelo banco. Disponibilidade de horario e conflito de agenda nunca passam por aqui.
 */
@Slf4j
@Service
public class AgendaCacheService {

    static final String PREFIXO_CHAVE = "lumina:agenda:dia:";
    private static final TypeReference<List<ConsultaResponse>> TIPO_LISTA = new TypeReference<>() {
    };
    /** Depois de uma falha, o Redis fica de fora por este tempo para nao atrasar cada requisicao. */
    private static final Duration PAUSA_APOS_FALHA = Duration.ofSeconds(30);

    private final StringRedisTemplate redis;
    private final ObjectMapper json;
    private final boolean habilitado;
    private final Duration ttl;
    private final int maxDias;

    private volatile long pausadoAteMillis;

    @Autowired
    public AgendaCacheService(ObjectProvider<StringRedisTemplate> redis,
                              ObjectMapper objectMapper,
                              @Value("${lumina.cache.agenda.enabled:false}") boolean habilitado,
                              @Value("${lumina.cache.agenda.ttl-segundos:1200}") long ttlSegundos,
                              @Value("${lumina.cache.agenda.max-dias:31}") int maxDias) {
        this(redis.getIfAvailable(), objectMapper, habilitado, Duration.ofSeconds(ttlSegundos), maxDias);
    }

    AgendaCacheService(StringRedisTemplate redis, ObjectMapper objectMapper,
                       boolean habilitado, Duration ttl, int maxDias) {
        this.redis = redis;
        this.json = objectMapper.copy().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.habilitado = habilitado && redis != null;
        this.ttl = ttl;
        this.maxDias = maxDias;
    }

    /** Cache desligado: toda leitura vai ao banco e invalidacoes nao fazem nada. */
    public static AgendaCacheService desativado() {
        return new AgendaCacheService((StringRedisTemplate) null, new ObjectMapper(), false, Duration.ZERO, 0);
    }

    /** So periodos fechados e curtos (dia, semana, mes) sao cacheados. */
    public boolean cacheavel(LocalDate dataInicio, LocalDate dataFim) {
        return habilitado
                && dataInicio != null
                && dataFim != null
                && !dataFim.isBefore(dataInicio)
                && ChronoUnit.DAYS.between(dataInicio, dataFim) < maxDias;
    }

    /**
     * Consultas do periodo, em ordem de data e horario.
     *
     * @param idUsuario           dentista a filtrar, ou {@code null} para todos
     * @param carregarTodoPeriodo busca no banco as consultas de TODOS os dentistas no periodo,
     *                            ordenadas por data e horario; so e chamada se faltar algum dia no cache
     */
    public List<ConsultaResponse> obterPeriodo(LocalDate dataInicio, LocalDate dataFim, Long idUsuario,
                                               Supplier<List<ConsultaResponse>> carregarTodoPeriodo) {
        List<ConsultaResponse> consultas = null;
        if (cacheavel(dataInicio, dataFim)) {
            List<LocalDate> dias = dataInicio.datesUntil(dataFim.plusDays(1)).toList();
            consultas = lerDias(dias);
            if (consultas == null) {
                consultas = carregarTodoPeriodo.get();
                gravarDias(dias, consultas);
            }
        } else {
            consultas = carregarTodoPeriodo.get();
        }

        if (idUsuario == null) {
            return consultas;
        }
        return consultas.stream()
                .filter(c -> c.getUsuario() != null && idUsuario.equals(c.getUsuario().getIdUsuario()))
                .toList();
    }

    /**
     * Descarta os dias informados. Dentro de uma transacao a remocao acontece apos o commit:
     * se fosse antes, uma leitura concorrente poderia regravar o cache com o dado antigo.
     */
    public void invalidar(LocalDate... dias) {
        if (!habilitado || dias == null) {
            return;
        }
        List<String> chaves = Arrays.stream(dias)
                .filter(Objects::nonNull)
                .distinct()
                .map(AgendaCacheService::chave)
                .toList();
        if (chaves.isEmpty()) {
            return;
        }

        aposCommit(() -> apagar(chaves));
    }

    /**
     * Descarta a agenda inteira. Usado quando muda um dado que aparece dentro de cada consulta
     * (cadastro do paciente ou do dentista): nao da para saber em quais dias ele tem consulta
     * sem ir ao banco, e essas edicoes sao raras.
     */
    public void invalidarTudo() {
        if (!habilitado) {
            return;
        }
        aposCommit(this::apagarTudo);
    }

    private void aposCommit(Runnable acao) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    acao.run();
                }
            });
        } else {
            acao.run();
        }
    }

    static String chave(LocalDate dia) {
        return PREFIXO_CHAVE + dia;
    }

    /** Retorna {@code null} quando falta algum dia (ou o Redis falhou): quem chama vai ao banco. */
    private List<ConsultaResponse> lerDias(List<LocalDate> dias) {
        if (emPausa()) {
            return null;
        }
        try {
            List<String> valores = redis.opsForValue().multiGet(dias.stream().map(AgendaCacheService::chave).toList());
            if (valores == null || valores.size() != dias.size() || valores.stream().anyMatch(Objects::isNull)) {
                log.debug("CACHE AGENDA: MISS para {} a {}", dias.get(0), dias.get(dias.size() - 1));
                return null;
            }
            List<ConsultaResponse> consultas = new ArrayList<>();
            for (String valor : valores) {
                consultas.addAll(json.readValue(valor, TIPO_LISTA));
            }
            log.debug("CACHE AGENDA: HIT para {} a {}", dias.get(0), dias.get(dias.size() - 1));
            return consultas;
        } catch (JsonProcessingException e) {
            // Formato antigo ou corrompido: descarta e recarrega do banco.
            log.warn("CACHE AGENDA: conteudo ilegivel no Redis, recarregando do banco: {}", e.getMessage());
            return null;
        } catch (RuntimeException e) {
            registrarFalha("leitura", e);
            return null;
        }
    }

    private void gravarDias(List<LocalDate> dias, List<ConsultaResponse> consultas) {
        if (emPausa()) {
            return;
        }
        try {
            Map<LocalDate, List<ConsultaResponse>> porDia = new HashMap<>();
            for (ConsultaResponse consulta : consultas) {
                if (consulta.getData() != null) {
                    porDia.computeIfAbsent(consulta.getData(), d -> new ArrayList<>()).add(consulta);
                }
            }
            // Dias sem consulta tambem sao gravados (lista vazia), senao nunca dariam HIT.
            for (LocalDate dia : dias) {
                redis.opsForValue().set(chave(dia), json.writeValueAsString(porDia.getOrDefault(dia, List.of())), ttl);
            }
        } catch (JsonProcessingException e) {
            log.warn("CACHE AGENDA: falha ao serializar consultas para o cache: {}", e.getMessage());
        } catch (RuntimeException e) {
            registrarFalha("gravacao", e);
        }
    }

    private void apagar(List<String> chaves) {
        // Invalidacao tenta mesmo durante a pausa: perder um DEL deixa dado velho ate o TTL.
        try {
            redis.delete(chaves);
            log.debug("CACHE AGENDA: invalidado {}", chaves);
        } catch (RuntimeException e) {
            registrarFalha("invalidacao", e);
        }
    }

    private void apagarTudo() {
        // SCAN em vez de KEYS: percorre em lotes, sem travar o Redis.
        try (Cursor<String> cursor = redis.scan(
                ScanOptions.scanOptions().match(PREFIXO_CHAVE + "*").count(500).build())) {
            List<String> chaves = new ArrayList<>();
            cursor.forEachRemaining(chaves::add);
            if (!chaves.isEmpty()) {
                redis.delete(chaves);
            }
            log.debug("CACHE AGENDA: agenda inteira invalidada ({} dia(s))", chaves.size());
        } catch (RuntimeException e) {
            registrarFalha("invalidacao", e);
        }
    }

    private boolean emPausa() {
        return System.currentTimeMillis() < pausadoAteMillis;
    }

    private void registrarFalha(String operacao, RuntimeException e) {
        if (!emPausa()) {
            log.warn("CACHE AGENDA: Redis indisponivel na {} ({}). Usando apenas o banco pelos proximos {}s.",
                    operacao, e.getMessage(), PAUSA_APOS_FALHA.toSeconds());
        }
        pausadoAteMillis = System.currentTimeMillis() + PAUSA_APOS_FALHA.toMillis();
    }
}
