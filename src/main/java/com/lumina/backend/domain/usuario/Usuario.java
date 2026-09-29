package com.lumina.backend.domain.usuario;

import java.util.Objects;

public class Usuario {
    private final UsuarioId id;
    private final String nome;
    private final String cpf;
    private final String email;
    private final String senha;
    private final Integer fkPerfil;
    private final String cro;
    private final Boolean ativo;

    private Usuario(UsuarioId id, String nome, String cpf, String email, String senha,
                   Integer fkPerfil, String cro, Boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.senha = senha;
        this.fkPerfil = fkPerfil;
        this.cro = cro;
        this.ativo = ativo;
    }

    public static Usuario criar(UsuarioCommand command) {
        return new Usuario(
            UsuarioId.generate(),
            command.nome(),
            command.cpf(),
            command.email(),
            command.senha(),
            command.fkPerfil(),
            command.cro(),
            command.ativo()
        );
    }

    public static Usuario reconstruir(UsuarioId id, UsuarioCommand command) {
        return new Usuario(
            id,
            command.nome(),
            command.cpf(),
            command.email(),
            command.senha(),
            command.fkPerfil(),
            command.cro(),
            command.ativo()
        );
    }

    public Usuario atualizar(UsuarioCommand command) {
        return new Usuario(
            this.id,
            command.nome(),
            command.cpf(),
            command.email(),
            command.senha(),
            command.fkPerfil(),
            command.cro(),
            command.ativo()
        );
    }

    public boolean estaAtivo() {
        return Boolean.TRUE.equals(ativo);
    }

    // Getters
    public UsuarioId getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCpf() {
        return cpf;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public Integer getFkPerfil() {
        return fkPerfil;
    }

    public String getCro() {
        return cro;
    }

    public Boolean getAtivo() {
        return ativo;
    }
}