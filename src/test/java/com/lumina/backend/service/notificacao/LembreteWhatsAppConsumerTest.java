package com.lumina.backend.service.notificacao;

import com.lumina.backend.dto.notificacao.LembreteConsultaEvent;
import com.lumina.backend.model.Cliente;
import com.lumina.backend.model.Consulta;
import com.lumina.backend.model.NotificacaoWhatsapp;
import com.lumina.backend.repository.ConsultaRepository;
import com.lumina.backend.repository.NotificacaoWhatsappRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LembreteWhatsAppConsumerTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private NotificacaoWhatsappRepository notificacaoWhatsappRepository;

    @InjectMocks
    private LembreteWhatsAppConsumer consumer;

    @Test
    @DisplayName("Deve higienizar celular nacional válido de 11 dígitos e adicionar DDI 55")
    void deveHigienizarCelularDeOnzeDigitos() {
        assertEquals("5511999998888", LembreteWhatsAppConsumer.higienizarCelularBrasileiro("(11) 99999-8888"));
        assertEquals("5521987654321", LembreteWhatsAppConsumer.higienizarCelularBrasileiro("21987654321"));
    }

    @Test
    @DisplayName("Deve preservar celular válido que já possui DDI 55 e 13 dígitos")
    void devePreservarCelularComDdi55() {
        assertEquals("5511999998888", LembreteWhatsAppConsumer.higienizarCelularBrasileiro("+55 (11) 99999-8888"));
    }

    @Test
    @DisplayName("Deve rejeitar telefones fixos (8 ou 10 dígitos) ou formatos inválidos")
    void deveRejeitarTelefonesFixosOuInvalidos() {
        assertNull(LembreteWhatsAppConsumer.higienizarCelularBrasileiro("(11) 3222-4455"));
        assertNull(LembreteWhatsAppConsumer.higienizarCelularBrasileiro("1140028922"));
        assertNull(LembreteWhatsAppConsumer.higienizarCelularBrasileiro("32224455"));
        assertNull(LembreteWhatsAppConsumer.higienizarCelularBrasileiro("11888887777"));
        assertNull(LembreteWhatsAppConsumer.higienizarCelularBrasileiro("00999998888"));
        assertNull(LembreteWhatsAppConsumer.higienizarCelularBrasileiro(""));
        assertNull(LembreteWhatsAppConsumer.higienizarCelularBrasileiro(null));
    }

    @Test
    @DisplayName("Deve criar notificação PENDENTE para paciente maior de idade com celular válido")
    void deveCriarNotificacaoPendenteParaPacienteTitular() {
        LocalDate dataConsulta = LocalDate.of(2026, 10, 21);
        Cliente paciente = new Cliente();
        paciente.setIdCliente(10L);
        paciente.setNome("Carlos Silva");
        paciente.setDataNascimento(LocalDate.of(1990, 5, 15));
        paciente.setNumeroCelular("(11) 98765-4321");

        Consulta consulta = new Consulta(1042L, paciente, null, dataConsulta, LocalTime.of(14, 30), LocalTime.of(15, 0));

        when(notificacaoWhatsappRepository.existsByConsultaIdConsulta(1042L)).thenReturn(false);
        when(consultaRepository.findById(1042L)).thenReturn(Optional.of(consulta));

        consumer.consumirEventoLembrete(new LembreteConsultaEvent(1042L, dataConsulta, "2026-10-20T08:00:00-03:00"));

        ArgumentCaptor<NotificacaoWhatsapp> captor = ArgumentCaptor.forClass(NotificacaoWhatsapp.class);
        verify(notificacaoWhatsappRepository).save(captor.capture());

        NotificacaoWhatsapp salva = captor.getValue();
        assertEquals(NotificacaoWhatsapp.STATUS_PENDENTE, salva.getStatus());
        assertEquals("5511987654321", salva.getNumeroTelefoneHigienizado());
        assertEquals("Carlos Silva", salva.getNomeDestinatario());
        assertEquals(paciente, salva.getDestinatario());
        assertNull(salva.getGrauParentesco());
        assertNull(salva.getMotivoFalha());
        assertNotNull(salva.getTokenConfirmacao());
    }

    @Test
    @DisplayName("Deve direcionar notificação ao responsável legal quando paciente for menor de 18 anos")
    void deveUsarResponsavelLegalQuandoPacienteMenorDeIdade() {
        LocalDate dataConsulta = LocalDate.of(2026, 10, 21);

        Cliente responsavel = new Cliente();
        responsavel.setIdCliente(20L);
        responsavel.setNome("Mariana Silva");
        responsavel.setNumeroCelular("11998887766");

        Cliente menor = new Cliente();
        menor.setIdCliente(21L);
        menor.setNome("Enzo Silva");
        menor.setDataNascimento(LocalDate.of(2015, 3, 10));
        menor.setResponsavel(responsavel);
        menor.setGrauParentescoResponsavel("Mãe");

        Consulta consulta = new Consulta(1042L, menor, null, dataConsulta, LocalTime.of(14, 30), LocalTime.of(15, 0));

        when(notificacaoWhatsappRepository.existsByConsultaIdConsulta(1042L)).thenReturn(false);
        when(consultaRepository.findById(1042L)).thenReturn(Optional.of(consulta));

        consumer.consumirEventoLembrete(new LembreteConsultaEvent(1042L, dataConsulta, "2026-10-20T08:00:00-03:00"));

        ArgumentCaptor<NotificacaoWhatsapp> captor = ArgumentCaptor.forClass(NotificacaoWhatsapp.class);
        verify(notificacaoWhatsappRepository).save(captor.capture());

        NotificacaoWhatsapp salva = captor.getValue();
        assertEquals(NotificacaoWhatsapp.STATUS_PENDENTE, salva.getStatus());
        assertEquals("Mariana Silva", salva.getNomeDestinatario());
        assertEquals("Mãe", salva.getGrauParentesco());
        assertEquals(responsavel, salva.getDestinatario());
        assertEquals("5511998887766", salva.getNumeroTelefoneHigienizado());
    }

    @Test
    @DisplayName("Deve registrar FALHA_CONTATO_INVALIDO quando telefone for fixo")
    void deveRegistrarFalhaContatoInvalidoQuandoTelefoneFixo() {
        LocalDate dataConsulta = LocalDate.of(2026, 10, 21);
        Cliente paciente = new Cliente();
        paciente.setIdCliente(10L);
        paciente.setNome("João Fixo");
        paciente.setDataNascimento(LocalDate.of(1980, 1, 1));
        paciente.setNumeroCelular("1133445566");

        Consulta consulta = new Consulta(1050L, paciente, null, dataConsulta, LocalTime.of(10, 0), LocalTime.of(10, 45));

        when(notificacaoWhatsappRepository.existsByConsultaIdConsulta(1050L)).thenReturn(false);
        when(consultaRepository.findById(1050L)).thenReturn(Optional.of(consulta));

        consumer.consumirEventoLembrete(new LembreteConsultaEvent(1050L, dataConsulta, "2026-10-20T08:00:00-03:00"));

        ArgumentCaptor<NotificacaoWhatsapp> captor = ArgumentCaptor.forClass(NotificacaoWhatsapp.class);
        verify(notificacaoWhatsappRepository).save(captor.capture());

        NotificacaoWhatsapp salva = captor.getValue();
        assertEquals(NotificacaoWhatsapp.STATUS_FALHA_CONTATO_INVALIDO, salva.getStatus());
        assertEquals(LembreteWhatsAppConsumer.MOTIVO_FALHA_TELEFONE_INVALIDO, salva.getMotivoFalha());
        assertNull(salva.getNumeroTelefoneHigienizado());
    }

    @Test
    @DisplayName("Deve garantir idempotência ignorando evento quando notificação já existe ou em DataIntegrityViolationException")
    void deveGarantirIdempotencia() {
        LocalDate dataConsulta = LocalDate.of(2026, 10, 21);
        when(notificacaoWhatsappRepository.existsByConsultaIdConsulta(1042L)).thenReturn(true);

        consumer.consumirEventoLembrete(new LembreteConsultaEvent(1042L, dataConsulta, "2026-10-20T08:00:00-03:00"));

        verify(consultaRepository, never()).findById(any());
        verify(notificacaoWhatsappRepository, never()).save(any());

        // Simula corrida concorrente disparando DataIntegrityViolationException
        Cliente paciente = new Cliente();
        paciente.setIdCliente(10L);
        paciente.setNome("Carlos Silva");
        paciente.setDataNascimento(LocalDate.of(1990, 5, 15));
        paciente.setNumeroCelular("11987654321");
        Consulta consulta = new Consulta(2000L, paciente, null, dataConsulta, LocalTime.of(9, 0), LocalTime.of(9, 30));

        when(notificacaoWhatsappRepository.existsByConsultaIdConsulta(2000L)).thenReturn(false);
        when(consultaRepository.findById(2000L)).thenReturn(Optional.of(consulta));
        when(notificacaoWhatsappRepository.save(any())).thenThrow(new DataIntegrityViolationException("uk_notificacao_consulta"));

        assertDoesNotThrow(() ->
                consumer.consumirEventoLembrete(new LembreteConsultaEvent(2000L, dataConsulta, "2026-10-20T08:00:00-03:00"))
        );
    }
}
