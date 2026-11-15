package com.lumina.backend.dto.usuario;

import com.lumina.backend.model.Perfil;
import com.lumina.backend.model.Usuario;

import java.util.List;
import java.util.Locale;

public class UsuarioMapper {

    public static UsuarioResponse toDto(Usuario model) {
        if (model == null) {
            return null;
        }

        return new UsuarioResponse(
                model.getIdUsuario(),
                model.getNome(),
                model.getCpf(),
                model.getEmail(),
                model.getSenha(),
                model.getPerfil() != null ? model.getPerfil().getIdPerfil() : null,
                model.getCro(),
                model.getAtivo()
        );
    }

    public static List<UsuarioResponse> toDto(List<Usuario> entities) {
        return entities.stream()
                .map(UsuarioMapper::toDto)
                .toList();
    }

    public static Usuario of(UsuarioRequest usuarioCriacaoDto) {
        if (usuarioCriacaoDto == null) {
            return null;
        }

        Usuario usuario = new Usuario();
        usuario.setNome(usuarioCriacaoDto.nome());
        usuario.setCpf(usuarioCriacaoDto.cpf());
        usuario.setEmail(usuarioCriacaoDto.email());
        usuario.setSenha(usuarioCriacaoDto.senha());
        if (usuarioCriacaoDto.fkPerfil() != null) {
            usuario.setPerfil(new Perfil(usuarioCriacaoDto.fkPerfil(), null));
        }
        usuario.setCro(usuarioCriacaoDto.cro());
        usuario.setAtivo(usuarioCriacaoDto.ativo());
        return usuario;
    }

    public static Usuario of(UsuarioLoginDto usuarioLoginDto) {
        Usuario usuario = new Usuario();
        usuario.setEmail(usuarioLoginDto.email());
        usuario.setSenha(usuarioLoginDto.senha());
        return usuario;
    }

    public static UsuarioTokenDto of(Usuario usuario, String token) {
        String perfil = (usuario.getPerfil() != null && usuario.getPerfil().getNome() != null)
                ? usuario.getPerfil().getNome().trim().toUpperCase(Locale.ROOT)
                : null;
        return new UsuarioTokenDto(
                usuario.getIdUsuario(),
                usuario.getNome(),
                usuario.getEmail(),
                perfil,
                token
        );
    }

    /**
     * Mapeia para o DTO de resposta do login — sem o token.
     *
     * <p>O token não pertence ao body: ele é enviado como cookie HttpOnly
     * via {@code Set-Cookie}. Este DTO carrega apenas os dados necessários
     * para o frontend identificar o usuário na sessão.</p>
     */
    public static UsuarioSessaoDto ofSessao(UsuarioTokenDto tokenDto) {
        return new UsuarioSessaoDto(
                tokenDto.userId(),
                tokenDto.nome(),
                tokenDto.email(),
                tokenDto.perfil()
        );
    }

    public static UsuarioListarDto of(Usuario usuario) {
        return new UsuarioListarDto(
                usuario.getIdUsuario(),
                usuario.getNome(),
                usuario.getEmail()
        );
    }
}
