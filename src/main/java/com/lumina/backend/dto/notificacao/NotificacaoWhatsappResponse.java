package com.lumina.backend.dto.notificacao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record NotificacaoWhatsappResponse(
        Long idNotificacao,
        Long idConsulta,
        String nomePaciente,
        String nomeDestinatario,
        String grauParentesco,
        String telefoneFormatado,
        LocalDate dataConsulta,
        LocalTime horarioConsulta,
        String dentista,
        String status,
        String motivoFalha,
        String linkWhatsApp,
        boolean consultaConfirmada,
        LocalDateTime disparadoEm,
        Long idUsuarioDisparo,
        LocalDateTime confirmadoEm
) {
}
