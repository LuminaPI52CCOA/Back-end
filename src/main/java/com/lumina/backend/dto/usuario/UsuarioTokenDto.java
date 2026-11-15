package com.lumina.backend.dto.usuario;

public record UsuarioTokenDto(
        Long userId,
        String nome,
        String email,
        String perfil,
        String token
) {
}
