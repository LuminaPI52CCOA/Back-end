package com.lumina.backend.infrastructure.persistence.jpa;

import com.lumina.backend.domain.cliente.Cliente;
import com.lumina.backend.domain.cliente.ClienteCommand;
import com.lumina.backend.domain.cliente.ClienteId;
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
@DisplayName("Testes unitários ClienteRepositoryAdapter")
class ClienteRepositoryAdapterTest {

    @Mock
    private ClienteRepositoryJpaSpring jpaRepository;

    @InjectMocks
    private ClienteRepositoryAdapter adapter;

    private ClienteJpaEntity entity(Long id, String nome) {
        ClienteJpaEntity e = new ClienteJpaEntity();
        e.setIdCliente(id);
        e.setNome(nome);
        e.setCpf("111");
        e.setEmail(nome + "@mail.com");
        return e;
    }

    @Test
    @DisplayName("Deve salvar e retornar o cliente convertido")
    void deveSalvar() {
        Cliente cliente = Cliente.criar(new ClienteCommand("ana", "111", null, null, null, null,
                null, null, null, "ana@mail.com", null, null, null, null, null));
        when(jpaRepository.save(any(ClienteJpaEntity.class))).thenReturn(entity(1L, "ana"));

        Cliente salvo = adapter.salvar(cliente);

        assertEquals(1L, salvo.getId().getValue());
    }

    @Test
    @DisplayName("Deve buscar por id existente e retornar vazio para id nulo")
    void deveBuscarPorId() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(entity(1L, "ana")));

        assertTrue(adapter.buscarPorId(ClienteId.of(1L)).isPresent());
        assertTrue(adapter.buscarPorId(null).isEmpty());
    }

    @Test
    @DisplayName("Deve buscar por cpf e email")
    void deveBuscarPorCpfEEmail() {
        when(jpaRepository.findByCpf("111")).thenReturn(Optional.of(entity(1L, "ana")));
        when(jpaRepository.findByEmail("x@mail.com")).thenReturn(Optional.empty());

        assertTrue(adapter.buscarPorCpf("111").isPresent());
        assertTrue(adapter.buscarPorEmail("x@mail.com").isEmpty());
    }

    @Test
    @DisplayName("Deve buscar por nome e todos")
    void deveBuscarPorNomeETodos() {
        when(jpaRepository.findByNomeContainingIgnoreCase("an")).thenReturn(List.of(entity(1L, "ana")));
        when(jpaRepository.findAll()).thenReturn(List.of(entity(1L, "ana"), entity(2L, "bia")));

        assertEquals(1, adapter.buscarPorNome("an").size());
        assertEquals(2, adapter.buscarTodos().size());
    }

    @Test
    @DisplayName("Deve verificar existência por cpf e email")
    void deveVerificarExistencia() {
        when(jpaRepository.existsByCpf("111")).thenReturn(true);
        when(jpaRepository.findByEmail("ana@mail.com")).thenReturn(Optional.of(entity(1L, "ana")));

        assertTrue(adapter.existePorCpf("111"));
        assertTrue(adapter.existePorEmail("ana@mail.com"));
    }
}
