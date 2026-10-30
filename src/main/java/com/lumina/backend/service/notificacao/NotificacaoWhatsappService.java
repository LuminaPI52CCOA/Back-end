package com.lumina.backend.service.notificacao;

import com.lumina.backend.dto.notificacao.ConfirmacaoConsultaResponse;
import com.lumina.backend.dto.notificacao.NotificacaoWhatsappResponse;
import com.lumina.backend.exception.EntidadeNaoEncontrada;
import com.lumina.backend.model.Cliente;
import com.lumina.backend.model.Consulta;
import com.lumina.backend.model.NotificacaoWhatsapp;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.repository.ConsultaRepository;
import com.lumina.backend.repository.NotificacaoWhatsappRepository;
import com.lumina.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NotificacaoWhatsappService {

    public static final ZoneId ZONE_SAO_PAULO = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter DATA_BR_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA_CURTA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final NotificacaoWhatsappRepository notificacaoWhatsappRepository;
    private final ConsultaRepository consultaRepository;
    private final UsuarioRepository usuarioRepository;
    private final String appPublicUrl;

    public NotificacaoWhatsappService(NotificacaoWhatsappRepository notificacaoWhatsappRepository,
                                      ConsultaRepository consultaRepository,
                                      UsuarioRepository usuarioRepository,
                                      @Value("${app.public-url:http://localhost:5173}") String appPublicUrl) {
        this.notificacaoWhatsappRepository = notificacaoWhatsappRepository;
        this.consultaRepository = consultaRepository;
        this.usuarioRepository = usuarioRepository;
        this.appPublicUrl = appPublicUrl != null ? appPublicUrl.replaceAll("/+$", "") : "http://localhost:5173";
    }

    @Transactional(readOnly = true)
    public List<NotificacaoWhatsappResponse> listar(LocalDate data, String status) {
        LocalDate dataFiltro = (data != null) ? data : LocalDate.now(ZONE_SAO_PAULO).plusDays(1);

        List<NotificacaoWhatsapp> notificacoes;
        if (status != null && !status.isBlank()) {
            notificacoes = notificacaoWhatsappRepository
                    .findByDataConsultaAndStatusIgnoreCaseOrderByHorarioConsultaAsc(dataFiltro, status.trim());
        } else {
            notificacoes = notificacaoWhatsappRepository
                    .findByDataConsultaOrderByHorarioConsultaAsc(dataFiltro);
        }

        return notificacoes.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public NotificacaoWhatsappResponse marcarComoDisparado(Long idNotificacao, Long idUsuarioDisparo) {
        NotificacaoWhatsapp notificacao = notificacaoWhatsappRepository.findById(idNotificacao)
                .orElseThrow(() -> new EntidadeNaoEncontrada("Notificação WhatsApp não encontrada"));

        if (NotificacaoWhatsapp.STATUS_CANCELADO.equalsIgnoreCase(notificacao.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível disparar uma notificação cancelada.");
        }

        if (NotificacaoWhatsapp.STATUS_FALHA_CONTATO_INVALIDO.equalsIgnoreCase(notificacao.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível disparar notificação com contato inválido.");
        }

        notificacao.setStatus(NotificacaoWhatsapp.STATUS_DISPARADO);
        notificacao.setDisparadoEm(LocalDateTime.now(ZONE_SAO_PAULO));

        if (idUsuarioDisparo != null) {
            Usuario usuario = usuarioRepository.findById(idUsuarioDisparo).orElse(null);
            notificacao.setUsuarioDisparo(usuario);
        }

        NotificacaoWhatsapp salva = notificacaoWhatsappRepository.save(notificacao);
        return toResponse(salva);
    }

    @Transactional
    public ConfirmacaoConsultaResponse confirmarConsultaPorToken(String token) {
        NotificacaoWhatsapp notificacao = notificacaoWhatsappRepository.findByTokenConfirmacao(token)
                .orElseThrow(() -> new EntidadeNaoEncontrada("Token de confirmação não encontrado."));

        Consulta consulta = notificacao.getConsulta();
        if (consulta == null) {
            throw new EntidadeNaoEncontrada("Consulta vinculada ao token não encontrada.");
        }

        if (NotificacaoWhatsapp.STATUS_CANCELADO.equalsIgnoreCase(notificacao.getStatus())
                || (consulta.getStatus() != null
                && (consulta.getStatus().equalsIgnoreCase("CANCELADA")
                || consulta.getStatus().equalsIgnoreCase("CANCELADO")))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Consulta cancelada.");
        }

        String nomePaciente = notificacao.getCliente() != null ? notificacao.getCliente().getNome() : "";
        String nomeDentista = (consulta.getUsuario() != null && consulta.getUsuario().getNome() != null)
                ? consulta.getUsuario().getNome()
                : "";
        String horarioFormatado = notificacao.getHorarioConsulta() != null
                ? notificacao.getHorarioConsulta().format(HORA_CURTA_FORMATTER)
                : "";

        boolean jaConfirmada = "CONFIRMADA".equalsIgnoreCase(consulta.getStatus())
                || notificacao.getConfirmadoEm() != null;

        if (jaConfirmada) {
            return new ConfirmacaoConsultaResponse(
                    true,
                    "Consulta já confirmada anteriormente!",
                    nomePaciente,
                    nomeDentista,
                    notificacao.getDataConsulta(),
                    horarioFormatado
            );
        }

        LocalDateTime agora = LocalDateTime.now(ZONE_SAO_PAULO);
        if (notificacao.getLimiteConfirmacaoEm() != null && agora.isAfter(notificacao.getLimiteConfirmacaoEm())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Link expirado.");
        }

        consulta.setStatus("CONFIRMADA");
        consultaRepository.save(consulta);

        notificacao.setConfirmadoEm(agora);
        notificacaoWhatsappRepository.save(notificacao);

        return new ConfirmacaoConsultaResponse(
                true,
                "Consulta confirmada com sucesso!",
                nomePaciente,
                nomeDentista,
                notificacao.getDataConsulta(),
                horarioFormatado
        );
    }

    public NotificacaoWhatsappResponse toResponse(NotificacaoWhatsapp notificacao) {
        Consulta consulta = notificacao.getConsulta();
        Cliente paciente = notificacao.getCliente();
        Cliente destinatario = notificacao.getDestinatario();

        String nomePaciente = paciente != null ? paciente.getNome() : "";
        String nomeDestinatario = notificacao.getNomeDestinatario();
        String dentista = (consulta != null && consulta.getUsuario() != null)
                ? consulta.getUsuario().getNome()
                : "";

        boolean consultaConfirmada = (consulta != null && "CONFIRMADA".equalsIgnoreCase(consulta.getStatus()))
                || notificacao.getConfirmadoEm() != null;

        String telefoneFormatado = formatarTelefoneExibicao(
                notificacao.getNumeroTelefoneHigienizado(),
                destinatario != null ? destinatario.getNumeroCelular() : null
        );

        String linkWhatsApp = montarLinkWhatsApp(notificacao, nomePaciente, dentista);

        Long idUsuarioDisparo = notificacao.getUsuarioDisparo() != null
                ? notificacao.getUsuarioDisparo().getIdUsuario()
                : null;

        return new NotificacaoWhatsappResponse(
                notificacao.getIdNotificacao(),
                consulta != null ? consulta.getIdConsulta() : null,
                nomePaciente,
                nomeDestinatario,
                notificacao.getGrauParentesco(),
                telefoneFormatado,
                notificacao.getDataConsulta(),
                notificacao.getHorarioConsulta(),
                dentista,
                notificacao.getStatus(),
                notificacao.getMotivoFalha(),
                linkWhatsApp,
                consultaConfirmada,
                notificacao.getDisparadoEm(),
                idUsuarioDisparo,
                notificacao.getConfirmadoEm()
        );
    }

    public String montarLinkWhatsApp(NotificacaoWhatsapp notificacao, String nomePaciente, String nomeDentista) {
        String telefone = notificacao.getNumeroTelefoneHigienizado();
        if (telefone == null || telefone.isBlank()
                || NotificacaoWhatsapp.STATUS_FALHA_CONTATO_INVALIDO.equalsIgnoreCase(notificacao.getStatus())) {
            return null;
        }

        String dataFormatada = notificacao.getDataConsulta() != null
                ? notificacao.getDataConsulta().format(DATA_BR_FORMATTER)
                : "";
        String horarioFormatado = notificacao.getHorarioConsulta() != null
                ? notificacao.getHorarioConsulta().format(HORA_CURTA_FORMATTER)
                : "";
        String linkConfirmacao = appPublicUrl + "/confirmar-consulta?token=" + notificacao.getTokenConfirmacao();

        boolean destinatarioEhResponsavel = notificacao.getCliente() != null
                && notificacao.getDestinatario() != null
                && notificacao.getCliente().getIdCliente() != null
                && !notificacao.getCliente().getIdCliente().equals(notificacao.getDestinatario().getIdCliente());

        String mensagem;
        if (destinatarioEhResponsavel) {
            mensagem = String.format(
                    "Olá, %s! Lembramos que a consulta de %s com Dr(a). %s na Lumina Odontologia está agendada para amanhã, %s, às %s.\n\nPara confirmar a presença com apenas 1 clique, acesse:\n%s",
                    notificacao.getNomeDestinatario(),
                    nomePaciente,
                    nomeDentista,
                    dataFormatada,
                    horarioFormatado,
                    linkConfirmacao
            );
        } else {
            mensagem = String.format(
                    "Olá, %s! Lembramos que sua consulta com Dr(a). %s na Lumina Odontologia está agendada para amanhã, %s, às %s.\n\nPara confirmar sua presença com apenas 1 clique, acesse:\n%s",
                    notificacao.getNomeDestinatario(),
                    nomeDentista,
                    dataFormatada,
                    horarioFormatado,
                    linkConfirmacao
            );
        }

        String textoCodificado = URLEncoder.encode(mensagem, StandardCharsets.UTF_8).replace("+", "%20");
        return "https://wa.me/" + telefone + "?text=" + textoCodificado;
    }

    public static String formatarTelefoneExibicao(String telefoneHigienizado, String telefoneBrutoFallback) {
        if (telefoneHigienizado != null && telefoneHigienizado.length() == 13 && telefoneHigienizado.startsWith("55")) {
            String ddd = telefoneHigienizado.substring(2, 4);
            String prefixo = telefoneHigienizado.substring(4, 9);
            String sufixo = telefoneHigienizado.substring(9, 13);
            return String.format("(%s) %s-%s", ddd, prefixo, sufixo);
        }
        return telefoneBrutoFallback;
    }
}
