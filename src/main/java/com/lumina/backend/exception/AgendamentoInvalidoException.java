package com.lumina.backend.exception;

/**
 * Regra de agendamento violada pelos dados informados (HTTP 400).
 */
public class AgendamentoInvalidoException extends RuntimeException {
    public AgendamentoInvalidoException(String message) {
        super(message);
    }
}
