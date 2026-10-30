package com.lumina.backend.service.notificacao;

import com.lumina.backend.dto.notificacao.ConfirmacaoConsultaResponse;
import com.lumina.backend.dto.notificacao.NotificacaoWhatsappResponse;
import com.lumina.backend.exception.EntidadeNaoEncontrada;
import com.lumina.backend.model.Cliente;
import com.lumina.backend.model.Consulta;
import com.lumina.backend.model.NotificacaoWhatsapp;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.repository.ConsultaRepository;
import com.lumina.backend.repository.NotificacaoWhatsappRepository;
import com.lumina.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacaoWhatsappServiceTest {

    @Mock
    private NotificacaoWhatsappRepository notificacaoWhatsappRepository;

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private NotificacaoWhatsappService service;

    @BeforeEach
    void setUp() {
        service = new NotificacaoWhatsappService(
                notificacaoWhatsappRepository,
                consultaRepository,
                usuarioRepository,
                "http://localhost:5173"
        );
    }

    @Test
    @DisplayName("Deve listar notificações gerando link wa.me dinâmico e telefone formatado")
    void deveListarNotificacoesComLinkWaMeDinamico() {
        LocalDate dataConsulta = LocalDate.of(2026, 10, 21);

        Cliente paciente = new Cliente();
        paciente.setIdCliente(1L);
        paciente.setNome("Enzo Silva");

        Cliente responsavel = new Cliente();
        responsavel.setIdCliente(2L);
        responsavel.setNome("Mariana Silva");

        Usuario dentista = new Usuario();
        dentista.setIdUsuario(5L);
        dentista.setNome("Carlos Eduardo");

        Consulta consulta = new Consulta(1042L, paciente, dentista, dataConsulta, LocalTime.of(14, 30), LocalTime.of(15, 0));

        NotificacaoWhatsapp notif = new NotificacaoWhatsapp();
        notif.setIdNotificacao(1L);
        notif.setConsulta(consulta);
        notif.setCliente(paciente);
        notif.setDestinatario(responsavel);
        notif.setNomeDestinatario("Mariana Silva");
        notif.setGrauParentesco("Mãe");
        notif.setNumeroTelefoneHigienizado("5511998887766");
        notif.setStatus(NotificacaoWhatsapp.STATUS_PENDENTE);
        notif.setDataConsulta(dataConsulta);
        notif.setHorarioConsulta(LocalTime.of(14, 30));
        notif.setTokenConfirmacao("token-uuid-123");

        when(notificacaoWhatsappRepository.findByDataConsultaOrderByHorarioConsultaAsc(dataConsulta))
                .thenReturn(List.of(notif));

        List<NotificacaoWhatsappResponse> resultado = service.listar(dataConsulta, null);

        assertEquals(1, resultado.size());
        NotificacaoWhatsappResponse dto = resultado.get(0);
        assertEquals("(11) 99888-7766", dto.telefoneFormatado());
        assertNotNull(dto.linkWhatsApp());
        assertTrue(dto.linkWhatsApp().startsWith("https://wa.me/5511998887766?text="));
        assertTrue(dto.linkWhatsApp().contains("token-uuid-123"));
    }

    @Test
    @DisplayName("Deve marcar notificação como DISPARADO e registrar recepcionista")
    void deveMarcarComoDisparado() {
        Usuario recepcionista = new Usuario();
        recepcionista.setIdUsuario(3L);
        recepcionista.setNome("Recepção Lumina");

        NotificacaoWhatsapp notif = new NotificacaoWhatsapp();
        notif.setIdNotificacao(1L);
        notif.setStatus(NotificacaoWhatsapp.STATUS_PENDENTE);
        notif.setNumeroTelefoneHigienizado("5511998887766");

        when(notificacaoWhatsappRepository.findById(1L)).thenReturn(Optional.of(notif));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(recepcionista));
        when(notificacaoWhatsappRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        NotificacaoWhatsappResponse response = service.marcarComoDisparado(1L, 3L);

        assertEquals(NotificacaoWhatsapp.STATUS_DISPARADO, response.status());
        assertEquals(3L, response.idUsuarioDisparo());
        assertNotNull(response.disparadoEm());
    }

    @Test
    @DisplayName("Deve confirmar consulta com 1 clique via token válido e ser idempotente")
    void deveConfirmarConsultaPorTokenComSucesso() {
        LocalDate dataFutura = LocalDate.now(NotificacaoWhatsappService.ZONE_SAO_PAULO).plusDays(1);

        Cliente paciente = new Cliente();
        paciente.setIdCliente(1L);
        paciente.setNome("Enzo Silva");

        Usuario dentista = new Usuario();
        dentista.setIdUsuario(2L);
        dentista.setNome("Dr. Carlos Eduardo");

        Consulta consulta = new Consulta(1042L, paciente, dentista, dataFutura, LocalTime.of(14, 30), LocalTime.of(15, 0));

        NotificacaoWhatsapp notif = new NotificacaoWhatsapp();
        notif.setIdNotificacao(1L);
        notif.setConsulta(consulta);
        notif.setCliente(paciente);
        notif.setDestinatario(paciente);
        notif.setDataConsulta(dataFutura);
        notif.setHorarioConsulta(LocalTime.of(14, 30));
        notif.setTokenConfirmacao("uuid-valido");
        notif.setLimiteConfirmacaoEm(LocalDateTime.of(dataFutura, LocalTime.of(14, 30)));

        when(notificacaoWhatsappRepository.findByTokenConfirmacao("uuid-valido")).thenReturn(Optional.of(notif));

        ConfirmacaoConsultaResponse res = service.confirmarConsultaPorToken("uuid-valido");

        assertTrue(res.sucesso());
        assertEquals("Consulta confirmada com sucesso!", res.mensagem());
        assertEquals("CONFIRMADA", consulta.getStatus());
        assertNotNull(notif.getConfirmadoEm());
        verify(consultaRepository).save(consulta);
        verify(notificacaoWhatsappRepository).save(notif);

        // Segunda chamada deve ser idempotente (200 OK)
        ConfirmacaoConsultaResponse resIdempotente = service.confirmarConsultaPorToken("uuid-valido");
        assertTrue(resIdempotente.sucesso());
    }

    @Test
    @DisplayName("Deve lançar 404 quando token não existir e 400 quando expirado ou cancelado")
    void deveValidarTokenInexistenteExpiradoOuCancelado() {
        when(notificacaoWhatsappRepository.findByTokenConfirmacao("inexistente")).thenReturn(Optional.empty());
        assertThrows(EntidadeNaoEncontrada.class, () -> service.confirmarConsultaPorToken("inexistente"));

        // Token expirado
        Consulta consulta = new Consulta();
        consulta.setIdConsulta(10L);
        consulta.setStatus("AGENDADA");

        NotificacaoWhatsapp notifExpirada = new NotificacaoWhatsapp();
        notifExpirada.setConsulta(consulta);
        notifExpirada.setStatus(NotificacaoWhatsapp.STATUS_PENDENTE);
        notifExpirada.setLimiteConfirmacaoEm(LocalDateTime.now(NotificacaoWhatsappService.ZONE_SAO_PAULO).minusHours(2));

        when(notificacaoWhatsappRepository.findByTokenConfirmacao("expirado")).thenReturn(Optional.of(notifExpirada));
        assertThrows(ResponseStatusException.class, () -> service.confirmarConsultaPorToken("expirado"));

        // Consulta cancelada
        consulta.setStatus("CANCELADA");
        when(notificacaoWhatsappRepository.findByTokenConfirmacao("cancelada")).thenReturn(Optional.of(notifExpirada));
        assertThrows(ResponseStatusException.class, () -> service.confirmarConsultaPorToken("cancelada"));
    }
}
