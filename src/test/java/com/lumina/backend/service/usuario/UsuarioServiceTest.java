package com.lumina.backend.service.usuario;

import com.lumina.backend.dto.usuario.UsuarioRequest;
import com.lumina.backend.dto.usuario.UsuarioTokenDto;
import com.lumina.backend.exception.EntidadeNaoEncontrada;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.repository.UsuarioRepository;
import com.lumina.backend.service.Usuario.UsuarioService;
import com.lumina.backend.swagger.GerenciadorTokenJwt;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    UsuarioRepository usuarioRepository;
    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    GerenciadorTokenJwt gerenciadorTokenJwt;
    @Mock
    PasswordEncoder passwordEncoder;
    @InjectMocks
    UsuarioService usuarioService;

    private Usuario criarUsuario(Long id, String email) {
        return new Usuario(id, "Ana", "123.456.789-00", email, "senha123", 1, "CRO-123", true);
    }

    @Nested
    class TesteListar{

        @Test
        @DisplayName("1.1 Deve retornar todos os usuários")
        void deveRetornarTodosOsUsuarios(){
            List<Usuario>  usuariosEsperados =List.of(new Usuario());
            when(usuarioRepository.findAll()).thenReturn(usuariosEsperados);

            List<Usuario> usuarios = usuarioService.listar();

            Assertions.assertEquals(usuariosEsperados.size(),usuarios.size());
            Mockito.verify(usuarioRepository,Mockito.times(1)).findAll();

        }

        @Test
        @DisplayName("1.2 Deve retornar o usuário sempre que a credencial seja válida")
        void deveRetornarTokenQuandoCredenciaisValidas() {

            Usuario usuario = criarUsuario(1L, "ana@email.com");

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

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(usuarioRepository).findByEmail("ana@email.com");
            verify(gerenciadorTokenJwt).generateToken(authMock);
        }


        @Test
        @DisplayName("1.3 Deve lançar uma excecão caso o email não estja cadastrado")
        void deveLancarExcecaoQuandoEmailNaoCadastrado() {

            Usuario usuario = criarUsuario(1L, "naocadastrado@email.com");

            Authentication authMock = mock(Authentication.class);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authMock);

            when(usuarioRepository.findByEmail("naocadastrado@email.com"))
                    .thenReturn(Optional.empty());

            ResponseStatusException excecao = assertThrows(
                    ResponseStatusException.class,
                    () -> usuarioService.autenticar(usuario)
            );

            assertThat(excecao.getStatusCode().value()).isEqualTo(404);

            verify(gerenciadorTokenJwt, never()).generateToken(any());
        }


        @Test
        @DisplayName("1.4 Deve lançar uma exceção sempre que a senha for inválida")
        void deveLancarExcecaoQuandoSenhaInvalida() {

            Usuario usuario = new Usuario();


            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Credenciais inválidas"));

            assertThrows(
                    BadCredentialsException.class,
                    () -> usuarioService.autenticar(usuario)
            );

            verify(usuarioRepository, never()).findByEmail(any());
            verify(gerenciadorTokenJwt, never()).generateToken(any());
        }

        @Test
        @DisplayName("1.5 Deve deletar o usuário quando ele existir")
        void deveDeletarUsuarioQuandoIdExistir() {

            Usuario usuario = new Usuario();

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(usuarioRepository.logicalDelete(false, 1L)).thenReturn(1); // 1 = linha afetada no banco

            int resultado = usuarioService.deletar(false, 1L);

            assertThat(resultado).isEqualTo(1);
            verify(usuarioRepository).findById(1L);
            verify(usuarioRepository).logicalDelete(false, 1L);
        }

        @Test
        @DisplayName("1.6 Deve lançar exceção quanod tentar deletar um usuário que não existe")
        void deveLancarExcecaoAoDeletarQuandoIdNaoExistir() {

            when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

            EntidadeNaoEncontrada excecao = assertThrows(
                    EntidadeNaoEncontrada.class,
                    () -> usuarioService.deletar(false, 99L)
            );

            assertThat(excecao.getMessage()).contains("99");

            verify(usuarioRepository, never()).logicalDelete(any(), any());
        }

        @Test
        @DisplayName("1.7 Deve atualizar o usuário quando o id existir")
        void deveAtualizarUsuarioQuandoIdExistir() {

            Usuario usuario = criarUsuario(1L, "ana@email.com");
            UsuarioRequest request = new UsuarioRequest();

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(usuarioRepository.atualizarPeloId(request, 1L)).thenReturn(1);

            int resultado = usuarioService.atualizar(request, 1L);

            assertThat(resultado).isEqualTo(1);
            verify(usuarioRepository).findById(1L);
            verify(usuarioRepository).atualizarPeloId(request, 1L);
        }

        @Test
        @DisplayName("1.8 Deve lançar uma exceção quinado atualizar um id que não existe")
        void deveLancarExcecaoAoAtualizarQuandoIdNaoExistir() {
            UsuarioRequest request = new UsuarioRequest();

            when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

            EntidadeNaoEncontrada excecao = assertThrows(
                    EntidadeNaoEncontrada.class,
                    () -> usuarioService.atualizar(request, 99L)
            );

            assertThat(excecao.getMessage()).contains("99");

            verify(usuarioRepository, never()).atualizarPeloId(any(), any());
        }

        @Test
        @DisplayName("1.9 Deve salvar e retornar o usuário")
        void deveSalvarUsuario() {
            Usuario usuario = criarUsuario(1L, "ana@email.com");
            when(usuarioRepository.save(usuario)).thenReturn(usuario);

            Usuario resultado = usuarioService.salvar(usuario);

            assertThat(resultado).isEqualTo(usuario);
            verify(usuarioRepository).save(usuario);
        }

        @Test
        @DisplayName("1.10 Deve retornar o usuário quando o id existir")
        void deveBuscarUsuarioPorId() {
            Usuario usuario = criarUsuario(1L, "ana@email.com");
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

            Optional<Usuario> resultado = usuarioService.buscarPorId(1L);

            assertThat(resultado).isPresent();
            assertThat(resultado.get().getEmail()).isEqualTo("ana@email.com");
            verify(usuarioRepository).findById(1L);
        }

        @Test
        @DisplayName("1.11 Deve lançar exceção quando o id não existir")
        void deveLancarExcecaoQuandoBuscarIdInexistente() {
            when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

            EntidadeNaoEncontrada excecao = assertThrows(
                    EntidadeNaoEncontrada.class,
                    () -> usuarioService.buscarPorId(99L)
            );

            assertThat(excecao.getMessage()).contains("99");
            verify(usuarioRepository).findById(99L);
        }

    }
}
