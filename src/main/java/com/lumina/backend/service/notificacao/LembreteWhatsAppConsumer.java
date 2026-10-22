package com.lumina.backend.service.notificacao;

import com.lumina.backend.configure.RabbitMqConfig;
import com.lumina.backend.dto.notificacao.LembreteConsultaEvent;
import com.lumina.backend.model.Cliente;
import com.lumina.backend.model.Consulta;
import com.lumina.backend.model.NotificacaoWhatsapp;
import com.lumina.backend.repository.ConsultaRepository;
import com.lumina.backend.repository.NotificacaoWhatsappRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class LembreteWhatsAppConsumer {

    public static final String MOTIVO_FALHA_TELEFONE_INVALIDO =
            "Telefone fixo ou formato incompatível com WhatsApp";

    private static final Set<String> STATUS_INATIVOS = Set.of(
            "CANCELADA", "CANCELADO", "FINALIZADA", "CONCLUIDA"
    );

    private final ConsultaRepository consultaRepository;
    private final NotificacaoWhatsappRepository notificacaoWhatsappRepository;

    public LembreteWhatsAppConsumer(ConsultaRepository consultaRepository,
                                    NotificacaoWhatsappRepository notificacaoWhatsappRepository) {
        this.consultaRepository = consultaRepository;
        this.notificacaoWhatsappRepository = notificacaoWhatsappRepository;
    }

    @RabbitListener(queues = RabbitMqConfig.QUEUE_WHATSAPP)
    @Transactional
    public void consumirEventoLembrete(LembreteConsultaEvent event) {
        if (event == null || event.idConsulta() == null) {
            log.warn("Evento LembreteConsultaEvent nulo ou sem idConsulta recebido. Ignorando.");
            return;
        }

        if (notificacaoWhatsappRepository.existsByConsultaIdConsulta(event.idConsulta())) {
            log.info("Notificação já existente para consulta ID={}. ACK idempotente.", event.idConsulta());
            return;
        }

        Optional<Consulta> consultaOpt = consultaRepository.findById(event.idConsulta());
        if (consultaOpt.isEmpty()) {
            log.warn("Consulta ID={} não encontrada no banco (Claim Check). Ignorando evento.", event.idConsulta());
            return;
        }

        Consulta consulta = consultaOpt.get();
        if (consulta.getStatus() != null && STATUS_INATIVOS.contains(consulta.getStatus().toUpperCase())) {
            log.info("Consulta ID={} encontra-se com status '{}'. Notificação não será gerada.",
                    consulta.getIdConsulta(), consulta.getStatus());
            return;
        }

        Cliente cliente = consulta.getCliente();
        if (cliente == null) {
            log.warn("Consulta ID={} não possui cliente associado. Ignorando evento.", consulta.getIdConsulta());
            return;
        }

        boolean menorDeIdade = cliente.getDataNascimento() != null
                && consulta.getData() != null
                && Period.between(cliente.getDataNascimento(), consulta.getData()).getYears() < 18;

        boolean usarResponsavel = menorDeIdade && cliente.getResponsavel() != null;
        Cliente destinatario = usarResponsavel ? cliente.getResponsavel() : cliente;
        String grauParentesco = usarResponsavel ? cliente.getGrauParentescoResponsavel() : null;

        String telefoneHigienizado = higienizarCelularBrasileiro(destinatario.getNumeroCelular());

        NotificacaoWhatsapp notificacao = new NotificacaoWhatsapp();
        notificacao.setConsulta(consulta);
        notificacao.setCliente(cliente);
        notificacao.setDestinatario(destinatario);
        notificacao.setNomeDestinatario(destinatario.getNome());
        notificacao.setGrauParentesco(grauParentesco);
        notificacao.setDataConsulta(consulta.getData());
        notificacao.setHorarioConsulta(consulta.getHorarioInicio());
        notificacao.setTokenConfirmacao(UUID.randomUUID().toString());
        notificacao.setLimiteConfirmacaoEm(LocalDateTime.of(consulta.getData(), consulta.getHorarioInicio()));

        if (telefoneHigienizado != null) {
            notificacao.setNumeroTelefoneHigienizado(telefoneHigienizado);
            notificacao.setStatus(NotificacaoWhatsapp.STATUS_PENDENTE);
            notificacao.setMotivoFalha(null);
        } else {
            notificacao.setNumeroTelefoneHigienizado(null);
            notificacao.setStatus(NotificacaoWhatsapp.STATUS_FALHA_CONTATO_INVALIDO);
            notificacao.setMotivoFalha(MOTIVO_FALHA_TELEFONE_INVALIDO);
        }

        try {
            notificacaoWhatsappRepository.save(notificacao);
            log.info("Notificação WhatsApp preparada para consulta ID={} com status={}",
                    consulta.getIdConsulta(), notificacao.getStatus());
        } catch (DataIntegrityViolationException ex) {
            log.info("Violação de unicidade capturada para consulta ID={}. ACK silencioso (idempotência).",
                    consulta.getIdConsulta());
        }
    }

    /**
     * Higieniza números de telefone móveis nacionais (padrão Anatel + DDI 55).
     * Retorna o número com 13 dígitos (ex: 5511999998888) ou null se for fixo/inválido.
     */
    public static String higienizarCelularBrasileiro(String telefoneBruto) {
        if (telefoneBruto == null || telefoneBruto.isBlank()) {
            return null;
        }

        String digitos = telefoneBruto.replaceAll("\\D", "");

        if (digitos.startsWith("55") && digitos.length() == 13) {
            int ddd = Integer.parseInt(digitos.substring(2, 4));
            char nonoDigito = digitos.charAt(4);
            if (ddd >= 11 && ddd <= 99 && nonoDigito == '9') {
                return digitos;
            }
            return null;
        }

        if (digitos.length() == 11) {
            int ddd = Integer.parseInt(digitos.substring(0, 2));
            char nonoDigito = digitos.charAt(2);
            if (ddd >= 11 && ddd <= 99 && nonoDigito == '9') {
                return "55" + digitos;
            }
            return null;
        }

        return null;
    }
}
