package com.lumina.backend.controller.notificacao;

import com.lumina.backend.controller.NotificacaoWhatsappController;
import com.lumina.backend.controller.PublicConsultaController;
import com.lumina.backend.dto.notificacao.ConfirmacaoConsultaResponse;
import com.lumina.backend.dto.notificacao.NotificacaoWhatsappResponse;
import com.lumina.backend.dto.notificacao.ProcessamentoD1Response;
import com.lumina.backend.repository.UsuarioRepository;
import com.lumina.backend.service.notificacao.LembretePublicadorService;
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
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacaoWhatsappControllerTest {

    @Mock
    private LembretePublicadorService lembretePublicadorService;

    @Mock
    private NotificacaoWhatsappService notificacaoWhatsappService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private NotificacaoWhatsappController notificacaoController;

    @InjectMocks
    private PublicConsultaController publicConsultaController;

    @Test
    @DisplayName("POST /api/notificacoes/processar-d1 deve retornar 200 OK com resumo do enfileiramento")
    void deveProcessarD1Manualmente() {
        ProcessamentoD1Response esperado = new ProcessamentoD1Response(
                true,
                "Processamento D-1 iniciado com sucesso.",
                LocalDate.of(2026, 10, 21),
                5
        );
        when(lembretePublicadorService.publicarLembretesD1()).thenReturn(esperado);

        ResponseEntity<ProcessamentoD1Response> response = notificacaoController.processarD1();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5, response.getBody().totalEnfileiradas());
    }

    @Test
    @DisplayName("GET /api/notificacoes/whatsapp deve listar notificações para a recepção")
    void deveListarNotificacoesWhatsapp() {
        LocalDate data = LocalDate.of(2026, 10, 21);
        NotificacaoWhatsappResponse item = new NotificacaoWhatsappResponse(
                1L, 1042L, "Enzo Silva", "Mariana Silva", "Mãe",
                "(11) 99888-7766", data, LocalTime.of(14, 30), "Dr. Carlos Eduardo",
                "PENDENTE", null, "https://wa.me/5511998887766?text=...", false,
                null, null, null
        );
        when(notificacaoWhatsappService.listar(data, "PENDENTE")).thenReturn(List.of(item));

        ResponseEntity<List<NotificacaoWhatsappResponse>> response =
                notificacaoController.listarNotificacoes(data, "PENDENTE");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("POST /api/public/consultas/confirmar/{token} deve confirmar consulta publicamente")
    void deveConfirmarConsultaPublicamente() {
        ConfirmacaoConsultaResponse confirmacao = new ConfirmacaoConsultaResponse(
                true,
                "Consulta confirmada com sucesso!",
                "Enzo Silva",
                "Dr. Carlos Eduardo",
                LocalDate.of(2026, 10, 21),
                "14:30"
        );
        when(notificacaoWhatsappService.confirmarConsultaPorToken("token-123")).thenReturn(confirmacao);

        ResponseEntity<ConfirmacaoConsultaResponse> response =
                publicConsultaController.confirmarConsulta("token-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Enzo Silva", response.getBody().paciente());
    }
}
