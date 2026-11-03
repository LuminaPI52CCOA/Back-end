package com.lumina.backend.swagger;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.LockedException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("Testes unitários AutenticacaoEntryPoint")
class AutenticacaoEntryPointTest {

    private final AutenticacaoEntryPoint entryPoint = new AutenticacaoEntryPoint();

    @Test
    @DisplayName("Deve retornar 401 quando exceção for BadCredentialsException")
    void deveRetornar401ParaBadCredentials() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        entryPoint.commence(request, response, new BadCredentialsException("Credenciais inválidas"));

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    @DisplayName("Deve retornar 401 quando exceção for InsufficientAuthenticationException")
    void deveRetornar401ParaInsufficientAuthentication() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        entryPoint.commence(request, response, new InsufficientAuthenticationException("Sem token"));

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    @DisplayName("Deve retornar 403 para outras exceções de autenticação")
    void deveRetornar403ParaOutrasExcecoes() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        entryPoint.commence(request, response, new LockedException("Conta bloqueada"));

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
    }
}
