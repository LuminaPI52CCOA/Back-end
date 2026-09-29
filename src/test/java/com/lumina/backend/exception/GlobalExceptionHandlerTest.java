package com.lumina.backend.exception;

import com.lumina.backend.exception.dto.ErroRespostaDto;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Test
    @DisplayName("Deve tratar EntidadeNaoEncontrada com status 404")
    void deveTratarEntidadeNaoEncontrada() {
        when(request.getRequestURI()).thenReturn("/clientes/99");

        ResponseEntity<ErroRespostaDto> resposta = handler.handleEntidadeNaoEncontrada(
                new EntidadeNaoEncontrada("Cliente não encontrado!"),
                request
        );

        assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(404, resposta.getBody().getStatus());
        assertEquals("Cliente não encontrado!", resposta.getBody().getMensagem());
        assertEquals("/clientes/99", resposta.getBody().getCaminho());
    }

    @Test
    @DisplayName("Deve tratar CpfDuplicadoException com status 409")
    void deveTratarCpfDuplicado() {
        when(request.getRequestURI()).thenReturn("/clientes");

        ResponseEntity<ErroRespostaDto> resposta = handler.handleConflito(
                new CpfDuplicadoException("CPF já existe"),
                request
        );

        assertEquals(HttpStatus.CONFLICT, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(409, resposta.getBody().getStatus());
        assertEquals("CPF já existe", resposta.getBody().getMensagem());
    }

    @Test
    @DisplayName("Deve tratar AccessDeniedException com status 403")
    void deveTratarAccessDenied() {
        when(request.getRequestURI()).thenReturn("/usuarios");

        ResponseEntity<ErroRespostaDto> resposta = handler.handleAccessDenied(
                new AccessDeniedException("Acesso negado"),
                request
        );

        assertEquals(HttpStatus.FORBIDDEN, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(403, resposta.getBody().getStatus());
    }

    @Test
    @DisplayName("Deve tratar Exception generica com status 500 sem expor stacktrace")
    void deveTratarExceptionGenerica() {
        when(request.getRequestURI()).thenReturn("/test");

        ResponseEntity<ErroRespostaDto> resposta = handler.handleGenericException(
                new NullPointerException("Internal null reference"),
                request
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(500, resposta.getBody().getStatus());
        assertEquals("Ocorreu um erro interno no servidor. Por favor, tente novamente mais tarde.", resposta.getBody().getMensagem());
    }
}
