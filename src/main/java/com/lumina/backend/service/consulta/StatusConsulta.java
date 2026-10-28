package com.lumina.backend.service.consulta;

import com.lumina.backend.exception.AgendamentoInvalidoException;

import java.util.Locale;

/**
 * Status de consulta persistidos na coluna consulta.status.
 * Valores legados (nulo, CANCELADO, FINALIZADA) sao normalizados na leitura.
 */
public enum StatusConsulta {
    AGENDADA,
    CONFIRMADA,
    CANCELADA,
    CONCLUIDA;

    public static StatusConsulta de(String valor) {
        if (valor == null || valor.isBlank()) {
            return AGENDADA;
        }
        return switch (valor.trim().toUpperCase(Locale.ROOT)) {
            case "CONFIRMADA" -> CONFIRMADA;
            case "CANCELADA", "CANCELADO" -> CANCELADA;
            case "CONCLUIDA", "FINALIZADA" -> CONCLUIDA;
            default -> AGENDADA;
        };
    }

    public static StatusConsulta deRequisicao(String valor) {
        if (valor != null) {
            for (StatusConsulta status : values()) {
                if (status.name().equalsIgnoreCase(valor.trim())) {
                    return status;
                }
            }
        }
        throw new AgendamentoInvalidoException("Status inválido: " + valor + ". Use AGENDADA, CONFIRMADA, CONCLUIDA ou CANCELADA.");
    }

    public boolean finalizado() {
        return this == CANCELADA || this == CONCLUIDA;
    }
}
