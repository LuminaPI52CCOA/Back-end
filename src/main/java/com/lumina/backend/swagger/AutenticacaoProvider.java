package com.lumina.backend.swagger;

import com.lumina.backend.service.Usuario.AutenticacaoService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AutenticacaoProvider implements AuthenticationProvider {

    private final AutenticacaoService usuarioAutorizacaoService;
    private final PasswordEncoder passwordEncoder;

    public AutenticacaoProvider(AutenticacaoService usuarioAutorizacaoService, PasswordEncoder passwordEncoder) {
        this.usuarioAutorizacaoService = usuarioAutorizacaoService;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        final String username = authentication.getName();
        final String password = authentication.getCredentials().toString();

        // Carrega o usuário do banco de dados pelo e-mail
        // Lança UsernameNotFoundException se o usuário não existir
        UserDetails userDetails = this.usuarioAutorizacaoService.loadUserByUsername(username);

        // Compara a senha digitada com o hash BCrypt armazenado no banco
        if (this.passwordEncoder.matches(password, userDetails.getPassword())) {
            // Credenciais válidas: retorna autenticação com authorities (perfis do usuário)
            return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        } else {
            // Lança exceção genérica para não revelar se o erro foi no e-mail ou na senha
            throw new BadCredentialsException("Usuário ou Senha inválidos");
        }
    }


    @Override
    public boolean supports(final Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
