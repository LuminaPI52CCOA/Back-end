package com.lumina.backend.dto.notificacao;

import java.time.LocalDate;

public record ConfirmacaoConsultaResponse(
        boolean sucesso,
        String mensagem,
        String paciente,
        String dentista,
        LocalDate data,
        String horario
) {
}
