package com.lumina.backend.dto.usuario;

import com.lumina.backend.model.Usuario;

import java.util.List;

public class UsuarioMapper {

    public static Usuario toEntity(UsuarioRequest dto) {
        if (dto == null) {
            return null;
        }

        Usuario entidade = new Usuario();
        entidade.setIdUsuario(dto.getIdUsuario());
        entidade.setNome(dto.getNome());
        entidade.setCpf(dto.getCpf());
        entidade.setEmail(dto.getEmail());
        entidade.setSenha(dto.getSenha());
        entidade.setCro(dto.getCro());
        entidade.setAtivo(dto.getAtivo());
        entidade.setFkPerfil(dto.getFkPerfil());

        return entidade;
    }

    public static UsuarioResponse toDto(Usuario model) {
        if (model == null) {
            return null;
        }

        Integer perfil = model.getFkPerfil();

        UsuarioResponse.UsuarioPerfil usuarioPerfilDto = new
                UsuarioResponse.UsuarioPerfil();

        UsuarioResponse dto = new UsuarioResponse(
                (long) Math.toIntExact(model.getIdUsuario()),
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

    public static List<UsuarioResponse> toDto(List<Usuario> entities) {
        return entities.stream()
                .map(UsuarioMapper::toDto)
                .toList();
    }

    public static Usuario of(UsuarioRequest usuarioCriacaoDto) {
        Usuario usuario = new Usuario();

        usuario.setEmail(usuarioCriacaoDto.getEmail());
        usuario.setNome(usuarioCriacaoDto.getNome());
        usuario.setSenha(usuarioCriacaoDto.getSenha());

        return usuario;
    }

    public static Usuario of(UsuarioLoginDto usuarioLoginDto) {
        Usuario usuario = new Usuario();

        usuario.setEmail(usuarioLoginDto.getEmail());
        usuario.setSenha(usuarioLoginDto.getSenha());

        return usuario;
    }

    public static UsuarioTokenDto of(Usuario usuario, String token) {
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

    public static UsuarioListarDto of(Usuario usuario) {
        UsuarioListarDto usuarioListarDto = new UsuarioListarDto();
        Long idUsuario = Long.valueOf(usuario.getIdUsuario());

        usuarioListarDto.setId(idUsuario);
        usuarioListarDto.setEmail(usuario.getEmail());
        usuarioListarDto.setNome(usuario.getNome());

        return usuarioListarDto;
    }

}
