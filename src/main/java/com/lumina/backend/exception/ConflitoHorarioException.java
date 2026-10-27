package com.lumina.backend.exception;

/**
 * O intervalo solicitado sobrepõe outra consulta ativa do mesmo dentista (HTTP 409).
 */
public class ConflitoHorarioException extends RuntimeException {
    public ConflitoHorarioException(String message) {
        super(message);
    }
}
