package com.lumina.backend.service.alexa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumina.backend.model.Consulta;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.model.UsuarioAlexa;
import com.lumina.backend.repository.ConsultaRepository;
import com.lumina.backend.repository.UsuarioAlexaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class AlexaReminderService {

    public static final ZoneId ZONE_SAO_PAULO = ZoneId.of("America/Sao_Paulo");
    private static final String LWA_TOKEN_URL = "https://api.amazon.com/auth/o2/token";
    private static final String REMINDERS_SCOPE = "alexa::alerts:reminders:skill:readwrite";

    @Value("${alexa.lwa.client-id:}")
    private String clientId;

    @Value("${alexa.lwa.client-secret:}")
    private String clientSecret;

    @Value("${alexa.reminders.enabled:true}")
    private boolean remindersEnabled;

    private final ConsultaRepository consultaRepository;
    private final UsuarioAlexaRepository usuarioAlexaRepository;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    private String cachedLwaToken;
    private Instant lwaTokenExpiresAt = Instant.MIN;

    public AlexaReminderService(ConsultaRepository consultaRepository,
                                UsuarioAlexaRepository usuarioAlexaRepository,
                                ObjectMapper objectMapper) {
        this.consultaRepository = consultaRepository;
        this.usuarioAlexaRepository = usuarioAlexaRepository;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().build();
    }

    /**
     * Executa a cada 60 segundos buscando consultas com antecedência de ~10 minutos
     * para enviar alertas proativos via Alexa Reminders API.
     */
    @Scheduled(cron = "0 * * * * *", zone = "America/Sao_Paulo")
    @Transactional
    public void verificarConsultasEEnviarLembretes() {
        if (!remindersEnabled) {
            log.trace("ALEXA REMINDERS: Serviço de lembretes desativado via configuração.");
            return;
        }

        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            log.trace("ALEXA REMINDERS: Credenciais LWA (client-id / client-secret) não configuradas. Aguardando configuração.");
            return;
        }

        LocalDate hoje = LocalDate.now(ZONE_SAO_PAULO);
        LocalTime agora = LocalTime.now(ZONE_SAO_PAULO);

        // Janela de verificação: consultas iniciando entre 8 e 12 minutos à frente (média 10 minutos)
        LocalTime janelaInicio = agora.plusMinutes(8);
        LocalTime janelaFim = agora.plusMinutes(12);

        List<Consulta> consultas = consultaRepository.findConsultasParaLembrete(hoje, janelaInicio, janelaFim);

        if (consultas.isEmpty()) {
            return;
        }

        log.info("ALEXA REMINDERS: Identificadas [{}] consulta(s) iminente(s) para disparo de lembrete", consultas.size());

        String lwaToken = obterTokenLwa();
        if (lwaToken == null) {
            log.warn("ALEXA REMINDERS: Não foi possível obter o token LWA da Amazon. Lembretes postergados.");
            return;
        }

        for (Consulta consulta : consultas) {
            try {
                processarLembreteConsulta(consulta, lwaToken);
            } catch (Exception e) {
                log.error("ALEXA REMINDERS: Falha ao enviar lembrete para consulta ID [{}]: {}",
                        consulta.getIdConsulta(), e.getMessage(), e);
            }
        }
    }

    private void processarLembreteConsulta(Consulta consulta, String lwaToken) {
        Usuario dentista = consulta.getUsuario();
        if (dentista == null) {
            return;
        }

        Optional<UsuarioAlexa> vinculoOpt = usuarioAlexaRepository.findByUsuario_IdUsuarioAndAtivoTrue(dentista.getIdUsuario());
        if (vinculoOpt.isEmpty()) {
            log.debug("ALEXA REMINDERS: Dentista [{}] não possui dispositivo Alexa vinculado. Pulando lembrete.", dentista.getEmail());
            return;
        }

        UsuarioAlexa vinculo = vinculoOpt.get();
        String apiEndpoint = (vinculo.getApiEndpoint() != null && !vinculo.getApiEndpoint().isBlank())
                ? vinculo.getApiEndpoint()
                : "https://api.amazonalexa.com";

        String pacienteNome = (consulta.getCliente() != null) ? consulta.getCliente().getNome() : "paciente";
        String textoLembrete = String.format(
                "Lumina informa: Doutor %s, sua consulta com %s começa em 10 minutos.",
                dentista.getNome(), pacienteNome
        );

        String reminderId = dispararRemindersApi(apiEndpoint, lwaToken, textoLembrete);

        if (reminderId != null) {
            consulta.setLembreteEnviado(true);
            consulta.setAlexaReminderId(reminderId);
            consultaRepository.save(consulta);
            log.info("AUDIT: Lembrete proativo Alexa enviado com sucesso para consulta ID [{}] (dentista: [{}], reminderId: [{}])",
                    consulta.getIdConsulta(), dentista.getNome(), reminderId);
        }
    }

    public synchronized String obterTokenLwa() {
        if (cachedLwaToken != null && Instant.now().isBefore(lwaTokenExpiresAt.minusSeconds(60))) {
            return cachedLwaToken;
        }

        try {
            String formBody = String.format(
                    "grant_type=client_credentials&client_id=%s&client_secret=%s&scope=%s",
                    clientId.trim(), clientSecret.trim(), REMINDERS_SCOPE
            );

            ResponseEntity<String> response = restClient.post()
                    .uri(LWA_TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formBody)
                    .retrieve()
                    .toEntity(String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode json = objectMapper.readTree(response.getBody());
                String accessToken = json.path("access_token").asText(null);
                int expiresIn = json.path("expires_in").asInt(3600);

                if (accessToken != null) {
                    this.cachedLwaToken = accessToken;
                    this.lwaTokenExpiresAt = Instant.now().plusSeconds(expiresIn);
                    log.info("ALEXA LWA: Token OAuth renovado com sucesso. Validade: [{}] segundos", expiresIn);
                    return accessToken;
                }
            }
        } catch (Exception e) {
            log.error("ALEXA LWA: Falha na autenticação OAuth junto à Amazon: {}", e.getMessage());
        }

        return null;
    }

    private String dispararRemindersApi(String apiEndpoint, String lwaToken, String mensagemTexto) {
        String url = apiEndpoint.replaceAll("/$", "") + "/v1/alerts/reminders";

        // Cria o payload da Alexa Reminders API com disparo imediato no Echo
        String requestTime = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);

        Map<String, Object> spokenContent = Map.of(
                "locale", "pt-BR",
                "text", mensagemTexto
        );

        Map<String, Object> spokenInfo = Map.of(
                "content", List.of(spokenContent)
        );

        Map<String, Object> alertInfo = Map.of(
                "spokenInfo", spokenInfo
        );

        Map<String, Object> trigger = Map.of(
                "type", "SCHEDULED_RELATIVE",
                "offsetInSeconds", 0
        );

        Map<String, Object> pushNotification = Map.of(
                "status", "ENABLED"
        );

        Map<String, Object> payload = Map.of(
                "requestTime", requestTime,
                "trigger", trigger,
                "alertInfo", alertInfo,
                "pushNotification", pushNotification
        );

        try {
            ResponseEntity<String> response = restClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + lwaToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toEntity(String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode json = objectMapper.readTree(response.getBody());
                return json.path("alertToken").asText(json.path("id").asText("reminder-ok"));
            }
        } catch (Exception e) {
            log.error("ALEXA REMINDERS API: Falha ao enviar notificação para [{}]: {}", url, e.getMessage());
        }

        return null;
    }
}
