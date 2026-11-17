package com.lumina.backend.service.consulta;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lumina.backend.dto.cliente.ClienteResponse;
import com.lumina.backend.dto.consulta.ConsultaResponse;
import com.lumina.backend.dto.usuario.UsuarioResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendaCacheServiceTest {

    private static final LocalDate SEGUNDA = LocalDate.of(2026, 11, 2);
    private static final LocalDate TERCA = SEGUNDA.plusDays(1);
    private static final LocalDate QUARTA = SEGUNDA.plusDays(2);
    private static final Duration TTL = Duration.ofSeconds(120);

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private ValueOperations<String, String> valores;

    private final ObjectMapper json = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private AgendaCacheService cache;

    @BeforeEach
    void criar() {
        lenient().when(redis.opsForValue()).thenReturn(valores);
        cache = new AgendaCacheService(redis, json, true, TTL, 31);
    }

    private static ConsultaResponse consulta(Long id, Long idDentista, LocalDate data, int hora) {
        ClienteResponse cliente = new ClienteResponse();
        cliente.setIdCliente(7L);
        cliente.setNome("Carlos Silva");
        cliente.setDataNascimento(LocalDate.of(1990, 5, 20));
        UsuarioResponse dentista = new UsuarioResponse();
        dentista.setIdUsuario(idDentista);
        dentista.setNome("Dra. Ana");
        ConsultaResponse c = new ConsultaResponse(id, cliente, dentista, data, LocalTime.of(hora, 0), LocalTime.of(hora, 30));
        c.setStatus("AGENDADA");
        c.setDuracaoMinutos(30);
        c.setDentes(List.of(16, 26));
        c.setPularOdontograma(false);
        return c;
    }

    private static Supplier<List<ConsultaResponse>> contando(AtomicInteger chamadas, List<ConsultaResponse> retorno) {
        return () -> {
            chamadas.incrementAndGet();
            return retorno;
        };
    }

    @Test
    @DisplayName("MISS: carrega do banco uma vez e grava uma chave por dia, inclusive dias vazios")
    void missCarregaDoBancoEGravaPorDia() throws Exception {
        when(valores.multiGet(anyCollection())).thenReturn(Arrays.asList(null, null, null));
        AtomicInteger idasAoBanco = new AtomicInteger();
        List<ConsultaResponse> doBanco = List.of(
                consulta(1L, 2L, SEGUNDA, 9), consulta(2L, 3L, SEGUNDA, 10), consulta(3L, 2L, QUARTA, 14));

        List<ConsultaResponse> resultado = cache.obterPeriodo(SEGUNDA, QUARTA, null, contando(idasAoBanco, doBanco));

        assertEquals(3, resultado.size());
        assertEquals(1, idasAoBanco.get());
        ArgumentCaptor<String> segunda = ArgumentCaptor.forClass(String.class);
        verify(valores).set(eq("lumina:agenda:dia:2026-11-02"), segunda.capture(), eq(TTL));
        assertEquals(2, json.readTree(segunda.getValue()).size());
        verify(valores).set("lumina:agenda:dia:2026-11-03", "[]", TTL);
        verify(valores).set(eq("lumina:agenda:dia:2026-11-04"), anyString(), eq(TTL));
    }

    @Test
    @DisplayName("HIT: devolve o que esta no Redis, na ordem dos dias, sem ir ao banco")
    void hitNaoVaiAoBanco() throws Exception {
        ConsultaResponse original = consulta(1L, 2L, SEGUNDA, 9);
        when(valores.multiGet(List.of("lumina:agenda:dia:2026-11-02", "lumina:agenda:dia:2026-11-03")))
                .thenReturn(List.of(json.writeValueAsString(List.of(original)),
                        json.writeValueAsString(List.of(consulta(2L, 3L, TERCA, 8)))));
        AtomicInteger idasAoBanco = new AtomicInteger();

        List<ConsultaResponse> resultado = cache.obterPeriodo(SEGUNDA, TERCA, null, contando(idasAoBanco, List.of()));

        assertEquals(0, idasAoBanco.get());
        assertEquals(List.of(1L, 2L), resultado.stream().map(ConsultaResponse::getId).toList());
        ConsultaResponse lida = resultado.get(0);
        // O conteudo que volta do cache tem de ser identico ao que a API devolveria do banco.
        assertEquals(json.writeValueAsString(original), json.writeValueAsString(lida));
        verify(valores, never()).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("Filtro por dentista e aplicado em memoria sobre o dia cacheado")
    void filtraPorDentista() throws Exception {
        when(valores.multiGet(anyCollection())).thenReturn(List.of(json.writeValueAsString(
                List.of(consulta(1L, 2L, SEGUNDA, 9), consulta(2L, 3L, SEGUNDA, 10)))));

        List<ConsultaResponse> resultado = cache.obterPeriodo(SEGUNDA, SEGUNDA, 3L, List::of);

        assertEquals(List.of(2L), resultado.stream().map(ConsultaResponse::getId).toList());
    }

    @Test
    @DisplayName("Um dia faltando ja e MISS do periodo inteiro")
    void diaFaltandoEhMiss() {
        when(valores.multiGet(anyCollection())).thenReturn(Arrays.asList("[]", null));
        AtomicInteger idasAoBanco = new AtomicInteger();

        cache.obterPeriodo(SEGUNDA, TERCA, null, contando(idasAoBanco, List.of()));

        assertEquals(1, idasAoBanco.get());
    }

    @Test
    @DisplayName("Redis fora do ar: responde pelo banco e para de tentar o Redis por um tempo")
    void redisForaDoArUsaBanco() {
        when(valores.multiGet(anyCollection())).thenThrow(new RedisConnectionFailureException("sem conexao"));
        AtomicInteger idasAoBanco = new AtomicInteger();
        Supplier<List<ConsultaResponse>> banco = contando(idasAoBanco, List.of(consulta(1L, 2L, SEGUNDA, 9)));

        assertEquals(1, cache.obterPeriodo(SEGUNDA, SEGUNDA, null, banco).size());
        assertEquals(1, cache.obterPeriodo(SEGUNDA, SEGUNDA, null, banco).size());

        assertEquals(2, idasAoBanco.get());
        verify(valores, times(1)).multiGet(anyCollection());
        verify(valores, never()).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("Conteudo ilegivel no Redis e tratado como MISS")
    void conteudoIlegivelEhMiss() {
        when(valores.multiGet(anyCollection())).thenReturn(List.of("{nao-e-json"));
        AtomicInteger idasAoBanco = new AtomicInteger();

        cache.obterPeriodo(SEGUNDA, SEGUNDA, null, contando(idasAoBanco, List.of()));

        assertEquals(1, idasAoBanco.get());
        verify(valores).set("lumina:agenda:dia:2026-11-02", "[]", TTL);
    }

    @Test
    @DisplayName("So periodos fechados de ate max-dias sao cacheaveis")
    void cacheavel() {
        assertTrue(cache.cacheavel(SEGUNDA, SEGUNDA));
        assertTrue(cache.cacheavel(SEGUNDA, SEGUNDA.plusDays(30)));
        assertFalse(cache.cacheavel(SEGUNDA, SEGUNDA.plusDays(31)));
        assertFalse(cache.cacheavel(SEGUNDA, null));
        assertFalse(cache.cacheavel(null, SEGUNDA));
        assertFalse(cache.cacheavel(TERCA, SEGUNDA));
        assertFalse(AgendaCacheService.desativado().cacheavel(SEGUNDA, SEGUNDA));
    }

    @Test
    @DisplayName("Invalidar fora de transacao apaga as chaves dos dias, sem repetir")
    void invalidarApagaChaves() {
        cache.invalidar(SEGUNDA, TERCA, SEGUNDA, null);

        verify(redis).delete(List.of("lumina:agenda:dia:2026-11-02", "lumina:agenda:dia:2026-11-03"));
    }

    @Test
    @DisplayName("Invalidar dentro de transacao so apaga depois do commit")
    void invalidarEsperaCommit() {
        TransactionSynchronizationManager.initSynchronization();
        try {
            cache.invalidar(SEGUNDA);
            verify(redis, never()).delete(anyCollection());

            List<TransactionSynchronization> pendentes =
                    new ArrayList<>(TransactionSynchronizationManager.getSynchronizations());
            assertEquals(1, pendentes.size());
            pendentes.get(0).afterCommit();
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }

        verify(redis).delete((Collection<String>) List.of("lumina:agenda:dia:2026-11-02"));
    }

    @Test
    @DisplayName("Invalidar tudo apaga todas as chaves da agenda encontradas por SCAN")
    @SuppressWarnings("unchecked")
    void invalidarTudoApagaTodasAsChaves() {
        Cursor<String> cursor = org.mockito.Mockito.mock(Cursor.class);
        Iterator<String> chaves = List.of("lumina:agenda:dia:2026-11-02", "lumina:agenda:dia:2026-11-03").iterator();
        org.mockito.Mockito.doAnswer(inv -> {
            chaves.forEachRemaining(inv.getArgument(0));
            return null;
        }).when(cursor).forEachRemaining(any());
        ArgumentCaptor<ScanOptions> opcoes = ArgumentCaptor.forClass(ScanOptions.class);
        when(redis.scan(opcoes.capture())).thenReturn(cursor);

        cache.invalidarTudo();

        assertEquals("lumina:agenda:dia:*", opcoes.getValue().getPattern());
        verify(redis).delete(List.of("lumina:agenda:dia:2026-11-02", "lumina:agenda:dia:2026-11-03"));
        verify(cursor).close();
    }

    @Test
    @DisplayName("Invalidar tudo com o Redis fora do ar nao propaga erro")
    void invalidarTudoComRedisForaDoAr() {
        when(redis.scan(any(ScanOptions.class))).thenThrow(new RedisConnectionFailureException("sem conexao"));

        cache.invalidarTudo();

        verify(redis, never()).delete(anyCollection());
    }

    @Test
    @DisplayName("Falha ao invalidar nao propaga para quem esta gravando a consulta")
    void falhaAoInvalidarNaoPropaga() {
        when(redis.delete(anyCollection())).thenThrow(new RedisConnectionFailureException("sem conexao"));

        cache.invalidar(SEGUNDA);
    }

    @Test
    @DisplayName("Cache desativado nunca toca no Redis")
    void desativadoNaoTocaNoRedis() {
        AgendaCacheService desligado = new AgendaCacheService(redis, json, false, TTL, 31);
        AtomicInteger idasAoBanco = new AtomicInteger();

        desligado.obterPeriodo(SEGUNDA, TERCA, null, contando(idasAoBanco, List.of()));
        desligado.invalidar(SEGUNDA);
        desligado.invalidarTudo();

        assertEquals(1, idasAoBanco.get());
        verifyNoInteractions(valores);
        verify(redis, never()).delete(anyCollection());
        verify(redis, never()).scan(any(ScanOptions.class));
    }
}
