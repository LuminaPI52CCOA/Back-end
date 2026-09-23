package com.lumina.backend.controller.alexa;

import com.lumina.backend.controller.AlexaController;
import com.lumina.backend.dto.alexa.AlexaGerarPinResponse;
import com.lumina.backend.dto.alexa.AlexaVincularRequest;
import com.lumina.backend.dto.alexa.AlexaVincularResponse;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.repository.UsuarioRepository;
import com.lumina.backend.service.alexa.AlexaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

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

    @Test
    @DisplayName("Deve gerar PIN com base no usuário autenticado")
    void deveGerarPinComUsuarioAutenticado() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("dentista@lumina.com");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

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

        SecurityContextHolder.clearContext();
    }

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
