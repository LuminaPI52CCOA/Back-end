package com.lumina.backend.infrastructure.persistence.jpa;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class UsuarioJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;
    private String nome;
    private String cpf;
    private String email;
    private String senha;
    private Integer fkPerfil;
    private String cro;
    private Boolean ativo;

    // Construtor padrão para JPA
    public UsuarioJpaEntity() {}

    // Getters e Setters
    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Integer getFkPerfil() {
        return fkPerfil;
    }

    public void setFkPerfil(Integer fkPerfil) {
        this.fkPerfil = fkPerfil;
    }

    public String getCro() {
        return cro;
    }

    public void setCro(String cro) {
        this.cro = cro;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}