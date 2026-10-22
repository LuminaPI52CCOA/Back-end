package com.lumina.backend.service.alexa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumina.backend.repository.ConsultaRepository;
import com.lumina.backend.repository.UsuarioAlexaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários AlexaReminderService")
class AlexaReminderServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private UsuarioAlexaRepository usuarioAlexaRepository;

    private AlexaReminderService service;

    @BeforeEach
    void setUp() {
        service = new AlexaReminderService(consultaRepository, usuarioAlexaRepository, new ObjectMapper());
        ReflectionTestUtils.setField(service, "remindersEnabled", true);
        ReflectionTestUtils.setField(service, "clientId", "client");
        ReflectionTestUtils.setField(service, "clientSecret", "secret");
    }

    @Test
    @DisplayName("Não deve consultar o banco quando os lembretes estiverem desativados")
    void naoDeveExecutarQuandoDesativado() {
        ReflectionTestUtils.setField(service, "remindersEnabled", false);

        service.verificarConsultasEEnviarLembretes();

        verifyNoInteractions(consultaRepository, usuarioAlexaRepository);
    }

    @Test
    @DisplayName("Não deve consultar o banco quando o client-id não estiver configurado")
    void naoDeveExecutarSemClientId() {
        ReflectionTestUtils.setField(service, "clientId", "");

        service.verificarConsultasEEnviarLembretes();

        verifyNoInteractions(consultaRepository, usuarioAlexaRepository);
    }

    @Test
    @DisplayName("Não deve consultar o banco quando o client-secret não estiver configurado")
    void naoDeveExecutarSemClientSecret() {
        ReflectionTestUtils.setField(service, "clientSecret", null);

        service.verificarConsultasEEnviarLembretes();

        verifyNoInteractions(consultaRepository, usuarioAlexaRepository);
    }

    @Test
    @DisplayName("Deve encerrar sem buscar vínculos quando não houver consultas iminentes")
    void deveEncerrarQuandoNaoHouverConsultas() {
        when(consultaRepository.findConsultasParaLembrete(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        service.verificarConsultasEEnviarLembretes();

        verify(consultaRepository).findConsultasParaLembrete(any(), any(), any());
        verify(consultaRepository, never()).save(any());
        verifyNoInteractions(usuarioAlexaRepository);
    }

    @Test
    @DisplayName("Deve retornar o token LWA em cache quando ainda for válido")
    void deveRetornarTokenEmCache() {
        ReflectionTestUtils.setField(service, "cachedLwaToken", "token-cache");
        ReflectionTestUtils.setField(service, "lwaTokenExpiresAt", Instant.now().plusSeconds(3600));

        assertEquals("token-cache", service.obterTokenLwa());
    }
}
