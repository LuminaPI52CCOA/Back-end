package com.lumina.backend.service.notificacao;

import com.lumina.backend.configure.RabbitMqConfig;
import com.lumina.backend.dto.notificacao.LembreteConsultaEvent;
import com.lumina.backend.dto.notificacao.ProcessamentoD1Response;
import com.lumina.backend.model.Consulta;
import com.lumina.backend.repository.ConsultaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LembretePublicadorServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private LembretePublicadorService publicadorService;

    @Test
    @DisplayName("Deve publicar eventos no Topic Exchange para todas as consultas D-1 elegíveis")
    void devePublicarEventosNoTopicExchange() {
        LocalDate dataAlvo = LocalDate.of(2026, 10, 21);

        Consulta c1 = new Consulta();
        c1.setIdConsulta(101L);
        c1.setData(dataAlvo);

        Consulta c2 = new Consulta();
        c2.setIdConsulta(102L);
        c2.setData(dataAlvo);

        when(consultaRepository.findConsultasElegiveisD1SemNotificacao(dataAlvo))
                .thenReturn(List.of(c1, c2));

        ProcessamentoD1Response response = publicadorService.publicarLembretesParaData(dataAlvo);

        assertTrue(response.sucesso());
        assertEquals(dataAlvo, response.dataAlvo());
        assertEquals(2, response.totalEnfileiradas());

        ArgumentCaptor<LembreteConsultaEvent> captor = ArgumentCaptor.forClass(LembreteConsultaEvent.class);
        verify(rabbitTemplate, times(2)).convertAndSend(
                eq(RabbitMqConfig.EXCHANGE_NOTIFICACOES),
                eq(RabbitMqConfig.ROUTING_KEY_LEMBRETE_WHATSAPP),
                captor.capture()
        );

        List<LembreteConsultaEvent> eventos = captor.getAllValues();
        assertEquals(101L, eventos.get(0).idConsulta());
        assertEquals(102L, eventos.get(1).idConsulta());
    }
}
