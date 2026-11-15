package com.lumina.backend.dto.usuario;

import com.lumina.backend.model.Perfil;
import com.lumina.backend.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes unitários UsuarioMapper")
class UsuarioMapperTest {

    @Test
    @DisplayName("Deve retornar null para entradas nulas")
    void deveRetornarNull() {
        assertNull(UsuarioMapper.of((UsuarioRequest) null));
        assertNull(UsuarioMapper.toDto((Usuario) null));
    }

    @Test
    @DisplayName("Deve converter UsuarioRequest e entidade Usuario")
    void deveConverterRequestEEntidade() {
        UsuarioRequest request = UsuarioRequest.builder()
                .nome("Dr. Carlos")
                .cpf("12345678900")
                .email("carlos@lumina.com")
                .senha("senha123")
                .fkPerfil(2)
                .cro("CRO-SP-111")
                .ativo(true)
                .build();

        Usuario entidade = UsuarioMapper.of(request);
        assertNotNull(entidade);
        assertEquals("Dr. Carlos", entidade.getNome());
        assertEquals(2, entidade.getPerfil().getIdPerfil());
        assertEquals("CRO-SP-111", entidade.getCro());
        assertTrue(entidade.getAtivo());

        entidade.setIdUsuario(1L);
        List<UsuarioResponse> responses = UsuarioMapper.toDto(List.of(entidade));
        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).idUsuario());
        assertEquals("Dr. Carlos", responses.get(0).nome());
        assertEquals(2, responses.get(0).fkPerfil());
    }

    @Test
    @DisplayName("Deve converter métodos auxiliares de login, token, sessão e listagem")
    void deveConverterMetodosAuxiliares() {
        UsuarioRequest req = UsuarioRequest.builder()
                .email("ana@lumina.com")
                .nome("Ana")
                .senha("123")
                .build();

        Usuario fromReq = UsuarioMapper.of(req);
        assertEquals("ana@lumina.com", fromReq.getEmail());

        UsuarioLoginDto loginDto = new UsuarioLoginDto("ana@lumina.com", "123");
        Usuario fromLogin = UsuarioMapper.of(loginDto);
        assertEquals("ana@lumina.com", fromLogin.getEmail());

        Perfil perfil = new Perfil();
        perfil.setIdPerfil(1);
        perfil.setNome("Admin");
        fromReq.setIdUsuario(10L);
        fromReq.setPerfil(perfil);

        UsuarioResponse modelDto = UsuarioMapper.toDto(fromReq);
        assertEquals(10L, modelDto.idUsuario());
        assertEquals(1, modelDto.fkPerfil());

        UsuarioTokenDto tokenDto = UsuarioMapper.of(fromReq, "jwt-token");
        assertEquals(10L, tokenDto.userId());
        assertEquals("ADMIN", tokenDto.perfil());
        assertEquals("jwt-token", tokenDto.token());

        UsuarioSessaoDto sessaoDto = UsuarioMapper.ofSessao(tokenDto);
        assertEquals(10L, sessaoDto.userId());
        assertEquals("ana@lumina.com", sessaoDto.email());
        assertEquals("ADMIN", sessaoDto.perfil());

        UsuarioListarDto listarDto = UsuarioMapper.of(fromReq);
        assertEquals(10L, listarDto.id());
        assertEquals("Ana", listarDto.nome());
    }
}
