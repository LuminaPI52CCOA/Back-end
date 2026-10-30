package com.lumina.backend.service.notificacao;

import com.lumina.backend.dto.notificacao.ProcessamentoD1Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LembreteConsultaScheduler {

    private final LembretePublicadorService lembretePublicadorService;

    public LembreteConsultaScheduler(LembretePublicadorService lembretePublicadorService) {
        this.lembretePublicadorService = lembretePublicadorService;
    }

    @Scheduled(cron = "0 0 8 * * *", zone = "America/Sao_Paulo")
    public void executarRotinaDiariaD1() {
        log.info("Iniciando agendamento automático de lembretes D-1 (08:00 BRT)...");
        ProcessamentoD1Response resultado = lembretePublicadorService.publicarLembretesD1();
        log.info("Agendamento D-1 finalizado: dataAlvo={}, totalEnfileiradas={}",
                resultado.dataAlvo(), resultado.totalEnfileiradas());
    }
}
