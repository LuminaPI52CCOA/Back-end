package com.lumina.backend.controller.consulta;

import com.lumina.backend.controller.PublicConsultaController;
import com.lumina.backend.dto.notificacao.ConfirmacaoConsultaResponse;
import com.lumina.backend.service.notificacao.NotificacaoWhatsappService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários PublicConsultaController")
class PublicConsultaControllerTest {

    @Mock
    private NotificacaoWhatsappService notificacaoWhatsappService;

    @InjectMocks
    private PublicConsultaController publicConsultaController;

    @Test
    @DisplayName("Deve confirmar consulta publicamente via token e retornar 200 OK")
    void deveConfirmarConsultaViaToken() {
        String token = "uuid-token-123";
        ConfirmacaoConsultaResponse esperado = new ConfirmacaoConsultaResponse(
                true,
                "Consulta confirmada com sucesso!",
                "Enzo Silva",
                "Dr. Carlos Eduardo",
                LocalDate.of(2026, 10, 23),
                "14:30"
        );

        when(notificacaoWhatsappService.confirmarConsultaPorToken(token)).thenReturn(esperado);

        ResponseEntity<ConfirmacaoConsultaResponse> response = publicConsultaController.confirmarConsulta(token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().sucesso());
        assertEquals("Enzo Silva", response.getBody().paciente());
        verify(notificacaoWhatsappService).confirmarConsultaPorToken(token);
    }
}
