package com.lumina.backend.dto.usuario;

import com.lumina.backend.domain.usuario.Usuario;
import com.lumina.backend.domain.usuario.UsuarioCommand;
import com.lumina.backend.domain.usuario.UsuarioId;
import com.lumina.backend.model.Perfil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes unitários UsuarioMapper")
class UsuarioMapperTest {

    @Test
    @DisplayName("Deve retornar null para entradas nulas")
    void deveRetornarNull() {
        assertNull(UsuarioMapper.toCommand(null));
        assertNull(UsuarioMapper.toDto((Usuario) null));
        assertNull(UsuarioMapper.toDto((com.lumina.backend.model.Usuario) null));
    }

    @Test
    @DisplayName("Deve converter UsuarioRequest e Usuario de domínio")
    void deveConverterDomainERequest() {
        UsuarioRequest request = new UsuarioRequest();
        request.setNome("Dr. Carlos");
        request.setCpf("12345678900");
        request.setEmail("carlos@lumina.com");
        request.setSenha("senha123");
        request.setFkPerfil(2);
        request.setCro("CRO-SP-111");
        request.setAtivo(true);

        UsuarioCommand command = UsuarioMapper.toCommand(request);
        assertNotNull(command);
        assertEquals("Dr. Carlos", command.nome());

        Usuario domainUsuario = Usuario.reconstruir(UsuarioId.of(1L), command);
        List<UsuarioResponse> responses = UsuarioMapper.toDto(List.of(domainUsuario));
        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).getIdUsuario());
        assertEquals("Dr. Carlos", responses.get(0).getNome());
    }

    @Test
    @DisplayName("Deve converter métodos auxiliares de login, token, sessão e listagem")
    void deveConverterMetodosAuxiliares() {
        UsuarioRequest req = new UsuarioRequest();
        req.setEmail("ana@lumina.com");
        req.setNome("Ana");
        req.setSenha("123");

        com.lumina.backend.model.Usuario fromReq = UsuarioMapper.of(req);
        assertEquals("ana@lumina.com", fromReq.getEmail());

        UsuarioLoginDto loginDto = new UsuarioLoginDto();
        loginDto.setEmail("ana@lumina.com");
        loginDto.setSenha("123");
        com.lumina.backend.model.Usuario fromLogin = UsuarioMapper.of(loginDto);
        assertEquals("ana@lumina.com", fromLogin.getEmail());

        Perfil perfil = new Perfil();
        perfil.setIdPerfil(1);
        perfil.setNome("Admin");
        fromReq.setIdUsuario(10L);
        fromReq.setPerfil(perfil);

        UsuarioResponse modelDto = UsuarioMapper.toDto(fromReq);
        assertEquals(10L, modelDto.getIdUsuario());
        assertEquals(1, modelDto.getFkPerfil());

        UsuarioTokenDto tokenDto = UsuarioMapper.of(fromReq, "jwt-token");
        assertEquals(10L, tokenDto.getUserId());
        assertEquals("ADMIN", tokenDto.getPerfil());
        assertEquals("jwt-token", tokenDto.getToken());

        UsuarioSessaoDto sessaoDto = UsuarioMapper.ofSessao(tokenDto);
        assertEquals(10L, sessaoDto.getUserId());
        assertEquals("ana@lumina.com", sessaoDto.getEmail());
        assertEquals("ADMIN", sessaoDto.getPerfil());

        UsuarioListarDto listarDto = UsuarioMapper.of(fromReq);
        assertEquals(10L, listarDto.getId());
        assertEquals("Ana", listarDto.getNome());
    }
}
