package com.lumina.backend.infrastructure.persistence.mapper;

import com.lumina.backend.domain.usuario.Usuario;
import com.lumina.backend.domain.usuario.UsuarioCommand;
import com.lumina.backend.domain.usuario.UsuarioId;
import com.lumina.backend.infrastructure.persistence.jpa.UsuarioJpaEntity;
import com.lumina.backend.model.Perfil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes unitários UsuarioMapperJpa")
class UsuarioMapperJpaTest {

    private UsuarioCommand command() {
        return new UsuarioCommand("Ana", "12345678900", "ana@lumina.com", "hash", 2, "CRO123", true);
    }

    @Test
    @DisplayName("Deve retornar null para entradas nulas")
    void deveRetornarNull() {
        assertNull(UsuarioMapperJpa.toJpa(null));
        assertNull(UsuarioMapperJpa.toDomain(null));
        assertNull(UsuarioMapperJpa.toCommand(null));
    }

    @Test
    @DisplayName("Deve converter domínio para entidade JPA")
    void deveConverterParaJpa() {
        Usuario usuario = Usuario.reconstruir(UsuarioId.of(1L), command());

        UsuarioJpaEntity entity = UsuarioMapperJpa.toJpa(usuario);

        assertEquals(1L, entity.getIdUsuario());
        assertEquals("Ana", entity.getNome());
        assertEquals("12345678900", entity.getCpf());
        assertEquals("ana@lumina.com", entity.getEmail());
        assertEquals("hash", entity.getSenha());
        assertEquals(2, entity.getPerfil().getIdPerfil());
        assertEquals("CRO123", entity.getCro());
        assertTrue(entity.getAtivo());
    }

    @Test
    @DisplayName("Deve converter domínio sem perfil para entidade JPA sem perfil")
    void deveConverterSemPerfil() {
        Usuario usuario = Usuario.criar(new UsuarioCommand("Ana", null, null, null, null, null, false));

        UsuarioJpaEntity entity = UsuarioMapperJpa.toJpa(usuario);

        assertNull(entity.getIdUsuario());
        assertNull(entity.getPerfil());
    }

    @Test
    @DisplayName("Deve converter entidade JPA para domínio")
    void deveConverterParaDomain() {
        Perfil perfil = new Perfil();
        perfil.setIdPerfil(3);
        UsuarioJpaEntity entity = new UsuarioJpaEntity();
        entity.setIdUsuario(5L);
        entity.setNome("Bia");
        entity.setEmail("bia@lumina.com");
        entity.setPerfil(perfil);
        entity.setAtivo(true);

        Usuario usuario = UsuarioMapperJpa.toDomain(entity);

        assertEquals(5L, usuario.getId().getValue());
        assertEquals("Bia", usuario.getNome());
        assertEquals(3, usuario.getFkPerfil());
        assertTrue(usuario.estaAtivo());
    }

    @Test
    @DisplayName("Deve converter domínio para command")
    void deveConverterParaCommand() {
        UsuarioCommand cmd = UsuarioMapperJpa.toCommand(Usuario.criar(command()));

        assertEquals(command(), cmd);
    }
}
