package com.lumina.backend.service.alexa;

import com.lumina.backend.dto.alexa.*;
import com.lumina.backend.exception.EntidadeNaoEncontrada;
import com.lumina.backend.model.Anamnese;
import com.lumina.backend.model.Cliente;
import com.lumina.backend.model.Consulta;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.model.UsuarioAlexa;
import com.lumina.backend.repository.AnamneseRepository;
import com.lumina.backend.repository.ConsultaRepository;
import com.lumina.backend.repository.UsuarioAlexaRepository;
import com.lumina.backend.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class AlexaService {

    public static final ZoneId ZONE_SAO_PAULO = ZoneId.of("America/Sao_Paulo");
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioAlexaRepository usuarioAlexaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ConsultaRepository consultaRepository;
    private final AnamneseRepository anamneseRepository;

    public AlexaService(UsuarioAlexaRepository usuarioAlexaRepository,
                        UsuarioRepository usuarioRepository,
                        ConsultaRepository consultaRepository,
                        AnamneseRepository anamneseRepository) {
        this.usuarioAlexaRepository = usuarioAlexaRepository;
        this.usuarioRepository = usuarioRepository;
        this.consultaRepository = consultaRepository;
        this.anamneseRepository = anamneseRepository;
    }

    @Transactional
    public AlexaGerarPinResponse gerarPin(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntidadeNaoEncontrada("Usuário não encontrado"));

        String pin = String.format("%06d", RANDOM.nextInt(1000000));
        LocalDateTime expiracao = LocalDateTime.now(ZONE_SAO_PAULO).plusMinutes(10);

        Optional<UsuarioAlexa> vinculoOpt = usuarioAlexaRepository.findByUsuario_IdUsuarioAndAtivoTrue(idUsuario);
        UsuarioAlexa vinculo;
        if (vinculoOpt.isPresent()) {
            vinculo = vinculoOpt.get();
        } else {
            vinculo = new UsuarioAlexa();
            vinculo.setUsuario(usuario);
            vinculo.setAlexaUserId("pending-" + idUsuario + "-" + System.currentTimeMillis());
            vinculo.setApiEndpoint("https://api.amazonalexa.com");
            vinculo.setAtivo(false);
        }

        vinculo.setCodigoPareamento(pin);
        vinculo.setCodigoExpiracao(expiracao);
        usuarioAlexaRepository.save(vinculo);

        log.info("ALEXA: PIN [{}] gerado com sucesso para o usuario [{}] com validade ate [{}]", pin, usuario.getEmail(), expiracao);

        String instrucao = String.format("Diga à Alexa: 'Alexa, abra a Lumina e vincule o código %s'", pin);
        return new AlexaGerarPinResponse(pin, 10, instrucao);
    }

    @Transactional
    public AlexaVincularResponse vincular(AlexaVincularRequest request) {
        if (request.getCodigo() == null || request.getCodigo().isBlank()) {
            throw new IllegalArgumentException("Código de pareamento não informado");
        }

        UsuarioAlexa vinculo = usuarioAlexaRepository.findByCodigoPareamento(request.getCodigo().trim())
                .orElseThrow(() -> new EntidadeNaoEncontrada("Código de pareamento inválido ou não encontrado"));

        if (vinculo.getCodigoExpiracao() != null && vinculo.getCodigoExpiracao().isBefore(LocalDateTime.now(ZONE_SAO_PAULO))) {
            throw new IllegalStateException("Código de pareamento expirado. Por favor, gere um novo código no painel Lumina.");
        }

        // Se o alexaUserId já estiver vinculado a outro registro antigo, desativa-o para manter unicidade
        Optional<UsuarioAlexa> vinculoExistente = usuarioAlexaRepository.findByAlexaUserIdAndAtivoTrue(request.getAlexaUserId());
        if (vinculoExistente.isPresent() && !vinculoExistente.get().getIdUsuarioAlexa().equals(vinculo.getIdUsuarioAlexa())) {
            UsuarioAlexa antigo = vinculoExistente.get();
            antigo.setAtivo(false);
            usuarioAlexaRepository.save(antigo);
            log.info("ALEXA: Desativando vinculo anterior para o alexaUserId [{}]", request.getAlexaUserId());
        }

        vinculo.setAlexaUserId(request.getAlexaUserId());
        if (request.getApiEndpoint() != null && !request.getApiEndpoint().isBlank()) {
            vinculo.setApiEndpoint(request.getApiEndpoint());
        }
        vinculo.setCodigoPareamento(null);
        vinculo.setCodigoExpiracao(null);
        vinculo.setAtivo(true);
        usuarioAlexaRepository.save(vinculo);

        String dentistaNome = vinculo.getUsuario().getNome();
        log.info("ALEXA: Dispositivo [{}] vinculado com sucesso ao Dr(a). [{}]", request.getAlexaUserId(), dentistaNome);

        return new AlexaVincularResponse(
                true,
                "Dispositivo Alexa vinculado com sucesso ao Dr(a). " + dentistaNome + "!",
                dentistaNome
        );
    }

    public Usuario resolverDentista(String alexaUserId, Long idUsuarioAutenticado) {
        if (alexaUserId != null && !alexaUserId.isBlank()) {
            return usuarioAlexaRepository.findByAlexaUserIdAndAtivoTrue(alexaUserId.trim())
                    .map(UsuarioAlexa::getUsuario)
                    .orElseThrow(() -> new EntidadeNaoEncontrada(
                            "Dispositivo Alexa não vinculado. Por favor, gere o código de pareamento no painel web da Lumina e diga: 'vincular código seguido dos números'."
                    ));
        }

        if (idUsuarioAutenticado != null) {
            return usuarioRepository.findById(idUsuarioAutenticado)
                    .orElseThrow(() -> new EntidadeNaoEncontrada("Usuário autenticado não encontrado"));
        }

        throw new IllegalStateException("Dentista não identificado para a consulta da Alexa");
    }

    public AlexaConsultaDto obterProximaConsulta(String alexaUserId, Long idUsuarioAutenticado) {
        Usuario dentista = resolverDentista(alexaUserId, idUsuarioAutenticado);
        LocalDate hoje = LocalDate.now(ZONE_SAO_PAULO);
        LocalTime agora = LocalTime.now(ZONE_SAO_PAULO);

        List<Consulta> proximas = consultaRepository.findProximasConsultas(dentista.getIdUsuario(), hoje, agora);

        if (proximas.isEmpty()) {
            String msgVoz = String.format("Doutor %s, você não possui mais consultas agendadas para hoje.", dentista.getNome());
            return new AlexaConsultaDto(null, null, hoje, null, null, null, msgVoz);
        }

        Consulta proxima = proximas.get(0);
        String clienteNome = (proxima.getCliente() != null) ? proxima.getCliente().getNome() : "paciente não informado";
        LocalTime inicio = proxima.getHorarioInicio();

        String msgVoz = String.format(
                "Doutor %s, sua próxima consulta é às %02d horas e %02d minutos com o paciente %s.",
                dentista.getNome(), inicio.getHour(), inicio.getMinute(), clienteNome
        );

        return new AlexaConsultaDto(
                proxima.getIdConsulta(),
                clienteNome,
                proxima.getData(),
                proxima.getHorarioInicio(),
                proxima.getHorarioFim(),
                proxima.getStatus(),
                msgVoz
        );
    }

    public AlexaResumoDiaDto obterConsultasHoje(String alexaUserId, Long idUsuarioAutenticado) {
        Usuario dentista = resolverDentista(alexaUserId, idUsuarioAutenticado);
        LocalDate hoje = LocalDate.now(ZONE_SAO_PAULO);
        LocalTime agora = LocalTime.now(ZONE_SAO_PAULO);

        List<Consulta> consultas = consultaRepository.findConsultasDoDia(dentista.getIdUsuario(), hoje);

        if (consultas.isEmpty()) {
            String msgVoz = String.format("Doutor %s, você não possui consultas agendadas para hoje.", dentista.getNome());
            return new AlexaResumoDiaDto(0, null, null, null, null, msgVoz);
        }

        int total = consultas.size();
        LocalTime primeiroHorario = consultas.get(0).getHorarioInicio();
        LocalTime ultimoHorario = consultas.get(consultas.size() - 1).getHorarioInicio();

        // Encontra a próxima a partir de agora
        Optional<Consulta> proximaOpt = consultas.stream()
                .filter(c -> !c.getHorarioInicio().isBefore(agora))
                .findFirst();

        String proximoPaciente = proximaOpt.map(c -> c.getCliente().getNome()).orElse(null);
        LocalTime proximoHorario = proximaOpt.map(Consulta::getHorarioInicio).orElse(null);

        StringBuilder msg = new StringBuilder();
        msg.append(String.format("Doutor %s, você tem %d consulta%s agendada%s hoje.",
                dentista.getNome(), total, (total > 1 ? "s" : ""), (total > 1 ? "s" : "")));

        if (proximoPaciente != null && proximoHorario != null) {
            msg.append(String.format(" A próxima é às %02d horas e %02d minutos com %s.",
                    proximoHorario.getHour(), proximoHorario.getMinute(), proximoPaciente));
        } else {
            msg.append(" Todas as consultas agendadas para hoje já foram concluídas ou já iniciaram.");
        }

        return new AlexaResumoDiaDto(total, primeiroHorario, ultimoHorario, proximoPaciente, proximoHorario, msg.toString());
    }

    public AlexaAnamneseAlertaDto obterAlertaAnamneseProxima(String alexaUserId, Long idUsuarioAutenticado) {
        Usuario dentista = resolverDentista(alexaUserId, idUsuarioAutenticado);
        LocalDate hoje = LocalDate.now(ZONE_SAO_PAULO);
        LocalTime agora = LocalTime.now(ZONE_SAO_PAULO);

        List<Consulta> proximas = consultaRepository.findProximasConsultas(dentista.getIdUsuario(), hoje, agora);
        if (proximas.isEmpty()) {
            return new AlexaAnamneseAlertaDto(
                    null,
                    false,
                    Collections.emptyList(),
                    "Doutor, você não possui próxima consulta agendada para consultar o histórico médico."
            );
        }

        Consulta proxima = proximas.get(0);
        Cliente cliente = proxima.getCliente();
        if (cliente == null) {
            return new AlexaAnamneseAlertaDto(null, false, Collections.emptyList(), "Paciente da consulta não identificado.");
        }

        List<Anamnese> anamneses = anamneseRepository.findByFkCliente_IdClienteOrderByDataAnamneseDesc(cliente.getIdCliente());
        if (anamneses.isEmpty()) {
            String msg = String.format("O paciente %s não possui ficha de anamnese registrada no sistema.", cliente.getNome());
            return new AlexaAnamneseAlertaDto(cliente.getNome(), false, Collections.emptyList(), msg);
        }

        Anamnese ficha = anamneses.get(0);
        List<String> alertas = new ArrayList<>();

        if (Boolean.TRUE.equals(ficha.getAlergiaMedicamentos())) {
            String detalhe = (ficha.getDescricaoAlergiaMedicamentos() != null && !ficha.getDescricaoAlergiaMedicamentos().isBlank())
                    ? ficha.getDescricaoAlergiaMedicamentos() : "medicamento não especificado";
            alertas.add("alergia a " + detalhe);
        }

        if (Boolean.TRUE.equals(ficha.getProblemaCardiaco())) {
            String detalhe = (ficha.getDescricaoProblemaCardiaco() != null && !ficha.getDescricaoProblemaCardiaco().isBlank())
                    ? ficha.getDescricaoProblemaCardiaco() : "problema cardíaco";
            alertas.add(detalhe);
        }

        if (Boolean.FALSE.equals(ficha.getPressaoArterialNormal())) {
            String detalhe = (ficha.getDescricaoPressaoArterial() != null && !ficha.getDescricaoPressaoArterial().isBlank())
                    ? ficha.getDescricaoPressaoArterial() : "pressão arterial alterada";
            alertas.add(detalhe);
        }

        if (Boolean.TRUE.equals(ficha.getSangramentoExcessivo())) {
            alertas.add("histórico de sangramento excessivo");
        }

        if (Boolean.TRUE.equals(ficha.getReacaoAnestesiaLocal())) {
            alertas.add("reação prévia a anestesia local");
        }

        if (Boolean.TRUE.equals(ficha.getGestante())) {
            alertas.add("paciente gestante");
        }

        if (Boolean.TRUE.equals(ficha.getHistoricoDiabetes())) {
            alertas.add("diabetes");
        }

        if (alertas.isEmpty()) {
            String msg = String.format("O paciente %s não possui alertas médicos ou alergias críticas registradas na anamnese.", cliente.getNome());
            return new AlexaAnamneseAlertaDto(cliente.getNome(), false, Collections.emptyList(), msg);
        }

        String alertasTexto = String.join(", ", alertas);
        String msgVoz = String.format("Atenção Doutor: O paciente %s relatou: %s.", cliente.getNome(), alertasTexto);

        return new AlexaAnamneseAlertaDto(cliente.getNome(), true, alertas, msgVoz);
    }
}
