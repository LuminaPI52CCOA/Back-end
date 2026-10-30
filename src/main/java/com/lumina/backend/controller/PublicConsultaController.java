package com.lumina.backend.controller;

import com.lumina.backend.dto.notificacao.ConfirmacaoConsultaResponse;
import com.lumina.backend.service.notificacao.NotificacaoWhatsappService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/public/consultas"})
@SecurityRequirements
@Tag(name = "Confirmação Pública de Consultas", description = "Endpoint público para confirmação 1-clique pelo paciente via token seguro")
public class PublicConsultaController {

    private final NotificacaoWhatsappService notificacaoWhatsappService;

    public PublicConsultaController(NotificacaoWhatsappService notificacaoWhatsappService) {
        this.notificacaoWhatsappService = notificacaoWhatsappService;
    }

    @PostMapping("/confirmar/{token}")
    @Operation(
            summary = "Confirmar consulta com 1 clique",
            description = "Confirma a presença do paciente na consulta a partir do token UUID enviado via WhatsApp."
    )
    public ResponseEntity<ConfirmacaoConsultaResponse> confirmarConsulta(
            @Parameter(description = "Token UUID v4 único de confirmação") @PathVariable String token
    ) {
        return ResponseEntity.ok(notificacaoWhatsappService.confirmarConsultaPorToken(token));
    }
}
