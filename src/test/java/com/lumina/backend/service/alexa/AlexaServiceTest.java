package com.lumina.backend.service.alexa;

import com.lumina.backend.dto.alexa.*;
import com.lumina.backend.exception.EntidadeNaoEncontrada;
import com.lumina.backend.model.*;
import com.lumina.backend.repository.AnamneseRepository;
import com.lumina.backend.repository.ConsultaRepository;
import com.lumina.backend.repository.UsuarioAlexaRepository;
import com.lumina.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlexaServiceTest {

    @Mock
    private UsuarioAlexaRepository usuarioAlexaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private AnamneseRepository anamneseRepository;

    @InjectMocks
    private AlexaService alexaService;

    @Nested
    @DisplayName("1. Testes de Geração de PIN")
    class GerarPinTest {

        @Test
        @DisplayName("Deve gerar PIN de 6 dígitos com validade de 10 minutos")
        void deveGerarPinComSucesso() {
            Usuario usuario = new Usuario();
            usuario.setIdUsuario(1L);
            usuario.setNome("Dra. Paula");
            usuario.setEmail("paula@lumina.com");

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(usuarioAlexaRepository.findByUsuario_IdUsuarioAndAtivoTrue(1L)).thenReturn(Optional.empty());
            when(usuarioAlexaRepository.save(any(UsuarioAlexa.class))).thenAnswer(i -> i.getArgument(0));

            AlexaGerarPinResponse response = alexaService.gerarPin(1L);

            assertNotNull(response);
            assertNotNull(response.getCodigo());
            assertEquals(6, response.getCodigo().length());
            assertEquals(10, response.getExpiraEmMinutos());
            assertTrue(response.getMensagem().contains(response.getCodigo()));
            verify(usuarioAlexaRepository).save(any(UsuarioAlexa.class));
        }

        @Test
        @DisplayName("Deve lançar exceção se usuário não for encontrado")
        void deveLancarExcecaoSeUsuarioNaoExistir() {
            when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(EntidadeNaoEncontrada.class, () -> alexaService.gerarPin(99L));
        }
    }

    @Nested
    @DisplayName("2. Testes de Pareamento / Vínculo")
    class VincularTest {

        @Test
        @DisplayName("Deve vincular dispositivo com código PIN válido")
        void deveVincularComSucesso() {
            Usuario usuario = new Usuario();
            usuario.setIdUsuario(1L);
            usuario.setNome("Dr. Roberto");

            UsuarioAlexa vinculo = new UsuarioAlexa();
            vinculo.setIdUsuarioAlexa(10L);
            vinculo.setUsuario(usuario);
            vinculo.setCodigoPareamento("123456");
            vinculo.setCodigoExpiracao(LocalDateTime.now().plusMinutes(5));
            vinculo.setAtivo(false);

            when(usuarioAlexaRepository.findByCodigoPareamento("123456")).thenReturn(Optional.of(vinculo));
            when(usuarioAlexaRepository.findByAlexaUserIdAndAtivoTrue("amzn1.ask.account.TEST")).thenReturn(Optional.empty());
            when(usuarioAlexaRepository.save(any(UsuarioAlexa.class))).thenAnswer(i -> i.getArgument(0));

            AlexaVincularRequest request = new AlexaVincularRequest("123456", "amzn1.ask.account.TEST", "https://api.amazonalexa.com");
            AlexaVincularResponse response = alexaService.vincular(request);

            assertNotNull(response);
            assertTrue(response.isSucesso());
            assertEquals("Dr. Roberto", response.getDentistaNome());
            assertEquals("amzn1.ask.account.TEST", vinculo.getAlexaUserId());
            assertTrue(vinculo.getAtivo());
            assertNull(vinculo.getCodigoPareamento());
        }

        @Test
        @DisplayName("Deve falhar se o código de pareamento estiver expirado")
        void deveFalharSePinExpirado() {
            Usuario usuario = new Usuario();
            usuario.setNome("Dr. Roberto");

            UsuarioAlexa vinculo = new UsuarioAlexa();
            vinculo.setUsuario(usuario);
            vinculo.setCodigoPareamento("123456");
            vinculo.setCodigoExpiracao(LocalDateTime.now().minusMinutes(1));

            when(usuarioAlexaRepository.findByCodigoPareamento("123456")).thenReturn(Optional.of(vinculo));

            AlexaVincularRequest request = new AlexaVincularRequest("123456", "amzn1.ask.account.TEST", "https://api.amazonalexa.com");

            assertThrows(IllegalStateException.class, () -> alexaService.vincular(request));
        }

        @Test
        @DisplayName("Deve falhar se o código de pareamento não for encontrado")
        void deveFalharSePinInvalido() {
            when(usuarioAlexaRepository.findByCodigoPareamento("000000")).thenReturn(Optional.empty());

            AlexaVincularRequest request = new AlexaVincularRequest("000000", "amzn1.ask.account.TEST", "https://api.amazonalexa.com");

            assertThrows(EntidadeNaoEncontrada.class, () -> alexaService.vincular(request));
        }
    }

    @Nested
    @DisplayName("3. Testes de Próxima Consulta")
    class ProximaConsultaTest {

        @Test
        @DisplayName("Deve retornar mensagem de voz com detalhes da próxima consulta")
        void deveRetornarProximaConsulta() {
            Usuario usuario = new Usuario();
            usuario.setIdUsuario(2L);
            usuario.setNome("Dr. Lucas");

            UsuarioAlexa vinculo = new UsuarioAlexa();
            vinculo.setUsuario(usuario);
            vinculo.setAlexaUserId("amzn1.ask.account.LUCAS");
            vinculo.setAtivo(true);

            Cliente cliente = new Cliente();
            cliente.setIdCliente(5L);
            cliente.setNome("Maria Silva");

            Consulta consulta = new Consulta();
            consulta.setIdConsulta(100L);
            consulta.setUsuario(usuario);
            consulta.setCliente(cliente);
            consulta.setData(LocalDate.now());
            consulta.setHorarioInicio(LocalTime.of(15, 30));
            consulta.setHorarioFim(LocalTime.of(16, 0));
            consulta.setStatus("AGENDADA");

            when(usuarioAlexaRepository.findByAlexaUserIdAndAtivoTrue("amzn1.ask.account.LUCAS")).thenReturn(Optional.of(vinculo));
            when(consultaRepository.findProximasConsultas(eq(2L), any(LocalDate.class), any(LocalTime.class)))
                    .thenReturn(List.of(consulta));

            AlexaConsultaDto dto = alexaService.obterProximaConsulta("amzn1.ask.account.LUCAS", null);

            assertNotNull(dto);
            assertEquals(100L, dto.getIdConsulta());
            assertEquals("Maria Silva", dto.getPacienteNome());
            assertTrue(dto.getMensagemVoz().contains("15 horas e 30 minutos"));
            assertTrue(dto.getMensagemVoz().contains("Maria Silva"));
        }

        @Test
        @DisplayName("Deve informar por voz quando não houver mais consultas hoje")
        void deveInformarQuandoSemConsultasRestantes() {
            Usuario usuario = new Usuario();
            usuario.setIdUsuario(2L);
            usuario.setNome("Dr. Lucas");

            UsuarioAlexa vinculo = new UsuarioAlexa();
            vinculo.setUsuario(usuario);
            vinculo.setAlexaUserId("amzn1.ask.account.LUCAS");

            when(usuarioAlexaRepository.findByAlexaUserIdAndAtivoTrue("amzn1.ask.account.LUCAS")).thenReturn(Optional.of(vinculo));
            when(consultaRepository.findProximasConsultas(eq(2L), any(LocalDate.class), any(LocalTime.class)))
                    .thenReturn(Collections.emptyList());

            AlexaConsultaDto dto = alexaService.obterProximaConsulta("amzn1.ask.account.LUCAS", null);

            assertNotNull(dto);
            assertNull(dto.getIdConsulta());
            assertTrue(dto.getMensagemVoz().contains("não possui mais consultas agendadas para hoje"));
        }
    }

    @Nested
    @DisplayName("4. Testes de Alertas de Anamnese")
    class AlertaAnamneseTest {

        @Test
        @DisplayName("Deve extrair alergias e condições de risco da anamnese do próximo paciente")
        void deveExtrairAlertasMedicos() {
            Usuario usuario = new Usuario();
            usuario.setIdUsuario(3L);
            usuario.setNome("Dra. Fernanda");

            UsuarioAlexa vinculo = new UsuarioAlexa();
            vinculo.setUsuario(usuario);

            Cliente cliente = new Cliente();
            cliente.setIdCliente(10L);
            cliente.setNome("Joao Pedro");

            Consulta consulta = new Consulta();
            consulta.setUsuario(usuario);
            consulta.setCliente(cliente);

            Anamnese ficha = new Anamnese();
            ficha.setAlergiaMedicamentos(true);
            ficha.setDescricaoAlergiaMedicamentos("Dipirona");
            ficha.setProblemaCardiaco(true);
            ficha.setDescricaoProblemaCardiaco("Sopro no coração");

            when(usuarioAlexaRepository.findByAlexaUserIdAndAtivoTrue("amzn1.alexa")).thenReturn(Optional.of(vinculo));
            when(consultaRepository.findProximasConsultas(eq(3L), any(LocalDate.class), any(LocalTime.class)))
                    .thenReturn(List.of(consulta));
            when(anamneseRepository.findByFkCliente_IdClienteOrderByDataAnamneseDesc(10L))
                    .thenReturn(List.of(ficha));

            AlexaAnamneseAlertaDto alerta = alexaService.obterAlertaAnamneseProxima("amzn1.alexa", null);

            assertNotNull(alerta);
            assertTrue(alerta.isPossuiRestricoes());
            assertEquals("Joao Pedro", alerta.getPacienteNome());
            assertTrue(alerta.getAlertas().contains("alergia a Dipirona"));
            assertTrue(alerta.getAlertas().contains("Sopro no coração"));
            assertTrue(alerta.getMensagemVoz().contains("Dipirona"));
        }
    }
}
