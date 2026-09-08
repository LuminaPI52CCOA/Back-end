package com.lumina.backend.domain.usuario;

public record UsuarioCommand(
    String nome,
    String cpf,
    String email,
    String senha,
    Integer fkPerfil,
    String cro,
    Boolean ativo
) {}