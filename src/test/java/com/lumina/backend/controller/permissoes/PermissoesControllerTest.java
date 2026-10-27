package com.lumina.backend.controller.permissoes;

import com.lumina.backend.controller.PermissoesController;
import com.lumina.backend.domain.usuario.Usuario;
import com.lumina.backend.domain.usuario.UsuarioCommand;
import com.lumina.backend.domain.usuario.UsuarioId;
import com.lumina.backend.dto.permissao.PermissaoRequest;
import com.lumina.backend.dto.permissao.PermissaoResponse;
import com.lumina.backend.exception.EntidadeNaoEncontrada;
import com.lumina.backend.service.Usuario.UsuarioService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários PermissoesController")
class PermissoesControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private PermissoesController permissoesController;

    @BeforeEach
    void autenticarComoAdmin() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin@lumina.com",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );
    }

    @AfterEach
    void limparAutenticacao() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComoRecepcionista() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "recepcao@lumina.com",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_RECEPCIONISTA"))
                )
        );
    }

    private void autenticarComoDentista() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "dentista@lumina.com",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_DENTISTA"))
                )
        );
    }

    private Usuario criarDentistaDomain(Long id, String nome, String email, Integer fkPerfil, Boolean ativo) {
        UsuarioCommand command = new UsuarioCommand(
                nome,
                "12345678901",
                email,
                "encodedPassword",
                fkPerfil,
                "SP-CD-12345",
                ativo
        );
        return Usuario.reconstruir(UsuarioId.of(id), command);
    }

    @Test
    @DisplayName("Deve instanciar PermissoesController")
    void deveInstanciarPermissoesController() {
        PermissoesController controller = new PermissoesController();
        assertNotNull(controller);
    }

    @Nested
    @DisplayName("1. Teste de listagem de permissões")
    class ListarPermissoesTest {

        @Test
        @DisplayName("1.1 Deve retornar lista de permissões com status 200 para ADMIN")
        void deveRetornarListaDePermissoesComSucessoParaAdmin() {
            Usuario u1 = criarDentistaDomain(1L, "Dra. Ana Silva", "ana@lumina.com", 2, true);
            Usuario u2 = criarDentistaDomain(2L, "Dr. Carlos Oliveira", "carlos@lumina.com", 1, true);

            Mockito.when(usuarioService.listar()).thenReturn(List.of(u1, u2));

            ResponseEntity<List<PermissaoResponse>> response = permissoesController.listarPermissoes(null, null);

            Assertions.assertEquals(200, response.getStatusCodeValue());
            Assertions.assertNotNull(response.getBody());
            Assertions.assertEquals(2, response.getBody().size());
            Assertions.assertEquals("restrito", response.getBody().get(0).getNivelAcesso());
            Assertions.assertEquals("todos", response.getBody().get(1).getNivelAcesso());
            Mockito.verify(usuarioService, Mockito.times(1)).listar();
        }

        @Test
        @DisplayName("1.2 Deve retornar status 204 quando não houver usuários para os filtros")
        void deveRetornarStatus204QuandoListaVazia() {
            Mockito.when(usuarioService.listar()).thenReturn(Collections.emptyList());

            ResponseEntity<List<PermissaoResponse>> response = permissoesController.listarPermissoes(null, null);

            Assertions.assertEquals(204, response.getStatusCodeValue());
            Assertions.assertNull(response.getBody());
        }

        @Test
        @DisplayName("1.3 Deve retornar status 403 quando usuário logado for DENTISTA")
        void deveRetornarStatus403QuandoDentista() {
            autenticarComoDentista();

            ResponseEntity<List<PermissaoResponse>> response = permissoesController.listarPermissoes(null, null);

            Assertions.assertEquals(403, response.getStatusCodeValue());
            Mockito.verify(usuarioService, Mockito.never()).listar();
        }
    }

    @Nested
    @DisplayName("2. Teste de listagem de dentistas para a tela de permissões")
    class ListarDentistasTest {

        @Test
        @DisplayName("2.1 Deve retornar lista de dentistas com status 200 para RECEPCIONISTA")
        void deveRetornarListaDeDentistasParaRecepcionista() {
            autenticarComoRecepcionista();
            Usuario d1 = criarDentistaDomain(1L, "Dra. Ana Silva", "ana@lumina.com", 2, true);
            Usuario d2 = criarDentistaDomain(2L, "Dr. Carlos Oliveira", "carlos@lumina.com", 2, false);

            Mockito.when(usuarioService.listarDentistas()).thenReturn(List.of(d1, d2));

            ResponseEntity<List<PermissaoResponse>> response = permissoesController.listarDentistas(null);

            Assertions.assertEquals(200, response.getStatusCodeValue());
            Assertions.assertNotNull(response.getBody());
            Assertions.assertEquals(2, response.getBody().size());
            Assertions.assertEquals("restrito", response.getBody().get(0).getNivelAcesso());
            Assertions.assertEquals("nenhum", response.getBody().get(1).getNivelAcesso());
            Mockito.verify(usuarioService, Mockito.times(1)).listarDentistas();
        }

        @Test
        @DisplayName("2.2 Deve filtrar apenas dentistas ativos quando apenasAtivos for true")
        void deveFiltrarApenasDentistasAtivos() {
            Usuario d1 = criarDentistaDomain(1L, "Dra. Ana Silva", "ana@lumina.com", 2, true);
            Usuario d2 = criarDentistaDomain(2L, "Dr. Carlos Oliveira", "carlos@lumina.com", 2, false);

            Mockito.when(usuarioService.listarDentistas()).thenReturn(List.of(d1, d2));

            ResponseEntity<List<PermissaoResponse>> response = permissoesController.listarDentistas(true);

            Assertions.assertEquals(200, response.getStatusCodeValue());
            Assertions.assertNotNull(response.getBody());
            Assertions.assertEquals(1, response.getBody().size());
            Assertions.assertEquals("Dra. Ana Silva", response.getBody().get(0).getNome());
        }

        @Test
        @DisplayName("2.3 Deve retornar status 403 quando usuário for DENTISTA")
        void deveRetornar403QuandoDentistaTentarListarDentistas() {
            autenticarComoDentista();

            ResponseEntity<List<PermissaoResponse>> response = permissoesController.listarDentistas(null);

            Assertions.assertEquals(403, response.getStatusCodeValue());
            Mockito.verify(usuarioService, Mockito.never()).listarDentistas();
        }
    }

    @Nested
    @DisplayName("3. Teste de busca de permissão por ID")
    class BuscarPermissaoPorIdTest {

        @Test
        @DisplayName("3.1 Deve retornar permissão do dentista com status 200")
        void deveRetornarPermissaoPorIdComSucesso() {
            Usuario dentista = criarDentistaDomain(1L, "Dra. Ana Silva", "ana@lumina.com", 2, true);
            Mockito.when(usuarioService.buscarPorId(1L)).thenReturn(Optional.of(dentista));

            ResponseEntity<PermissaoResponse> response = permissoesController.buscarPermissaoPorId(1L);

            Assertions.assertEquals(200, response.getStatusCodeValue());
            Assertions.assertNotNull(response.getBody());
            Assertions.assertEquals("Dra. Ana Silva", response.getBody().getNome());
            Assertions.assertEquals("DENTISTA", response.getBody().getNomePerfil());
            Assertions.assertEquals("restrito", response.getBody().getNivelAcesso());
        }

        @Test
        @DisplayName("3.2 Deve retornar status 404 quando dentista não for encontrado")
        void deveRetornar404QuandoNaoEncontrado() {
            Mockito.when(usuarioService.buscarPorId(99L)).thenReturn(Optional.empty());

            ResponseEntity<PermissaoResponse> response = permissoesController.buscarPermissaoPorId(99L);

            Assertions.assertEquals(404, response.getStatusCodeValue());
        }
    }

    @Nested
    @DisplayName("4. Teste de atualização de permissões (Todos os Acessos / Restrito / Nenhum Acesso)")
    class AtualizarPermissaoTest {

        @Test
        @DisplayName("4.1 Deve atualizar permissão do dentista para 'todos' (Acesso Total) com status 200")
        void deveAtualizarPermissaoParaTodosOsAcessos() {
            PermissaoRequest request = new PermissaoRequest(null, null, "todos");
            Usuario dentistaPromovido = criarDentistaDomain(1L, "Dra. Ana Silva", "ana@lumina.com", 1, true);

            Mockito.when(usuarioService.atualizarPermissao(eq(1L), any(PermissaoRequest.class)))
                    .thenReturn(dentistaPromovido);

            ResponseEntity<PermissaoResponse> response = permissoesController.atualizarPermissao(1L, request);

            Assertions.assertEquals(200, response.getStatusCodeValue());
            Assertions.assertNotNull(response.getBody());
            Assertions.assertEquals(1, response.getBody().getFkPerfil());
            Assertions.assertEquals("ADMIN", response.getBody().getNomePerfil());
            Assertions.assertEquals("todos", response.getBody().getNivelAcesso());
            Assertions.assertTrue(response.getBody().getAtivo());
        }

        @Test
        @DisplayName("4.2 Deve atualizar permissão do dentista para 'nenhum' (Inativo) via PATCH com status 200")
        void deveAtualizarPermissaoParaNenhumAcessoViaPatch() {
            autenticarComoRecepcionista();
            PermissaoRequest request = new PermissaoRequest(2, false, "nenhum");
            Usuario dentistaInativo = criarDentistaDomain(1L, "Dra. Ana Silva", "ana@lumina.com", 2, false);

            Mockito.when(usuarioService.atualizarPermissao(eq(1L), any(PermissaoRequest.class)))
                    .thenReturn(dentistaInativo);

            ResponseEntity<PermissaoResponse> response = permissoesController.ajustarNivelAcesso(1L, request);

            Assertions.assertEquals(200, response.getStatusCodeValue());
            Assertions.assertNotNull(response.getBody());
            Assertions.assertFalse(response.getBody().getAtivo());
            Assertions.assertEquals("nenhum", response.getBody().getNivelAcesso());
        }

        @Test
        @DisplayName("4.3 Deve lançar EntidadeNaoEncontrada ao tentar atualizar permissão de usuário inexistente")
        void deveLancarExcecaoQuandoUsuarioNaoExistir() {
            PermissaoRequest request = new PermissaoRequest(1, true, "todos");

            Mockito.when(usuarioService.atualizarPermissao(eq(99L), any(PermissaoRequest.class)))
                    .thenThrow(new EntidadeNaoEncontrada("Usuario de id: 99 não encontrado"));

            assertThrows(
                    EntidadeNaoEncontrada.class,
                    () -> permissoesController.atualizarPermissao(99L, request)
            );
        }

        @Test
        @DisplayName("4.4 Deve retornar status 403 quando DENTISTA tentar alterar permissões")
        void deveBloquearDentistaAoTentarAtualizarPermissao() {
            autenticarComoDentista();
            PermissaoRequest request = new PermissaoRequest(1, true, "todos");

            ResponseEntity<PermissaoResponse> response = permissoesController.atualizarPermissao(1L, request);

            Assertions.assertEquals(403, response.getStatusCodeValue());
            Mockito.verify(usuarioService, Mockito.never()).atualizarPermissao(any(), any());
        }
    }

    @Nested
    @DisplayName("5. Teste de alteração de status de acesso (Ativar / Inativar)")
    class AlterarStatusAcessoTest {

        @Test
        @DisplayName("5.1 Deve inativar acesso do usuário com sucesso e retornar status 204")
        void deveInativarAcessoComSucesso() {
            Mockito.doNothing().when(usuarioService).deletar(false, 1L);

            ResponseEntity<Void> response = permissoesController.alterarStatusAcesso(1L, false);

            Assertions.assertEquals(204, response.getStatusCodeValue());
            Mockito.verify(usuarioService, Mockito.times(1)).deletar(false, 1L);
        }

        @Test
        @DisplayName("5.2 Deve retornar status 403 quando DENTISTA tentar alterar status de acesso")
        void deveRetornar403QuandoDentistaAlterarStatus() {
            autenticarComoDentista();

            ResponseEntity<Void> response = permissoesController.alterarStatusAcesso(1L, false);

            Assertions.assertEquals(403, response.getStatusCodeValue());
            Mockito.verify(usuarioService, Mockito.never()).deletar(any(), any());
        }
    }
}
