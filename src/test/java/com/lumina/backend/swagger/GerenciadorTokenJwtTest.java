package com.lumina.backend.swagger;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes unitários GerenciadorTokenJwt")
class GerenciadorTokenJwtTest {

    private static final String SECRET =
            "dGVzdGUtc2VjcmV0LWx1bWluYS1jaGF2ZS1jb20tdGFtYW5oby1zdWZpY2llbnRlLXBhcmEtaG1hYw==";

    private GerenciadorTokenJwt gerenciador;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        gerenciador = new GerenciadorTokenJwt();
        ReflectionTestUtils.setField(gerenciador, "secret", SECRET);
        ReflectionTestUtils.setField(gerenciador, "jwtTokenValidity", 3600L);
        authentication = new UsernamePasswordAuthenticationToken(
                "ana@lumina.com", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("Deve gerar token contendo username e authorities")
    void deveGerarTokenComClaims() {
        String token = gerenciador.generateToken(authentication);

        assertNotNull(token);
        assertEquals("ana@lumina.com", gerenciador.getUsernameFromToken(token));
        assertEquals("ROLE_ADMIN", gerenciador.getClaimForToken(token, c -> c.get("authorities", String.class)));
        assertTrue(gerenciador.getExpirationDateFromToken(token).after(new Date()));
    }

    @Test
    @DisplayName("Deve validar token para o usuário correto")
    void deveValidarToken() {
        String token = gerenciador.generateToken(authentication);
        UserDetails user = User.withUsername("ana@lumina.com").password("x").authorities("ROLE_ADMIN").build();

        assertTrue(gerenciador.validateToken(token, user));
    }

    @Test
    @DisplayName("Deve invalidar token para usuário diferente")
    void deveInvalidarTokenUsuarioDiferente() {
        String token = gerenciador.generateToken(authentication);
        UserDetails outro = User.withUsername("outro@lumina.com").password("x").authorities("ROLE_ADMIN").build();

        assertFalse(gerenciador.validateToken(token, outro));
    }

    @Test
    @DisplayName("Deve lançar exceção ao ler token adulterado")
    void deveLancarExcecaoTokenAdulterado() {
        GerenciadorTokenJwt outro = new GerenciadorTokenJwt();
        ReflectionTestUtils.setField(outro, "secret",
                "b3V0cmEtY2hhdmUtc2VjcmV0YS1kaWZlcmVudGUtcGFyYS10ZXN0YXItYXNzaW5hdHVyYS1pbnZhbGlkYQ==");
        ReflectionTestUtils.setField(outro, "jwtTokenValidity", 3600L);
        String token = outro.generateToken(authentication);

        assertThrows(Exception.class, () -> gerenciador.getClaimForToken(token, Claims::getSubject));
    }

    @Test
    @DisplayName("Deve lançar IllegalStateException quando a chave secreta não estiver configurada")
    void deveLancarExcecaoSemSecret() {
        ReflectionTestUtils.setField(gerenciador, "secret", "");

        assertThrows(IllegalStateException.class, () -> gerenciador.generateToken(authentication));
    }
}
