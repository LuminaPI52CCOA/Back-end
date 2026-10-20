package com.lumina.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class FormatoArquivoInvalidoException extends RuntimeException {
    public FormatoArquivoInvalidoException(String message) {
        super(message);
    }
}
