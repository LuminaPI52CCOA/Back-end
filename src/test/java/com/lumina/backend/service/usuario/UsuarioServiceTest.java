package com.lumina.backend.service.usuario;

import com.lumina.backend.dto.usuario.UsuarioRequest;
import com.lumina.backend.dto.usuario.UsuarioTokenDto;
import com.lumina.backend.exception.EntidadeNaoEncontradaException;
import com.lumina.backend.model.Perfil;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.repository.UsuarioRepository;
import com.lumina.backend.security.GerenciadorTokenJwt;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @AfterEach
    void limparContextoDeSeguranca() {
        SecurityContextHolder.clearContext();
    }

    private Usuario criarUsuarioModel(Long id, String email) {
        return new Usuario(id, "Ana", "123.456.789-00", email, "senha123",
                new Perfil(1, "ADMIN"), "CRO-123", true);
    }

    @Nested
    class TesteUsuarioService {

        @Test
        @DisplayName("1.1 Deve retornar todos os usuários")
        void deveRetornarTodosOsUsuarios() {
            when(usuarioRepository.findAll()).thenReturn(List.of(criarUsuarioModel(1L, "ana@email.com")));

            List<Usuario> usuarios = usuarioService.listar();

            assertThat(usuarios).hasSize(1);
            verify(usuarioRepository).findAll();
        }

        @Test
        @DisplayName("1.1.1 Deve retornar página de usuários filtrada por perfil, ordenada por nome")
        void deveRetornarPaginaDeUsuariosPorPerfil() {
            Pageable pageable = PageRequest.of(0, 6, Sort.by(Sort.Direction.ASC, "nome"));
            Page<Usuario> pagina = new PageImpl<>(List.of(criarUsuarioModel(1L, "ana@email.com")), pageable, 1);
            when(usuarioRepository.buscarComFiltros(2, null, pageable)).thenReturn(pagina);

            Page<Usuario> resultado = usuarioService.listarPaginado(2, null, PageRequest.of(0, 6));

            assertThat(resultado.getTotalElements()).isEqualTo(1);
            verify(usuarioRepository).buscarComFiltros(2, null, pageable);
        }

        @Test
        @DisplayName("1.2 Deve retornar o usuário sempre que a credencial seja válida")
        void deveRetornarTokenQuandoCredenciaisValidas() {
            Usuario usuario = criarUsuarioModel(1L, "ana@email.com");
            Authentication authMock = mock(Authentication.class);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authMock);
            when(usuarioRepository.findByEmail("ana@email.com")).thenReturn(Optional.of(usuario));
            when(gerenciadorTokenJwt.generateToken(authMock)).thenReturn("token.jwt.mockado");

            UsuarioTokenDto resultado = usuarioService.autenticar(usuario);

            assertThat(resultado).isNotNull();
            assertThat(resultado.token()).isEqualTo("token.jwt.mockado");
            assertThat(resultado.email()).isEqualTo("ana@email.com");

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(usuarioRepository).findByEmail("ana@email.com");
            verify(gerenciadorTokenJwt).generateToken(authMock);
        }

        @Test
        @DisplayName("1.3 Deve lançar uma exceção caso o email não esteja cadastrado")
        void deveLancarExcecaoQuandoEmailNaoCadastrado() {
            Usuario usuario = criarUsuarioModel(1L, "naocadastrado@email.com");
            Authentication authMock = mock(Authentication.class);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authMock);
            when(usuarioRepository.findByEmail("naocadastrado@email.com")).thenReturn(Optional.empty());

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
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Credenciais inválidas"));

            assertThrows(
                    BadCredentialsException.class,
                    () -> usuarioService.autenticar(new Usuario())
            );

            verify(usuarioRepository, never()).findByEmail(any());
            verify(gerenciadorTokenJwt, never()).generateToken(any());
        }

        @Test
        @DisplayName("1.5 Deve deletar logicamente o usuário quando ele existir")
        void deveDeletarUsuarioQuandoIdExistir() {
            when(usuarioRepository.findById(1L))
                    .thenReturn(Optional.of(criarUsuarioModel(1L, "ana@email.com")));

            usuarioService.deletar(false, 1L);

            verify(usuarioRepository).logicalDelete(false, 1L);
        }

        @Test
        @DisplayName("1.6 Deve lançar exceção quando tentar deletar um usuário que não existe")
        void deveLancarExcecaoAoDeletarQuandoIdNaoExistir() {
            when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

            EntidadeNaoEncontradaException excecao = assertThrows(
                    EntidadeNaoEncontradaException.class,
                    () -> usuarioService.deletar(false, 99L)
            );

            assertThat(excecao.getMessage()).contains("99");
            verify(usuarioRepository, never()).logicalDelete(any(), any());
        }

        @Test
        @DisplayName("1.7 Deve atualizar o usuário mantendo os campos não informados")
        void deveAtualizarUsuarioQuandoIdExistir() {
            when(usuarioRepository.findById(1L))
                    .thenReturn(Optional.of(criarUsuarioModel(1L, "ana@email.com")));
            when(usuarioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            UsuarioRequest request = UsuarioRequest.builder()
                    .nome("Ana Maria")
                    .build();

            Usuario resultado = usuarioService.atualizar(request, 1L);

            assertThat(resultado.getNome()).isEqualTo("Ana Maria");
            assertThat(resultado.getEmail()).isEqualTo("ana@email.com");
            assertThat(resultado.getSenha()).isEqualTo("senha123");
            verify(passwordEncoder, never()).encode(any());
        }

        @Test
        @DisplayName("1.7.1 Deve aplicar hash quando uma nova senha for informada")
        void deveCodificarNovaSenhaAoAtualizar() {
            when(usuarioRepository.findById(1L))
                    .thenReturn(Optional.of(criarUsuarioModel(1L, "ana@email.com")));
            when(usuarioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(passwordEncoder.encode("nova")).thenReturn("hash-da-nova");
            UsuarioRequest request = UsuarioRequest.builder()
                    .senha("nova")
                    .build();

            Usuario resultado = usuarioService.atualizar(request, 1L);

            assertThat(resultado.getSenha()).isEqualTo("hash-da-nova");
        }

        @Test
        @DisplayName("1.8 Deve lançar uma exceção quando atualizar um id que não existe")
        void deveLancarExcecaoAoAtualizarQuandoIdNaoExistir() {
            when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

            EntidadeNaoEncontradaException excecao = assertThrows(
                    EntidadeNaoEncontradaException.class,
                    () -> usuarioService.atualizar(UsuarioRequest.builder().build(), 99L)
            );

            assertThat(excecao.getMessage()).contains("99");
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("1.9 Deve salvar e retornar o usuário")
        void deveSalvarUsuario() {
            Usuario usuario = criarUsuarioModel(1L, "ana@email.com");
            when(usuarioRepository.save(usuario)).thenReturn(usuario);

            Usuario resultado = usuarioService.salvar(usuario);

            assertThat(resultado).isSameAs(usuario);
        }

        @Test
        @DisplayName("1.10 Deve retornar o usuário quando o id existir")
        void deveBuscarUsuarioPorId() {
            when(usuarioRepository.findById(1L))
                    .thenReturn(Optional.of(criarUsuarioModel(1L, "ana@email.com")));

            Optional<Usuario> resultado = usuarioService.buscarPorId(1L);

            assertThat(resultado).isPresent();
            assertThat(resultado.get().getEmail()).isEqualTo("ana@email.com");
        }

        @Test
        @DisplayName("1.11 Deve retornar optional vazio quando o id não existir")
        void deveRetornarVazioQuandoBuscarIdInexistente() {
            when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

            assertThat(usuarioService.buscarPorId(99L)).isEmpty();
        }
    }
}
