package com.lumina.backend.infrastructure.persistence.jpa;

import com.lumina.backend.domain.usuario.Usuario;
import com.lumina.backend.domain.usuario.UsuarioCommand;
import com.lumina.backend.domain.usuario.UsuarioId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários UsuarioRepositoryAdapter")
class UsuarioRepositoryAdapterTest {

    @Mock
    private UsuarioRepositoryJpaSpring jpaRepository;

    @InjectMocks
    private UsuarioRepositoryAdapter adapter;

    private UsuarioJpaEntity entity(Long id, String nome) {
        UsuarioJpaEntity e = new UsuarioJpaEntity();
        e.setIdUsuario(id);
        e.setNome(nome);
        e.setEmail(nome + "@lumina.com");
        e.setAtivo(true);
        return e;
    }

    @Test
    @DisplayName("Deve salvar e retornar o usuário convertido")
    void deveSalvar() {
        Usuario usuario = Usuario.criar(new UsuarioCommand("ana", null, "ana@lumina.com", "x", null, null, true));
        when(jpaRepository.save(any(UsuarioJpaEntity.class))).thenReturn(entity(1L, "ana"));

        Usuario salvo = adapter.salvar(usuario);

        assertEquals(1L, salvo.getId().getValue());
        assertEquals("ana", salvo.getNome());
    }

    @Test
    @DisplayName("Deve buscar por id existente")
    void deveBuscarPorId() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(entity(1L, "ana")));

        assertTrue(adapter.buscarPorId(UsuarioId.of(1L)).isPresent());
    }

    @Test
    @DisplayName("Deve retornar vazio ao buscar com id nulo")
    void deveRetornarVazioIdNulo() {
        assertTrue(adapter.buscarPorId(null).isEmpty());
        verifyNoInteractions(jpaRepository);
    }

    @Test
    @DisplayName("Deve buscar por email")
    void deveBuscarPorEmail() {
        when(jpaRepository.findByEmail("ana@lumina.com")).thenReturn(Optional.of(entity(1L, "ana")));

        assertEquals("ana", adapter.buscarPorEmail("ana@lumina.com").orElseThrow().getNome());
    }

    @Test
    @DisplayName("Deve buscar todos")
    void deveBuscarTodos() {
        when(jpaRepository.findAll()).thenReturn(List.of(entity(1L, "ana"), entity(2L, "bia")));

        assertEquals(2, adapter.buscarTodos().size());
    }

    @Test
    @DisplayName("Deve deletar logicamente")
    void deveDeletarLogicamente() {
        adapter.deletarLogicamente(UsuarioId.of(1L), false);

        verify(jpaRepository).logicalDelete(false, 1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar com id nulo")
    void deveLancarExcecaoDeletarIdNulo() {
        assertThrows(IllegalArgumentException.class, () -> adapter.deletarLogicamente(null, false));
    }
}
