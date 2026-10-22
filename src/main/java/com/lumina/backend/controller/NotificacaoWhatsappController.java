package com.lumina.backend.controller;

import com.lumina.backend.dto.notificacao.NotificacaoWhatsappResponse;
import com.lumina.backend.dto.notificacao.ProcessamentoD1Response;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.repository.UsuarioRepository;
import com.lumina.backend.service.notificacao.LembretePublicadorService;
import com.lumina.backend.service.notificacao.NotificacaoWhatsappService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/notificacoes")
@PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA', 'RECEPCAO')")
@Tag(name = "Notificações WhatsApp", description = "Endpoints da recepção e administração para lembretes de consultas D-1 via WhatsApp")
public class NotificacaoWhatsappController {

    private final LembretePublicadorService lembretePublicadorService;
    private final NotificacaoWhatsappService notificacaoWhatsappService;
    private final UsuarioRepository usuarioRepository;

    public NotificacaoWhatsappController(LembretePublicadorService lembretePublicadorService,
                                         NotificacaoWhatsappService notificacaoWhatsappService,
                                         UsuarioRepository usuarioRepository) {
        this.lembretePublicadorService = lembretePublicadorService;
        this.notificacaoWhatsappService = notificacaoWhatsappService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/processar-d1")
    @Operation(
            summary = "Processar fila D-1 sob demanda",
            description = "Executa manualmente a varredura de consultas elegíveis de amanhã (D-1) e publica eventos no RabbitMQ."
    )
    public ResponseEntity<ProcessamentoD1Response> processarD1() {
        ProcessamentoD1Response response = lembretePublicadorService.publicarLembretesD1();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/whatsapp")
    @Operation(
            summary = "Listar notificações de WhatsApp para a recepção",
            description = "Lista as notificações de lembrete com link wa.me gerado dinamicamente."
    )
    public ResponseEntity<List<NotificacaoWhatsappResponse>> listarNotificacoes(
            @Parameter(description = "Data da consulta (padrão: amanhã em America/Sao_Paulo)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @Parameter(description = "Filtro opcional de status (ex: PENDENTE, DISPARADO, FALHA_CONTATO_INVALIDO)")
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(notificacaoWhatsappService.listar(data, status));
    }

    @PatchMapping("/whatsapp/{id}/disparar")
    @Operation(
            summary = "Marcar notificação como disparada",
            description = "Registra auditoria de disparo assistido (data/hora e recepcionista autenticado)."
    )
    public ResponseEntity<NotificacaoWhatsappResponse> marcarComoDisparado(
            @Parameter(description = "ID da notificação") @PathVariable Long id
    ) {
        Long idUsuarioLogado = obterUsuarioIdAutenticado();
        return ResponseEntity.ok(notificacaoWhatsappService.marcarComoDisparado(id, idUsuarioLogado));
    }

    private Long obterUsuarioIdAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null
                && auth.getName() != null
                && !auth.getName().isBlank()
                && !"anonymousUser".equalsIgnoreCase(auth.getName())) {
            return usuarioRepository.findByEmail(auth.getName())
                    .map(Usuario::getIdUsuario)
                    .orElse(null);
        }
        return null;
    }
}
