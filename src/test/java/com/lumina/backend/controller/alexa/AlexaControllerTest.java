package com.lumina.backend.controller.alexa;

import com.lumina.backend.controller.AlexaController;
import com.lumina.backend.dto.alexa.AlexaGerarPinResponse;
import com.lumina.backend.dto.alexa.AlexaStatusResponse;
import com.lumina.backend.dto.alexa.AlexaVincularRequest;
import com.lumina.backend.dto.alexa.AlexaVincularResponse;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.repository.UsuarioRepository;
import com.lumina.backend.service.alexa.AlexaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlexaControllerTest {

    @Mock
    private AlexaService alexaService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private AlexaController alexaController;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContext(String email, String role) {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(email);
        doReturn(Collections.singletonList(new SimpleGrantedAuthority(role))).when(auth).getAuthorities();

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @Nested
    @DisplayName("1. Geração de PIN")
    class GerarPinTest {

        @Test
        @DisplayName("Deve gerar PIN com base no usuário autenticado")
        void deveGerarPinComUsuarioAutenticado() {
            mockSecurityContext("dentista@lumina.com", "ROLE_DENTISTA");

            Usuario usuario = new Usuario();
            usuario.setIdUsuario(5L);
            usuario.setEmail("dentista@lumina.com");

            when(usuarioRepository.findByEmail("dentista@lumina.com")).thenReturn(Optional.of(usuario));
            when(alexaService.gerarPin(5L)).thenReturn(new AlexaGerarPinResponse("654321", 10, "Instruções"));

            ResponseEntity<AlexaGerarPinResponse> response = alexaController.gerarPin(null);

            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertEquals("654321", response.getBody().getCodigo());
            verify(alexaService).gerarPin(5L);
        }

        @Test
        @DisplayName("Administrador pode gerar PIN para outro dentista")
        void adminPodeGerarPinParaOutroDentista() {
            mockSecurityContext("admin@lumina.com", "ROLE_ADMIN");

            Usuario admin = new Usuario();
            admin.setIdUsuario(1L);
            admin.setEmail("admin@lumina.com");

            when(usuarioRepository.findByEmail("admin@lumina.com")).thenReturn(Optional.of(admin));
            when(alexaService.gerarPin(10L)).thenReturn(new AlexaGerarPinResponse("112233", 10, "Instruções"));

            ResponseEntity<AlexaGerarPinResponse> response = alexaController.gerarPin(10L);

            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            verify(alexaService).gerarPin(10L);
        }

        @Test
        @DisplayName("Dentista não pode gerar PIN para outro dentista")
        void dentistaNaoPodeGerarPinParaOutro() {
            mockSecurityContext("dentista1@lumina.com", "ROLE_DENTISTA");

            Usuario dentista = new Usuario();
            dentista.setIdUsuario(5L);
            dentista.setEmail("dentista1@lumina.com");

            when(usuarioRepository.findByEmail("dentista1@lumina.com")).thenReturn(Optional.of(dentista));

            assertThrows(AccessDeniedException.class, () -> alexaController.gerarPin(9L));
            verifyNoInteractions(alexaService);
        }
    }

    @Nested
    @DisplayName("2. Consulta de Status")
    class StatusTest {

        @Test
        @DisplayName("Deve retornar status da Alexa do próprio dentista")
        void deveRetornarStatusDoProprioDentista() {
            mockSecurityContext("dentista@lumina.com", "ROLE_DENTISTA");

            Usuario dentista = new Usuario();
            dentista.setIdUsuario(5L);
            dentista.setEmail("dentista@lumina.com");

            AlexaStatusResponse statusResponse = new AlexaStatusResponse(
                    true,
                    "amzn1.ask.account.TESTE",
                    "https://api.amazonalexa.com",
                    LocalDateTime.now(),
                    "Dr. Ricardo"
            );

            when(usuarioRepository.findByEmail("dentista@lumina.com")).thenReturn(Optional.of(dentista));
            when(alexaService.obterStatus(5L)).thenReturn(statusResponse);

            ResponseEntity<AlexaStatusResponse> response = alexaController.obterStatus(null);

            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getBody().getConectado());
            assertEquals("Dr. Ricardo", response.getBody().getDentistaNome());
            verify(alexaService).obterStatus(5L);
        }

        @Test
        @DisplayName("Administrador pode consultar status de qualquer dentista")
        void adminPodeConsultarStatusDeOutroDentista() {
            mockSecurityContext("admin@lumina.com", "ROLE_ADMIN");

            Usuario admin = new Usuario();
            admin.setIdUsuario(1L);
            admin.setEmail("admin@lumina.com");

            AlexaStatusResponse statusResponse = new AlexaStatusResponse(
                    false,
                    null,
                    null,
                    null,
                    "Dra. Beatriz"
            );

            when(usuarioRepository.findByEmail("admin@lumina.com")).thenReturn(Optional.of(admin));
            when(alexaService.obterStatus(12L)).thenReturn(statusResponse);

            ResponseEntity<AlexaStatusResponse> response = alexaController.obterStatus(12L);

            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertFalse(response.getBody().getConectado());
            verify(alexaService).obterStatus(12L);
        }

        @Test
        @DisplayName("Dentista não pode consultar status de outro dentista")
        void dentistaNaoPodeConsultarStatusDeOutro() {
            mockSecurityContext("dentista@lumina.com", "ROLE_DENTISTA");

            Usuario dentista = new Usuario();
            dentista.setIdUsuario(5L);
            dentista.setEmail("dentista@lumina.com");

            when(usuarioRepository.findByEmail("dentista@lumina.com")).thenReturn(Optional.of(dentista));

            assertThrows(AccessDeniedException.class, () -> alexaController.obterStatus(8L));
            verifyNoInteractions(alexaService);
        }
    }

    @Nested
    @DisplayName("3. Desconexão de Dispositivo")
    class DesconectarTest {

        @Test
        @DisplayName("Deve desconectar dispositivo com sucesso pelo próprio dentista")
        void deveDesconectarPeloProprioDentista() {
            mockSecurityContext("dentista@lumina.com", "ROLE_DENTISTA");

            Usuario dentista = new Usuario();
            dentista.setIdUsuario(5L);
            dentista.setEmail("dentista@lumina.com");

            when(usuarioRepository.findByEmail("dentista@lumina.com")).thenReturn(Optional.of(dentista));
            when(alexaService.desconectar(5L)).thenReturn("amzn1.ask.account.TESTE");

            ResponseEntity<Void> response = alexaController.desconectar(null);

            assertNotNull(response);
            assertEquals(204, response.getStatusCode().value());
            verify(alexaService).desconectar(5L);
        }

        @Test
        @DisplayName("Administrador pode desconectar dispositivo de outro dentista")
        void adminPodeDesconectarDispositivoDeOutro() {
            mockSecurityContext("admin@lumina.com", "ROLE_ADMIN");

            Usuario admin = new Usuario();
            admin.setIdUsuario(1L);
            admin.setEmail("admin@lumina.com");

            when(usuarioRepository.findByEmail("admin@lumina.com")).thenReturn(Optional.of(admin));
            when(alexaService.desconectar(15L)).thenReturn("amzn1.ask.account.ANOTHER");

            ResponseEntity<Void> response = alexaController.desconectar(15L);

            assertNotNull(response);
            assertEquals(204, response.getStatusCode().value());
            verify(alexaService).desconectar(15L);
        }

        @Test
        @DisplayName("Dentista não pode desconectar dispositivo de outro dentista")
        void dentistaNaoPodeDesconectarOutro() {
            mockSecurityContext("dentista@lumina.com", "ROLE_DENTISTA");

            Usuario dentista = new Usuario();
            dentista.setIdUsuario(5L);
            dentista.setEmail("dentista@lumina.com");

            when(usuarioRepository.findByEmail("dentista@lumina.com")).thenReturn(Optional.of(dentista));

            assertThrows(AccessDeniedException.class, () -> alexaController.desconectar(8L));
            verifyNoInteractions(alexaService);
        }
    }

    @Nested
    @DisplayName("4. Vínculo de Dispositivo")
    class VincularTest {

        @Test
        @DisplayName("Deve vincular dispositivo com sucesso")
        void deveVincularDispositivo() {
            AlexaVincularRequest request = new AlexaVincularRequest("654321", "amzn1.ask.account.TESTE", "https://api.amazonalexa.com");
            when(alexaService.vincular(request)).thenReturn(new AlexaVincularResponse(true, "Sucesso", "Dra. Maria"));

            ResponseEntity<AlexaVincularResponse> response = alexaController.vincular(request);

            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getBody().isSucesso());
            assertEquals("Dra. Maria", response.getBody().getDentistaNome());
        }
    }
}
