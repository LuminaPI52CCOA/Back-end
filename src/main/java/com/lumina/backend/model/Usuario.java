package com.lumina.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuario")
public class Usuario implements UserDetails {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long idUsuario;
        private String nome;
        private String cpf;
        private String email;
        private String senha;
            @ManyToOne
    @JoinColumn(name = "fk_perfil")
    private Perfil perfil;
        private String cro;
        private Boolean ativo;

        public Usuario(){
        }

        public Usuario(Long idUsuario, String nome, String cpf, String email, String senha, Perfil perfil, String cro, Boolean ativo) {
                this.idUsuario = idUsuario;
                this.nome = nome;
                this.cpf = cpf;
                this.email = email;
                this.senha = senha;
                this.perfil = perfil;
                this.cro = cro;
                this.ativo = ativo;
        }

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

        public Perfil getPerfil() {
                return perfil;
        }

        public void setPerfil(Perfil perfil) {
                this.perfil = perfil;
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

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities(){
                return List.of(new SimpleGrantedAuthority("ROLE_" + (this.perfil != null && this.perfil.getNome() != null ? this.perfil.getNome().toUpperCase() : "USER")));
        }

        @Override
        public String getPassword(){
                return this.senha;
        }

        @Override
        public String getUsername(){
                return this.email;
        }
}
