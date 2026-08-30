package com.lumina.backend;

import com.lumina.backend.domain.usuario.Usuario;
import com.lumina.backend.domain.usuario.UsuarioCommand;
import com.lumina.backend.domain.usuario.UsuarioId;
import com.lumina.backend.dto.usuario.UsuarioRequest;
import com.lumina.backend.dto.usuario.UsuarioTokenDto;
import com.lumina.backend.exception.EntidadeNaoEncontrada;
import com.lumina.backend.repository.UsuarioRepository;
import com.lumina.backend.service.Usuario.UsuarioService;
import com.lumina.backend.swagger.GerenciadorTokenJwt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTeste {

    @Mock
    UsuarioRepository usuarioRepository;
    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    GerenciadorTokenJwt gerenciadorTokenJwt;
    @InjectMocks
    UsuarioService usuarioService;

    private com.lumina.backend.model.Usuario criarUsuarioModel(Long id, String email) {
        return new com.lumina.backend.model.Usuario(id, "Ana", "123.456.789-00", email, "senha123", 1, "CRO-123", true);
    }

    private Usuario criarUsuarioDomain(Long id, String email) {
        UsuarioCommand command = new UsuarioCommand("Ana", "123.456.789-00", email, "senha123", 1, "CRO-123", true);
        return Usuario.reconstruir(UsuarioId.of(id), command);
    }

    @Nested
    class TesteListar {

        @Test
        @DisplayName("1.1 Deve retornar todos os usuários")
        void deveRetornarTodosOsUsuarios() {
            com.lumina.backend.model.Usuario model = criarUsuarioModel(1L, "ana@email.com");
            when(usuarioRepository.findAll()).thenReturn(List.of(model));

            List<Usuario> usuarios = usuarioService.listar();

            Assertions.assertEquals(1, usuarios.size());
            Mockito.verify(usuarioRepository, Mockito.times(1)).findAll();
        }

        @Test
        @DisplayName("1.2 Deve retornar o usuário sempre que a credencial seja válida")
        void deveRetornarTokenQuandoCredenciaisValidas() {
            com.lumina.backend.model.Usuario usuario = criarUsuarioModel(1L, "ana@email.com");
            Authentication authMock = mock(Authentication.class);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authMock);
            when(usuarioRepository.findByEmail("ana@email.com"))
                    .thenReturn(Optional.of(usuario));
            when(gerenciadorTokenJwt.generateToken(authMock))
                    .thenReturn("token.jwt.mockado");

            UsuarioTokenDto resultado = usuarioService.autenticar(usuario);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getToken()).isEqualTo("token.jwt.mockado");
            assertThat(resultado.getEmail()).isEqualTo("ana@email.com");
        }

        @Test
        @DisplayName("1.3 Deve lançar uma excecão caso o email não estja cadastrado")
        void deveLancarExcecaoQuandoEmailNaoCadastrado() {
            com.lumina.backend.model.Usuario usuario = criarUsuarioModel(1L, "naocadastrado@email.com");
            Authentication authMock = mock(Authentication.class);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authMock);
            when(usuarioRepository.findByEmail("naocadastrado@email.com"))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ResponseStatusException.class,
                    () -> usuarioService.autenticar(usuario)
            );
        }

        @Test
        @DisplayName("1.4 Deve lançar uma exceção sempre que a senha for inválida")
        void deveLancarExcecaoQuandoSenhaInvalida() {
            com.lumina.backend.model.Usuario usuario = new com.lumina.backend.model.Usuario();

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Credenciais inválidas"));

            assertThrows(
                    BadCredentialsException.class,
                    () -> usuarioService.autenticar(usuario)
            );
        }

        @Test
        @DisplayName("1.5 Deve deletar o usuário quando ele existir")
        void deveDeletarUsuarioQuandoIdExistir() {
            com.lumina.backend.model.Usuario usuario = criarUsuarioModel(1L, "ana@email.com");

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(usuarioRepository.logicalDelete(false, 1L)).thenReturn(1);

            usuarioService.deletar(false, 1L);

            verify(usuarioRepository).findById(1L);
            verify(usuarioRepository).logicalDelete(false, 1L);
        }

        @Test
        @DisplayName("1.6 Deve lançar exceção quando tentar deletar um usuário que não existe")
        void deveLancarExcecaoAoDeletarQuandoIdNaoExistir() {
            when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

            EntidadeNaoEncontrada excecao = assertThrows(
                    EntidadeNaoEncontrada.class,
                    () -> usuarioService.deletar(false, 99L)
            );

            assertThat(excecao.getMessage()).contains("99");
        }

        @Test
        @DisplayName("1.7 Deve atualizar o usuário quando o id existir")
        void deveAtualizarUsuarioQuandoIdExistir() {
            com.lumina.backend.model.Usuario usuario = criarUsuarioModel(1L, "ana@email.com");
            UsuarioRequest request = new UsuarioRequest();
            request.setNome("Ana Maria");

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(usuarioRepository.save(any(com.lumina.backend.model.Usuario.class))).thenReturn(usuario);

            Usuario resultado = usuarioService.atualizar(request, 1L);

            assertThat(resultado).isNotNull();
            verify(usuarioRepository).findById(1L);
        }

        @Test
        @DisplayName("1.8 Deve lançar uma exceção quando atualizar um id que não existe")
        void deveLancarExcecaoAoAtualizarQuandoIdNaoExistir() {
            UsuarioRequest request = new UsuarioRequest();

            when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(
                    EntidadeNaoEncontrada.class,
                    () -> usuarioService.atualizar(request, 99L)
            );
        }
    }
}
