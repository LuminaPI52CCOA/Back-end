package com.lumina.backend.controller.usuario;

import com.lumina.backend.controller.UsuarioController;
import com.lumina.backend.domain.usuario.Usuario;
import com.lumina.backend.domain.usuario.UsuarioCommand;
import com.lumina.backend.domain.usuario.UsuarioId;
import com.lumina.backend.dto.usuario.UsuarioLoginDto;
import com.lumina.backend.dto.usuario.UsuarioRequest;
import com.lumina.backend.dto.usuario.UsuarioResponse;
import com.lumina.backend.dto.usuario.UsuarioSessaoDto;
import com.lumina.backend.dto.usuario.UsuarioTokenDto;
import com.lumina.backend.exception.EntidadeNaoEncontrada;
import com.lumina.backend.service.Usuario.UsuarioService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HttpServletResponse httpServletResponse;

    @InjectMocks
    private UsuarioController usuarioController;

    private Usuario criarUsuarioDomain(Long id, String nome, String email) {
        UsuarioCommand command = new UsuarioCommand(nome, "12345678901", email, "encodedPassword", 2, "SP-CD-12345", true);
        return Usuario.reconstruir(UsuarioId.of(id), command);
    }

    @Nested
    @DisplayName("1. Teste de cadastro de usuário")
    class CadastrarUsuariosTest {

        @Test
        @DisplayName("1.1 Deve cadastrar usuário com sucesso e retornar status 201")
        void deveCadastrarUsuarioComSucesso() {
            UsuarioRequest request = new UsuarioRequest();
            request.setIdUsuario(1L);
            request.setNome("Ana Maria Souza");
            request.setCpf("12345678901");
            request.setEmail("ana.souza@lumina.com");
            request.setSenha("Senha@123");
            request.setFkPerfil(2);
            request.setCro("SP-CD-12345");
            request.setAtivo(true);

            Usuario usuarioSalvo = criarUsuarioDomain(1L, "Ana Maria Souza", "ana.souza@lumina.com");

            Mockito.when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            Mockito.when(usuarioService.salvar(any(Usuario.class))).thenReturn(usuarioSalvo);

            ResponseEntity<UsuarioResponse> response = usuarioController.cadastrarUsuarios(request);

            Assertions.assertEquals(201, response.getStatusCodeValue());
            Assertions.assertNotNull(response.getBody());
            Assertions.assertEquals("Ana Maria Souza", response.getBody().getNome());

            Mockito.verify(passwordEncoder, Mockito.times(1)).encode(anyString());
            Mockito.verify(usuarioService, Mockito.times(1)).salvar(any(Usuario.class));
        }
    }

    @Nested
    @DisplayName("2. Teste de login")
    class LoginTest {

        @Test
        @DisplayName("2.1 Deve realizar login com sucesso e retornar status 200")
        void deveRealizarLoginComSucesso() {
            UsuarioLoginDto loginDto = new UsuarioLoginDto();
            loginDto.setEmail("ana.souza@lumina.com");
            loginDto.setSenha("Senha@123");

            UsuarioTokenDto tokenDto = new UsuarioTokenDto();
            tokenDto.setUserId(1L);
            tokenDto.setNome("Ana Maria Souza");
            tokenDto.setEmail("ana.souza@lumina.com");
            tokenDto.setToken("jwt-token-123");

            Mockito.when(usuarioService.autenticar(any(com.lumina.backend.model.Usuario.class))).thenReturn(tokenDto);

            ResponseEntity<UsuarioSessaoDto> response = usuarioController.login(loginDto, httpServletResponse);

            Assertions.assertEquals(200, response.getStatusCodeValue());
            Assertions.assertNotNull(response.getBody());
            Assertions.assertEquals("Ana Maria Souza", response.getBody().getNome());
            Assertions.assertEquals("ana.souza@lumina.com", response.getBody().getEmail());

            Mockito.verify(usuarioService, Mockito.times(1)).autenticar(any(com.lumina.backend.model.Usuario.class));
        }
    }

    @Nested
    @DisplayName("3. Teste de logout")
    class LogoutTest {

        @Test
        @DisplayName("3.1 Deve realizar logout com sucesso e retornar status 204")
        void deveRealizarLogoutComSucesso() {
            ResponseEntity<Void> response = usuarioController.logout(httpServletResponse);

            Assertions.assertEquals(204, response.getStatusCodeValue());
        }
    }

    @Nested
    @DisplayName("4. Teste de listagem de usuários")
    class ListarUsuariosTest {

        @Test
        @DisplayName("4.1 Deve retornar lista de usuários com status 200")
        void deveRetornarListaDeUsuariosComSucesso() {
            Usuario usuario1 = criarUsuarioDomain(1L, "Ana Maria Souza", "ana.souza@lumina.com");
            Usuario usuario2 = criarUsuarioDomain(2L, "Carlos Silva", "carlos.silva@lumina.com");

            List<Usuario> usuarios = List.of(usuario1, usuario2);

            Mockito.when(usuarioService.listar()).thenReturn(usuarios);

            ResponseEntity<List<UsuarioResponse>> response = usuarioController.listarUsuarios();

            Assertions.assertEquals(200, response.getStatusCodeValue());
            Assertions.assertNotNull(response.getBody());
            Assertions.assertFalse(response.getBody().isEmpty());
            Assertions.assertEquals(2, response.getBody().size());

            Mockito.verify(usuarioService, Mockito.times(1)).listar();
        }

        @Test
        @DisplayName("4.2 Deve retornar status 204 quando não houver usuários cadastrados")
        void deveRetornarStatus204QuandoListaVazia() {
            Mockito.when(usuarioService.listar()).thenReturn(Collections.emptyList());

            ResponseEntity<List<UsuarioResponse>> response = usuarioController.listarUsuarios();

            Assertions.assertEquals(204, response.getStatusCodeValue());
            Assertions.assertNull(response.getBody());

            Mockito.verify(usuarioService, Mockito.times(1)).listar();
        }
    }

    @Nested
    @DisplayName("5. Teste de busca por ID")
    class BuscarPorIdTest {

        @Test
        @DisplayName("5.1 Deve retornar usuário encontrado com status 200")
        void deveRetornarUsuarioEncontradoComSucesso() {
            Usuario usuario = criarUsuarioDomain(1L, "Ana Maria Souza", "ana.souza@lumina.com");

            Mockito.when(usuarioService.buscarPorId(1L)).thenReturn(Optional.of(usuario));

            ResponseEntity<UsuarioResponse> response = usuarioController.buscarPorId(1L);

            Assertions.assertEquals(200, response.getStatusCodeValue());
            Assertions.assertNotNull(response.getBody());
            Assertions.assertEquals("Ana Maria Souza", response.getBody().getNome());

            Mockito.verify(usuarioService, Mockito.times(1)).buscarPorId(1L);
        }

        @Test
        @DisplayName("5.2 Deve retornar 404 quando usuário não existir")
        void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
            Mockito.when(usuarioService.buscarPorId(99L)).thenReturn(Optional.empty());

            ResponseEntity<UsuarioResponse> response = usuarioController.buscarPorId(99L);

            Assertions.assertEquals(404, response.getStatusCodeValue());
            Mockito.verify(usuarioService, Mockito.times(1)).buscarPorId(99L);
        }
    }

    @Nested
    @DisplayName("6. Teste de deleção de usuário")
    class DeletarUsuarioTest {

        @Test
        @DisplayName("6.1 Deve deletar usuário com sucesso e retornar status 204")
        void deveDeletarUsuarioComSucesso() {
            Mockito.doNothing().when(usuarioService).deletar(false, 1L);

            ResponseEntity<Void> response = usuarioController.deletarUsuario(1L);

            Assertions.assertEquals(204, response.getStatusCodeValue());

            Mockito.verify(usuarioService, Mockito.times(1)).deletar(false, 1L);
        }

        @Test
        @DisplayName("6.2 Deve lançar EntidadeNaoEncontrada ao tentar deletar usuário inexistente")
        void deveLancarExcecaoAoDeletarUsuarioInexistente() {
            Mockito.doThrow(new EntidadeNaoEncontrada("Usuario de id: 99 não encontrado"))
                    .when(usuarioService).deletar(false, 99L);

            EntidadeNaoEncontrada exception = assertThrows(
                    EntidadeNaoEncontrada.class,
                    () -> usuarioController.deletarUsuario(99L)
            );

            Assertions.assertEquals("Usuario de id: 99 não encontrado", exception.getMessage());
            Mockito.verify(usuarioService, Mockito.times(1)).deletar(false, 99L);
        }
    }

    @Nested
    @DisplayName("7. Teste de atualização de usuário")
    class AtualizarTest {

        @Test
        @DisplayName("7.1 Deve atualizar usuário com sucesso e retornar status 200")
        void deveAtualizarUsuarioComSucesso() {
            UsuarioRequest request = new UsuarioRequest();
            request.setNome("Ana Maria Souza Atualizado");
            request.setEmail("ana.souza.atualizado@lumina.com");

            Usuario usuarioAtualizado = criarUsuarioDomain(1L, "Ana Maria Souza Atualizado", "ana.souza.atualizado@lumina.com");

            Mockito.when(usuarioService.atualizar(any(UsuarioRequest.class), anyLong())).thenReturn(usuarioAtualizado);

            ResponseEntity<UsuarioResponse> response = usuarioController.atualizar(request, 1L);

            Assertions.assertEquals(200, response.getStatusCodeValue());
            Assertions.assertNotNull(response.getBody());
            Assertions.assertEquals("Ana Maria Souza Atualizado", response.getBody().getNome());

            Mockito.verify(usuarioService, Mockito.times(1)).atualizar(any(UsuarioRequest.class), anyLong());
        }

        @Test
        @DisplayName("7.2 Deve lançar EntidadeNaoEncontrada ao tentar atualizar usuário inexistente")
        void deveLancarExcecaoAoAtualizarUsuarioInexistente() {
            UsuarioRequest request = new UsuarioRequest();
            request.setNome("Teste");

            Mockito.when(usuarioService.atualizar(any(UsuarioRequest.class), anyLong()))
                    .thenThrow(new EntidadeNaoEncontrada("Usuario de id: 99 não encontrado"));

            EntidadeNaoEncontrada exception = assertThrows(
                    EntidadeNaoEncontrada.class,
                    () -> usuarioController.atualizar(request, 99L)
            );

            Assertions.assertEquals("Usuario de id: 99 não encontrado", exception.getMessage());
            Mockito.verify(usuarioService, Mockito.times(1)).atualizar(any(UsuarioRequest.class), anyLong());
        }
    }
}
