package com.lumina.backend.service.consulta;

import com.lumina.backend.dto.consulta.ConsultaRequest;
import com.lumina.backend.exception.EntidadeNaoEncontrada;
import com.lumina.backend.model.Cliente;
import com.lumina.backend.model.Consulta;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.repository.ClienteRepository;
import com.lumina.backend.repository.ConsultaRepository;
import com.lumina.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
class ConsultaServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ConsultaService consultaService;

    @Nested
    @DisplayName("1. Teste de listagem de todas as consultas")
    class ListarTest {

        @Test
        @DisplayName("1.1 Deve retornar uma lista cheia com sucesso")
        void deveRetornarUmaListaCheiaComSucesso() {
            Consulta c1 = new Consulta();
            c1.setIdConsulta(1L);
            Consulta c2 = new Consulta();
            c2.setIdConsulta(2L);

            List<Consulta> listaCheia = List.of(c1, c2);

            Mockito.when(consultaRepository.findAll()).thenReturn(listaCheia);

            List<Consulta> resultado = consultaService.listar();

            Assertions.assertFalse(resultado.isEmpty());
            Assertions.assertEquals(2, resultado.size());
            Mockito.verify(consultaRepository, Mockito.times(1)).findAll();
        }

        @Test
        @DisplayName("1.2 Deve retornar uma lista vazia")
        void deveRetornarUmaListaVazia() {
            Mockito.when(consultaRepository.findAll()).thenReturn(Collections.emptyList());

            List<Consulta> resultado = consultaService.listar();

            Assertions.assertTrue(resultado.isEmpty());
            Mockito.verify(consultaRepository, Mockito.times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("2. Teste de cadastro de consulta")
    class CadastrarTest {

        @Test
        @DisplayName("2.1 Cadastro deve ser realizado com sucesso")
        void deveCriarConsultaCorretamente() {
            ConsultaRequest request = new ConsultaRequest();
            request.setIdCliente(1L);
            request.setIdUsuario(2L);

            Cliente cliente = new Cliente();
            cliente.setIdCliente(1L);

            Usuario usuario = new Usuario();
            usuario.setIdUsuario(2L);

            Consulta consultaSalva = new Consulta();
            consultaSalva.setIdConsulta(1L);
            consultaSalva.setCliente(cliente);
            consultaSalva.setUsuario(usuario);

            Mockito.when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            Mockito.when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
            Mockito.when(consultaRepository.save(any(Consulta.class))).thenReturn(consultaSalva);

            Consulta resultado = consultaService.cadastrar(request);

            Assertions.assertNotNull(resultado);
            Assertions.assertEquals(1L, resultado.getCliente().getIdCliente());
            Assertions.assertEquals(2L, resultado.getUsuario().getIdUsuario());

            Mockito.verify(clienteRepository, Mockito.times(1)).findById(1L);
            Mockito.verify(usuarioRepository, Mockito.times(1)).findById(2L);
            Mockito.verify(consultaRepository, Mockito.times(1)).save(any(Consulta.class));
        }

        @Test
        @DisplayName("2.2 Deve retornar EntidadeNaoEncontrada quando cliente não existir")
        void deveLancarExcecaoQuandoClienteNaoEncontrado() {
            ConsultaRequest request = new ConsultaRequest();
            request.setIdCliente(1L);

            Mockito.when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

            EntidadeNaoEncontrada exception = assertThrows(
                    EntidadeNaoEncontrada.class,
                    () -> consultaService.cadastrar(request)
            );

            Assertions.assertEquals("Cliente não encontrado", exception.getMessage());
            Mockito.verify(usuarioRepository, Mockito.never()).findById(anyLong());
            Mockito.verify(consultaRepository, Mockito.never()).save(any(Consulta.class));
        }

        @Test
        @DisplayName("2.3 Deve retornar EntidadeNaoEncontrada quando usuário não existir")
        void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
            ConsultaRequest request = new ConsultaRequest();
            request.setIdCliente(1L);
            request.setIdUsuario(2L);

            Cliente cliente = new Cliente();
            cliente.setIdCliente(1L);

            Mockito.when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            Mockito.when(usuarioRepository.findById(2L)).thenReturn(Optional.empty());

            EntidadeNaoEncontrada exception = assertThrows(
                    EntidadeNaoEncontrada.class,
                    () -> consultaService.cadastrar(request)
            );

            Assertions.assertEquals("Usuário não encontrado", exception.getMessage());
            Mockito.verify(consultaRepository, Mockito.never()).save(any(Consulta.class));
        }
    }

    @Nested
    @DisplayName("3. Teste de busca por id")
    class BuscarPorIdTest {

        @Test
        @DisplayName("3.1 Deve retornar a consulta buscada pelo id especifico corretamente")
        void deveBuscarCorretamenteAConsultaPorId() {
            Consulta consulta = new Consulta();
            consulta.setIdConsulta(10L);

            Mockito.when(consultaRepository.findById(10L)).thenReturn(Optional.of(consulta));

            Consulta resultado = consultaService.buscarPorId(10L);

            Assertions.assertEquals(10L, resultado.getIdConsulta());
            Mockito.verify(consultaRepository, Mockito.times(1)).findById(10L);
        }

        @Test
        @DisplayName("3.2 Deve lançar EntidadeNaoEncontrada se consulta não existir")
        void naoDeveBuscarAConsultaPorId() {
            Mockito.when(consultaRepository.findById(10L)).thenReturn(Optional.empty());

            EntidadeNaoEncontrada exception = assertThrows(
                    EntidadeNaoEncontrada.class,
                    () -> consultaService.buscarPorId(10L)
            );

            Assertions.assertEquals("Consulta não encontrada", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("4. Testes de reagendamento de consulta")
    class ReagendarTest {

        @Test
        @DisplayName("4.1 Deve reagendar a consulta com sucesso")
        void deveReagendarAConsultaComSucesso() {
            Long consultaId = 1L;

            Consulta consultaExistente = new Consulta();
            consultaExistente.setIdConsulta(consultaId);
            consultaExistente.setData(LocalDate.of(2023, 10, 10));
            consultaExistente.setHorarioInicio(LocalTime.of(10, 0));
            consultaExistente.setHorarioFim(LocalTime.of(11, 0));

            ConsultaRequest request = new ConsultaRequest();
            request.setData(LocalDate.of(2023, 11, 20));
            request.setHorarioInicio(LocalTime.of(14, 0));
            request.setHorarioFim(LocalTime.of(15, 0));

            Mockito.when(consultaRepository.findById(consultaId)).thenReturn(Optional.of(consultaExistente));
            Mockito.when(consultaRepository.save(any(Consulta.class))).thenReturn(consultaExistente);

            Consulta resultado = consultaService.reagendar(consultaId, request);

            Assertions.assertEquals(LocalDate.of(2023, 11, 20), resultado.getData());
            Assertions.assertEquals(LocalTime.of(14, 0), resultado.getHorarioInicio());
            Assertions.assertEquals(LocalTime.of(15, 0), resultado.getHorarioFim());

            Mockito.verify(consultaRepository, Mockito.times(1)).findById(consultaId);
            Mockito.verify(consultaRepository, Mockito.times(1)).save(consultaExistente);
        }

        @Test
        @DisplayName("4.2 Deve lançar EntidadeNaoEncontrada ao tentar reagendar uma consulta inexistente")
        void deveLancarExcecaoAoReagendarConsultaInexistente() {
            ConsultaRequest request = new ConsultaRequest();

            Mockito.when(consultaRepository.findById(1L)).thenReturn(Optional.empty());

            EntidadeNaoEncontrada exception = assertThrows(
                    EntidadeNaoEncontrada.class,
                    () -> consultaService.reagendar(1L, request)
            );

            Assertions.assertEquals("Consulta não encontrada", exception.getMessage());
            Mockito.verify(consultaRepository, Mockito.never()).save(any(Consulta.class));
        }
    }

    @Nested
    @DisplayName("5. Testes de cancelamento de consulta")
    class CancelarTest {

        @Test
        @DisplayName("5.1 Deve cancelar a consulta com sucesso")
        void deveCancelarAConsultaComSucesso() {
            Consulta consultaExistente = new Consulta();
            consultaExistente.setIdConsulta(1L);

            Mockito.when(consultaRepository.findById(1L)).thenReturn(Optional.of(consultaExistente));

            consultaService.cancelar(1L);

            Mockito.verify(consultaRepository, Mockito.times(1)).findById(1L);
            Mockito.verify(consultaRepository, Mockito.times(1)).delete(consultaExistente);
        }

        @Test
        @DisplayName("5.2 Deve lançar EntidadeNaoEncontrada ao tentar cancelar uma consulta inexistente")
        void deveLancarExcecaoAoCancelarConsultaInexistente() {
            Mockito.when(consultaRepository.findById(1L)).thenReturn(Optional.empty());

            EntidadeNaoEncontrada exception = assertThrows(
                    EntidadeNaoEncontrada.class,
                    () -> consultaService.cancelar(1L)
            );

            Assertions.assertEquals("Consulta não encontrada", exception.getMessage());
            Mockito.verify(consultaRepository, Mockito.never()).delete(any(Consulta.class));
        }
    }
}