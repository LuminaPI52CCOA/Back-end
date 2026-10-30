package com.lumina.backend.swagger;

import com.lumina.backend.service.Usuario.AutenticacaoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários AutenticacaoProvider")
class AutenticacaoProviderTest {

    @Mock
    private AutenticacaoService autenticacaoService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AutenticacaoProvider provider;

    @Test
    @DisplayName("Deve autenticar com sucesso quando a senha corresponder ao hash")
    void deveAutenticarComSucesso() {
        UserDetails userDetails = new User(
                "ana@lumina.com",
                "$2a$10$hash",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        when(autenticacaoService.loadUserByUsername("ana@lumina.com")).thenReturn(userDetails);
        when(passwordEncoder.matches("senha123", "$2a$10$hash")).thenReturn(true);

        Authentication authRequest = new UsernamePasswordAuthenticationToken("ana@lumina.com", "senha123");
        Authentication resultado = provider.authenticate(authRequest);

        assertNotNull(resultado);
        assertEquals(userDetails, resultado.getPrincipal());
        assertNull(resultado.getCredentials());
        assertEquals(1, resultado.getAuthorities().size());
    }

    @Test
    @DisplayName("Deve lançar BadCredentialsException quando a senha for inválida")
    void deveLancarExcecaoQuandoSenhaInvalida() {
        UserDetails userDetails = new User(
                "ana@lumina.com",
                "$2a$10$hash",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        when(autenticacaoService.loadUserByUsername("ana@lumina.com")).thenReturn(userDetails);
        when(passwordEncoder.matches("senhaErrada", "$2a$10$hash")).thenReturn(false);

        Authentication authRequest = new UsernamePasswordAuthenticationToken("ana@lumina.com", "senhaErrada");

        assertThrows(BadCredentialsException.class, () -> provider.authenticate(authRequest));
    }

    @Test
    @DisplayName("Deve suportar UsernamePasswordAuthenticationToken")
    void deveSuportarUsernamePasswordAuthenticationToken() {
        assertTrue(provider.supports(UsernamePasswordAuthenticationToken.class));
        assertFalse(provider.supports(Authentication.class));
    }
}
