package com.lumina.backend.dto.permissao;

import com.lumina.backend.domain.usuario.Usuario;

import java.util.List;

public class PermissaoMapper {

    public static PermissaoResponse toDto(Usuario model) {
        if (model == null) {
            return null;
        }

        Long idUsuario = model.getId() != null ? model.getId().getValue() : null;
        Integer fkPerfil = model.getFkPerfil();
        Boolean ativo = model.getAtivo();

        return new PermissaoResponse(
                idUsuario,
                model.getNome(),
                model.getCpf(),
                model.getEmail(),
                model.getCro(),
                fkPerfil,
                resolverNomePerfil(fkPerfil),
                ativo,
                resolverNivelAcesso(fkPerfil, ativo)
        );
    }

    public static List<PermissaoResponse> toDto(List<Usuario> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(PermissaoMapper::toDto)
                .toList();
    }

    public static String resolverNomePerfil(Integer fkPerfil) {
        if (fkPerfil == null) {
            return "INDEFINIDO";
        }
        return switch (fkPerfil) {
            case 1 -> "ADMIN";
            case 2 -> "DENTISTA";
            case 3 -> "RECEPCIONISTA";
            default -> "OUTRO";
        };
    }

    public static String resolverNivelAcesso(Integer fkPerfil, Boolean ativo) {
        if (Boolean.FALSE.equals(ativo)) {
            return "nenhum";
        }
        if (fkPerfil != null && (fkPerfil == 1 || fkPerfil == 3)) {
            return "todos";
        }
        return "restrito";
    }
}
