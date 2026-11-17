package com.lumina.backend.service.consulta;

import com.lumina.backend.domain.cliente.Cliente;
import com.lumina.backend.domain.cliente.ClienteCommand;
import com.lumina.backend.domain.cliente.ClienteId;
import com.lumina.backend.domain.cliente.ClienteRepositoryPort;
import com.lumina.backend.dto.cliente.ClienteRequest;
import com.lumina.backend.dto.usuario.UsuarioRequest;
import com.lumina.backend.exception.EntidadeNaoEncontrada;
import com.lumina.backend.model.Perfil;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.repository.AnamneseRepository;
import com.lumina.backend.repository.EstadoCivilRepository;
import com.lumina.backend.repository.UsuarioRepository;
import com.lumina.backend.service.Usuario.UsuarioService;
import com.lumina.backend.service.cliente.ClienteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Cada consulta cacheada leva junto os dados do paciente e do dentista, entao editar um
 * deles tem de descartar a agenda inteira.
 */
@ExtendWith(MockitoExtension.class)
class AgendaCacheInvalidacaoCadastroTest {

    @Mock
    private AgendaCacheService agendaCache;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ClienteRepositoryPort clienteRepositoryPort;
    @Mock
    private AnamneseRepository anamneseRepository;
    @Mock
    private EstadoCivilRepository estadoCivilRepository;

    private UsuarioService usuarioService() {
        UsuarioService service = new UsuarioService(null, null, null, usuarioRepository, null);
        service.setAgendaCache(agendaCache);
        return service;
    }

    private static Usuario dentista() {
        return new Usuario(2L, "Dra. Ana", "98765432100", "ana@lumina.com", "hash", new Perfil(2, "Dentista"), "SP-1", true);
    }

    @Test
    @DisplayName("Atualizar usuario invalida a agenda inteira depois de salvar")
    void atualizarUsuarioInvalida() {
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(dentista()));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        UsuarioRequest request = new UsuarioRequest();
        request.setNome("Dra. Ana Maria");

        usuarioService().atualizar(request, 2L);

        InOrder ordem = inOrder(usuarioRepository, agendaCache);
        ordem.verify(usuarioRepository).save(any(Usuario.class));
        ordem.verify(agendaCache).invalidarTudo();
    }

    @Test
    @DisplayName("Ativar ou desativar usuario invalida a agenda inteira")
    void desativarUsuarioInvalida() {
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(dentista()));

        usuarioService().deletar(false, 2L);

        verify(usuarioRepository).logicalDelete(false, 2L);
        verify(agendaCache).invalidarTudo();
    }

    @Test
    @DisplayName("Usuario inexistente nao invalida nada")
    void usuarioInexistenteNaoInvalida() {
        when(usuarioRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontrada.class, () -> usuarioService().deletar(false, 9L));
        verifyNoInteractions(agendaCache);
    }

    @Test
    @DisplayName("Atualizar paciente invalida a agenda inteira depois de salvar")
    void atualizarPacienteInvalida() {
        Cliente existente = Cliente.reconstruir(ClienteId.of(1L), new ClienteCommand(
                "Carlos Silva", "111.111.111-11", "RG-123", LocalDate.of(1998, 5, 21), "SP", "Brasileira",
                'M', "01010-000", "Rua Flores", "carlos@email.com", "(11) 98765-4321", 1, null, null, null));
        when(clienteRepositoryPort.buscarPorId(ClienteId.of(1L))).thenReturn(Optional.of(existente));
        when(clienteRepositoryPort.salvar(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));
        ClienteService service = new ClienteService(clienteRepositoryPort, anamneseRepository, estadoCivilRepository);
        service.setAgendaCache(agendaCache);

        ClienteRequest request = new ClienteRequest();
        request.setNome("Carlos Souza");
        request.setCpf("111.111.111-11");
        request.setRg("RG-123");
        request.setDataNascimento(LocalDate.of(1998, 5, 21));
        request.setNaturalidade("SP");
        request.setNacionalidade("Brasileira");
        request.setSexo('M');
        request.setCep("01010-000");
        request.setEnderecoResidencial("Rua Flores");
        request.setEmail("carlos@email.com");
        request.setNumeroCelular("(11) 91234-5678");
        request.setFkEstadoCivil(1);

        service.atualizar(request, 1L);

        InOrder ordem = inOrder(clienteRepositoryPort, agendaCache);
        ordem.verify(clienteRepositoryPort).salvar(any(Cliente.class));
        ordem.verify(agendaCache).invalidarTudo();
    }
}
