package com.lumina.backend.dto.notificacao;

import java.time.LocalDate;

public record LembreteConsultaEvent(
        Long idConsulta,
        LocalDate dataConsulta,
        String publicadoEm
) {
}
