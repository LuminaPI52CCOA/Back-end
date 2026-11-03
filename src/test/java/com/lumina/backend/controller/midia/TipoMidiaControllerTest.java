package com.lumina.backend.controller.midia;

import com.lumina.backend.controller.TipoMidiaController;
import com.lumina.backend.dto.midia.TipoMidiaRequest;
import com.lumina.backend.dto.midia.TipoMidiaResponse;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.repository.UsuarioRepository;
import com.lumina.backend.service.midia.TipoMidiaService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários TipoMidiaController")
class TipoMidiaControllerTest {

    @Mock
    private TipoMidiaService tipoMidiaService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private TipoMidiaController tipoMidiaController;

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

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        return usuario;
    }

    @Nested
    @DisplayName("1. Listar Tipos de Mídia")
    class ListarTest {

        @Test
        @DisplayName("1.1 Deve retornar lista de tipos de mídia com status 200")
        void deveRetornarListaComSucesso() {
            Usuario usuario = mockUsuarioAutenticado("dentista@lumina.com");
            TipoMidiaResponse tipo = new TipoMidiaResponse(1L, "Raio-X", "Exame radiográfico", false);

            when(tipoMidiaService.listarDisponiveis(usuario)).thenReturn(List.of(tipo));

            ResponseEntity<List<TipoMidiaResponse>> response = tipoMidiaController.listar();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            assertEquals("Raio-X", response.getBody().get(0).getNome());
            verify(tipoMidiaService).listarDisponiveis(usuario);
        }

        @Test
        @DisplayName("1.2 Deve retornar status 204 quando nenhum tipo for encontrado")
        void deveRetornarNoContentQuandoVazio() {
            when(tipoMidiaService.listarDisponiveis(null)).thenReturn(Collections.emptyList());

            ResponseEntity<List<TipoMidiaResponse>> response = tipoMidiaController.listar();

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            assertNull(response.getBody());
            verify(tipoMidiaService).listarDisponiveis(null);
        }
    }

    @Nested
    @DisplayName("2. Cadastrar Tipo de Mídia Personalizado")
    class CadastrarTest {

        @Test
        @DisplayName("2.1 Deve cadastrar tipo personalizado e retornar status 201")
        void deveCadastrarTipoPersonalizadoComSucesso() {
            Usuario usuario = mockUsuarioAutenticado("dentista@lumina.com");
            TipoMidiaRequest request = new TipoMidiaRequest("Tomografia", "Exame 3D");
            TipoMidiaResponse criado = new TipoMidiaResponse(5L, "Tomografia", "Exame 3D", true);

            when(tipoMidiaService.criarPersonalizado(request, usuario)).thenReturn(criado);

            ResponseEntity<TipoMidiaResponse> response = tipoMidiaController.cadastrar(request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(5L, response.getBody().getId());
            assertTrue(response.getBody().getPersonalizado());
            verify(tipoMidiaService).criarPersonalizado(request, usuario);
        }
    }
}
