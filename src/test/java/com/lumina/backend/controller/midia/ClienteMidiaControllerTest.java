package com.lumina.backend.controller.midia;

import com.lumina.backend.controller.ClienteMidiaController;
import com.lumina.backend.dto.midia.ClienteMidiaResponse;
import com.lumina.backend.dto.midia.IniciarUploadRequest;
import com.lumina.backend.dto.midia.UploadArquivoItemRequest;
import com.lumina.backend.dto.midia.UploadPresignedItemResponse;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.repository.UsuarioRepository;
import com.lumina.backend.service.midia.ClienteMidiaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários ClienteMidiaController")
class ClienteMidiaControllerTest {

    @Mock
    private ClienteMidiaService clienteMidiaService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ClienteMidiaController clienteMidiaController;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Usuario mockUsuarioAutenticado(String email) {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(email);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setEmail(email);
        usuario.setNome("Dr. Teste");

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        return usuario;
    }

    @Nested
    @DisplayName("1. Iniciar Upload")
    class IniciarUploadTest {

        @Test
        @DisplayName("1.1 Deve iniciar upload com sucesso e retornar status 201")
        void deveIniciarUploadComSucesso() {
            Usuario usuario = mockUsuarioAutenticado("dentista@lumina.com");
            IniciarUploadRequest request = new IniciarUploadRequest(
                    1L,
                    null,
                    "Legenda",
                    List.of(new UploadArquivoItemRequest("foto.jpg", "image/jpeg", 1024L))
            );
            UploadPresignedItemResponse item = new UploadPresignedItemResponse(
                    10L, "foto.jpg", "https://s3/put-url", "clientes/1/foto.jpg"
            );

            when(clienteMidiaService.iniciarUpload(1L, request, usuario)).thenReturn(List.of(item));

            ResponseEntity<List<UploadPresignedItemResponse>> response =
                    clienteMidiaController.iniciarUpload(1L, request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            assertEquals(10L, response.getBody().get(0).getMidiaId());
            verify(clienteMidiaService).iniciarUpload(1L, request, usuario);
        }
    }

    @Nested
    @DisplayName("2. Confirmar Upload")
    class ConfirmarUploadTest {

        @Test
        @DisplayName("2.1 Deve confirmar upload com sucesso e retornar status 200")
        void deveConfirmarUploadComSucesso() {
            Usuario usuario = mockUsuarioAutenticado("dentista@lumina.com");
            ClienteMidiaResponse midiaResponse = new ClienteMidiaResponse();
            midiaResponse.setId(101L);
            midiaResponse.setNomeOriginal("foto.jpg");

            when(clienteMidiaService.confirmarUpload(1L, 101L, usuario)).thenReturn(midiaResponse);

            ResponseEntity<ClienteMidiaResponse> response =
                    clienteMidiaController.confirmarUpload(1L, 101L);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(101L, response.getBody().getId());
            verify(clienteMidiaService).confirmarUpload(1L, 101L, usuario);
        }
    }

    @Nested
    @DisplayName("3. Listar Mídias")
    class ListarMidiasTest {

        @Test
        @DisplayName("3.1 Deve listar mídias ativas e retornar status 200")
        void deveListarMidiasComSucesso() {
            Usuario usuario = mockUsuarioAutenticado("dentista@lumina.com");
            ClienteMidiaResponse midiaResponse = new ClienteMidiaResponse();
            midiaResponse.setId(101L);

            when(clienteMidiaService.listarMidias(1L, 2L, 3L, 4L, "2026-10-22", usuario))
                    .thenReturn(List.of(midiaResponse));

            ResponseEntity<List<ClienteMidiaResponse>> response =
                    clienteMidiaController.listarMidias(1L, 2L, 3L, 4L, "2026-10-22");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
        }

        @Test
        @DisplayName("3.2 Deve retornar status 204 quando lista estiver vazia")
        void deveRetornarNoContentQuandoListaVazia() {
            SecurityContextHolder.clearContext();
            when(clienteMidiaService.listarMidias(eq(1L), any(), any(), any(), any(), eq(null)))
                    .thenReturn(Collections.emptyList());

            ResponseEntity<List<ClienteMidiaResponse>> response =
                    clienteMidiaController.listarMidias(1L, null, null, null, null);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            assertNull(response.getBody());
        }
    }

    @Nested
    @DisplayName("4. Excluir Mídia")
    class ExcluirMidiaTest {

        @Test
        @DisplayName("4.1 Deve executar soft delete e retornar status 204")
        void deveExcluirMidiaComSucesso() {
            Usuario usuario = mockUsuarioAutenticado("dentista@lumina.com");
            doNothing().when(clienteMidiaService).excluirMidia(1L, 101L, usuario);

            ResponseEntity<Void> response = clienteMidiaController.excluirMidia(1L, 101L);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            verify(clienteMidiaService).excluirMidia(1L, 101L, usuario);
        }
    }
}
