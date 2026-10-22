package com.lumina.backend.service.notificacao;

import com.lumina.backend.configure.RabbitMqConfig;
import com.lumina.backend.dto.notificacao.LembreteConsultaEvent;
import com.lumina.backend.dto.notificacao.ProcessamentoD1Response;
import com.lumina.backend.model.Consulta;
import com.lumina.backend.repository.ConsultaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
public class LembretePublicadorService {

    public static final ZoneId ZONE_SAO_PAULO = ZoneId.of("America/Sao_Paulo");

    private final ConsultaRepository consultaRepository;
    private final RabbitTemplate rabbitTemplate;

    public LembretePublicadorService(ConsultaRepository consultaRepository,
                                     RabbitTemplate rabbitTemplate) {
        this.consultaRepository = consultaRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public ProcessamentoD1Response publicarLembretesD1() {
        LocalDate amanha = LocalDate.now(ZONE_SAO_PAULO).plusDays(1);
        return publicarLembretesParaData(amanha);
    }

    public ProcessamentoD1Response publicarLembretesParaData(LocalDate dataAlvo) {
        List<Consulta> consultasElegiveis = consultaRepository.findConsultasElegiveisD1SemNotificacao(dataAlvo);

        String publicadoEm = OffsetDateTime.now(ZONE_SAO_PAULO)
                .truncatedTo(ChronoUnit.SECONDS)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        for (Consulta consulta : consultasElegiveis) {
            LembreteConsultaEvent event = new LembreteConsultaEvent(
                    consulta.getIdConsulta(),
                    consulta.getData(),
                    publicadoEm
            );

            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.EXCHANGE_NOTIFICACOES,
                    RabbitMqConfig.ROUTING_KEY_LEMBRETE_WHATSAPP,
                    event
            );
        }

        log.info("Processamento D-1 concluído para dataAlvo={}: {} consulta(s) enfileirada(s).",
                dataAlvo, consultasElegiveis.size());

        return new ProcessamentoD1Response(
                true,
                "Processamento D-1 iniciado com sucesso.",
                dataAlvo,
                consultasElegiveis.size()
        );
    }
}
