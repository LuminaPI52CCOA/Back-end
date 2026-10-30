package com.lumina.backend.swagger;

import com.lumina.backend.controller.UsuarioController;
import com.lumina.backend.service.Usuario.AutenticacaoService;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários AutenticacaoFilter")
class AutenticacaoFilterTest {

    @Mock
    private AutenticacaoService autenticacaoService;

    @Mock
    private GerenciadorTokenJwt jwtTokenManager;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private AutenticacaoFilter filter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve autenticar usuário via cabeçalho Authorization Bearer")
    void deveAutenticarViaAuthorizationHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/clientes");
        request.addHeader("Authorization", "Bearer token-valido");
        MockHttpServletResponse response = new MockHttpServletResponse();

        UserDetails userDetails = new User("ana@lumina.com", "x", Collections.emptyList());

        when(jwtTokenManager.getUsernameFromToken("token-valido")).thenReturn("ana@lumina.com");
        when(autenticacaoService.loadUserByUsername("ana@lumina.com")).thenReturn(userDetails);
        when(jwtTokenManager.validateToken("token-valido", userDetails)).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("ana@lumina.com", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve autenticar usuário via cookie HttpOnly")
    void deveAutenticarViaCookie() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/clientes");
        request.setCookies(new Cookie(UsuarioController.COOKIE_NOME, "token-cookie"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        UserDetails userDetails = new User("dentista@lumina.com", "x", Collections.emptyList());

        when(jwtTokenManager.getUsernameFromToken("token-cookie")).thenReturn("dentista@lumina.com");
        when(autenticacaoService.loadUserByUsername("dentista@lumina.com")).thenReturn(userDetails);
        when(jwtTokenManager.validateToken("token-cookie", userDetails)).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("dentista@lumina.com", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve ignorar token malformado e prosseguir na cadeia de filtros sem autenticar")
    void deveIgnorarTokenMalformado() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/clientes");
        request.addHeader("Authorization", "Bearer token-invalido");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtTokenManager.getUsernameFromToken("token-invalido"))
                .thenThrow(new MalformedJwtException("Formato inválido"));

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Não deve filtrar rotas iniciadas por /consultas/dentista/")
    void naoDeveFiltrarRotasDeConsultaPorDentista() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/consultas/dentista/1");
        assertTrue(filter.shouldNotFilter(request));
    }
}
