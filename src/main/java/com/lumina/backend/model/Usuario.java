package com.lumina.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Usuario {

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

        public Usuario(){

        }

        public Usuario(Long idUsuario, String nome, String cpf, String email, String senha, Integer fkPerfil, String cro, Boolean ativo) {
                this.idUsuario = idUsuario;
                this.nome = nome;
                this.cpf = cpf;
                this.email = email;
                this.senha = senha;
                this.fkPerfil = fkPerfil;
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
