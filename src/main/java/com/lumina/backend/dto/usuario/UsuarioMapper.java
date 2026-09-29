package com.lumina.backend.dto.usuario;

import com.lumina.backend.domain.usuario.UsuarioCommand;
import com.lumina.backend.domain.usuario.UsuarioId;
import com.lumina.backend.domain.usuario.Usuario;

import java.util.List;

public class UsuarioMapper {

    public static UsuarioCommand toCommand(UsuarioRequest dto) {
        if (dto == null) {
            return null;
        }

        return new UsuarioCommand(
            dto.getNome(),
            dto.getCpf(),
            dto.getEmail(),
            dto.getSenha(),
            dto.getFkPerfil(),
            dto.getCro(),
            dto.getAtivo()
        );
    }

    public static UsuarioResponse toDto(Usuario model) {
        if (model == null) {
            return null;
        }

        Integer perfil = model.getFkPerfil();
        Long idUsuario = model.getId() != null ? model.getId().getValue() : null;

        UsuarioResponse.UsuarioPerfil usuarioPerfilDto = new
                UsuarioResponse.UsuarioPerfil();

        UsuarioResponse dto = new UsuarioResponse(
                idUsuario,
                model.getNome(),
                model.getCpf(),
                model.getEmail(),
                model.getSenha(),
                perfil,
                model.getCro(),
                model.getAtivo()
        );

        return dto;
    }

    public static UsuarioResponse toDto(com.lumina.backend.model.Usuario model) {
        if (model == null) {
            return null;
        }

        return new UsuarioResponse(
                model.getIdUsuario(),
                model.getNome(),
                model.getCpf(),
                model.getEmail(),
                model.getSenha(),
                model.getFkPerfil(),
                model.getCro(),
                model.getAtivo()
        );
    }

    public static List<UsuarioResponse> toDto(List<Usuario> entities) {
        return entities.stream()
                .map(UsuarioMapper::toDto)
                .toList();
    }

    public static com.lumina.backend.model.Usuario of(UsuarioRequest usuarioCriacaoDto) {
        com.lumina.backend.model.Usuario usuario = new com.lumina.backend.model.Usuario();

        usuario.setEmail(usuarioCriacaoDto.getEmail());
        usuario.setNome(usuarioCriacaoDto.getNome());
        usuario.setSenha(usuarioCriacaoDto.getSenha());

        return usuario;
    }

    public static com.lumina.backend.model.Usuario of(UsuarioLoginDto usuarioLoginDto) {
        com.lumina.backend.model.Usuario usuario = new com.lumina.backend.model.Usuario();

        usuario.setEmail(usuarioLoginDto.getEmail());
        usuario.setSenha(usuarioLoginDto.getSenha());

        return usuario;
    }

    public static UsuarioTokenDto of(com.lumina.backend.model.Usuario usuario, String token) {
        UsuarioTokenDto usuarioTokenDto = new UsuarioTokenDto();
        Long idUsuario = Long.valueOf(usuario.getIdUsuario());;

        usuarioTokenDto.setUserId(idUsuario);
        usuarioTokenDto.setEmail(usuario.getEmail());
        usuarioTokenDto.setNome(usuario.getNome());
        usuarioTokenDto.setToken(token);

        return usuarioTokenDto;
    }

    /**
     * Mapeia para o DTO de resposta do login — sem o token.
     *
     * <p>O token não pertence ao body: ele é enviado como cookie HttpOnly
     * via {@code Set-Cookie}. Este DTO carrega apenas os dados necessários
     * para o frontend identificar o usuário na sessão.</p>
     */
    public static UsuarioSessaoDto ofSessao(UsuarioTokenDto tokenDto) {
        UsuarioSessaoDto dto = new UsuarioSessaoDto();

        dto.setUserId(tokenDto.getUserId());
        dto.setEmail(tokenDto.getEmail());
        dto.setNome(tokenDto.getNome());

        return dto;
    }

    public static UsuarioListarDto of(com.lumina.backend.model.Usuario usuario) {
        UsuarioListarDto usuarioListarDto = new UsuarioListarDto();
        Long idUsuario = Long.valueOf(usuario.getIdUsuario());

        usuarioListarDto.setId(idUsuario);
        usuarioListarDto.setEmail(usuario.getEmail());
        usuarioListarDto.setNome(usuario.getNome());

        return usuarioListarDto;
    }

}
