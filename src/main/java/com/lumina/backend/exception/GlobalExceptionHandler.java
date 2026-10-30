package com.lumina.backend.exception;

import com.lumina.backend.exception.dto.ErroRespostaDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntidadeNaoEncontrada.class)
    public ResponseEntity<ErroRespostaDto> handleEntidadeNaoEncontrada(EntidadeNaoEncontrada ex, HttpServletRequest request) {
        log.warn("Recurso não encontrado: {} no caminho {}", ex.getMessage(), request.getRequestURI());
        ErroRespostaDto erro = new ErroRespostaDto(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(AnamneseVazio.class)
    public ResponseEntity<ErroRespostaDto> handleAnamneseVazio(AnamneseVazio ex, HttpServletRequest request) {
        log.warn("Anamnese vazia: {} no caminho {}", ex.getMessage(), request.getRequestURI());
        ErroRespostaDto erro = new ErroRespostaDto(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler({CpfDuplicadoException.class, EmailDuplicadoException.class})
    public ResponseEntity<ErroRespostaDto> handleConflito(RuntimeException ex, HttpServletRequest request) {
        log.warn("Conflito de dados duplicados: {} no caminho {}", ex.getMessage(), request.getRequestURI());
        ErroRespostaDto erro = new ErroRespostaDto(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }

    @ExceptionHandler(ConflitoHorarioException.class)
    public ResponseEntity<ErroRespostaDto> handleConflitoHorario(ConflitoHorarioException ex, HttpServletRequest request) {
        log.warn("Conflito de horario: {} no caminho {}", ex.getMessage(), request.getRequestURI());
        ErroRespostaDto erro = new ErroRespostaDto(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }

    @ExceptionHandler(AgendamentoInvalidoException.class)
    public ResponseEntity<ErroRespostaDto> handleAgendamentoInvalido(AgendamentoInvalidoException ex, HttpServletRequest request) {
        log.warn("Agendamento invalido: {} no caminho {}", ex.getMessage(), request.getRequestURI());
        ErroRespostaDto erro = new ErroRespostaDto(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            HandlerMethodValidationException.class
    })
    public ResponseEntity<ErroRespostaDto> handleRequisicaoMalFormada(Exception ex, HttpServletRequest request) {
        log.warn("Requisicao mal formada no caminho {}: {}", request.getRequestURI(), ex.getMessage());
        String mensagem;
        if (ex instanceof MissingServletRequestParameterException faltante) {
            mensagem = "Parâmetro obrigatório ausente: " + faltante.getParameterName() + ".";
        } else if (ex instanceof MethodArgumentTypeMismatchException tipoInvalido) {
            mensagem = "Valor inválido para o parâmetro " + tipoInvalido.getName() + ".";
        } else if (ex instanceof HandlerMethodValidationException) {
            mensagem = "Parâmetros inválidos na requisição.";
        } else {
            mensagem = "Corpo da requisição inválido. Verifique os formatos (data AAAA-MM-DD, horário HH:mm:ss).";
        }
        ErroRespostaDto erro = new ErroRespostaDto(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                mensagem,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(FormatoArquivoInvalidoException.class)
    public ResponseEntity<ErroRespostaDto> handleFormatoArquivoInvalido(FormatoArquivoInvalidoException ex, HttpServletRequest request) {
        log.warn("Arquivo inválido: {} no caminho {}", ex.getMessage(), request.getRequestURI());
        ErroRespostaDto erro = new ErroRespostaDto(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroRespostaDto> handleValidacao(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> campos = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            campos.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        log.warn("Falha de validação de campos no caminho {}: {}", request.getRequestURI(), campos);
        ErroRespostaDto erro = new ErroRespostaDto(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Erro de validação nos campos informados.",
                request.getRequestURI(),
                campos
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErroRespostaDto> handleMaxUploadSize(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        log.warn("Tamanho de arquivo excedido no caminho {}: {}", request.getRequestURI(), ex.getMessage());
        ErroRespostaDto erro = new ErroRespostaDto(
                HttpStatus.PAYLOAD_TOO_LARGE.value(),
                HttpStatus.PAYLOAD_TOO_LARGE.getReasonPhrase(),
                "Tamanho do arquivo excede o limite máximo permitido de 10MB.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(erro);
    }

    @ExceptionHandler({BadCredentialsException.class, org.springframework.security.core.AuthenticationException.class})
    public ResponseEntity<ErroRespostaDto> handleBadCredentials(Exception ex, HttpServletRequest request) {
        log.warn("Credenciais inválidas no caminho {}: {}", request.getRequestURI(), ex.getMessage());
        ErroRespostaDto erro = new ErroRespostaDto(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Credenciais inválidas. Verifique seu e-mail e senha.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(erro);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroRespostaDto> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Acesso negado no caminho {}: {}", request.getRequestURI(), ex.getMessage());
        ErroRespostaDto erro = new ErroRespostaDto(
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                "Acesso negado. Você não possui permissão para acessar este recurso.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(erro);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErroRespostaDto> handleResponseStatusException(ResponseStatusException ex, HttpServletRequest request) {
        log.warn("ResponseStatusException no caminho {}: {}", request.getRequestURI(), ex.getReason());
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        String reasonPhrase = (status != null) ? status.getReasonPhrase() : "Error";
        ErroRespostaDto erro = new ErroRespostaDto(
                ex.getStatusCode().value(),
                reasonPhrase,
                ex.getReason() != null ? ex.getReason() : reasonPhrase,
                request.getRequestURI()
        );
        return ResponseEntity.status(ex.getStatusCode()).body(erro);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroRespostaDto> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Erro interno nao tratado no caminho {}: ", request.getRequestURI(), ex);
        ErroRespostaDto erro = new ErroRespostaDto(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Ocorreu um erro interno no servidor. Por favor, tente novamente mais tarde.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
    }
}
