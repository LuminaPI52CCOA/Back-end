package com.lumina.backend.service.usuario;

import com.lumina.backend.dto.usuario.UsuarioDetalhesDto;
import com.lumina.backend.model.Perfil;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.repository.UsuarioRepository;
import com.lumina.backend.service.Usuario.AutenticacaoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários AutenticacaoService")
class AutenticacaoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private AutenticacaoService service;

    @Test
    @DisplayName("Deve retornar UserDetails quando o usuário existir")
    void deveRetornarUserDetailsQuandoUsuarioExistir() {
        Perfil perfil = new Perfil();
        perfil.setIdPerfil(2);
        Usuario usuario = new Usuario();
        usuario.setNome("Ana");
        usuario.setEmail("ana@lumina.com");
        usuario.setSenha("hash");
        usuario.setPerfil(perfil);

        when(usuarioRepository.findByEmail("ana@lumina.com")).thenReturn(Optional.of(usuario));

        UserDetails details = service.loadUserByUsername("ana@lumina.com");

        assertInstanceOf(UsuarioDetalhesDto.class, details);
        assertEquals("ana@lumina.com", details.getUsername());
        assertEquals("hash", details.getPassword());
        assertEquals("ROLE_DENTISTA", details.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException quando o usuário não existir")
    void deveLancarExcecaoQuandoUsuarioNaoExistir() {
        when(usuarioRepository.findByEmail("x@x.com")).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("x@x.com"));
        assertTrue(ex.getMessage().contains("x@x.com"));
    }
}
