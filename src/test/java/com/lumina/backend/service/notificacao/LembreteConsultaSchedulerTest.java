package com.lumina.backend.service.notificacao;

import com.lumina.backend.dto.notificacao.ProcessamentoD1Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários LembreteConsultaScheduler")
class LembreteConsultaSchedulerTest {

    @Mock
    private LembretePublicadorService lembretePublicadorService;

    @InjectMocks
    private LembreteConsultaScheduler scheduler;

    @Test
    @DisplayName("Deve executar a rotina diária D-1 delegando ao LembretePublicadorService")
    void deveExecutarRotinaDiariaD1() {
        ProcessamentoD1Response response = new ProcessamentoD1Response(
                true,
                "Processado",
                LocalDate.of(2026, 10, 23),
                3
        );
        when(lembretePublicadorService.publicarLembretesD1()).thenReturn(response);

        scheduler.executarRotinaDiariaD1();

        verify(lembretePublicadorService, times(1)).publicarLembretesD1();
    }
}
