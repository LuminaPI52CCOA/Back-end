package com.lumina.backend.service.midia;

import com.lumina.backend.dto.midia.TipoMidiaRequest;
import com.lumina.backend.dto.midia.TipoMidiaResponse;
import com.lumina.backend.model.TipoMidia;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.repository.TipoMidiaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TipoMidiaServiceTest {

    @Mock
    private TipoMidiaRepository tipoMidiaRepository;

    @InjectMocks
    private TipoMidiaService tipoMidiaService;

    private Usuario usuario;
    private TipoMidia tipoGlobal;
    private TipoMidia tipoCustomizado;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setNome("Dr. John Doe");
        usuario.setEmail("john@doe.com");

        tipoGlobal = new TipoMidia(1L, "Foto Intraoral", "Descrição padrão", null, true);
        tipoCustomizado = new TipoMidia(2L, "Evolução Ortodôntica", "Custom do Dr. John", usuario, true);
    }

    @Test
    @DisplayName("Deve listar tipos globais e do usuário quando autenticado")
    void listarDisponiveis_UsuarioAutenticado() {
        when(tipoMidiaRepository.findDisponiveisPorUsuario(1L)).thenReturn(List.of(tipoGlobal, tipoCustomizado));

        List<TipoMidiaResponse> resultado = tipoMidiaService.listarDisponiveis(usuario);

        assertEquals(2, resultado.size());
        assertFalse(resultado.get(0).getPersonalizado());
        assertTrue(resultado.get(1).getPersonalizado());
        verify(tipoMidiaRepository, times(1)).findDisponiveisPorUsuario(1L);
    }

    @Test
    @DisplayName("Deve listar apenas tipos globais quando usuário for nulo")
    void listarDisponiveis_UsuarioNulo() {
        when(tipoMidiaRepository.findGlobais()).thenReturn(List.of(tipoGlobal));

        List<TipoMidiaResponse> resultado = tipoMidiaService.listarDisponiveis(null);

        assertEquals(1, resultado.size());
        assertEquals("Foto Intraoral", resultado.get(0).getNome());
        assertFalse(resultado.get(0).getPersonalizado());
        verify(tipoMidiaRepository, times(1)).findGlobais();
    }

    @Test
    @DisplayName("Deve criar tipo de mídia personalizado vinculado ao usuário")
    void criarPersonalizado_ComSucesso() {
        TipoMidiaRequest request = new TipoMidiaRequest("Tomografia 3D Maxilar", "Exame específico");
        TipoMidia salvo = new TipoMidia(3L, "Tomografia 3D Maxilar", "Exame específico", usuario, true);

        when(tipoMidiaRepository.save(any(TipoMidia.class))).thenReturn(salvo);

        TipoMidiaResponse response = tipoMidiaService.criarPersonalizado(request, usuario);

        assertNotNull(response);
        assertEquals(3L, response.getId());
        assertEquals("Tomografia 3D Maxilar", response.getNome());
        assertTrue(response.getPersonalizado());
        verify(tipoMidiaRepository, times(1)).save(any(TipoMidia.class));
    }
}
