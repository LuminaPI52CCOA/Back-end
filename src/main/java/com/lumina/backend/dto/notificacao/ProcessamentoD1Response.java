package com.lumina.backend.dto.notificacao;

import java.time.LocalDate;

public record ProcessamentoD1Response(
        boolean sucesso,
        String mensagem,
        LocalDate dataAlvo,
        int totalEnfileiradas
) {
}
